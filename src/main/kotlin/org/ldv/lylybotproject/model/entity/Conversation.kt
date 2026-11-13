package org.ldv.lylybotproject.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Conversation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    @Column(nullable = false)
    var titre: String,

    @Column(nullable = false)
    var dateCreation: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var dateModification: LocalDateTime = LocalDateTime.now(),
)
