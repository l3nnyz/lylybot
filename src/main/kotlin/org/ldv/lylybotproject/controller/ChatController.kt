package org.ldv.lylybotproject.controller

import org.ldv.lylybotproject.model.dao.ConversationDAO
import org.ldv.lylybotproject.model.dao.ModeleIADAO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.LocalDate

/** Page principale de l'utilisateur connecté (pas encore de vraie authentification). */
@Controller
class ChatController(
    private val conversationDAO: ConversationDAO,
    private val modeleDAO: ModeleIADAO,
) {
    @GetMapping("/chat")
    fun chat(model: Model): String {
        val debutJour = LocalDate.now().atStartOfDay()
        val (aujourdhui, avant) = conversationDAO.findAll()
            .sortedByDescending { it.dateModification }
            .partition { it.dateModification >= debutJour }
        model.addAttribute("aujourdhui", aujourdhui)
        model.addAttribute("avant", avant)
        model.addAttribute("modeles", modeleDAO.findAll())
        return "chat"
    }
}
