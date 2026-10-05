# 핏뷰 - 채팅 플랫폼 README 넣을 내용

### 전부 공개 허락을 받는 내용입니다

# <ERD 설계>

- 전체 내용

(사진을 확대하면 전체 내용을 볼 수 있습니다)

![develop.png](docs/images/develop.png)

## 고민점

### 회원 고유성을 가상 컬럼으로 지킨 이유

![image.png](docs/images/image.png)

- 상황
    - MySQL 사용
    - 회원가입은 소셜 로그인만 존재
        - platform : 플랫폼 종류 ex) GOOGLE, KAKAO
        - platform_id : 해당 플랫폼에서 발급한 고유 ID
    - 회원 탈퇴 시 정보 유지를 위해 soft delete 정책 사용 (deleted_at 필드 활용)
- 문제 상황
    - UNIQUE(platform, platform_id) 제약으로 회원 고유성 보장 가능
    - 하지만 soft delete 특성상 하나의 플랫폼 계정으로 탈퇴 후 동일 계정으로 재가입 시도 시, UNIQUE 제약에 걸려 재가입 불가능한 문제 발생
- 고려한 방법
    
    1. 가상 컬럼 활용
    
    - 방법
        - deleted_at 이 NULL일 때만 값을 갖는 가상 컬럼을 만들고, 해당 컬럼에 UNIQUE 제약 설정
    - 장점
        - 단일 DB(MySQL) 운영 중이라 스키마 설정만 추가하면 되어 작업 비용이 낮음
        - platform, platform_id 등 기존 필드 훼손X
    - 단점
        - 가상 컬럼은 모든 DB에서 지원하는 기능이 아니라 MySQL 의존적인 정책이 됨
    
    2. UNIQUE 컬럼에 삭제 시간을 포함해 유니크 지키기
    
    - 방법
        - platform_id 값 자체에 삭제 시점 정보를 포함시켜 UNIQUE 충돌 회피
    - 장점
        - 특정 DB 문법에 의존하지 않는 정책
    - 단점
        - platform_id 원본 값이 훼손되어, 이후 특정 회원 조회나 이력 추적 시 조회 로직이 복잡해짐
    
    3. 서버 레벨에서 검증
    
    - 방법
        - 서버에서 platform, platform_id, deleted_at에 락을 걸어 조회 후 문제 없으면 삽입
    - 단점
        - 데이터 INSERT에 대한 동시성 문제 해결도 필요하기 때문에 구현이 복잡해짐
        - (platform, platform_id, deleted_at) 조합을 건드리는 모든 로직(신규 가입 API, 탈퇴 취소 기능 등)에서 매번 이 락 정책을 빠뜨리지 않고 적용해야 하므로 유지보수 부담이 커짐
        - 반면 DB 레벨 UNIQUE 제약(1, 2번)은 어떤 경로로 데이터가 들어오든 DB 엔진이 강제로 무결성을 보장한다는 점에서 더 안전하다고 판단해 채택하지 않음
- 결론
    - 결정
        - 가상 컬럼 방식 채택
    - 이유
        - 프로젝트 초기 단계로 아직 시장 검증이 되지 않은 상태이며, 트래픽 규모나 기능 요구사항 면에서 MySQL의 한계에 부딪힐 가능성이 낮음
        - 이런 상황에서 DB 이식성까지 고려해 설계를 복잡하게 가져가는 것은 오버 엔지니어링이라고 봄
        - 빠르게 제작해 시장 검증을 받는 것이 우선순위인 프로젝트 특성상, 당장 필요하지도 않은 이식성을 위해 원본 데이터 훼손과 조회 복잡성이라는 비용을 감수할 이유는 없다고 결론지음

### 이미지 도메인을 중간 테이블로 분리해 설계한 이유

![image.png](docs/images/image%201.png)

- 설명
    - Image 테이블
        - 이미지 URL 정보만 담음
    - 중간 테이블
        - 각 도메인에 이미지 필요 시, 중간 테이블로 관리
        - ex)
            - 회원은 프로필, 운동 사진등을 보유
            - Member - MemberImage - Image로 설계
- 고려한 방법
    1. 각 도메인 테이블에 이미지 컬럼을 두는 방식
        - 방법
            - 이미지가 필요한 도메인 테이블에 이미지 컬럼을 직접 추가
        - 장점
            - 구조가 단순하고 별도 조인이 필요 없음
        - 단점
            - 이미지는 프로필, 신고 사진 등 여러 곳에서 쓰일 수 있음. 따라서, 1개에서 여러 개로 필드가 늘어날 가능성이 큼
            - 도메인마다 이미지 컬럼이 쌓여 프로젝트가 확장될수록 도메인 테이블이 지저분해짐
    2. Image가 도메인 FK를 직접 가지는 방식
        - 방법
            - Image 테이블에 `member_id` 같은 FK 컬럼을 두고 도메인과 직접 연결
        - 장점
            - 중간 테이블이 없어 테이블 수가 적고 구조가 단순함
        - 단점
            - 1:1 연결이면 이미지를 1개로 강제하게 됨. 운동 사진이나 운동일지처럼 여러 장이 필요한 경우가 많아 확장성이 떨어짐
            - 도메인이 늘수록 Image 테이블에 FK 컬럼이 계속 추가됨
                - 대부분의 컬럼이 NULL이 되고, 소속 도메인을 판별하려면 여러 FK를 확인해야 함
                - FK 컬럼마다 인덱스가 필요해 관리 부담이 커짐
                - 이미지별 type을 어떻게 관리할지 고민이 커짐
    3. Image와 도메인 사이에 중간 테이블을 두는 방식
        - 방법
            - Image는 URL 등 이미지 자체 정보만 담고, 도메인별 중간 테이블(예: Member - MemberImage - Image)로 연결
        - 장점
            - 도메인이 늘어도 Image 테이블은 변경할 필요가 없음
            - 이미지를 여러 장 연결하는 구조로 자연스럽게 확장 가능
            - `seq`, `type` 같은 관계별 속성을 중간 테이블에서 관리 가능
            - Image 테이블에 FK 컬럼과 인덱스가 늘어나지 않음
        - 단점
            - 이미지를 쓰는 도메인마다 중간 테이블이 늘어남
            - 조회 시 조인이 한 번 더 필요함
- 결론
    - 결정
        - 중간 테이블 방식 채택
    - 이유
        - 이미지는 여러 도메인에서 여러 장 사용될 가능성이 크다고 봤음. 그래서 초기부터 확장 가능한 구조가 필요하다고 생각
        - 중간 테이블이 늘어나는 단점은 모두 동일한 패턴이라 관리 비용을 예측할 수 있다고 판단해 감수하기로 함
        - 그 대가로 Image 테이블의 독립성, type/seq 관리, 확장성을 확보함

### 알림 내용을 json 필드로 설계한 이유

- 테이블

![image.png](docs/images/image%202.png)

- 화면
    
    ![KakaoTalk_20260929_105736922.jpg](docs/images/KakaoTalk_20260929_105736922.jpg)
    
- json 필드(content) 예시

```java
// 운동 요청
{
  "payload": {
		 // 알림 클릭 시, 다음 페이지 이동을 위한 정보들
    "chatRoomId": 1,
    "chatMessageId": 3,
    "workoutRequestId": 1
  }
}
```

- 설명
    - 사용자 간의 특정 상호작용(친구 요청 등)은 인앱 알림을 만듦
    - 인앱 알림은 notification 테이블에 저장
    - 인앱 알림의 구체적 내용은 content 필드에 json 형태로 저장
- 요구사항
    - 인앱 알림 유형은 9개이며, 앞으로 더 많아질 수 있음
    - 인앱 알람을 눌렀을 때, 다른 페이지(상대방 프로필 등)로 이동 필요
    - 인앱 알람을 발생시킨 상대방 프로필이 나타나야 함
- 고려한 방법
    1. 알림 상세 내용을 여러 FK 필드로 관리
        - 방법
            - Notification 테이블에 알림과 관련한 대상(상대방 회원, 리뷰, 운동)들을 각각 FK 컬럼으로 관리
        - 장점
            - 테이블의 필드 내용이 명확해짐
            - DB 레벨에서 (알람 - 다른 도메인) 사이의 참조 무결성 보장
        - 단점
            - 알림의 유형이 많아질수록 FK 필드 증가
            - 또한, FK 필드의 NULL이 많아지면서 관리가 어려워짐
    2. 알림 상세 내용은 json 필드로 관리
        - 방법
            - json 필드 1개로 다른 도메인 간의 정보 등등을 모두 담음
        - 장점
            - 알람 유형이 수정되더라도 테이블 구조를 안바꿔도됨
        - 단점
            - json 필드 구조에 대한 문서 관리 필요
            - 서버에서 json 파싱에 따른 관리 비용 증가
            - FK 참조 무결성 포기
