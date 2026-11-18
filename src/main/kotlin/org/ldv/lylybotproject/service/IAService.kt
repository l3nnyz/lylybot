package org.ldv.lylybotproject.service

import org.ldv.lylybotproject.model.entity.Message
import org.ldv.lylybotproject.model.entity.ModeleIA
import org.ldv.lylybotproject.model.entity.TypeMessage

interface IAService {

    fun getFournisseur(): String

    fun envoyerMessages(messages: List<Message>, modele: ModeleIA): String

    fun envoyerPrompt(prompt: String, modele: ModeleIA): String =
        envoyerMessages(listOf(Message(contenu = prompt, typeMessage = TypeMessage.UTILISATEUR)), modele)
}
