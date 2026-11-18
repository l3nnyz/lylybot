package org.ldv.lylybotproject.model.dao

import org.ldv.lylybotproject.model.entity.Utilisateur
import org.springframework.data.jpa.repository.JpaRepository

interface UtilisateurDAO : JpaRepository<Utilisateur, Long> {
    fun findByEmailIgnoreCase(email: String): Utilisateur?
}
