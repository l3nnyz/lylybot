package org.ldv.lylybotproject.dto.gemini

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GeminiChatRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
)
