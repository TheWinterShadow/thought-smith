package com.thewintershadow.thoughtsmith.data

/**
 * Represents a single message in a chat conversation.
 *
 * This data class is used to store messages exchanged between the user and the AI assistant
 * in the chat interface. Each message contains the text content, indicates whether it's from
 * the user or the AI, and includes a timestamp for chronological ordering.
 *
 * @property content The text content of the message. This can include formatted text,
 *                   questions, responses, or any conversational content.
 * @property isUser True if the message was sent by the user, false if sent by the AI assistant.
 *                  This determines message styling and positioning in the UI.
 * @property timestamp Unix timestamp in milliseconds indicating when the message was created.
 *                     Defaults to the current system time when the message is instantiated.
 *
 * Usage Example:
 * ```kotlin
 * // User message
 * val userMessage = Message(
 *     content = "How was your day?",
 *     isUser = true
 * )
 *
 * // AI response
 * val aiResponse = Message(
 *     content = "I'd love to hear about your day! What made it special?",
 *     isUser = false
 * )
 * ```
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
data class Message(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)
