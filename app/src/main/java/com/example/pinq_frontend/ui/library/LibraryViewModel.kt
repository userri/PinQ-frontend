package com.example.pinq_frontend.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinq_frontend.data.model.AttemptItem
import com.example.pinq_frontend.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 오답노트 / 북마크 / 전체 풀이이력 화면 공용 ViewModel.
 *
 *  - 각 탭은 [loadWrongNotes], [loadBookmarks], [loadAttempts] 로 명시 로드한다.
 *  - 북마크 토글은 [toggleBookmark] — 낙관적 업데이트 후 실패 시 롤백.
 *  - 한 탭에서 토글된 북마크 상태는 다른 탭 리스트에도 동기화된다.
 *
 * Phase 4 메모:
 *  - 이전엔 SharedPreferences 기반이었던 오답노트가 서버 기반으로 이관됐다.
 *    화면 진입 시마다 가벼운 GET 으로 최신 데이터를 받아 표시한다.
 */
class LibraryViewModel(
    private val repository: LibraryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    fun loadWrongNotes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingWrong = true, wrongError = null) }
            runCatching { repository.getWrongNotes() }
                .onSuccess { list ->
                    _state.update { it.copy(isLoadingWrong = false, wrongNotes = list) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoadingWrong = false,
                            wrongError = e.message ?: "오답노트를 불러오지 못했어요",
                        )
                    }
                }
        }
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingBookmark = true, bookmarkError = null) }
            runCatching { repository.getBookmarks() }
                .onSuccess { list ->
                    _state.update { it.copy(isLoadingBookmark = false, bookmarks = list) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoadingBookmark = false,
                            bookmarkError = e.message ?: "북마크를 불러오지 못했어요",
                        )
                    }
                }
        }
    }

    fun loadAttempts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingAttempts = true, attemptsError = null) }
            runCatching { repository.getAttempts() }
                .onSuccess { list ->
                    _state.update { it.copy(isLoadingAttempts = false, attempts = list) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isLoadingAttempts = false,
                            attemptsError = e.message ?: "풀이 이력을 불러오지 못했어요",
                        )
                    }
                }
        }
    }

    /**
     * 북마크 토글. 낙관적 업데이트 — 즉시 UI 반영, 실패 시 롤백.
     *
     * 북마크 탭에서 해제한 경우엔 리스트에서 즉시 제거되지만,
     * "전체 이력"이나 "오답노트" 의 같은 quizId 항목 bookmarked 도 함께 갱신된다.
     */
    fun toggleBookmark(quizId: Long, currentlyBookmarked: Boolean) {
        val nextBookmarked = !currentlyBookmarked
        applyBookmarkSync(quizId, nextBookmarked, removeFromBookmarksIfFalse = true)

        viewModelScope.launch {
            runCatching {
                if (nextBookmarked) repository.addBookmark(quizId)
                else repository.removeBookmark(quizId)
            }.onFailure { e ->
                // 실패 시 롤백
                applyBookmarkSync(quizId, currentlyBookmarked, removeFromBookmarksIfFalse = false)
                _state.update { it.copy(toggleError = e.message ?: "북마크 처리에 실패했어요") }
            }
        }
    }

    fun clearToggleError() {
        _state.update { it.copy(toggleError = null) }
    }

    /**
     * wrongNotes / bookmarks / attempts 의 동일 quizId 항목에 새 북마크 상태를 적용한다.
     *
     * @param removeFromBookmarksIfFalse  bookmarks 리스트에서 해제된 경우 즉시 제거할지.
     *        토글 도중(낙관적)에는 true, 롤백 시엔 false 로 호출.
     */
    private fun applyBookmarkSync(
        quizId: Long,
        newBookmarked: Boolean,
        removeFromBookmarksIfFalse: Boolean,
    ) {
        _state.update { s ->
            s.copy(
                wrongNotes = s.wrongNotes.syncBookmark(quizId, newBookmarked),
                attempts = s.attempts.syncBookmark(quizId, newBookmarked),
                bookmarks = if (!newBookmarked && removeFromBookmarksIfFalse) {
                    s.bookmarks.filterNot { it.quizId == quizId }
                } else {
                    s.bookmarks.syncBookmark(quizId, newBookmarked)
                },
            )
        }
    }

    private fun List<AttemptItem>.syncBookmark(quizId: Long, bookmarked: Boolean): List<AttemptItem> =
        map { if (it.quizId == quizId) it.copy(bookmarked = bookmarked) else it }

    companion object {
        fun factory(repository: LibraryRepository) = viewModelFactory {
            initializer { LibraryViewModel(repository) }
        }
    }
}
