package org.ldv.lylybotproject.dto.mistral

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MistralMessage(
    val role: String,
    val content: String,
)
