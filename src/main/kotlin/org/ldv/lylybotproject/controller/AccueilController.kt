package org.ldv.lylybotproject.controller

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AccueilController {

    @GetMapping("/")
    fun accueil(): String = "accueil"

    @GetMapping("/login")
    fun login(): String {
        val auth = SecurityContextHolder.getContext().authentication
        val connecte = auth != null && auth.isAuthenticated && auth !is AnonymousAuthenticationToken
        return if (connecte) "redirect:/chat" else "auth/login"
    }
}