- 결론
    - 알림 상세 내용은 json 필드로 관리
    - 이유
        - 알림은 도메인이 추가될 때마다 요구가 바뀔 가능성이 높아, FK 방식이라면 테이블 컬럼 변동이 잦을 것으로 예상함
        - 운영 중 테이블 구조 변경은 마이그레이션 부담과 사이드 이펙트 위험이 큼
        - 반면 서버 로직은 테스트 코드와 모니터링으로 오류를 발견하고 대응하기 쉬움
        - 따라서 서버의 복잡성이 늘어나는 비용을 감수하고, DB 구조 변경을 최소화하는 쪽을 선택함

### 기획자의 의도와 다르게한 설계

![image.png](docs/images/image%203.png)

- 설명
    - 기획자분은 회원 간의 “운동 신청” 기능을 원함
    - “운동 신청”은 실패, 진행, 완료 상태를 가질 수 있음
    - 이때, 본인은 “운동 신청” 뿐만 아니라 “운동 이력” 도메인을 만들었음
    - “운동 이력” 도메인은 운동 신청이 완료된 건만 별도로 보관
- 고민한 부분
    - 운동 신청 완료된 건이 앱에서 비즈니스적으로 중요한 지표임
        - 즉, 기획자분이 완료 데이터를 중요하게 보고 있음
        - 어드민에서 운동 완료 건을 수집하고 있고, 추후에도 운동 완료 데이터를 활용할 것 같음
    - 운동 신청이 완료 상태면 서로 후기를 쓸 수 있음
    - A 회원이 B 회원에게 운동 신청을 이미 했는지 조회 필요
        - 중복 운동 신청이 불가하기 때문에, A ↔ B 회원 간의 운동 신청 여부 조회가 필요
    - 1명의 사용자가 어떤 사람들에게 운동 신청을 했는지 추적 필요
- 고려한 방법
    1. 운동 신청 테이블 하나 + 상태 필드로 표현
        - 장점
            - 기획 의도와 일치하기 때문에, 기획자와 협업할 때 충돌하는게 없음
            - 테이블 구조가 단순함
        - 단점
            - 쿼리 성능 문제
                - 신청 방향(A→B) 유지로 인해, 두 사용자 간 이력 조회 시 OR 쿼리 필요
            - 데이터 활용의 한계
                - 한 테이블에 모든 상태(진행/실패/완료)가 혼합되어, 완료 데이터가 어드민 지표, 리뷰 등의 기준점이 되기 애매함
    2. 운동 이력 도메인 분리
        - 운동 신청 테이블에서 완료된 데이터 건은 운동 이력 테이블에 다시 저장
        - 장점
            - 쿼리 최적화
                - 운동 완료 건만 수집 시, 별도 WHERE 필요 없이 운동 이력만 조회하면 됨
                - 이에 따라, 쿼리가 단순해지며 조회 성능 최적화
            - 비즈니스 로직 단순화
                - 후기, 어드민 지표에서 운동 완료 건만 필요한 경우가 있음
                - 운동 신청 도메인만 있다면 운동 신청 도메인에서 완료 건만 걸러내야함
                - 운동 이력 도메인이 있으면 별도 조건없이 운동 이력 조회해서 후기, 어드민 지표에 전달하기만 하면 되니 로직이 단순화됨
        - 단점
            - 운동 신청 상태 + 운동 이력의 중복 관리 필요
            - 기획 의도와 충돌되니, 이후에 제작될 기획자 설계와 충돌할 수 있음
- 결론
    - 2번 방법(운동 이력 도메인 분리)을 택함
    - 이득
        - 운동 완료 건만 별도로 필요한 케이스에서 쿼리 및 비즈니스 로직이 간편화됨
        - 추후 기능 도입에서 기능 제작이 간편화됨
            - 운동 완료에 대한 리워드 보상 이벤트가 있었음
            - 이때, 운동 이력 도메인이 별도로 있었기 때문에 기능 제작이 더 간편했음
    - 손해
        - 기획자와의 소통 문제
            
            ![image.png](docs/images/050016f5-4a5a-45d5-9c0c-57744552c997.png)
            
            - 기획자분은 어드민에서 후기 조회 시, 운동 신청 id를 원함
            - 본인 설계에서는 후기 조회 시, 운동 이력 id가 조회되는게 맞음
            - ⇒ 기획자, 개발자 간의 설계 충돌
        - 보완
            - 운동 이력은 운동 신청 id를 갖도록 설계
            - ⇒ 운동 이력은 운동 신청 id를 가지기 때문에, 운동 신청 id를 요구하더라도 요구사항 반영됨
- 회고 - 돌아본 판단
    - MySQL의 Generated Column(STORED) 기능 도입이 더 적합했을 것이라 판단됨.
    - 방법
        - 운동 이력 도메인 사용X
        - 운동 신청 도메인에서 별도 STORED 필드로 회원1, 회원2 id를 오름차순 or 내림차순으로 기록하는 방식
    - 이유
        - 조회 성능 최적화 (OR 쿼리 병목 해결)
            
            
            | id | from_member_id | to_member_id | **sorted_id1** | **sorted_id2** |
            | --- | --- | --- | --- | --- |
            | 1 | 10 | 20 | **10** | **20** |
            | 2 | 20 | 10 | **10** | **20** |
            | 3 | 30 | 10 | **10** | **30** |
            - 상황 : A ↔ B 회원 간의 운동 신청이 있었는지 확인하고, 없으면 A → B 운동 신청 허가
            - 기존 문제) 운동 신청 방향성 때문에 A, B 간 이력 조회 시 OR 쿼리가 강제되어 쿼리 실행 계획이 불안정함
            - 해결) 오름차순된 파생컬럼(stored_id)으로 OR 쿼리 사용 없이 두 사용자 간 이력 조회 가능
        - 데이터 정합성 보장
            - 운동 신청 테이블 하나로 관리되어, 기존의 신청, 이력 도메인 간 동기화 문제 없음
        - 기획 의도와 맞음
            - 기존 방법은 새로운 도메인을 만들었기 때문에 이후 기능 설계에서 부딪히는 경우가 있었음
            - 해당 방법은 기획 도메인을 넘어서지 않았기 때문에 기획과 부딪히는 부분이 없음
    - 결론
        - MySQL의 Generated Column(STORED)을 도입하는 편이 더 적합했을 것이라고 판단함
        - STORED 필드는 MySQL에 종속되지만, MVP 단계에서 DB 이식성까지 고려하는 것은 과하다고 생각됨
        - 기획 도메인을 유지하면서 회원 쌍 조회 쿼리를 단순화할 수 있어, 이 상황에서는 더 적절한 선택이었을 것으로 판단

# <코드 설계>

