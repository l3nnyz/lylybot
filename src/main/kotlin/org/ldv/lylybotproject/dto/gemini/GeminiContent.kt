package org.ldv.lylybotproject.dto.gemini

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart> = emptyList(),
)
