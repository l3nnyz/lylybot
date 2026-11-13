package org.ldv.lylybotproject.model.dao

import org.ldv.lylybotproject.model.entity.Message
import org.springframework.data.jpa.repository.JpaRepository

interface MessageDAO : JpaRepository<Message, Long>