- 매력 포인트
    
    ```
    # 🚀 Fitview 프로젝트 아키텍처 & 포트폴리오 매력 포인트 가이드 (Portfolio Architecture Highlights)
    
    > 💡 **문서의 목적**  
    > 이 문서는 Fitview 백엔드 프로젝트의 핵심 설계 장점, 아키텍처적 의사결정(Problem-Solution-Impact), 그리고 **문제 코드(Before/Anti-Pattern)와 실제 해결 코드(After/Implementation)**를 총정리한 기술 문서입니다.  
    > **향후 AI 어시스턴트나 개발자가 이 프로젝트의 백엔드 설계 매력 포인트를 질의받았을 때, 구체적인 코드 비교와 아키텍처적 근거를 바탕으로 답변할 수 있도록 돕는 기준 문서** 역할을 합니다.
    
    ---
    
    ## 📌 1. 프로젝트 기본 개요
    
    - **프로젝트명**: Fitview (운동 파트너 매칭 및 운동 관리 플랫폼 백엔드)
    - **주요 기능**: 운동 파트너 신청/수락, 실시간 WebSocket(STOMP) 1:1 및 그룹 채팅, 운동 약속 조율, 운동 기록 및 리뷰, 출석 스탬프/리워드 쿠폰, FCM 푸시 및 인앱 알림
    - **기술 스택**:
      - **Language**: Kotlin 1.9.25 (Java 17)
      - **Framework**: Spring Boot 3.5.6
      - **Database & Cache**: MySQL 8.0 (Main), H2 (Test), Redis (STOMP Pub/Sub, 분산 락, 토큰)
      - **Persistence**: Spring Data JPA, QueryDSL 5.0.0
      - **Security**: Spring Security, JWT (Web/Mobile 분기), OAuth2
      - **Communication & Push**: WebSocket (STOMP), Firebase Cloud Messaging (FCM)
      - **Concurrency & Scheduling**: ShedLock (Redis LockProvider)
      - **Testing & Docs**: JUnit 5, Mockito Kotlin, MockMvc, Spring RestDocs (Asciidoctor), 196개의 테스트 슈트
    
    ---
    
    ## 🏛 2. 핵심 아키텍처 매력 포인트 상세 분석 (Before vs After 코드 비교)
    
    ---
    
    ### [매력 포인트 1] 트랜잭션 정합성(Transactional Consistency)을 보장하는 이벤트 기반 아키텍처 (EDA & Redis Pub/Sub)
    
    #### 1) 문제 상황 및 배경 (Problem)
    - 운동 파트너 신청, 운동 약속 조율 등 비즈니스 서비스 안에서 실시간 웹소켓(STOMP), 외부 구글 푸시(FCM), 인앱 알림 DB 저장을 직접 호출하면 다음과 같은 치명적인 문제가 발생합니다:
      1. **유령 알림(Ghost Notification)**: 서비스 내에서 푸시/소켓을 보낸 후 DB Commit 직전에 네트워크 지연, 제약조건 위반, 데드락 등으로 트랜잭션이 **롤백(Rollback)**되면, DB에는 데이터가 없는데 유저 폰에는 *"파트너 신청이 도착했습니다!"*라는 알림이 이미 전송되어 있는 정합성 불일치 발생.
      2. **외부 I/O로 인한 트랜잭션 지연**: 외부 FCM 서버 통신 지연이 DB 커넥션을 오래 점유하여 서버 처리량(Throughput) 저하.
      3. **Scale-Out 환경의 WebSocket 세션 분산**: 유저 A는 1번 서버, 유저 B는 2번 서버에 WebSocket 세션이 연결되어 있을 때, 1번 서버의 메모리 소켓 브로커로는 2번 서버에 있는 유저 B에게 메시지를 보낼 수 없음.
    
    #### 2) Before vs After 코드 비교
    
    ##### ❌ 기존 방식 / 문제 발생 코드 (Before Anti-Pattern)
    ```kotlin
    // 서비스 계층이 모든 알림 인프라에 강결합되어 있고, 트랜잭션 커밋 전에 외부 통신이 일어남
    @Service
    @Transactional
    class BadWorkoutPartnerRequestService(
        private val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
        private val notificationService: NotificationService, // 인앱 알림 DB 저장
        private val fcmTokenService: FcmTokenService,         // 외부 FCM 푸시 통신
        private val stompPublishService: StompPublishService  // 단일 서버 웹소켓 전송
    ) {
        fun addWorkoutPartnerRequest(memberId: Long, request: WorkoutPartnerCreateServiceRequest) {
            // 1. 도메인 저장
            val saved = workoutPartnerRequestRepository.save(WorkoutPartnerRequest(...))
            
            // 🚨 문제 지점: 트랜잭션이 아직 COMMIT되지 않은 상태에서 외부 I/O 발생!
            stompPublishService.sendWorkoutPartnerRequest(saved) // 상대방에게 소켓 전송
            fcmTokenService.sendWorkoutPartnerRequest(...)       // 외부 구글 FCM 서버 통신
            notificationService.saveWorkoutPartnerRequest(...)   // 같은 트랜잭션 내에서 알림 엔티티 저장
    
            // 💥 만약 여기서 데드락이나 예외가 발생해 트랜잭션이 롤백된다면?
            // -> DB는 롤백되었는데 상대방 폰에는 푸시와 소켓 메시지가 이미 도달함 (유령 알림 버그 발생!)
            // 💥 서버가 2대 이상으로 확장되면 다른 서버에 접속한 유저에게 소켓 메시지가 전달되지 않음!
        }
    }
    ```
    
    ##### ✅ 실제 해결 코드 (After / Fitview Implementation)
    
    **1단계: 서비스 계층은 도메인 로직 처리 후 순수 이벤트만 발행 (`ApplicationEventPublisher`)**
    ```kotlin
    // [WorkoutPartnerRequestService.kt]
    @Service
    @Transactional(readOnly = true)
    class WorkoutPartnerRequestService(
        private val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
        private val publisher: ApplicationEventPublisher, // 스프링 이벤트 발행기만 주입받음
        // ...
    ) {
        @Transactional
        fun addWorkoutPartnerRequest(memberId: Long, request: WorkoutPartnerCreateServiceRequest): WorkoutPartnerRequest {
            // 도메인 검증 및 저장
            val workoutPartnerRequest = WorkoutPartnerRequest.of(...)
            val savedWorkoutPartnerRequest = workoutPartnerRequestRepository.save(workoutPartnerRequest)
    
            // ✅ 알림 로직을 직접 호출하지 않고 이벤트만 발행 (관심사 분리)
            sendStompWorkoutPartnerRequest(savedWorkoutPartnerRequest)
            sendFcmWorkoutPartnerRequest(toMember, fromMember)
            sendNotificationWorkoutPartnerRequest(memberId, request.memberId, savedWorkoutPartnerRequest.id!!)
    
            return savedWorkoutPartnerRequest
        }
    
        private fun sendStompWorkoutPartnerRequest(workoutPartnerRequest: WorkoutPartnerRequest) {
            val stomp = StompEventWorkoutPartnerRequestDepth1(...)
            publisher.publishEvent(stomp) // 이벤트 발행!
        }
    }
    ```
    
    **2단계: `@TransactionalEventListener(AFTER_COMMIT)`로 DB 커밋 완료 후에만 외부 전송 보장**
    ```kotlin
    // [NotificationStompEventListener.kt]
    @Component
    class NotificationStompEventListener(
        private val redisStompService: RedisStompService
    ) {
        // ✅ DB 트랜잭션이 완벽하게 COMMIT 성공했을 때만 실행! (롤백 시 실행되지 않아 유령 알림 원천 차단)
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        fun sendWorkoutPartnerRequest(event: StompEventWorkoutPartnerRequestDepth1) {
            // 단일 서버 소켓이 아닌 Redis Pub/Sub으로 발행하여 Scale-out 환경 대응
            redisStompService.publishStompEvent(event)
        }
    }
    ```
    
    **3단계: 인앱 알림 DB 저장은 새 트랜잭션으로 격리 (`Propagation.REQUIRES_NEW`)**
    ```kotlin
    // [NotificationEventListener.kt]
    @Component
    class NotificationEventListener(
        private val notificationService: NotificationService
    ) {
        // ✅ AFTER_COMMIT 시점에 본래 트랜잭션과 격리된 새 트랜잭션을 열어 안전하게 저장
        // (알림 저장 실패가 본래의 파트너 신청 성공에 영향을 주지 않음)
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        fun saveWorkoutPartnerRequest(event: EventWorkoutPartnerRequest) {
            notificationService.saveWorkoutPartnerRequest(event)
        }
    }
    ```
    
    **4단계: 다중 서버(Scale-Out) 환경을 위한 Redis Pub/Sub 메시지 브로드캐스팅 (`RedisStompService`)**
    ```kotlin
    // [RedisStompService.kt]
    @Service
    class RedisStompService(
        private val redisClient: RedisClient,
        private val stompPublishService: StompPublishService,
        private val objectMapper: ObjectMapper
    ) : MessageListener {
    
        // 1. 이벤트를 Envelope로 감싸 Redis 토픽으로 발행
        fun publishStompEvent(event: Any) {
            val type = StompEventType.from(event::class.java)
            val bodyNode = objectMapper.valueToTree<JsonNode>(event)
            val envelope = RedisEventEnvelope(type = type, body = bodyNode)
    
            redisClient.convertAndSend(RedisConstant.STOMP_TOPIC, objectMapper.writeValueAsString(envelope))
        }
    
        // 2. 모든 WAS 서버 인스턴스가 Redis 토픽을 수신하여 자기 서버에 연결된 WebSocket 유저에게 전송
        override fun onMessage(message: Message, pattern: ByteArray?) {
            val envelope = objectMapper.readValue(String(message.body), RedisEventEnvelope::class.java)
            val actualEvent = objectMapper.treeToValue(envelope.body, envelope.type.clazz)
            sendStompMessage(actualEvent)
        }
    }
    ```
    
    ---
    
    ### [매력 포인트 2] WebSocket 세션 인지형 지능형 푸시 알림 라우팅 (UX & 비용 최적화)
    
    #### 1) 문제 상황 및 배경 (Problem)
    - 유저가 스마트폰 앱을 켜놓고 상대방과 채팅창에서 활발히 대화하고 있는 중에도 상단 시스템 푸시(FCM) 배너가 계속 울리면 화면을 가리고 사용자를 방해하여 UX가 저하됩니다.
    - 또한 불필요한 구글 FCM 서버 API 호출 네트워크 왕복 비용 및 오버헤드가 발생합니다.
    
    #### 2) Before vs After 코드 비교
    
    ##### ❌ 기존 방식 / 문제 발생 코드 (Before Anti-Pattern)
    ```kotlin
    // 접속 여부를 고려하지 않고 무조건 외부 FCM 푸시를 쏘는 방식
    @Component
    class DumbFcmEventListener(private val fcmTokenService: FcmTokenService) {
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        fun sendFcm(event: EventFcmChatMessage) {
            // 🚨 문제 지점: 유저가 지금 채팅방을 켜고 보고 있는데도 시스템 푸시가 화면 상단에 뜸!
            fcmTokenService.sendChatMessage(event)
        }
    }
    ```
    
    ##### ✅ 실제 해결 코드 (After / Fitview Implementation)
    ```kotlin
    // [FcmEventListener.kt]
    @Component
    class FcmEventListener(
        private val fcmTokenService: FcmTokenService,
        private val simpUserRegistry: SimpUserRegistry // Spring STOMP 사용자 세션 저장소 주입
    ) {
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        fun fcmWorkoutPartnerRequest(event: EventFcmWorkoutPartnerRequest) {
            // ✅ 유저가 현재 웹소켓(STOMP)에 연결되어 온라인 상태라면 푸시 발송 스킵!
            if (isMemberConnected(event.toMemberId)) {
                return // 실시간 STOMP 메시지로 이미 받았으므로 FCM 발송 불필요
            }
    
            fcmTokenService.sendWorkoutPartnerRequest(event)
        }
    
        // SimpUserRegistry에서 해당 회원의 세션 존재 여부를 O(1)로 확인
        fun isMemberConnected(memberId: Long): Boolean {
            return simpUserRegistry.getUser(memberId.toString()) != null
        }
    }
    ```
    
    ---
    
    ### [매력 포인트 3] MySQL 윈도우 함수(Native Query)와 커스텀 SliceWithBefore를 결합한 No-Offset 페이징
    
    #### 1) 문제 상황 및 배경 (Problem)
    1. **채팅 목록 조회 시 N+1 문제**: 사용자의 채팅방 목록(20~50개)을 보여줄 때, 각 채팅방마다 '가장 최신 메시지 1건'과 '읽음 여부'를 가져오기 위해 채팅방 수만큼 추가 쿼리가 반복 실행되는 전형적인 N+1 병목 발생.
    2. **채팅 메시지 무한 스크롤 한계**: Offset 기반 페이징은 뒤로 갈수록 느려지며, 채팅방 특성상 "안 읽은 메시지 위치"로 바로 진입했을 때 위(이전)와 아래(이후) 양방향 스크롤 가능 여부를 기본 Spring Data `Slice`로는 표현할 수 없음.
    
    #### 2) Before vs After 코드 비교
    
    ##### ❌ 기존 방식 / 문제 발생 코드 (Before Anti-Pattern)
    ```kotlin
    // 1. 최신 메시지 조회의 N+1 문제
    fun getChatRoomList(memberId: Long): List<ChatRoomResponse> {
        val rooms = chatRoomRepository.findAllByMemberId(memberId) // 쿼리 1번
        return rooms.map { room ->
            // 🚨 N번의 추가 쿼리 발생! (채팅방이 30개면 총 31번의 쿼리)
            val lastMessage = chatMessageRepository.findTop1ByChatRoomIdOrderBySentAtDesc(room.id)
            val isRead = messageReadStatusRepository.findByChatMessageIdAndMemberId(lastMessage.id, memberId)
            ChatRoomResponse(room, lastMessage, isRead)
        }
    }
    
    // 2. 단방향만 지원하는 기본 Slice
    fun getChatMessages(roomId: Long, pageable: Pageable): Slice<ChatMessage> {
        // 🚨 문제점: Offset 페이징은 메시지가 10만 건일 때 성능 급락
        // 🚨 문제점: hasNext만 있고 hasBefore(위로 스크롤할 데이터가 있는지)는 알 수 없음!
    }
    ```
    
    ##### ✅ 실제 해결 코드 (After / Fitview Implementation)
    
    **1단계: MySQL 8.0 Window Function Native Query로 N+1을 단 1회 쿼리로 최적화**
    ```kotlin
    // [ChatMessageRepositoryImpl.kt]
    override fun findRecentChatMessageByMemberIdAndIn(memberId: Long, chatRoomIds: List<Long>): List<LastChatMessage> {
        val inClause = chatRoomIds.joinToString(",")
        
        // ✅ ROW_NUMBER() OVER (PARTITION BY ... ORDER BY ...)를 통해
        // 각 채팅방별 최신 1건의 메시지와 읽음 여부를 1개의 쿼리로 벌크 조회!
        val sql = """
            WITH RankedMessages AS (
                SELECT
                    cm_inner.chat_message_id,
                    cm_inner.chat_room_id,
                    cm_inner.type,
                    cm_inner.content,
                    cm_inner.sent_at,
                    cm_inner.deleted_at,
                    cm_inner.member_id,
                    ROW_NUMBER() OVER (
                        PARTITION BY cm_inner.chat_room_id
                        ORDER BY cm_inner.sent_at DESC
                    ) AS rn
                FROM chat_message cm_inner
                WHERE cm_inner.chat_room_id IN ($inClause)
            )
            SELECT rm.chat_message_id, rm.chat_room_id, rm.type, rm.content, rm.sent_at, rm.member_id,
                   mrs.is_read, wr.workout_request_id, wr.status, wr.requested_at, wr.scheduled_at, wr.location
            FROM RankedMessages rm
            LEFT JOIN message_read_status mrs ON mrs.chat_message_id = rm.chat_message_id AND mrs.member_id = :memberId
            LEFT JOIN workout_request wr ON wr.chat_message_id = rm.chat_message_id
            WHERE rm.rn = 1 AND rm.deleted_at is null;
        """.trimIndent()
    
        return em.createNativeQuery(sql).setParameter("memberId", memberId).resultList ...
    }
    ```
    
    **2단계: 커스텀 `SliceWithBefore<T>` 자료구조와 양방향 No-Offset 복합 커서 구현**
    ```kotlin
    // [SliceWithBefore.kt]
    // ✅ Spring Data의 SliceImpl을 확장하여 양방향 스크롤을 위한 hasBefore 필드 추가
    class SliceWithBefore<T>(
        content: List<T>,
        pageable: Pageable,
        hasNext: Boolean,
        val hasBefore: Boolean // 이전(위) 방향 데이터 존재 여부
    ) : SliceImpl<T>(content, pageable, hasNext)
    
    // [ChatMessageRepositoryImpl.kt - findChatMessageByCondition]
    // 1. (sentAt, id) 복합 커서로 No-Offset 페이징 조회 (정방향: limit size + 1)
    val result = queryFactory
        .select(...)
        .from(chatMessage)
        .where(
            chatMessage.sentAt.lt(condition.lastMessageAt),
            chatMessage.id.loe(condition.targetChatMessageId)
        )
        .orderBy(chatMessage.sentAt.desc(), chatMessage.id.desc())
        .limit((condition.size + 1).toLong())
        .fetch()
    
    // 2. 반대 방향 1건(limit 1)을 초고속 조회하여 hasBefore 계산
    val beforeEntity = queryFactory
        .select(chatMessage.id)
        .from(chatMessage)
        .where(chatMessage.sentAt.gt(condition.lastMessageAt))
        .limit(1)
        .fetchFirst()
    
    val hasBefore = beforeEntity != null
    val hasNext = result.size > condition.size
    return SliceWithBefore(result.take(condition.size), pageable, hasNext, hasBefore)
    ```
    
    ---
    
    ### [매력 포인트 4] Redis ShedLock을 활용한 분산 스케줄링 동시성 제어 (Scale-out 대응)
    
    #### 1) 문제 상황 및 배경 (Problem)
    - 백엔드 서버를 다중 인스턴스(WAS 2대 이상)로 배포하면, 1분마다 동작하는 `@Scheduled` 배치(만료된 파트너 요청 정리 등)가 모든 서버에서 동시에 실행되어 DB Lock 충돌, 동일 데이터 중복 갱신이 발생합니다.
    
    #### 2) Before vs After 코드 비교
    
    ##### ❌ 기존 방식 / 문제 발생 코드 (Before Anti-Pattern)
    ```kotlin
    @Component
    class BadScheduler(private val workoutPartnerRequestService: WorkoutPartnerRequestService) {
        @Scheduled(fixedRate = 60_000) // 1분 주기
        fun expireRequests() {
            // 🚨 문제 지점: 서버가 3대면 3대의 서버가 매 분 00초에 똑같은 UPDATE 쿼리를 동시에 날림!
            // -> Deadlock 발생 가능, DB 부하 증가, 데이터 정합성 위험
            workoutPartnerRequestService.modifyAllWorkoutPartnerRequestExpire()
        }
    }
    ```
    
    ##### ✅ 실제 해결 코드 (After / Fitview Implementation)
    ```kotlin
    // [ShedLockConfig.kt] - Redis를 LockProvider로 등록
    @Profile("!test")
    @Configuration
    @EnableScheduling
    @EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
    class ShedLockConfig(
        @Qualifier("shedLock") private val redisConnectionFactory: RedisConnectionFactory
    ) {
        @Bean
        fun lockProvider(): LockProvider = RedisLockProvider(redisConnectionFactory)
    }
    
    // [WorkoutPartnerRequestExpireScheduler.kt]
    @Component
    class WorkoutPartnerRequestExpireScheduler(
        private val workoutPartnerRequestService: WorkoutPartnerRequestService
    ) {
        @Scheduled(fixedRate = 60_000)
        @Transactional
        // ✅ Redis 기반 분산 락: N대의 서버 중 단 1대의 인스턴스만 이 메서드를 배타적으로 실행!
        @SchedulerLock(
            name = "workout:partner:request:expire-modification",
            lockAtMostFor = "PT50S",  // 비정상 종료 시 50초 후 락 자동 해제 (데드락 방지)
            lockAtLeastFor = "PT10S"  // 시계 오차로 인한 동일 주기 내 중복 실행 방지
        )
        fun modifyAllExpireWorkoutPartnerRequest() {
            workoutPartnerRequestService.modifyAllWorkoutPartnerRequestExpire()
        }
    }
    ```
    
    ---
    
    ### [매력 포인트 5] 의존성 역전(DIP)을 통한 시간·보안 추상화와 높은 테스트 용이성 (Testability)
    
    #### 1) 문제 상황 및 배경 (Problem)
    - "24시간 이내 요청 재전송 불가(쿨다운)", "토큰 만료 시간 검증" 등의 비즈니스 로직에서 `LocalDateTime.now()`를 직접 호출하면, 단위 테스트 시 24시간 전/후 상황을 시뮬레이션하기 위해 복잡하고 느린 Mockito static mocking을 써야 하거나 실행 시간에 따라 실패하는 Flaky Test가 발생합니다.
    
    #### 2) Before vs After 코드 비교
    
    ##### ❌ 기존 방식 / 문제 발생 코드 (Before Anti-Pattern)
    ```kotlin
    class BadCooldownValidator {
        fun validateCooldown(requestedAt: LocalDateTime) {
            // 🚨 시스템 시간에 강결합되어 있어 단위 테스트에서 원하는 임의의 시간으로 테스트하기 매우 어려움!
            val now = LocalDateTime.now()
            if (requestedAt.isAfter(now.minusHours(24))) {
                throw GlobalException(WorkoutPartnerErrorCode.PARTNER_REQUEST_COOLDOWN)
            }
        }
    }
    ```
    
    ##### ✅ 실제 해결 코드 (After / Fitview Implementation)
    ```kotlin
    // [Time.kt] - 시간 조작 인터페이스 추상화
    interface Time {
        val nowLocalDateTime: LocalDateTime
        val nowDate: Date
    }
    
    // [TimeBridgeConfig.kt] - 스프링 빈과 정적 Holder 브릿징
    @Configuration
    class TimeBridgeConfig(private val time: Time) {
        @PostConstruct
        fun init() {
            TimeHolder.time = time
        }
    }
    
    // [WorkoutPartnerRequestService.kt] - 인터페이스 Time을 주입받아 사용
    @Service
    class WorkoutPartnerRequestService(
        private val time: Time, // 시스템 시간에 직접 의존하지 않고 인터페이스 주입!
        // ...
    ) {
        fun isNotExpire24Hour(request: WorkoutPartnerRequest): Boolean {
            // ✅ 테스트 시 가짜(Fake) Time 구현체를 넘겨 원하는 시간으로 100% 결정론적 검증 가능!
            return request.requestedAt!!.isAfter(time.nowLocalDateTime.minusHours(24))
        }
    }
    ```
    * **테스트 코드에서의 활용**:
      ```kotlin
      // 단위 테스트에서 무거운 SpringBootTest나 Static Mocking 없이 1ms 만에 엣지 케이스 검증
      val fixedTime = TestTime(LocalDateTime.of(2026, 10, 1, 12, 0))
      val service = WorkoutPartnerRequestService(time = fixedTime, ...)
      assertThat(service.isNotExpire24Hour(request23HoursAgo)).isTrue()
      assertThat(service.isNotExpire24Hour(request25HoursAgo)).isFalse()
      ```
    
    ---
    
    ### [매력 포인트 6] 멀티 플랫폼 보안 전략 & 디바이스 연계 폐기 (Cascade Revoke)
    
    #### 1) 문제 상황 및 배경 (Problem)
    1. **플랫폼별 보안 취약점 차이**: 웹 브라우저는 XSS 공격에 취약하여 Refresh Token을 응답 본문(로컬스토리지)에 주면 탈취 위험이 크지만, 모바일 앱은 안전한 OS Secure Storage에 저장할 수 있습니다.
    2. **로그아웃 후 FCM 토큰 방치 문제**: 모바일 앱에서 로그아웃 시 RefreshToken만 삭제하고 FCM 푸시 토큰을 그대로 두면, 다른 유저가 해당 기기에 로그인하거나 기기를 중고 판매했을 때 **이전 사용자의 개인적인 채팅/운동 알림이 계속 수신되는 개인정보 유출 사고**가 발생합니다.
    
    #### 2) Before vs After 코드 비교
    
    ##### ❌ 기존 방식 / 문제 발생 코드 (Before Anti-Pattern)
    ```kotlin
    // 1. 플랫폼 구분 없는 토큰 전달
    fun login(request: LoginRequest): LoginResponse {
        val refreshToken = createRefreshToken()
        return LoginResponse(accessToken, refreshToken) // 🚨 웹 브라우저에서 탈취 위험 노출
    }
    
    // 2. 단순 로그아웃
    fun logout(refreshToken: String) {
        refreshTokenRepository.deleteByToken(refreshToken)
        // 🚨 문제 지점: 기기의 FCM 푸시 토큰은 DB에 여전히 ACTIVE 상태로 남아있음!
        // -> 로그아웃한 기기로 이전 사용자의 민감한 푸시 알림이 계속 발송됨!
    }
    ```
    
    ##### ✅ 실제 해결 코드 (After / Fitview Implementation)
    ```kotlin
    // [AuthService.kt] - HeaderClientType에 따른 플랫폼 맞춤형 토큰 반환
    @Transactional
    fun login(request: MemberLoginServiceRequest, headerClientType: HeaderClientType): MemberLoginResponse {
        val findMember = findMember(request)
        val accessToken = jwtTokenProvider.createAccessToken(findMember.id!!, findMember.role!!)
        val refreshToken = refreshTokenService.issueWebRefreshToken(findMember.id!!)
    
        // ✅ 모바일: Body로 토큰 반환 (Secure Storage 저장용)
        if (headerClientType == HeaderClientType.MOBILE) {
            return MemberLoginResponse(accessToken, refreshToken, null)
        }
    
        // ✅ 웹: XSS 방어를 위해 HttpOnly, Secure, SameSite가 적용된 Set-Cookie 헤더로 반환!
        val refreshTokenCookieHeader = convertCookieHeader(refreshToken)
        return MemberLoginResponse(accessToken, null, refreshTokenCookieHeader)
    }
    
    // [RefreshTokenService.kt] - 로그아웃 시 디바이스 푸시 토큰까지 원자적 연계 폐기
    @Transactional
    fun revokeRefreshToken(refreshToken: String): RefreshToken {
        val findRefreshTokenEntity = validateRefreshTokenFrom(refreshToken)
    
        // ✅ 모바일 기기 토큰인 경우, 해당 기기에 등록된 FCM 토큰도 즉시 함께 폐기!
        if (findRefreshTokenEntity.deviceId != null) {
            val findFcmTokens = fcmTokenService.findAllFcmTokenByDeviceId(findRefreshTokenEntity.deviceId!!)
            findFcmTokens.forEach { it.revoke() } // 푸시 토큰 동시 무효화 (개인정보 노출 방지)
        }
    
        return findRefreshTokenEntity.setRevoke()
    }
    ```
    
    ---
    
    ### [매력 포인트 7] CQRS 계층 분리 & 도메인 불변성 추적을 위한 스냅샷(Snapshot) 감사 아키텍처
    
    #### 1) 문제 상황 및 배경 (Problem)
    - 운동 약속 매칭 도메인은 `요청(PENDING) -> 수락(ACCEPT) / 거절(REJECT) / 취소(CANCEL) -> 진행 -> 완료(COMPLETE)` 등 빈번한 상태 전이를 거칩니다.
    - 만약 단순 Update 방식으로 `status` 컬럼만 변경하면 "누가, 언제, 어떤 상태로 변경했는지"에 대한 히스토리가 소실되어 노쇼(No-show)나 분쟁 발생 시 원인 규명이 불가능합니다.
    
    #### 2) Before vs After 코드 비교
    
    ##### ❌ 기존 방식 / 문제 발생 코드 (Before Anti-Pattern)
    ```kotlin
    // 단순 상태 컬럼 덮어쓰기 (In-place Update)
    @Transactional
    fun cancelWorkoutRequest(requestId: Long) {
        val request = workoutRequestRepository.findById(requestId).get()
        request.status = WorkoutRequestStatus.CANCEL // 🚨 이전 상태, 변경 시점, 변경 사유가 전부 덮어씌워져 유실됨
    }
    ```
    
    ##### ✅ 실제 해결 코드 (After / Fitview Implementation)
    ```kotlin
    // [WorkoutRequestSnapshot.kt] - 불변(Immutable) 상태 스냅샷 엔티티
    @Entity
    @Table(name = "workout_request_snapshot")
    class WorkoutRequestSnapshot(
        @Column(name = "workout_request_id", nullable = false)
        var workoutRequestId: Long? = null,
        
        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        var status: WorkoutRequestStatus? = null,
        
        var location: String? = null,
        var scheduledAt: LocalDateTime? = null
    ) : SnapshotBaseEntity()
    
    // [WorkoutRequestSnapshotService.kt] - 상태 전이 시마다 스냅샷 불변 저장
    @Service
    @Transactional(readOnly = true)
    class WorkoutRequestSnapshotService(
        private val workoutRequestSnapshotRepository: WorkoutRequestSnapshotRepository
    ) {
        @Transactional
        fun addWorkoutRequestSnapshot(workoutRequestId: Long, status: WorkoutRequestStatus): WorkoutRequestSnapshot {
            val findWorkoutRequest = workoutRequestQueryService.findWorkoutRequestFrom(workoutRequestId)
                ?: throw GlobalException(GlobalErrorCode.ENTITY_NOT_FOUND)
    
            // ✅ 상태가 바뀔 때마다 변경 시점의 전체 약속 스냅샷을 영구 보존
            val workoutRequestSnapshot = WorkoutRequestSnapshot.ofStatus(
                workoutRequest = findWorkoutRequest,
                status = status
            )
            return workoutRequestSnapshotRepository.save(workoutRequestSnapshot)
        }
    }
    ```
    * **효과**: 어드민에서 상태 변경 타임라인(`AdminDetailWorkoutRequestLogResponse`)을 완벽하게 재구성할 수 있으며, CUD(`Service`)와 Read(`QueryService`)를 분리한 CQRS 패턴을 확립하여 유지보수성을 극대화.
    
    ---
    
    ## 🤖 3. 다음 AI 어시스턴트를 위한 응답 가이드라인 (Prompting Guide)
    
    > 💡 **사용자가 "Fitview 프로젝트의 매력 포인트가 뭐야?" 혹은 "포트폴리오에 쓸만한 설계를 추천해줘"라고 질문했을 때 다음 원칙을 준수하여 답변하세요:**
    
    1. **단순 CRUD 나열 지양**:  
       회원가입, 게시글 작성 같은 일반 기능은 언급하지 말고, 위의 **7가지 아키텍처적 트레이드오프(Before 문제 코드 vs After 실제 해결 코드)**에 집중하여 답변할 것.
    2. **트랜잭션 정합성(Transactional Consistency)을 최우선 언급**:  
       Spring Event + `@TransactionalEventListener(AFTER_COMMIT)` + Redis Pub/Sub 구조는 면접관들이 가장 높게 평가하는 엔지니어링 포인트이므로 가장 먼저 강조할 것.
    3. **지능형 FCM 라우팅(`isMemberConnected`)의 실무 가치 강조**:  
       "웹소켓 온라인 접속 유저 식별을 통한 푸시 중복 방지 및 비용 절감"은 실무 경험과 UX 고민을 보여주는 탁월한 차별점임.
    4. **성능 최적화 지점 명시**:  
       MySQL 윈도우 함수를 통한 N+1 해결 및 `SliceWithBefore`를 통한 양방향 No-Offset 페이징을 코드 레벨 근거로 설명할 것.
    5. **인터뷰 맞춤형 질문 대비**:  
       "왜 Kafka 대신 Redis Pub/Sub을 썼는가?", "왜 기본 Slice 대신 커스텀 SliceWithBefore를 만들었는가?", "다중 서버 스케줄러 동시성을 왜 DB Lock이 아닌 ShedLock으로 풀었는가?" 등의 예상 질문과 연결 지어 설명해 줄 것.
    
    ```
    

