# 제목
`변경 내용을 간결하게 요약해 주세요.`
ex)
- feat: 구글 소셜 로그인 기능 추가
- fix: 이메일 중복 체크 버그 수정
- chore: CI 빌드 캐시 설정 추가
- release: v1.2.0 배포

`키워드`
feat	새로운 기능 추가
fix	버그 수정
docs	문서 변경
style	코드 형식(들여쓰기, 공백 등) 변경(코드 동작에 영향을 주면 안됨)
refactor	코드 리팩토링(코드 기능 변경X)
test	테스트 코드의 작성 및 수정
chore	라이브러리 및 설정 추가
release 배포

## PR 타입(하나 이상의 PR 타입을 선택해주세요)
- [ ] 기능 추가
- [ ] 기능 삭제
- [ ] 기능 수정
- [ ] 버그 수정
- [ ] 의존성, 환경 변수, 빌드 관련 코드 업데이트

## 관련 Issue Number
ex)
- #130 fix: 회원가입 시 이메일 검증 오류 수정
- #135 chore: Gradle 빌드 경량화

## 반영 브랜치
`어떤 브랜치에서 작업했고 어떤 브랜치로 병합할 지 남겨주세요.`
ex) feat/login -> develop
ex) feat/login -> main

## 작업사항
`해당 이슈사항을 해결하기 위해 어떤 작업을 했는지 남겨주세요.`
ex) 로그인 시, 구글 소셜 로그인 기능을 추가했습니다.

## 테스트 방법
`테스트 위치/대상/유형을 남겨주세요.`
`(테스트를 위해 준비해야되는 환경이 있다면 남겨주세요)`
ex)
1. test/kotlin/kr/co/fitview/api/app/domain/auth/controller/AuthControllerTest
- User 도메인에 대한 Repository 테스트 완료
- Mock 객체 기반 단위 테스트 진행
2. test/kotlin/kr/co/fitview/api/app/domain/auth/service/AuthServiceTest
- User 도메인에 대한 Service 테스트 완료
- 통합 테스트(h2 DB) 진행

## 체크리스트
- [ ] 코드가 빌드되는지 확인함
- [ ] 모든 테스트가 통과하는지 확인함