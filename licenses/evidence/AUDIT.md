# 라이선스 추가 검토 (2026-11-14)
기존 미확인 18개 중 13 AndroidX는 정확 버전 공식 Google Maven POM에서 Apache License 2.0 선언 확인. listenablefuture 1.0은 공식 POM 부모 guava-parent 26.0-android의 Apache 2.0 선언으로 확인. javax.inject 1은 공식 Maven sources.jar의 모든 Java 소스 헤더에서 Apache 2.0과 JSR-330 Expert Group 2009 고지 확인.

따라서 선언·원본 소스 근거가 추가 확보된 것은 15개. 여전히 Vehicle 2.3.2, vehicle-aidl 1.1.1, vehicle-data 1.1.11의 개별 재배포 조건은 미확정. 기존 22개와 합쳐 37개에 Apache2 근거가 있으나 모든 실제 빌드 JAR의 원본 동일성, 포함된 제3자 코드, NOTICE 및 최종 APK/ZIP 고지 보존은 별도 검사 필요. 이를 법적 완전성 확인이라고 표시하지 말 것.

SDK 공식 시스템 이미지 XML의 라이선스는 1.1에서 SDK(API/emulator/documents)를 정의, 3.1에서 호환 CRP 앱 개발 목적으로 제한 사용 허용, 3.4에서 SDK 및 그 일부의 재배포 등에 제한, 3.7에서 고지 삭제 금지. 4.1은 개발 앱의 권리를 구분. 이 문구만으로 SDK 런타임이 포함된 공모전 앱 APK 배포의 허용/금지를 확정할 수 없음. SDK 원본 JAR/AAR, emulator, credentials는 제출 소스 ZIP에 포함하지 않는 보수적 패키징을 권고. 공식 공모전은 실행 가능한 release APK 선택 제출을 명시하므로 앱 배포와 SDK 자체 재배포를 구분해야 함.

원문은 sources.json의 공식 URL과 내려받은 POM/헤더를 참조. 기존 결과물 수정·키 생성·서명·외부 제출 없음.