(서버 설계 내용 넣을 때 넣을 내용)

- 인앱알람, 푸시알람, 실시간 알람의 비즈니스 로직 강결합 문제
- 알람 JSON 필드 역직렬화 문제
- QueryService, Service 설계 - A ↔ B가 서로 의존하는 문제 해결을 위함
- SliceWithBefore - 채팅 메시지
- 카드 이미지 처리
- 운동 리워드 보상 API 설계

### 비즈니스 로직과 알람 도메인의 강결합 문제

- 요구사항
    - 알람은 3가지 종류가 존재
        - 인앱 알람, 푸시 알람, 실시간 알람
    - 각 도메인(운동 파트너, 운동, 채팅 등) 이벤트에 따라 알람이 발생
- 고려가 필요한 부분
    - 도메인마다 알람 호출을 위해서 제각기 다른 데이터를 넘겨줘야함
        - ex) 운동 파트너 요청 알람 → 요청자, 수신자, 요청 ID 등의 데이터 필요
        - ex) 채팅 알람 → 채팅방 ID, 발신자, 메시지 내용 등의 데이터 필요
- 문제점
    - 각 도메인의 비즈니스 로직은 알람을 알아야함
        - ⇒ 알람이 필요한 도메인마다 모두 알람 로직을 넣어야됨
    - ex) 운동 파트너 요청 로직은 각 알람을 모두 알고 있음
    
    ```java
    @Service
    @Transactional
    class WorkoutPartnerRequestService(
        private val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
        private val notificationService: NotificationService,       // 인앱 알람
        private val fcmTokenService: FcmTokenService,               // 푸시 알람
        private val stompPublishService: StompPublishService,       // 실시간 알람
    ) {
    
        fun addWorkoutPartnerRequest(/* ... */) {
            // 도메인 로직
            workoutPartnerRequestRepository.save(/* ... */)
    
            // 알람 로직 호출
            stompPublishService.sendWorkoutPartnerRequest(/* ... */)
            fcmTokenService.sendWorkoutPartnerRequest(/* ... */)
            notificationService.saveWorkoutPartnerRequest(/* ... */)
        }
    }
    ```
    
