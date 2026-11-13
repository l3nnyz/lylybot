package org.ldv.lylybotproject.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    var contenu: String,

    @Column(nullable = false)
    var dateCreation: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var typeMessage: TypeMessage,
)
