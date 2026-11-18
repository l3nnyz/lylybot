package org.ldv.lylybotproject.model.dao

import org.ldv.lylybotproject.model.entity.Conversation
import org.ldv.lylybotproject.model.entity.Utilisateur
import org.springframework.data.jpa.repository.JpaRepository

interface ConversationDAO : JpaRepository<Conversation, Long> {
    fun findByUtilisateur(utilisateur: Utilisateur): List<Conversation>
    fun findByUtilisateurIsNull(): List<Conversation>
    fun findByModeleIsNull(): List<Conversation>
}