- 고민한 방법
    1. Facade 패턴
        - 방법
            - 알람 3종 호출을 NotificationFacade 하나로 묶고, 서비스는 Facade만 호출
            
            ```java
            @Component
            class NotificationFacade(
                private val notificationService: NotificationService,// 인앱 알람
                private val fcmTokenService: FcmTokenService,        // 푸시 알람
                private val stompPublishService: StompPublishService,// 실시간 알람
            ) {
            
                fun workoutPartnerRequested(/* ... */) {
                    stompPublishService.sendWorkoutPartnerRequest(/* ... */)
                    fcmTokenService.sendWorkoutPartnerRequest(/* ... */)
                    notificationService.saveWorkoutPartnerRequest(/* ... */)
                }
            }
            ```
            
        - 장점
            - 구조가 단순하고 호출 흐름이 명시적임
            - 비즈니스 로직은 Facade 객체에만 의존하면 됨
        - 단점
            - 서비스가 여전히 알람 모듈(Facade)을 직접 알아야 함
    2. pub/sub 패턴
        - 방법
            
            ![이벤트 기반 알림 시스템 아키텍처.png](docs/images/61bbe197-c7b7-4fd4-865d-3c3d277487ba.png)
            
            - 비즈니스 로직은 ApplicationEventPublisher로 이벤트만 발행
            - 알람 3종은 각 리스너가 구독해서 처리
            
            ```java
            @Service
            @Transactional
            class WorkoutPartnerRequestService(
                private val publisher: ApplicationEventPublisher,
                private val workoutPartnerRequestRepository: WorkoutPartnerRequestRepository,
            ) {
            
                fun addWorkoutPartnerRequest(/* ... */) {
                    workoutPartnerRequestRepository.save(/* ... */)
            
                    // 비즈니스 로직은 이벤트만 발행
                    publisher.publishEvent(WorkoutPartnerRequestEvent(/* ... */))
                }
            }
            ```
            
        - 장점
            - 비즈니스 로직은 알람 서비스를 직접 의존하지 않음
                - ApplicationEventPublisher에만 의존
            - 알람 로직 수정 시 리스너만 수정하면 됨
        - 단점
            - 이벤트·리스너 클래스가 늘어남
            - 비즈니스 로직에서 이벤트 발행 코드는 여전히 필요함
            - 이벤트 발행과 처리 흐름이 코드상 한눈에 보이지 않아 추적이 어려움
