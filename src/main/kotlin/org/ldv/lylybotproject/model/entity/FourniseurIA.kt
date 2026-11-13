package org.ldv.lylybotproject.model.entity

import jakarta.persistence.*

@Entity
class FourniseurIA(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    @Column(nullable = false)
    var nom: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    var cheminLogo: String? = null,
)
