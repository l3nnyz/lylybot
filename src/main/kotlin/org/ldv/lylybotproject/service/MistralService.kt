package org.ldv.lylybotproject.service

import org.ldv.lylybotproject.dto.mistral.MistralChatRequest
import org.ldv.lylybotproject.dto.mistral.MistralChatResponse
import org.ldv.lylybotproject.dto.mistral.MistralMessage
import org.ldv.lylybotproject.model.entity.Message
import org.ldv.lylybotproject.model.entity.ModeleIA
import org.ldv.lylybotproject.model.entity.TypeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class MistralService(
    private val restClientBuilder: RestClient.Builder,
    @Value("\${mistral.api.key}")
    private val apiKey: String,
) : IAService {

    override fun getFournisseur(): String = "Mistral"

    override fun envoyerMessages(messages: List<Message>, modele: ModeleIA): String {
        val mistralMessages = messages.map {
            val role = when (it.typeMessage) {
                TypeMessage.UTILISATEUR -> "user"
                TypeMessage.ASSISTANT -> "assistant"
                TypeMessage.SYSTEME -> "system"
            }
            MistralMessage(role = role, content = it.contenu)
        }

        val request = MistralChatRequest(model = modele.identifiantAPI, messages = mistralMessages)

        val response = restClientBuilder.build().post()
            .uri(modele.urlAPI)
            .header("Authorization", "Bearer $apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(MistralChatResponse::class.java)

        return response?.choices?.firstOrNull()?.message?.content ?: "Aucune réponse reçue de Mistral."
    }
}
