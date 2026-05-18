package com.example.pinq_frontend.ui.library

import com.example.pinq_frontend.data.model.AttemptItem

/**
 * "내 문제함" UI 상태.
 *
 * 한 ViewModel 이 오답노트 / 북마크 / 전체이력 3개 탭을 모두 관리한다.
 * 탭 전환 시 별도 API 호출 없이 즉시 표시가 되도록 각 리스트를 별도 필드로 보관한다.
 *  - 첫 진입 시 모두 비어있고, 각 탭이 처음 노출될 때 lazy 로드.
 *
 * 토글 결과를 화면에 즉시 반영하기 위해 wrongNotes / bookmarks / attempts 각각의 항목에서
 * bookmarked 플래그를 동기화한다 (낙관적 업데이트).
 */
data class LibraryUiState(
    val wrongNotes: List<AttemptItem> = emptyList(),
    val bookmarks: List<AttemptItem> = emptyList(),
    val attempts: List<AttemptItem> = emptyList(),
    /** 각 탭의 로딩 상태. true 면 스피너 표시. */
    val isLoadingWrong: Boolean = false,
    val isLoadingBookmark: Boolean = false,
    val isLoadingAttempts: Boolean = false,
    /** 화면 단위 에러 메시지. 토글 실패는 toggleError 로 분리. */
    val wrongError: String? = null,
    val bookmarkError: String? = null,
    val attemptsError: String? = null,
    /** 북마크 토글 도중 발생한 일시 에러 (Snackbar 등으로 노출). */
    val toggleError: String? = null,
)
