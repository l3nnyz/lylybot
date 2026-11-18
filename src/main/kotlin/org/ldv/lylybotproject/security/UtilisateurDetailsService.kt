package org.ldv.lylybotproject.security

import org.ldv.lylybotproject.model.dao.UtilisateurDAO
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UtilisateurDetailsService(private val utilisateurDAO: UtilisateurDAO) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val u = utilisateurDAO.findByEmailIgnoreCase(username.trim())
            ?: throw UsernameNotFoundException("Utilisateur introuvable")
        return User.withUsername(u.email)
            .password(u.motDePasse)
            .authorities(u.roles.map { SimpleGrantedAuthority(it.name) })
            .build()
    }
}
