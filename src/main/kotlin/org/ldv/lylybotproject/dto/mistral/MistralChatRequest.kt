package org.ldv.lylybotproject.dto.mistral

data class MistralChatRequest(
    val model: String,
    val messages: List<MistralMessage>,
)