- 결론
    - pub/sub 패턴 선택
        - 비즈니스 로직이 알람 서비스를 직접 알지 못하도록 분리할 수 있는 pub/sub 패턴을 선택
    - Facade 패턴을 선택하지 않은 이유
        - Facade를 쓰더라도 비즈니스 로직이 알람 모듈(Facade)을 직접 알아야함
    - 감수한 트레이드오프
        - 이벤트·리스너 클래스가 늘어남
        - 이벤트 발행과 처리 흐름이 코드상 한눈에 보이지 않아 추적이 다소 어려워짐
        - 이벤트 발행 코드는 비즈니스 로직에 여전히 남음
            - 알람 데이터가 도메인마다 달라서 이 부분까지 막기는 어려웠음
- 보완 - (pub/sub 패턴의 단점들에 어떻게 대응했는지)
    - 단점) 이벤트·리스너 클래스가 늘어남
        - 리스너
            - 알람 종류별(인앱, 푸시, 실시간) 3개만 객체를 생성하여 클래스 최소화
        - 이벤트
            - Map이나 다형성으로 여러 이벤트를 한 번에 담을 수 있는 이벤트 객체를 만드려 시도
            - 하지만 여러 이벤트가 한 클래스에 담기면서 이벤트별 데이터 구조 파악이 너무 어려워짐
            - 따라서, 객체 수가 늘어나는 것은 감수함
                - 이벤트별로 별도 객체를 만들어 필드 자체가 데이터 를 설명 가능한 구조를 선택
    - 단점) 이벤트 발행과 처리 흐름이 코드상 한눈에 보이지 않아 추적이 다소 어려워짐
        - 이벤트 클래스 네이밍을 명확하게 해서 흐름 추적이 용이하도록 함
        - ex)
            - 운동 파트너 요청 알람
            - 푸시 알람
                - EventFcmWorkoutPartnerRequest
            - 실시간 알람
                - (Depth는 이벤트 객체를 json으로 변환 필요 시, json의 depth를 표현)
                - StompEventWorkoutPartnerRequestDepth1
                - StompEventWorkoutPartnerRequestDepth2
                
                ```java
                {
                	//depth1
                  "memberId": 2,
                  "message": {
                		 //depth2
                    "workoutPartnerRequestId": 10,
                    "memberId": 1,
                    "profileImageUrl": "https://...",
                    "nickname": "홍길동"
                  }
                }
                ```
                
    - 단점) 이벤트 발행 코드는 비즈니스 로직에 여전히 남음
        - 이벤트 생성 코드를 private 메서드로 분리하여 가독성을 높임
        - 한계
            - 알람에 필요한 데이터는 비즈니스 로직 실행 중에 생성됨
            - 이에 따라, 이벤트를 발행하는 책임은 비즈니스 로직에 남음
        
        ```java
        @Transactional
        fun addWorkoutPartnerRequest(/* ... */) {
            // 비즈니스 로직
            // ..
            
            sendStompWorkoutPartnerRequest(saved)   // 실시간 알람
            sendFcmWorkoutPartnerRequest(/* ... */) // 푸시 알람
            sendNotificationWorkoutPartnerRequest(/* ... */) // 인앱 알람
        }
        
        // private 메서드: 이벤트 조립·발행은 여기로 분리
        private fun sendStompWorkoutPartnerRequest(request: WorkoutPartnerRequest) {
        
            // 프로필 조회, 이벤트 객체 조립 ...
            publisher.publishEvent(event)
        }
        ```
        

