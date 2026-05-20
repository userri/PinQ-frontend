package com.finq.app.data.local

// Phase 4 변경: 오답노트가 서버 기반으로 이관되어 로컬 저장소가 더 이상 필요하지 않다.
// 이전 LocalModule.init / LocalModule.switchUser 호출은 모두 제거됐다.
//
// 기기에 잔존하는 SharedPreferences 파일(wrong_notes_v1_*) 이 있을 수 있으나,
// 사용자 식별을 nickname 으로 했으므로 다음 로그인 시점에 자동으로 무효화된다.
// 명시적인 정리가 필요하면 별도 마이그레이션 로직에서 deleteSharedPreferences 호출.
