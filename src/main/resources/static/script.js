// DOM 요소 가져오기
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const emailInput = document.getElementById('email');
const signupButton = document.getElementById('signupButton');
const loginButton = document.getElementById('loginButton');
const logoutButton = document.getElementById('logoutButton');
const memberDataButton = document.getElementById('memberDataButton');
const adminDataButton = document.getElementById('adminDataButton');
const resultDiv = document.getElementById('result');

// ✨ 추가된 부분: OAuth2 추가 정보 입력 폼
const oauthSignupForm = document.getElementById('oauthSignupForm');
const oauthUsernameInput = document.getElementById('oauthUsername');
const oauthSignupButton = document.getElementById('oauthSignupButton');

// ✨ 추가된 부분: ID가 부여된 div들을 가져옴
const localAuthForm = document.getElementById('localAuthForm');
const localAuthButtons = document.getElementById('localAuthButtons');


// Access Token을 저장할 변수 (페이지 로드 시 로컬 스토리지에서 가져옴)
let accessToken = localStorage.getItem('accessToken');
let tempToken = null; // ✨ 추가된 부분: OAuth2 임시 토큰 저장

// 로그를 화면에 표시하는 함수
const log = (message) => {
    resultDiv.textContent += message + '\n';
};

// ## API 요청 함수들 ##

// 회원가입
signupButton.addEventListener('click', async () => {
    const username = usernameInput.value;
    const password = passwordInput.value;
    const email = emailInput.value;
    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, email, admin: false }) // 일반 유저로 가입
        });
        const data = await response.text();
        log(`회원가입 결과: ${data}`);
    } catch (error) {
        log(`에러: ${error.message}`);
    }
});

// 로그인
loginButton.addEventListener('click', async () => {
    const username = usernameInput.value;
    const password = passwordInput.value;
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) throw new Error('로그인 실패');

        const data = await response.json();
        // 응답 본문에서 accessToken만 가져옴
        accessToken = data.accessToken;

        // accessToken만 localStorage에 저장
        localStorage.setItem('accessToken', accessToken);

        log('로그인 성공!');
        log('서버로부터 HttpOnly Refresh Token 쿠키를 받았습니다.');
        log(`Access Token: ${accessToken.substring(0, 30)}...`);
    } catch (error) {
        log(`에러: ${error.message}`);
    }
});

// 로그아웃
logoutButton.addEventListener('click', async () => {
    try {
        // 서버에 로그아웃 요청을 보내 쿠키를 만료시키도록 함
        await fetch('/api/auth/logout', { method: 'POST' });
    } catch (e) {
        console.error("로그아웃 요청 실패", e);
    } finally {
        accessToken = null;
        localStorage.removeItem('accessToken');
        log('로그아웃 되었습니다. (쿠키와 로컬 스토리지 정리)');
    }
});

// 멤버 데이터 요청
memberDataButton.addEventListener('click', async () => {
    try {
        const data = await fetchWithAuth('/api/member/data');
        log('멤버 데이터: ' + data);
    } catch (error) {
        log('멤버 데이터 요청 실패: ' + error.message);
    }
});

// 관리자 데이터 요청
adminDataButton.addEventListener('click', async () => {
    try {
        const data = await fetchWithAuth('/api/admin/data');
        log('관리자 데이터: ' + data);
    } catch (error) {
        log('관리자 데이터 요청 실패: ' + error.message);
    }
});

// 🔄 수정된 부분: 페이지 로드 시 실행되는 로직
window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('accessToken');
    const tempTokenFromUrl = urlParams.get('tempToken'); // tempToken 확인

    if (tokenFromUrl) {
        log('로그인 성공! Access Token을 저장합니다.');
        accessToken = "Bearer " + tokenFromUrl;
        localStorage.setItem('accessToken', accessToken);
        window.history.replaceState({}, document.title, window.location.pathname);
    } else if (tempTokenFromUrl) {
        log('OAuth2 신규 사용자입니다. 추가 정보를 입력해주세요.');
        tempToken = tempTokenFromUrl; // 임시 토큰 저장

        // 🔄 수정: 더 간단하고 명확한 방식으로 폼 숨기기/보이기
        localAuthForm.style.display = 'none';
        localAuthButtons.style.display = 'none';
        oauthSignupForm.style.display = 'block'; // OAuth2용 폼 보이기

        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

// ✨ 추가된 부분: OAuth2 최종 가입 버튼 이벤트 리스너
oauthSignupButton.addEventListener('click', async () => {
    const username = oauthUsernameInput.value;
    if (!username) {
        log('사용할 아이디를 입력해주세요.');
        return;
    }

    try {
        const response = await fetch('/api/auth/oauth-signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, tempToken })
        });

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData || '가입 실패');
        }

        const data = await response.json();
        accessToken = data.accessToken;
        localStorage.setItem('accessToken', accessToken);

        log('최종 회원가입 및 로그인 성공!');
        oauthSignupForm.style.display = 'none'; // 폼 숨기기
    } catch (error) {
        log(`에러: ${error.message}`);
    }
});


// =================================================================
// ## 🚀 자동 토큰 재발급 로직이 포함된 fetch 함수 ##
// =================================================================
async function fetchWithAuth(url, options = {}) {
    // 1. Access Token과 함께 첫 요청 보내기
    let response = await fetch(url, {
        ...options,
        headers: {
            ...options.headers,
            'Authorization': accessToken
        }
    });

    // 2. 만약 응답이 401 Unauthorized 에러(토큰 만료)라면?
    if (response.status === 401) {
        log('Access Token이 만료되었습니다. 재발급을 시도합니다...');

        try {
            // 3. reissue 요청 시 Refresh Token을 헤더에 담지 않음
            // 브라우저가 HttpOnly 쿠키를 자동으로 포함하여 요청함
            const refreshResponse = await fetch('/api/auth/reissue', {
                method: 'POST'
            });

            if (!refreshResponse.ok) {
                throw new Error('토큰 재발급에 실패했습니다. 다시 로그인 해주세요.');
            }

            const data = await refreshResponse.json();
            const newAccessToken = data.accessToken;
            log('새로운 Access Token 발급 성공!');

            // 4. 새로 받은 토큰으로 교체하고 저장
            accessToken = newAccessToken;
            localStorage.setItem('accessToken', newAccessToken);

            // 5. 원래 하려던 API 요청을 새로운 토큰으로 다시 시도
            log('원래 요청을 재시도합니다...');
            response = await fetch(url, {
                ...options,
                headers: {
                    ...options.headers,
                    'Authorization': accessToken
                }
            });

        } catch (refreshError) {
            // 리프레시 토큰마저 만료된 경우 강제 로그아웃 처리
            logout();
            throw refreshError; // 에러를 호출한 곳으로 전파
        }
    }

    // 최종 응답이 성공이 아니면 에러 발생
    if (!response.ok) {
        throw new Error(`API 요청 실패 (상태: ${response.status})`);
    }

    // 성공적인 응답의 텍스트 본문을 반환
    return response.text();
}