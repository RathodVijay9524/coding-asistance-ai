package com.vijay.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * This is our single, unified service that provides ALL AI tools.
 * It implements the AiToolProvider interface so the indexer can find it.
 */
@Service
@RequiredArgsConstructor
public class AIAgentToolService implements AiToolProvider {

    private static final Logger logger = LoggerFactory.getLogger(AIAgentToolService.class);

    // Injects the API key from application.properties
    @Value("${serpapi.api-key:}")
    private String serpapiApiKey;

    @Value("${OPENWEATHER_API_KEY:}")
    private String openWeatherApiKey;

    // We'll use Jackson (from spring-boot-starter-web) to parse the JSON
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public record GoogleSearchRequest(String query, Integer numResults) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SerpApiResponse(List<OrganicResult> organic_results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OrganicResult(String title, String snippet) {}


    public record EmailRequest(String to, String body) {}
    public record EmailResponse(boolean success, String message) {}
    public record AddRequest(int a, int b) {}
    public record AddResponse(int result) {}
    public record MultiplyRequest(int a, int b) {}
    public record MultiplyResponse(int result) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WeatherResponseDto(String name, Map<String, String> sys, Map<String, Double> main, List<WeatherDescription> weather, Map<String, Double> wind) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WeatherDescription(String description, String main) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ForecastResponseDto(City city, List<ForecastItem> list) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record City(String name, String country) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ForecastItem(String dt_txt, Map<String, Double> main, List<WeatherDescription> weather) {}

    public record WeatherRequest(String city, String units) {}

    // --- TOOL 1: Normal Method ---
    @Tool(description = "Get the current date and time in the user's timezone")
    public String getCurrentDateTime() {
        System.out.println("--- AI TOOL: Getting date tool called ");
        return LocalDateTime.now()
                .atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .toString();
    }

    // --- TOOL 2: "add" (Corrected) ---
    // Flattened parameters so Spring AI generates correct schema: {"a": 2, "b": 10}
    @Tool(description ="Adds two numbers together.")
    public int add(int a, int b) {
        System.out.println("--- TOOL CALLED: add(" + a + ", " + b + ") ---");
        return a + b;
    }

    // --- TOOL 3: "multiply" (Corrected) ---
    // Flattened parameters so Spring AI generates correct schema: {"a": 2, "b": 10}
    @Tool(description ="Multiplies two numbers together.")
    public int multiply(int a, int b) {
        System.out.println("--- TOOL CALLED: multiply(" + a + ", " + b + ") ---");
        return a * b;
    }

    // --- TOOL 4: Normal Method ---
    /**
     * NEW Tool 1: Get the current weather.
     * This is a "normal" method that returns a simple string.
     */
    @Tool(description = "Get the current, real-time weather for a specific city.")
    public String getCurrentWeather(WeatherRequest request) {
        logger.info("--- AI TOOL: Calling OpenWeatherMap for current weather in: {}", request.city());

        if (openWeatherApiKey == null || openWeatherApiKey.isBlank()) {
            logger.error("OpenWeatherMap API key is missing. Set 'OPENWEATHER_API_KEY' in application.properties.");
            return "Error: Weather is not configured by the administrator.";
        }

        String units = (request.units() == null) ? "metric" : request.units();
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=%s",
                URLEncoder.encode(request.city(), StandardCharsets.UTF_8), openWeatherApiKey, units);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url)).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status == 404) {
                return "Error: City not found: " + request.city();
            } else if (status != 200) {
                return "Error: Weather API failed with status code " + status;
            }

            // Parse the JSON and format it into a clean string
            WeatherResponseDto data = objectMapper.readValue(response.body(), WeatherResponseDto.class);
            String tempUnit = "metric".equals(units) ? "°C" : "°F";

            return String.format(
                    "Weather in %s, %s:\n" +
                            "Condition: %s\n" +
                            "Temperature: %.1f %s\n" +
                            "Feels Like: %.1f %s\n" +
                            "Humidity: %.0f%%\n" +
                            "Wind Speed: %.1f m/s",
                    data.name(), data.sys().get("country"),
                    data.weather().get(0).description(),
                    data.main().get("temp"), tempUnit,
                    data.main().get("feels_like"), tempUnit,
                    data.main().get("humidity"),
                    data.wind().get("speed")
            );

        } catch (Exception e) {
            logger.error("Error calling OpenWeatherMap: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return "Error: " + e.getMessage();
        }
    }


    /**
     * NEW Tool 2: Get the weather forecast.
     * This is a "normal" method that returns a simple string.
     */
    @Tool(description = "Get the 5-day weather forecast for a specific city.")
    public String getWeatherForecast(WeatherRequest request) {
        logger.info("--- AI TOOL: Calling OpenWeatherMap for forecast in: {}", request.city());

        if (openWeatherApiKey == null || openWeatherApiKey.isBlank()) {
            return "Error: Weather is not configured by the administrator.";
        }

        String units = (request.units() == null) ? "metric" : request.units();
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=%s&cnt=40",
                URLEncoder.encode(request.city(), StandardCharsets.UTF_8), openWeatherApiKey, units);

        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url)).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status == 404) {
                return "Error: City not found: " + request.city();
            } else if (status != 200) {
                return "Error: Weather API failed with status code " + status;
            }

            // Parse the JSON and format it into a clean string
            ForecastResponseDto data = objectMapper.readValue(response.body(), ForecastResponseDto.class);
            String tempUnit = "metric".equals(units) ? "°C" : "°F";

            StringBuilder resultBuilder = new StringBuilder(
                    String.format("5-Day Forecast for %s, %s:\n", data.city().name(), data.city().country())
            );

            // The API returns data every 3 hours. We'll pick one entry per day (midday).
            for (ForecastItem item : data.list()) {
                if (item.dt_txt().contains("12:00:00")) { // Just get the noon forecast
                    resultBuilder.append(String.format(
                            "- %s: %.1f %s, %s\n",
                            item.dt_txt().split(" ")[0], // Get just the date
                            item.main().get("temp"),
                            tempUnit,
                            item.weather().get(0).description()
                    ));
                }
            }
            return resultBuilder.toString();

        } catch (Exception e) {
            logger.error("Error calling OpenWeatherMap forecast: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return "Error: " + e.getMessage();
        }
    }


    // --- TOOL 5: Normal Method ---
    @Tool(description = "Send an email to a recipient")
    public EmailResponse sendEmail(EmailRequest request) {
        System.out.println("--- AI TOOL: Sending email to " + request.to());
        return new EmailResponse(true, "Email sent successfully to " + request.to());
    }


    @Tool(description = "Performs a Google search for a query and returns the top results as a simple text summary.")
    public String googleSearch(GoogleSearchRequest request) {
        logger.info("--- AI TOOL: Calling Google Search for: {}", request.query());

        if (serpapiApiKey == null || serpapiApiKey.isBlank()) {
            logger.error("SerpAPI key is missing. Set 'serpapi.api-key' in application.properties.");
            return "Error: Search is not configured by the administrator.";
        }
        if (request == null || request.query() == null || request.query().isBlank()) {
            return "Error: 'query' must be provided for the search.";
        }

        // Set defaults for the search
        int num = (request.numResults() == null) ? 3 : Math.max(1, Math.min(request.numResults(), 5));
        String query = URLEncoder.encode(request.query(), StandardCharsets.UTF_8);
        String url = "https://serpapi.com/search.json?q=" + query + "&num=" + num + "&api_key=" + URLEncoder.encode(serpapiApiKey, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        try {
            // Make the web call
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                // --- THIS IS THE NEW LOGIC ---
                // Parse the JSON and return a simple string
                SerpApiResponse apiResponse = objectMapper.readValue(response.body(), SerpApiResponse.class);

                if (apiResponse.organic_results() == null || apiResponse.organic_results().isEmpty()) {
                    return "No search results found.";
                }

                // Format the JSON into a clean string for the LLM
                StringBuilder resultBuilder = new StringBuilder("Here are the top search results:\n");
                int count = 1;
                for (OrganicResult res : apiResponse.organic_results()) {
                    resultBuilder.append(count++)
                            .append(". ")
                            .append(res.title())
                            .append(": ")
                            .append(res.snippet())
                            .append("\n");
                }
                return resultBuilder.toString();
                // --- END OF NEW LOGIC ---

            } else {
                logger.error("SerpAPI failed with HTTP status: {}", status);
                return "Error: Search API failed with status code " + status;
            }
        } catch (IOException e) {
            logger.error("IO error calling SerpAPI: {}", e.getMessage());
            return "Error: IO error during search: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Search interrupted: {}", e.getMessage());
            return "Error: Search was interrupted.";
        }
    }

    // =================================================================================
    // Calendar Tools (NEWLY ADDED)
    // =================================================================================

    // --- DTOs for Calendar Tools ---
    public record CreateEventRequest(String title, String startTime, String endTime, String description, String location, List<String> attendees) {}
    public record ScheduleMeetingRequest(String title, List<String> participants, Integer durationMinutes, String preferredTime, String description) {}
    public record FindSlotsRequest(String date, Integer durationMinutes, List<String> participants) {}
    public record CancelEventRequest(String eventId, String reason) {}
    public record UpcomingEventsRequest(Integer days) {}

    // --- Helper to parse date/time ---
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            if (dateTimeStr.contains("T")) {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use 'YYYY-MM-DD HH:mm' or 'YYYY-MM-DDTHH:mm:ss'");
        }
    }

    /**
     * NEW Tool 3: Creates a new calendar event.
     */
    @Tool(description = "Create a new event in the calendar. Start time is required. Use ISO format (e.g., '2025-11-12T14:00') or 'YYYY-MM-DD HH:mm'.")
    public String createCalendarEvent(CreateEventRequest request) {
        logger.info("--- AI TOOL: Creating calendar event: {}", request.title());
        try {
            LocalDateTime start = parseDateTime(request.startTime());
            LocalDateTime end = (request.endTime() != null) ? parseDateTime(request.endTime()) : start.plusHours(1);

            // Mocked logic
            String eventId = "evt_" + System.currentTimeMillis();
            return String.format(
                    "Success: Event '%s' created.\nID: %s\nStart: %s\nEnd: %s\nLocation: %s\nAttendees: %s",
                    request.title(),
                    eventId,
                    start.toString(),
                    end.toString(),
                    request.location() != null ? request.location() : "N/A",
                    request.attendees() != null ? request.attendees().toString() : "None"
            );
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * NEW Tool 4: Gets all of today's calendar events.
     */
    @Tool(description = "Get a list of all calendar events scheduled for today.")
    public String getTodayEvents() {
        logger.info("--- AI TOOL: Getting today's events ---");

        // Mocked logic from your Python script
        LocalDate today = LocalDate.now();
        String events = String.format(
                "Today's Events (%s):\n" +
                        "1. Team Standup (09:00 - 09:30) at Zoom\n" +
                        "2. Sprint Planning (14:00 - 15:00) at Conference Room A",
                today.toString()
        );
        return events;
    }

    /**
     * NEW Tool 5: Gets upcoming events for the next N days.
     */
    @Tool(description = "Get a list of upcoming calendar events for the next 'N' days.")
    public String getUpcomingEvents(UpcomingEventsRequest request) {
        int days = (request.days() == null) ? 7 : request.days();
        logger.info("--- AI TOOL: Getting upcoming events for next {} days ---", days);

        // Mocked logic from your Python script
        return String.format(
                "Found 5 upcoming events in the next %d days (mocked):\n" +
                        "- Meeting on Monday (10:00)\n" +
                        "- Meeting on Tuesday (10:00)\n" +
                        "- Meeting on Wednesday (10:00)\n" +
                        "- Meeting on Thursday (10:00)\n" +
                        "- Meeting on Friday (10:00)",
                days
        );
    }

    /**
     * NEW Tool 6: Schedules a meeting, finding a time if not provided.
     */
    @Tool(description = "Schedules a new meeting with a list of participants. If preferredTime is not given, it finds the next available slot.")
    public String scheduleMeeting(ScheduleMeetingRequest request) {
        logger.info("--- AI TOOL: Scheduling meeting: {}", request.title());
        try {
            int duration = (request.durationMinutes() == null) ? 60 : request.durationMinutes();
            LocalDateTime meetingTime;

            if (request.preferredTime() != null) {
                meetingTime = parseDateTime(request.preferredTime());
            } else {
                // Mock logic: find next business day at 2 PM
                meetingTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
            }

            LocalDateTime endTime = meetingTime.plusMinutes(duration);
            String meetingId = "mtg_" + System.currentTimeMillis();

            return String.format(
                    "Success: Meeting '%s' scheduled.\nID: %s\nStart: %s\nEnd: %s\nParticipants: %s\nLink: https://meet.school.com/%s",
                    request.title(),
                    meetingId,
                    meetingTime.toString(),
                    endTime.toString(),
                    request.participants().toString(),
                    meetingId
            );
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * NEW Tool 7: Finds available time slots on a specific date.
     */
    @Tool(description = "Finds free time slots on a specific date for a given duration.")
    public String findFreeSlots(FindSlotsRequest request) {
        logger.info("--- AI TOOL: Finding free slots for date: {}", request.date());
        try {
            LocalDate.parse(request.date(), DateTimeFormatter.ISO_LOCAL_DATE); // Validate date

            // Mocked logic from your Python script
            return String.format(
                    "Found 7 available slots on %s for %d minutes (mocked):\n" +
                            "- 09:00 - 10:00\n" +
                            "- 10:00 - 11:00\n" +
                            "- 11:00 - 12:00\n" +
                            "- 14:00 - 15:00\n" +
                            "- 15:00 - 16:00\n" +
                            "- 16:00 - 17:00\n" +
                            "- 17:00 - 18:00",
                    request.date(),
                    request.durationMinutes() != null ? request.durationMinutes() : 60
            );
        } catch (Exception e) {
            return "Error: Invalid date format. Use 'YYYY-MM-DD'.";
        }
    }

    /**
     * NEW Tool 8: Cancels an event by its ID.
     */
    @Tool(description = "Cancel a specific calendar event using its event_id.")
    public String cancelEvent(CancelEventRequest request) {
        logger.info("--- AI TOOL: Cancelling event: {}", request.eventId());
        // Mocked logic
        return String.format(
                "Success: Event %s has been cancelled. Reason: %s",
                request.eventId(),
                request.reason() != null ? request.reason() : "N/A"
        );
    }
}