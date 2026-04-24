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
    너는 시니어 백엔드 개발자야. 아래 [코드 변경사항]을 분석해서 [PR 템플릿] 양식에 맞춰 한국어로 작성해줘.
    [PR 템플릿]
    # 제목
    `변경 내용을 간결하게 요약해 주세요.`
    ## PR 타입
    - [ ] 기능 추가
    - [ ] 기능 수정
    ## 반영 브랜치
    {TARGET_BRANCH}
    ## 작업사항
    `해당 이슈사항을 해결하기 위해 어떤 작업을 했는지 남겨주세요.`
    [코드 변경사항(diff)]
    {diff[:15000]}
    """
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={GEMINI_API_KEY}"
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

            # 2. 업데이트 하기 직전에 10초를 쉽니다. (순서 변경됨!)
            print("⏳ GitHub 서버 동기화를 위해 10초간 대기합니다...")
            time.sleep(10)

            # 3. 그 다음 업데이트를 시도합니다.
            update_pr_body(ai_response)
        else:
            print("⚠️ 변경 사항이 없어 스킵합니다.")

    except urllib.error.HTTPError as e:
        print(f"❌ HTTP 에러 발생: {e.code} {e.reason}")
        print(f"🔍 서버 응답 상세 내용: {e.read().decode('utf-8')}")
    except Exception as e:
        print(f"❌ 에러 발생: {e}")