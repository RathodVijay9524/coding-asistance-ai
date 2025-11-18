# Part 4 – REST API Overview

This part summarizes the main REST APIs exposed by the application.

---

## 4.1 Edit History & Patterns (Phase 3.1)

Base path: `/api/edits`

### 4.1.1 Track Edit

**POST** `/api/edits/track`

Purpose: Track a new edit.

Request body (example):
```json
{
  "userId": "user123",
  "filePath": "src/main/java/Test.java",
  "originalCode": "public void test() {}",
  "editedCode": "public void testMethod() {}",
  "editType": "rename_method",
  "suggestionSource": "AI",
  "accepted": true,
  "description": "Renamed method"
}
```

### 4.1.2 User Edit History

**GET** `/api/edits/history/{userId}?page=0&size=10`

Returns paginated edit history for a user.

### 4.1.3 Recent Edits

**GET** `/api/edits/recent/{userId}?limit=10`

Returns the most recent edits for a user.

### 4.1.4 Statistics & Patterns

- **GET** `/api/edits/stats/{userId}`  
  High-level statistics for a user.

- **GET** `/api/edits/patterns/{userId}`  
  User patterns (from `UserPattern`).

- **GET** `/api/edits/patterns/high-acceptance/{userId}`  
  High acceptance patterns.

- **GET** `/api/edits/patterns/frequent/{userId}`  
  Most frequent patterns.

- **GET** `/api/edits/acceptance-rate/{userId}`  
  Acceptance rate summary.

- **GET** `/api/edits/range/{userId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`  
  Edits within a date range.

---

## 4.2 User Feedback (Phase 3.2)

Base path: `/api/feedback`

### 4.2.1 Record Feedback

**POST** `/api/feedback/record`

Records feedback on a suggestion.

Body (example):
```json
{
  "userId": "user123",
  "suggestionId": 1,
  "suggestionType": "extract_method",
  "suggestionContent": "public void helper() {}",
  "rating": 5,
  "action": "accepted",
  "feedback": "Great suggestion!",
  "helpful": true,
  "relevant": true,
  "accurate": true,
  "sentiment": "positive"
}
```

### 4.2.2 Feedback History & Filters

- **GET** `/api/feedback/history/{userId}?page=0&size=10`
- **GET** `/api/feedback/suggestion/{suggestionId}`
- **GET** `/api/feedback/rating/{userId}?rating=5`
- **GET** `/api/feedback/action/{userId}?action=accepted`
- **GET** `/api/feedback/helpful/{userId}`
- **GET** `/api/feedback/not-helpful/{userId}`
- **GET** `/api/feedback/sentiment/{userId}?sentiment=positive`
- **GET** `/api/feedback/range/{userId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

### 4.2.3 Feedback Analytics

- **GET** `/api/feedback/stats/{userId}`  
  Returns:
  - totalFeedback
  - averageRating
  - helpful/notHelpful counts & percentages
  - acceptance/rejection rates
  - sentiment, action, suggestion type distributions

- **GET** `/api/feedback/effectiveness/{userId}`  
  Average rating and helpful percentage per suggestion type.

- **GET** `/api/feedback/most-helpful/{userId}`  
  Sorted by highest averageRating.

- **GET** `/api/feedback/least-helpful/{userId}`  
  Sorted by lowest averageRating.

### 4.2.4 Update/Delete Feedback

- **PUT** `/api/feedback/update/{feedbackId}`
- **DELETE** `/api/feedback/delete/{feedbackId}`

---

## 4.3 Inline Suggestions (Phase 3.3)

Base path: `/api/suggestions`

### 4.3.1 Inline Suggestions

**POST** `/api/suggestions/inline`

Generates real-time suggestions based on code, cursor and context.

Body (example):
```json
{
  "userId": "user123",
  "code": "public void test() { int a = 1; int b = 2; }",
  "language": "java",
  "cursorPosition": 42,
  "context": "method"
}
```

Returns a list of `InlineSuggestion` objects with fields like: `type`, `title`, `description`, `suggestion`, `confidence`, `priority`, `lineNumber`.

### 4.3.2 Context & Personalization

- **POST** `/api/suggestions/context`  
  Uses recent edits and current method/class for context-aware suggestions.

- **POST** `/api/suggestions/personalized`  
  Uses user patterns and high-acceptance suggestions to generate personalized suggestions.

### 4.3.3 Quick Fixes

- **POST** `/api/suggestions/quick-fix`  
  Given `code` + `errorMessage`, suggests quick fixes (e.g. add null check, bounds check).

### 4.3.4 History & Actions

- **GET** `/api/suggestions/history/{userId}?limit=10`
- **POST** `/api/suggestions/apply`
- **POST** `/api/suggestions/reject`

---

## 4.4 Test Generation (Phase 3.4)

Base path: `/api/tests`

### 4.4.1 Generic Test Generation

**POST** `/api/tests/generate`

Body:
```json
{
  "userId": "user123",
  "language": "java",
  "testType": "unit",
  "framework": "junit5",
  "sourceCode": "public class Calculator { public int add(int a, int b){ return a+b; } }",
  "classNameOverride": "Calculator"
}
```

### 4.4.2 Specific Types

- **POST** `/api/tests/generate-unit`
- **POST** `/api/tests/generate-integration`
- **POST** `/api/tests/generate-edge-cases`

### 4.4.3 Frameworks

- **GET** `/api/tests/frameworks`  
  Returns supported frameworks (currently `junit5`).
