package org.ldv.lylybotproject.service

import org.ldv.lylybotproject.dto.gemini.GeminiChatRequest
import org.ldv.lylybotproject.dto.gemini.GeminiChatResponse
import org.ldv.lylybotproject.dto.gemini.GeminiContent
import org.ldv.lylybotproject.dto.gemini.GeminiPart
import org.ldv.lylybotproject.model.entity.Message
import org.ldv.lylybotproject.model.entity.ModeleIA
import org.ldv.lylybotproject.model.entity.TypeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class GeminiService(
    private val restClientBuilder: RestClient.Builder,
    @Value("\${gemini.api.key}")
    private val apiKey: String,
) : IAService {

    override fun getFournisseur(): String = "Gemini"

    override fun envoyerMessages(messages: List<Message>, modele: ModeleIA): String {

        val contents = messages.filter { it.typeMessage != TypeMessage.SYSTEME }.map {
            val role = if (it.typeMessage == TypeMessage.ASSISTANT) "model" else "user"
            GeminiContent(role = role, parts = listOf(GeminiPart(text = it.contenu)))
        }
        val systeme = messages.filter { it.typeMessage == TypeMessage.SYSTEME }
            .takeIf { it.isNotEmpty() }
            ?.let { s -> GeminiContent(parts = s.map { GeminiPart(text = it.contenu) }) }

        val response = restClientBuilder.build().post()
            .uri(modele.urlAPI)
            .header("x-goog-api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(GeminiChatRequest(contents = contents, systemInstruction = systeme))
            .retrieve()
            .body(GeminiChatResponse::class.java)

        val texte = response?.candidates?.firstOrNull()?.content?.parts
            ?.mapNotNull { it.text }?.joinToString("")
        return if (texte.isNullOrBlank()) "Aucune réponse reçue de Gemini." else texte
    }
}
