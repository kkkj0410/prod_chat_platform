import os
import subprocess
import json
import urllib.request
import time  # time 모듈이 import 되어 있는지 확인하세요!

# GitHub Actions 환경변수 로드
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
token_raw = os.getenv("GITHUB_TOKEN")
GITHUB_TOKEN = token_raw.strip() if token_raw else None
PR_NUMBER = os.getenv("PR_NUMBER")
REPO = os.getenv("GITHUB_REPOSITORY")
TARGET_BRANCH = os.getenv("TARGET_BRANCH")

def ask_ai(diff):
    print("🤖 AI 분석 시작...")
    prompt = f"""
너는 바쁜 팀원들을 위해 핵심만 명확하게 전달하는 시니어 백엔드 개발자야. 
    아래 [코드 변경사항]을 분석해서 [PR 템플릿] 양식에 맞춰 한국어로 작성해줘.

    [작성 지침 - 매우 중요]
    1. 간결성: 줄글 형태의 장황한 설명과 파일 경로/이름 나열은 절대 금지한다.
    2. 목적 중심: '어떤 코드를 바꿨는지'보다 '무엇을, 왜 추가/수정했는지' 기능 위주로 요약한다.
    3. TMI 제외: 단순 공백 제거, Import 구문 수정, 사소한 오타 수정 등 비즈니스 로직과 무관한 변경사항은 요약에서 철저히 제외한다.
    4. 분량 제한: `## 작업사항`의 각 항목은 반드시 '- ' 기호를 사용한 개조식으로 작성하며, 항목당 1줄 이내로 제한한다. 최대 4개 항목을 넘지 않도록 한다.
    
    [PR 템플릿]
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
    관련 Issue Number가 없으면 해당란은 생략한다.
    
    
    ## 체크리스트
    - [ ] 코드가 빌드되는지 확인함
    - [ ] 모든 테스트가 통과하는지 확인함
    ## 반영 브랜치
    {TARGET_BRANCH}
    ## 작업사항
    `해당 이슈사항을 해결하기 위해 어떤 작업을 했는지 남겨주세요.`
    
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
    [코드 변경사항(diff)]
    {diff[:15000]}
    """
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={GEMINI_API_KEY}"
    payload = {"contents": [{"parts": [{"text": prompt}]}]}
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as response:
        result = json.loads(response.read().decode('utf-8'))
        return result['candidates'][0]['content']['parts'][0]['text']

def update_pr_body(body):
    print(f"🚀 PR #{PR_NUMBER} 본문 업데이트 중...")
    url = f"https://api.github.com/repos/{REPO}/pulls/{PR_NUMBER}"
    headers = {
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json",
        "Content-Type": "application/json",
        "User-Agent": "github-actions-ai-summarizer"
    }
    data = json.dumps({"body": body}).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers=headers, method='PATCH')
    with urllib.request.urlopen(req) as response:
        if response.status == 200:
            print("✅ PR 업데이트 성공!")

if __name__ == "__main__":
    try:
        diff_text = subprocess.check_output(
            ['git', 'diff', f'origin/{TARGET_BRANCH}...HEAD'],
            encoding='utf-8'
        )

        if diff_text.strip():
            # 1. AI 답변을 먼저 생성합니다.
            ai_response = ask_ai(diff_text)

            # 3. 그 다음 업데이트를 시도합니다.
            update_pr_body(ai_response)
        else:
            print("⚠️ 변경 사항이 없어 스킵합니다.")

    except urllib.error.HTTPError as e:
        print(f"❌ HTTP 에러 발생: {e.code} {e.reason}")
        print(f"🔍 서버 응답 상세 내용: {e.read().decode('utf-8')}")
    except Exception as e:
        print(f"❌ 에러 발생: {e}")