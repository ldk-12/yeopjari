# 근거와 후속 실측

공식 PLEOS는 CarUxRestrictions 상태/ActiveRestrictions를 적용하도록 요구한다. DO 미선언 앱은 Park 가능, Drive 불가가 기본이다. 카테고리의 제한 허용 예시는 승인 보장이 아니다.
- https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
- https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy
- https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-category
- https://developer.android.com/reference/android/car/drivingstate/CarUxRestrictionsManager

재접속 후: android.car API stub 서명확인 및 컴파일; 승인된 기존 debug키 서명; 별도패키지 설치. 초기 service미연결/제한상태부터 앱 시작해 WebView를 보여주지 않는지 내부 assertion. 가상 Park→Drive→Park와 activity pause/resume, service 실패를 각각 검사. 시연용 예시 데이터 단위테스트와 실제 callback 시험을 구분한다. 기존 콘솔 신호 주입은 가상입력일 뿐 실차 안전성과 동일하지 않다. 실제 권한 revoke/system kill 같은 미승인 고장주입은 하지 않는다.

기록은 UX raw flag/DO요구·게이트결정·WebView visibility·세션 초기화 상태로 남긴다. 플랫폼 차단 overlay를 본 것만으로 앱 자체 숨김이 검증됐다고 하지 않는다. 사용자 위치 접근 없이 진행한다.
