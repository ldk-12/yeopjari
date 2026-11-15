# 옆자리

익숙한 길의 주의할 지점을 출발 전에 확인하고, 도착 후 메모를 남기는 PLEOS 앱 프로토타입.

## 기능

- **차량 연동:** Vehicle SDK로 기어와 브레이크 상태를 읽는다.
- **화면 제한:** CarUxRestrictions에 제한이 없고 최근 기어 상태가 Park일 때만 화면을 표시한다. 상태 미확인·주행·앱 이탈 시 숨긴다. DO 예외는 사용하지 않는다.
- **화면 시연:** 경로·교통·메모는 시연용 예시 데이터다. 위치 수집과 길 안내, 운전 평가는 아직 연결하지 않았다.

## 개발 환경

- PLEOS Connect Emulator **2.1.0 ARM64**, Android 14
- 화면 **2560×1440**, 160dpi
- JDK, Android SDK Platform 35, Build Tools 35.0.0
- Vehicle SDK **2.3.2**와 의존성 캐시
- 런타임 공유 라이브러리: `ai.pleos.playground.car`

## 빌드와 실행

공식 SDK 설정으로 의존성을 받은 뒤 저장소 루트에서 실행한다. 준비 스크립트는 Gradle 캐시의 파일을 해시로 검증해 가져온다.

```sh
export JAVA_HOME=/path/to/jdk
export ANDROID_HOME=/path/to/android-sdk
export ANDROID_CAR_JAR="$ANDROID_HOME/platforms/android-35/optional/android.car.jar"
python3 scripts/prepare-dependencies.py
export PLEOS_DEPS_DIR="$PWD/build/dependencies"
bash scripts/build-unsigned.sh
```

결과: `build/coach-boundary-unsigned.apk`. javac → D8 → aapt2 → zipalign 순서로 빌드하며, 설치하려면 별도 서명이 필요하다. SDK 자동 다운로드와 서명은 스크립트에 포함하지 않는다.

저장소 루트에서 APK를 설치하고 실행한다:

```sh
adb install -r apk/yeopjari_release.apk
adb shell am start -n app.pleos.coachboundary.integrated/app.pleos.coachboundary.MainActivity
```

기존 debug 앱과 서명이 다르면 덮어쓸 수 없으므로 별도 AVD를 사용한다.

### 시연 설정

1. 일반 휴대폰 AVD가 아닌 PLEOS Connect 전용 AVD를 실행한다.
2. 가상 차량을 **Park, 속도 0**으로 설정하고 앱을 연다.
3. 차량 서비스가 연결되고 화면 제한이 해제되면 준비 화면이 나온다. SDK 초기화 중에는 숨김 상태를 유지한다.

`CAR_DRIVE`와 `VEHICLE_BINDING` 권한은 Manifest에 선언돼 있다. 위치·마이크·네트워크 설정은 필요하지 않다. 시험에서는 가상 콘솔로 Drive와 Park를 전환했다.

## 테스트

Node.js 20+, Python 3.11+:

```sh
node tests/ui.cjs
python3 tests/integration.py
python3 tests/repository.py
```

GitHub Actions에서 UI 상태 16개, Android 연동 코드, 저장소 포함 파일을 검사한다. UI 검사는 모의 DOM을 사용한다. Android 빌드와 에뮬레이터 시험은 로컬에서 진행한다.

서명 APK(`debuggable=false`)로 **Park 표시 → Drive 숨김 → 제한 상태에서 재시작 → Park 복귀**를 확인했다. [시험 기록](docs/RUNTIME_RESULT.json)에 조건과 결과를 남겼다. 실차·전체 터치 흐름·서비스 장애·지역별 정책은 후속 시험 대상이다.

## 코드 구조

```text
android/src/main/java/    차량 상태, 화면 제한, WebView
android/src/main/assets/  차량용 UI와 예시 데이터
scripts/                 의존성 준비·APK 빌드
tests/                   상태·코드·파일 검사
docs/                    정책 근거·시험 기록
licenses/                의존성 목록·원본 근거·고지
```

## APK 위치

[release APK 다운로드](apk/yeopjari_release.apk)

- 경로: `apk/yeopjari_release.apk`
- 버전: `0.3-integrated-demo` (versionCode 2)
- 크기: 8,831,195 bytes
- SHA-256: `af1e936836cc3dfaade847058040a7edb48eeb31a99a294c590e4c4abbda9eeb`
- 전용 배포 키 서명, `debuggable=false`. PLEOS 에뮬레이터 실행 확인.

공모전 검토용 APK다. 개인키와 암호는 포함하지 않는다. 최종 팀명에 맞춘 파일명은 접수 시 확인한다.

## 라이선스

`Vehicle 2.3.2`, `vehicle-aidl 1.1.1`, `vehicle-data 1.1.11`의 배포 조건과 추가 고지는 확인 중이다. 의존성 40개의 원본 해시와 37개의 Apache-2.0 근거는 [라이선스 문서](licenses/README.md)에 정리했다. [NOTICE](NOTICE)도 함께 확인한다. 앱 코드의 공개 라이선스는 아직 정하지 않았다.