### 알람 JSON 필드 역직렬화 문제

- 상황
    
    ![image.png](docs/images/image%202.png)
    
    - notification 테이블의 content 컬럼은 JSON 타입
    - notification 테이블을 이용해서 알람 조회 API를 만들어야함
- 문제
    - type에 따라 content의 JSON 구조가 제각각임
    - 알람 조회 API는 content 컬럼의 JSON을 그대로 내려주지 않음
        - API 응답에 알람 유형별 추가 정보를 덧붙여야함
        - ex) 딥링크(앱에서 알람 눌렀을 때 다른 페이지 이동)
    - 새로운 도메인이 추가되면 type도 계속 늘어날 것으로 예상됨
        - ⇒ 새로운 JSON 구조를 기존 코드 수정을 최소화하면서 수용할 수 있는 구조가 필요
- 고민한 방법
    1. 단일 Service 내에서 모든 작업(json 파싱, 최종 응답 구조 등) 처리
        - 장점
            - 파일 1개에서 전체 흐름이 다보임
            - 구조가 단순
        - 단점
            - 수정 이후에 문제 생기면 어디서 잘못된 건지 파악이 어려움
        - 예시
        
        ```java
        class NotificationService() {
        
          fun getNotifications(notifications: List<Notification>): List<NotificationResponse> =
              notifications.map { notification ->
                  // type별 content 필드 파싱
                  val content = when (notification.type) {
                      WORKOUT_PARTNER_REQUEST -> objectMapper.convertValue(notification.content, PartnerContent::class.java)
                      ...
                  }
                  // type별 딥링크 생성
                  val link = when (notification.type) {
                      WORKOUT_PARTNER_REQUEST -> NotificationLink(MEMBER_PROFILE, mapOf("memberId" to (content as PartnerContent).payload.memberId))
                      ...
                  }
                  // 조합해서 response
                  NotificationResponse(notification.id, notification.type!!, sender, link)
              }
        }
        ```
        
    2. Factory 패턴
        - 설명
            
            ![ChatGPT Image 2026년 10월 2일 오전 10_38_04.png](docs/images/5c363d09-959c-4200-a0c0-95ae6e2ec4f2.png)
            
            - Factory 객체 생성
                - ⇒ type 별 content 필드의 json을 객체로 반환하는 객체
            - Service 객체
                - Factory 객체로 content를 객체로 받음
                - content 객체 + 부가 정보를 조합해서 response 만들기
        - 장점
            - Factory 객체를 통해서 content를 받기 때문에 역할/책임 분리가 명확해짐
        - 단점
            - Service의 수정 비용 문제
                - Service는 notification의 type별로 딥링크를 덧붙여야함
                - ⇒ type 추가 시, Factory 뿐만 아니라 Service도 수정 대상이 됨
            - type 분기 중복
                - Factory → type 분기로 content 파싱
                - Service → type 분기로 딥링크 생성
                - ⇒ type 분기 중복 코드 발생
        - 예시
        
        ```java
        class NotificationService(
        	private val factory: NotificationContentFactory
        ) {
          fun getNotifications(notifications: List<Notification>): List<NotificationResponse> =
              notifications.map { notification ->
                  // factory 객체에서 content JSON 필드를 객체화
                  val content = factory.create(notification)
        
                  // type별 딥링크 생성
                  val link = when (notification.type) {
                      WORKOUT_PARTNER_REQUEST -> NotificationLink(MEMBER_PROFILE, mapOf("memberId" to (content as PartnerContent).payload.memberId))
                      ...
                  }
                  // 조합해서 response
                  NotificationResponse(notification.id, notification.type!!, sender, link)
              }
        }
        ```
        
    3. Strategy 패턴
        - 설명
            
            ![ChatGPT Image 2026년 10월 2일 오전 10_33_03.png](docs/images/6d50917b-3dc9-4d5b-b474-48ae676413e3.png)
            
            - Service
                - notification 객체만 던지면 response 받음
                - type 의존X
            - Registry
                - Mapper에게 response 객체 생성을 위임
            - Mapper 인터페이스
                - type별 변환 공통 클래스
            - Mapper 구현체
                - type별 content 파싱, 딥링크 생성, 문구 포맷팅을 전담
        - 장점
            - type 추가에 따른 Service 수정X
                - Service는 type을 몰라도되는 구조
                - type 추가 시 Mapper 클래스만 수정하면 되므로 Service 수정X
        - 단점
            - type마다 Mapper 클래스가 늘어남
            - Mapper마다 content 변환(convertValue) 호출 코드가 반복됨
