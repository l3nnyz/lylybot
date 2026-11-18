package org.ldv.lylybotproject.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "utilisateur")
class Utilisateur(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var motDePasse: String,

    var prenom: String? = null,

    @Column(nullable = false)
    var dateCreation: LocalDateTime = LocalDateTime.now(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "utilisateur_roles", joinColumns = [JoinColumn(name = "utilisateur_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var roles: MutableList<NomRole> = mutableListOf(NomRole.ROLE_USER),

    @OneToMany(mappedBy = "utilisateur", cascade = [CascadeType.REMOVE])
    var conversations: MutableList<Conversation> = mutableListOf(),
)
