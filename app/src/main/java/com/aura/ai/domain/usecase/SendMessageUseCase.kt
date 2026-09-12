package com.aura.ai.domain.usecase

import com.aura.ai.data.model.AuraAction
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.data.repository.ConversationRepository
import com.aura.ai.data.repository.MemoryRepository
import com.aura.ai.domain.action.ActionParser
import javax.inject.Inject

/**
 * Orchestrates sending a user message to Ollama and executing any resulting action.
 *
 * Flow:
 * 1. Save user message to local DB.
 * 2. Build system prompt (persona + memory context).
 * 3. Send conversation history to Ollama.
 * 4. Parse the response for actions.
 * 5. Return the parsed [AuraAction] and assistant reply text.
 */
class SendMessageUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val makeCallUseCase: MakeCallUseCase,
    private val sendSmsUseCase: SendSmsUseCase,
    private val setReminderUseCase: SetReminderUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val resolveContactUseCase: ResolveContactUseCase
) {
    data class Result(
        val replyText: String,
        val action: AuraAction,
        val actionResult: kotlin.Result<Unit>? = null
    )

    suspend fun execute(userText: String, history: List<ChatMessage>): Result {
        val userMessage = ChatMessage(role = ChatMessage.Role.USER, content = userText)
        conversationRepository.saveMessage(userMessage)

        val memoryContext = memoryRepository.buildMemoryContext()
        val systemPrompt = buildSystemPrompt(memoryContext)

        val rawResponse = conversationRepository.sendToOllama(
            systemPrompt = systemPrompt,
            history = history + userMessage
        )

        val (action, replyOverride) = ActionParser.parse(rawResponse)
        val replyText = replyOverride ?: when (action) {
            is AuraAction.Reply -> action.text
            is AuraAction.MakeCall -> "Calling ${action.contact}…"
            is AuraAction.SendSms -> "Sending message to ${action.contact}…"
            is AuraAction.SetReminder -> "Reminder set: ${action.title}"
            is AuraAction.LaunchApp -> "Opening ${action.appName}…"
        }

        val assistantMessage = ChatMessage(role = ChatMessage.Role.ASSISTANT, content = replyText)
        conversationRepository.saveMessage(assistantMessage)

        val actionResult = executeAction(action)
        return Result(replyText = replyText, action = action, actionResult = actionResult)
    }

    private fun executeAction(action: AuraAction): kotlin.Result<Unit>? {
        return when (action) {
            is AuraAction.MakeCall -> {
                val number = action.number ?: resolveContactUseCase.execute(action.contact) ?: action.contact
                makeCallUseCase.execute(number)
            }
            is AuraAction.SendSms -> {
                val number = action.number ?: resolveContactUseCase.execute(action.contact) ?: action.contact
                sendSmsUseCase.execute(number, action.message)
            }
            is AuraAction.SetReminder -> setReminderUseCase.execute(
                action.title, action.description, action.epochMillis
            )
            is AuraAction.LaunchApp -> launchAppUseCase.execute(action.appName, action.packageName)
            is AuraAction.Reply -> null
        }
    }

    private fun buildSystemPrompt(memoryContext: String): String {
        val base = """
            You are AURA, an intelligent AI personal assistant running on an Android phone.
            You help the user by having natural conversations and by performing real actions on their device:
            making phone calls, sending SMS, setting reminders, and opening apps.

            When the user asks you to perform a device action, respond with a JSON code block 
            (```json ... ```) containing the action details, plus a "reply" field with what to say.
            For plain conversation, just reply normally without a JSON block.

            Action schemas:
            - make_call:    { "action": "make_call",    "contact": "<name>", "reply": "..." }
            - send_sms:     { "action": "send_sms",     "contact": "<name>", "message": "<text>", "reply": "..." }
            - set_reminder: { "action": "set_reminder", "title": "<title>",  "datetime": "yyyy-MM-dd HH:mm", "reply": "..." }
            - launch_app:   { "action": "launch_app",   "app_name": "<name>", "reply": "..." }

            Be concise, helpful, and friendly.
        """.trimIndent()

        return if (memoryContext.isNotBlank()) {
            "$base\n\nUser memory / context:\n$memoryContext"
        } else base
    }
}