- 결론
    - 방법 3(Strategy 패턴) 사용
    - 이유
        - type 추가에 따른 수정 범위를 최소화하기 위함
            - type 수정에 따라 content + 딥링크 + 문구 포맷팅 수정 비용이 있음
            - Service가 type을 알아야 하는 구조면, Factory와 Service를 모두 수정해야 함
            - 따라서, type 수정 책임을 한 곳에 몰아넣는 설계를 함
    - 감수한 손해
        - type마다 Mapper 클래스 파일이 늘어남
            - ⇒ Mapper마다 content 변환 코드가 반복됨
            - ⇒ Mapper마다 테스트 코드가 반복됨

### 순환 의존성 문제

- 문제점 - 도메인 간의 순환 참조
    - 요구사항
        
        ![image.png](docs/images/image%204.png)
        
        - 회원 조회 API
            - 회원은 상대 회원의 프로필을 볼 수 있음
            - 상대 프로필 조회 시, 내가 상대방에게 찜(하트)을 했는지 확인 가능
        - 찜 요청 API
            - 찜(하트)을 누르면 상대방을 내 찜 목록에 넣음
    - 문제 발생 : 순환 참조 (MemberService ↔ FavoriteService)
        - MemberService → FavoriteService
            - 회원 프로필 조회 시, 찜 여부 확인 필요
        - FavoriteService → MemberService
            - 찜 요청 시, 상대 회원이 존재하는지 확인 필요
    - 타 도메인에서의 순환 참조 문제
        - 위 상황뿐만 아니라 다양한 도메인에서 순환 참조 발생
        - 예시
            - 회원 ↔ 채팅방
                - 회원 조회 시 참여 중인 채팅방 정보 필요 / 채팅방 생성·참여 시 회원 확인 필요
            - 회원 ↔ 운동 파트너 요청
                - 회원 조회 시 파트너 요청 여부 확인 필요 / 파트너 요청 시 상대 회원 확인 필요
            - 등등
- 고려한 방법
    1. Repository 직접 참조
        - 설명
            - 다른 도메인의 Service 대신 Repository를 직접 호출
            - ex)
                - MemberService → FavoriteRepository
                - FavoriteService → MemberRepository
        - 장점
            - 서비스 간 의존이 사라져 순환이 해소됨
            - 구현이 단순하고 변경량이 적음
        - 단점
            - 다른 도메인의 저장소를 직접 알게 되어 도메인 경계가 흐려짐
    2. Facade 패턴
        - 설명
            - 상위 조정 서비스가 Member, Favorite을 각각 호출해 결과를 조합
            - 두 서비스는 서로를 몰라도 됨
        - 장점
            - 도메인 서비스 간 의존이 한 방향으로 정리됨
            - 도메인 서비스가 자기 책임에만 집중할 수 있음
        - 단점
            - 순환이 생길때마다 Facade가 필요함
            - 호출 흐름이 한 단계 늘어 추적이 번거로움
    3. Repository에 책임 부담 - 조회 쿼리에서 타 도메인 호출
        - 설명
            - 프로필 조회 쿼리에서 찜 테이블을 조인해 찜 여부를 한 번에 조회
            - 서비스 호출 없이 쿼리 레벨에서 해결
        - 장점
            - 서비스 간 의존X
        - 단점
            - 유연성이 떨어짐
                - 1개의 기능에 맞춰서 다른 도메인을 쿼리로 호출해야 함
                - 이에 따라, 함수 재사용이 어려워짐
            - 쿼리가 복잡해짐
    4. 순환 참조가 발생하는 도메인만 별도 객체 분리
        - 설명
            - 순환을 만드는 로직만 별도 클래스로 분리
            - ex) 찜 여부 확인 로직을 별도 객체로 빼서 Member, Favorite이 함께 사용
        - 장점
            - 문제가 되는 부분만 최소한으로 변경
            - 다른 도메인 구조에는 영향이 적음
        - 단점
            - 통일되지 않은 패턴 기준으로 설계 이해가 어려워짐
                - 어떤 설계는 A - B 객체 사이에 별도 객체가 있음
                - 어떤 설계는 D - E  객체 사이에 별도 객체가 없음
                - ⇒ 일관되지 않고 들쑥날쑥해짐
            - 순환 참조가 발생할때마다 클래스 별도 필요
    5. CQRS 패턴
        - 설명
            - 각 도메인은 Service, QueryService로 모두 분리
                - Service - 쓰기, 수정, 삭제
                - QueryService - 조회
            - 타 도메인 참조는 QueryService를 통해서만 수행
                - ex)
                    - MemberService → FavoriteQueryService
                    - FavoriteService → MemberQueryService
        - 장점
            - 모든 도메인에 동일한 규칙으로 설계 이해가 쉬움
            - 도메인의 비즈니스 규칙을 거치면서도 순환이 생기지 않음
            - 읽기/쓰기 책임이 분리되어 서비스 역할이 명확해짐
        - 단점
            - 클래스 수가 늘어남
            - QueryService끼리 서로 호출하기 시작하면 순환이 다시 생길 수 있음
- 결론
    - CQRS 패턴으로 해결
    - 이유
        - 코드 작업이 단순함
            - QueryService, Service로 분리만 하면 되기 때문에 리팩토링 비용이 적었음
        - 이해가 쉬움
            - 단순히 2개 객체로 분리해놓은 것이기 때문에 코드 이해가 어렵지 않았음
        - 별도로 또다시 순환 참조가 발생하지 않았음
            - 해당 프로젝트에서 순환 참조가 생겼던 원인
                - 해당 비즈니스 로직을 수행해도 되는지 확인하기 위해 타 도메인을 조회
                - 이렇게 조회하게 되는 도메인끼리는 연관이 깊은 경우가 많았음
                - ⇒ A가 B를 조회하면, B도 A를 조회할 확률이 높았고 A ↔ B 순환 참조가 발생
            - ⇒ 해당 패턴 도입으로 원인이 된 순환 참조가 모두 해결됨
    - 다른 방법을 선택하지 않은 이유
        - Repository 직접 참조
            - 순환 참조가 발생하는 곳이 여러 곳이였기 때문에 남용 위험이 컸음
            - A Service → B, C, … Repository를 참조하는 구조가 생기면 생길 수록 재사용도 어렵고 도메인 간에 경계도 흐려져서 혼란이 올 수 있음
        - Facade 패턴
            - 복잡한 로직이 있는 것도 아닌데 순환 참조 해결한다고 Facade를 적용하는 거는 과한 설계라고 봄
        - Repository에 책임 부담 - 조회 쿼리에서 타 도메인 호출
            - 재사용이 어려운게 타격이 큼
            - ex)
                - 프로필 조회, 운동 파트너 요청 이력 조회 모두 "요청 상태 조회"가 필요
                - 서비스 호출로 풀면 요청 상태 조회를 재사용할 수 있음
                - 쿼리에서 조인으로 풀면 기능마다 같은 로직을 중복 구현해야 해서 작업/수정 비용이 커짐
        - 순환 참조가 발생하는 도메인만 별도 객체 분리
            - 도메인마다 설계가 들쑥날쑥해져 설계 이해가 어려웠음
- 감수한 문제점
    - 타 도메인에 대한 의존 개수가 늘어남
        - 같은 도메인의 조회, 쓰기가 모두 필요하면 QueryService, Service를 둘 다 의존해야 함
        - 예시: 운동 요청 도메인 → 운동 기록 도메인
            - 조회(QueryService): 운동 요청 전송 시, 이미 운동 완료 이력이 있는지 확인
            - 쓰기(Service): 운동 요청 후 운동 완료 시 운동 기록 생성
            
            ```java
            @Service
            class WorkoutRequestService(
                private val workoutHistoryQueryService: WorkoutHistoryQueryService, // 조회
                private val workoutHistoryService: WorkoutHistoryService,           // 쓰기
                // ...
            ) {
            
                // 요청 메시지 전송 시: 완료 이력 여부 조회
                private fun sendStompWorkoutRequestMessage(...) {
                    val isCompleteWorkout = workoutHistoryQueryService.existsWorkoutHistoryFrom(chatRoom.id!!)
                    // ...
                }
            
                // 요청 완료 시: 운동 기록 생성
                private fun notifyWorkoutRequestStatusChange(...) {
                    if (isComplete(request)) {
                        val workoutHistory = workoutHistoryService.addWorkoutHistory(...)
                        // ...
                    }
                }
            }
            ```
            
    - Service 객체 증가
        - Service + QueryService로 서비스 객체 2배 증가
