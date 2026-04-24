# 🔍 코드베이스 리팩토링 및 일관성 피드백 (Refactoring Feedback)

현재 프로젝트의 코드베이스를 분석한 결과, `guideline.md`에서 정의한 규칙과 실제 구현 사이에 불일치하는 부분들이 여럿 발견되었습니다. 향후 유지보수와 코드 품질 향상을 위해 다음 항목들에 대한 리팩토링을 제안합니다.

## 1. 사용하지 않는 의존성 제거 (Lombok)V
*   **문제점**: 프로젝트는 100% Kotlin으로 작성되어 있으며, Kotlin은 `data class` 등을 통해 Lombok의 기능을 자체적으로 지원합니다. 그러나 `build.gradle.kts`에는 여전히 `org.projectlombok:lombok`이 포함되어 있습니다.
*   **리팩토링 방향**: 불필요한 빌드 속도 저하와 어노테이션 프로세서(`kapt`) 충돌을 방지하기 위해 `build.gradle.kts`에서 Lombok 관련 의존성(`compileOnly`, `annotationProcessor`)을 제거해야 합니다.

## 2. CQS(Command Query Separation) 패턴 적용의 불일치V
*   **문제점**: `guideline.md`에는 비즈니스 로직(Command)과 조회 로직(Query)을 분리(`Service` / `QueryService`)하여 관리한다고 명시되어 있습니다. `Member`, `AppFeedback`, `Review` 등의 도메인은 이를 잘 따르고 있습니다.
*   **일관성 위반 도메인**: `AddressService`V, `TermService`V, `DocsService`V, `InvitationService`V, `ChatService`V 등 일부 서비스 클래스들은 이 규칙을 따르지 않고 Command와 Query 로직이 하나의 Service에 혼재되어 있습니다.
*   **리팩토링 방향**: 프로젝트 전반에 걸쳐 일관된 아키텍처를 유지하기 위해, 조회 중심의 도메인 로직은 별도의 `XXXQueryService`로 분리하는 것이 좋습니다.

## 3. API 응답 스펙(ApiResponse) 및 제네릭 타입의 불일치
*   **문제점**: 대부분의 컨트롤러는 공통 응답 포맷인 `ResponseEntity<ApiResponse<T>>`를 반환하고 있습니다. 그러나 다음과 같은 예외와 안티 패턴이 존재합니다.
    1.  **공통 포맷 누락**: `MemberController`의 `memberRecommendationList` 메서드는 `ResponseEntity<MemberRecommendationWithMetaResponse>`를 직접 반환하여 API 응답 규격을 깼습니다. 클라이언트 입장에서 파싱 에러를 유발할 수 있습니다.
    2.  **와일드카드 타입 남용**: 여러 컨트롤러에서 `ResponseEntity<ApiResponse<*>>`를 반환 타입으로 지정하고, 내부적으로 `ApiResponse.success("ok")`를 반환하고 있습니다. 이는 타입 안정성을 저해하는 안티 패턴입니다.
        *   **남용 사례 목록**:
            *   `AddressController`: `addressModify`
            *   `AppFeedbackController`: `modifyAppFeedbackCoupon`
            *   `AuthController`: `memberSave`, `memberSaveTest`
            *   `ChatController`: `chatMessageRead`
            *   `DocsController`: `docsLogin`
            *   `FavoriteController`: `favoriteAdd`, `favoriteDelete`
            *   `FcmController`: `fcmTokenAdd`
            *   `InvitationController`: `invitationWorkoutPartnerAdd`
            *   `MemberController`: `memberRemove`, `memberModify`, `memberWithdraw`, `recommendationsAppFeedbackDismiss`
            *   `AdminMemberController`: `memberRestore`
            *   `NotificationController`: `notificationReadModify`
            *   `OAuth2Controller`: `oAuth2Signup`
            *   `ReportController`: `reportChatRoomAdd`, `reportMemberAdd`
            *   `WorkoutPartnerController`: `workoutPartnerAdd`, `workoutPartnerModify`
            *   `WorkoutRewardController`: `workoutRewardClaimAdd`
            *   `GlobalExceptionController`: `exception`
*   **리팩토링 방향**: 
    *   모든 API는 예외 없이 `ApiResponse<T>`로 감싸서 반환하도록 일원화해야 합니다.
    *   데이터가 없는 성공 응답의 경우 와일드카드(`*`) 대신 명시적으로 `ResponseEntity<ApiResponse<String>>` (또는 `Unit`)으로 타입을 강제하여 타입 안정성을 확보해야 합니다.

## 4. HTTP Method 및 엔드포인트 네이밍의 불일치
*   **문제점**: REST API 설계 원칙에 어긋나는 엔드포인트와 HTTP Method 매핑이 혼재되어 있습니다.
    *   예시: `MemberController`의 `memberWithdraw` 메서드는 내부적으로 `deleteMember`를 호출하는 명백한 "삭제" 작업임에도 `@PostMapping("/withdraw")`를 사용하고 있습니다. 
    *   메서드 네이밍: `memberMe`, `memberRemove`, `recommendationsAppFeedbackDismiss` 등 명사와 동사의 순서, 명명 규칙이 컨트롤러마다 제각각입니다.
*   **리팩토링 방향**: 
    *   상태 변경(수정/삭제) 로직에는 목적에 맞는 HTTP Method(`PUT`, `PATCH`, `DELETE`)를 엄격하게 적용하세요.
    *   컨트롤러 내부의 Kotlin 함수명은 `get~`, `modify~`, `remove~` 와 같은 동사형 접두사를 사용하여 통일하는 것을 권장합니다.
