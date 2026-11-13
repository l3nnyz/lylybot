package org.ldv.lylybotproject.model.dao

import org.ldv.lylybotproject.model.entity.Conversation
import org.springframework.data.jpa.repository.JpaRepository

interface ConversationDAO : JpaRepository<Conversation, Long>
