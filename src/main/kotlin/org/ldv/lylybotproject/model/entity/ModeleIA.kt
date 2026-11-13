package org.ldv.lylybotproject.model.entity

import jakarta.persistence.*

@Entity
class ModeleIA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    @Column(nullable = false)
    var nom: String,

    @Column(nullable = false)
    var urlAPI: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var cheminLogo: String? = null,
)
