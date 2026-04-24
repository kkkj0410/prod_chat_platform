import os
import subprocess
import json
import urllib.request

# GitHub Actions 환경변수 로드
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
token_raw = os.getenv("GITHUB_TOKEN")
GITHUB_TOKEN = token_raw.strip() if token_raw else None
PR_NUMBER = os.getenv("PR_NUMBER")
REPO = os.getenv("GITHUB_REPOSITORY")
TARGET_BRANCH = os.getenv("TARGET_BRANCH")

def ask_ai(diff):
    print("🤖 AI 분석 시작...")

    # 핏뷰 팀의 고유 템플릿 주입
    prompt = f"""
    너는 시니어 백엔드 개발자야. 아래 [코드 변경사항]을 분석해서 [PR 템플릿] 양식에 맞춰 한국어로 작성해줘.
    가이드(` `)나 예시(ex) 문구는 삭제하고 네가 분석한 실제 내용만 채워. 
    관련이 있는 PR 타입에는 [ ]를 [x]로 표시해.

    [PR 템플릿]
    # 제목
    `변경 내용을 간결하게 요약해 주세요.`

    ## PR 타입(하나 이상의 PR 타입을 선택해주세요)
    - [ ] 기능 추가
    - [ ] 기능 삭제
    - [ ] 기능 수정
    - [ ] 버그 수정
    - [ ] 의존성, 환경 변수, 빌드 관련 코드 업데이트

    ## 관련 Issue Number
    `이슈 번호를 추출해서 넣어줘. 모르면 비워둬.`

    ## 반영 브랜치
    ex) feat/branch -> {TARGET_BRANCH}

    ## 작업사항
    `해당 이슈사항을 해결하기 위해 어떤 작업을 했는지 남겨주세요.`

    ## 테스트 방법
    `테스트 위치/대상/유형을 분석해서 남겨주세요.`

    ## 체크리스트
    - [x] 코드가 빌드되는지 확인함
    - [x] 모든 테스트가 통과하는지 확인함

    [코드 변경사항(diff)]
    {diff[:15000]}
    """

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={GEMINI_API_KEY}"
    payload = {
        "contents": [{"parts": [{"text": prompt}]}]
    }

    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})

    with urllib.request.urlopen(req) as response:
        result = json.loads(response.read().decode('utf-8'))
        return result['candidates'][0]['content']['parts'][0]['text']

def update_pr_body(body):
    print(f"🚀 PR #{PR_NUMBER} 본문 업데이트 중...")
    # URL이 정확한지 로그로 찍어서 확인
    url = f"https://api.github.com/repos/{REPO}/pulls/{PR_NUMBER}"
    print(f"🔗 요청 URL: {url}")

    headers = {
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json",
        "Content-Type": "application/json",
        "User-Agent": "github-actions-ai-summarizer"
    }

    data = json.dumps({"body": body}).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers=headers, method='PATCH')

    try:
        with urllib.request.urlopen(req) as response:
            if response.status == 200:
                print("✅ PR 업데이트 성공!")
    except urllib.error.HTTPError as e:
        print(f"❌ HTTP 에러 발생: {e.code} {e.reason}")
        # 404의 진짜 원인을 출력합니다 (핵심!)
        error_body = e.read().decode('utf-8')
        print(f"🔍 서버 응답 상세 내용: {error_body}")
        raise e

if __name__ == "__main__":
    try:
        # 서버(CI) 환경에서는 origin/브랜치명 형식을 사용해야 합니다.
        diff_text = subprocess.check_output(
            ['git', 'diff', f'origin/{TARGET_BRANCH}...HEAD'],
            encoding='utf-8'
        )

        if diff_text.strip():
            ai_response = ask_ai(diff_text)
            update_pr_body(ai_response)

            print("⏳ GitHub 서버 동기화를 위해 10초간 대기합니다...")
            time.sleep(10)

        else:
            print("⚠️ 변경 사항이 없어 스킵합니다.")

    except Exception as e:
        print(f"❌ 에러 발생: {e}")