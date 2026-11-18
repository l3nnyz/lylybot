package org.ldv.lylybotproject.controller

import org.ldv.lylybotproject.model.dao.ModeleIADAO
import org.ldv.lylybotproject.model.dao.UtilisateurDAO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminController(
    private val utilisateurDAO: UtilisateurDAO,
    private val modeleDAO: ModeleIADAO,
) {
    @GetMapping("/admin")
    fun admin(model: Model): String {
        model.addAttribute("utilisateurs", utilisateurDAO.findAll())
        model.addAttribute("modeles", modeleDAO.findAll())
        return "admin"
    }
}
