package org.ldv.lylybotproject.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class AccueilController {

    @GetMapping("/")
    fun accueil(): String = "accueil"

    @GetMapping("/login")
    fun login(): String = "auth/login"

    // Pas encore de vérification : on redirige simplement vers l'espace utilisateur
    @PostMapping("/login")
    fun connexion(): String = "redirect:/chat"
}
