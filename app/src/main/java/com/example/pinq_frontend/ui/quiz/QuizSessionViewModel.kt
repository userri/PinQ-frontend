package com.example.pinq_frontend.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pinq_frontend.data.local.SavedWrongNote
import com.example.pinq_frontend.data.local.WrongNoteStore
import com.example.pinq_frontend.data.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 퀴즈 세션 전체를 책임지는 ViewModel.
 *
 * 책임:
 *  - 오늘의 퀴즈 로딩
 *  - 사용자 옵션 선택 / 정답 제출 / 다음 문제 이동 / 재시작
 *  - 누적 정답 개수 관리
 *  - 오답노트 저장 (saveWrongNotes)
 */
class QuizSessionViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizSessionUiState())
    val uiState: StateFlow<QuizSessionUiState> = _uiState.asStateFlow()

    init {
        loadQuizzes()
    }

    /** 오늘의 퀴즈를 불러온다. 실패 시 error 메시지를 상태에 담아둔다. */
    fun loadQuizzes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { quizRepository.getTodayQuizzes() }
                .onSuccess { quizzes ->
                    _uiState.update { it.copy(isLoading = false, quizzes = quizzes) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
        }
    }

    /** 사용자가 옵션을 탭했을 때 호출. */
    fun selectOption(optionId: Long) {
        _uiState.update { it.copy(selectedOptionId = optionId) }
    }

    /**
     * 현재 선택된 옵션으로 정답 제출.
     * 결과는 [QuizSessionUiState.lastAnswer] 로 흘러간다.
     * 정답이면 [QuizSessionUiState.correctCount] 가 1 증가.
     */
    fun submitAnswer() {
        val state = _uiState.value
        val quiz = state.currentQuiz ?: return
        val selected = state.selectedOptionId ?: return
        // 이미 제출된 문제는 재제출 차단 (네트워크 중복 호출 방지)
        if (state.lastAnswer != null || state.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            runCatching { quizRepository.submitAnswer(quiz.id, selected) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            lastAnswer = result,
                            correctCount = if (result.isCorrect) it.correctCount + 1
                            else it.correctCount,
                            answerHistory = it.answerHistory + result,
                            error = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e.message ?: "Submit failed",
                        )
                    }
                }
        }
    }

    /**
     * 다음 문제로 이동.
     * 마지막 문제에서는 인덱스를 유지한다 (화면 전환은 NavController 가 isLastQuiz 로 판단).
     */
    fun moveToNext() {
        _uiState.update {
            if (it.isLastQuiz) {
                it
            } else {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOptionId = null,
                    lastAnswer = null,
                )
            }
        }
    }

    /** 처음부터 다시 풀기. 로드된 퀴즈 목록은 유지. */
    fun restart() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                selectedOptionId = null,
                lastAnswer = null,
                correctCount = 0,
                answerHistory = emptyList(),
            )
        }
    }

    /**
     * 세션 오답을 WrongNoteStore 에 저장한다.
     * answerHistory 에서 isCorrect=false 인 항목을 quizzes 와 join 해
     * SavedWrongNote 를 만들고 store.upsert() 를 호출한다.
     * relatedArticle 정보도 함께 저장해 오답노트 카드에서 바로 기사를 열 수 있게 한다.
     */
    fun saveWrongNotes(store: WrongNoteStore) {
        viewModelScope.launch {
            val state = _uiState.value
            val wrongNotes = state.quizzes.zip(state.answerHistory)
                .filter { (_, answer) -> !answer.isCorrect }
                .map { (quiz, answer) ->
                    val myAnswerText = quiz.options
                        .find { it.id == answer.selectedOptionId }?.text ?: "-"
                    val correctAnswerText = quiz.options
                        .find { it.id == answer.correctOptionId }?.text ?: "-"
                    val article = answer.relatedArticle
                    SavedWrongNote(
                        quizId = quiz.id,
                        question = quiz.question,
                        categoryName = quiz.category.name,
                        categoryDisplay = quiz.category.displayName,
                        myAnswerText = myAnswerText,
                        correctAnswerText = correctAnswerText,
                        explanation = answer.explanation,
                        keyword = answer.keyword,
                        relatedArticleTitle = article.title.takeIf { it.isNotBlank() },
                        relatedArticleUrl = article.url.takeIf { it.isNotBlank() },
                        relatedArticleSource = article.source.takeIf { it.isNotBlank() },
                    )
                }
            if (wrongNotes.isNotEmpty()) {
                withContext(Dispatchers.IO) { store.upsert(wrongNotes) }
            }
        }
    }

    companion object {
        fun factory(repository: QuizRepository) = viewModelFactory {
            initializer { QuizSessionViewModel(repository) }
        }
    }
}
