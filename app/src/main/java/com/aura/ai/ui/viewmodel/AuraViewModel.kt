package com.aura.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.data.model.AuraAction
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.data.repository.ConversationRepository
import com.aura.ai.data.repository.MemoryRepository
import com.aura.ai.domain.ai.AiConnectionResult
import com.aura.ai.domain.ai.AiProvider
import com.aura.ai.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuraUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val errorMessage: String? = null,
    val pendingAction: AuraAction? = null,
    val ollamaModel: String = com.aura.ai.BuildConfig.OLLAMA_MODEL,
    val connectionTest: AiConnectionResult? = null,
    val isTestingConnection: Boolean = false
)

@HiltViewModel
class AuraViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val aiProvider: AiProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuraUiState())
    val uiState: StateFlow<AuraUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            conversationRepository.messages.collectLatest { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentHistory = _uiState.value.messages
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = runCatching {
                sendMessageUseCase.execute(
                    userText = text,
                    history = currentHistory
                )
            }
            result.onSuccess { outcome ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingAction = if (outcome.action !is AuraAction.Reply) outcome.action else null
                )
                outcome.actionResult?.onFailure { err ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Action failed: ${err.message}"
                    )
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Unknown error"
                )
            }
        }
    }

    fun testOllamaConnection() {
        _uiState.value = _uiState.value.copy(isTestingConnection = true, connectionTest = null)
        viewModelScope.launch {
            val result = aiProvider.testConnection()
            _uiState.value = _uiState.value.copy(
                isTestingConnection = false,
                connectionTest = result,
                errorMessage = result.failure?.message
            )
        }
    }

    fun setListening(listening: Boolean) {
        _uiState.value = _uiState.value.copy(isListening = listening)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun dismissAction() {
        _uiState.value = _uiState.value.copy(pendingAction = null)
    }

    fun clearConversation() {
        viewModelScope.launch {
            conversationRepository.clearHistory()
        }
    }

    fun rememberFact(key: String, value: String) {
        viewModelScope.launch {
            memoryRepository.remember(key, value)
        }
    }
}
