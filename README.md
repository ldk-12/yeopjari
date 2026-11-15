# 옆자리

출발 전에는 익숙한 길의 주의할 곳을 떠올리고, 도착 후에는 짧게 메모하는 PLEOS 차량용 시연 앱입니다.

## 구현 범위

| 구성 | 현재 동작 |
| --- | --- |
| 차량 상태 | PLEOS Vehicle SDK의 Gear·Brake 읽기 |
| 화면 제한 | CarUxRestrictions와 최근 Park 상태가 확인될 때 표시 |
| 경로·교통·메모 | 시연용 예시 데이터로 화면 흐름 구현 |
| 위치·길 안내·운전 평가 | 미연동, 개발 예정 |

처음에는 화면을 숨깁니다. 제한·비주차·알 수 없는 상태·앱 이탈 시 다시 숨깁니다. 운전 중 사용 예외(DO)는 선언하지 않습니다.

## 구조

```text
android/src/main/java/    SDK 상태, 화면 게이트, WebView
android/src/main/assets/  차량창 UI와 예시 데이터
scripts/                 의존성 준비 및 미서명 APK 빌드
tests/                   UI 상태·통합 경계·배포 파일 검사
docs/                    플랫폼 정책 근거와 실행 결과
licenses/                의존성 해시, 원본 근거, 고지
```

## 검사

Node.js 20 이상과 Python 3.11 이상에서 외부 패키지 없이 실행합니다.

```sh
node tests/ui.cjs
python3 tests/integration.py
python3 tests/repository.py
```

GitHub Actions도 이 세 검사를 실행합니다. UI 검사는 가짜 DOM으로 상태와 출력 문자열을 검사하며, Android 빌드·에뮬레이터·실차 시험을 대신하지 않습니다. SDK를 내려받는 권한이나 배포 키를 CI에 넣지 않았습니다.

## Android 빌드

JDK, Android SDK Platform 35 / Build Tools 35.0.0 및 공식 SDK 설정으로 확보한 Gradle 의존성 캐시가 필요합니다.

```sh
export JAVA_HOME=/path/to/jdk
export ANDROID_HOME=/path/to/android-sdk
export ANDROID_CAR_JAR="$ANDROID_HOME/platforms/android-35/optional/android.car.jar"
python3 scripts/prepare-dependencies.py
export PLEOS_DEPS_DIR="$PWD/build/dependencies"
bash scripts/build-unsigned.sh
```

`build/coach-boundary-unsigned.apk`가 생성됩니다. 서명 전에는 설치할 수 없습니다. 준비 스크립트는 원본 아카이브와 추출 JAR의 SHA-256을 확인하며, SDK 다운로드·약관 동의를 자동으로 수행하지 않습니다. 캐시가 없는 환경에서의 자동 다운로드 재현은 아직 검증하지 않았습니다.

## 실행 검증

PLEOS Connect Emulator 2.1.0 ARM64, Android 14, 2560×1440, 160dpi에서 확인했습니다. 필요한 런타임 공유 라이브러리는 `ai.pleos.playground.car`입니다.

전용 키로 서명한 APK를 별도 AVD에 설치하여 Park 표시 → Drive 숨김 → 제한 상태 새 시작 숨김 → Park 복귀 표시를 검증했습니다. 실제 앱의 `debuggable`은 false입니다. 빌드는 javac/D8/aapt2/zipalign/apksigner 절차로 수행했습니다. 상세 결과는 [실행 기록](docs/RUNTIME_RESULT.json)을 참조하세요.

전체 터치 흐름·서비스 장애·모든 지역 정책·실차 및 안전효과는 검증하지 않았습니다. UX 상태의 getter와 listener 지연을 각각 측정한 결과도 아닙니다.

## 배포 상태

이 저장소에는 **소스만** 있습니다. APK·SDK JAR/AAR·개인 서명키·암호는 포함하지 않습니다. APK는 별도로 서명·검증했지만 SDK 배포 조건 확인 전까지 GitHub 업로드를 보류했습니다. 따라서 이 저장소만으로 공모전의 소스+release APK 제출 구성이 완성되지는 않습니다.

40개 의존성 원본 해시와 37개 Apache-2.0 근거를 확인했습니다. `Vehicle 2.3.2`, `vehicle-aidl 1.1.1`, `vehicle-data 1.1.11`의 개별 배포 조건 및 추가 고지는 확인 중입니다. [NOTICE](NOTICE)와 [의존성 고지](licenses/README.md)를 참조하세요. 앱 자체 코드의 공개 라이선스는 아직 지정하지 않았습니다.
