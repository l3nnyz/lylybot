package org.ldv.lylybotproject.controller

import org.ldv.lylybotproject.model.dao.ConversationDAO
import org.ldv.lylybotproject.model.dao.FourniseurIADAO
import org.ldv.lylybotproject.model.dao.MessageDAO
import org.ldv.lylybotproject.service.IAServiceFactory
import org.ldv.lylybotproject.service.ImageService
import org.ldv.lylybotproject.model.dao.UtilisateurDAO
import org.ldv.lylybotproject.model.entity.Conversation
import org.ldv.lylybotproject.model.entity.Message
import org.ldv.lylybotproject.model.entity.TypeMessage
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Controller
class ChatController(
    private val conversationDAO: ConversationDAO,
    private val fournisseurDAO: FourniseurIADAO,
    private val utilisateurDAO: UtilisateurDAO,
    private val messageDAO: MessageDAO,
    private val iaServiceFactory: IAServiceFactory,
    private val imageService: ImageService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/chat")
    fun creerConversation(
        @RequestParam prompt: String,
        @RequestParam idFournisseur: Long,
        auth: Authentication,
        redirect: RedirectAttributes,
    ): String {
        val texte = prompt.trim()
        if (texte.isEmpty()) return "redirect:/chat"
        val fournisseur = fournisseurDAO.findById(idFournisseur)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Fournisseur introuvable") }
        val modele = fournisseur.modeles.minByOrNull { it.id ?: Long.MAX_VALUE }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun modèle pour ce fournisseur")
        val utilisateur = utilisateurDAO.findByEmailIgnoreCase(auth.name)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

        val conversation = conversationDAO.save(
            Conversation(titre = texte.take(50), utilisateur = utilisateur, modele = modele)
        )
        repondre(conversation, texte, redirect)
        return "redirect:/chat/${conversation.id}"
    }

    @PostMapping("/chat/{id}/messages")
    fun envoyerMessage(
        @PathVariable id: Long,
        @RequestParam prompt: String,
        auth: Authentication,
        redirect: RedirectAttributes,
    ): String {
        val texte = prompt.trim()
        if (texte.isNotEmpty()) repondre(conversationDe(id, auth), texte, redirect)
        return "redirect:/chat/$id"
    }

    private fun repondre(conversation: Conversation, texte: String, redirect: RedirectAttributes) {
        messageDAO.save(Message(contenu = texte, typeMessage = TypeMessage.UTILISATEUR, conversation = conversation))
        val modele = conversation.modele
        try {
            if (modele == null) throw IllegalStateException("Aucun modèle associé à la conversation")
            val historique = listOf(Message(contenu = consignes(conversation), typeMessage = TypeMessage.SYSTEME)) +
                messageDAO.findByConversationIdOrderByDateCreationAsc(conversation.id!!)
                    .filter { it.typeMessage != TypeMessage.SYSTEME }
            val reponse = imageService.verifierImages(iaServiceFactory.getService(modele).envoyerMessages(historique, modele))
            messageDAO.save(Message(contenu = reponse, typeMessage = TypeMessage.ASSISTANT, conversation = conversation))
        } catch (e: Exception) {
            log.warn("Erreur lors de l'appel au modèle {} : {}", modele?.nom, e.message)
            redirect.addFlashAttribute("erreur", "Le modèle n'a pas pu répondre. Votre question est enregistrée, réessayez dans un instant.")
        }
        conversation.dateModification = LocalDateTime.now()
        conversationDAO.save(conversation)
    }

    private fun consignes(conversation: Conversation): String {
        val prenom = conversation.utilisateur?.prenom?.takeIf { it.isNotBlank() } ?: return PROMPT_SYSTEME
        return PROMPT_SYSTEME + "\n\nL'utilisateur s'appelle $prenom. Tu peux l'appeler par son prénom de temps en temps, naturellement (pas à chaque message)."
    }

    companion object {

        val PROMPT_SYSTEME = """
            Tu es Lylybot, un assistant chaleureux, utile et clair.
            Réponds en français, sauf si l'utilisateur écrit dans une autre langue.

            Mise en forme :
            - Rédige TOUJOURS en Markdown : titres (##), **gras**, listes, tableaux, blocs de code avec le langage, citations.
            - Utilise BEAUCOUP d'émojis pertinents 🎉 : dans les titres, en début de points de liste et pour illustrer les idées clés ✨.
            - Garde des paragraphes courts et aérés.

            Images :
            - Quand une image aide vraiment (drapeau, monument, animal, logo, carte…) ou même pour illustrer ce que tu explique a l'utilisateur (par exemple si il te demande qu'est ce que s'est un requin mako), intègre-la en Markdown : ![description](url).
            - Drapeaux : https://flagcdn.com/w320/{code pays ISO en minuscules}.png (ex. ![Drapeau de la France](https://flagcdn.com/w320/fr.png)).
            - Toute autre image : n'écris JAMAIS d'URL toi-même. Écris wiki: suivi de 2 à 4 mots-clés en anglais séparés par des _ ; le serveur trouvera une vraie photo sur Wikimedia Commons.
              Exemples : ![Un tiramisu](wiki:tiramisu_dessert) ou ![La tour Eiffel](wiki:eiffel_tower_paris).
            - Si tu n'es pas sûr qu'une image existe, n'en mets pas : décris plutôt ou propose un lien.
            - Une ou deux images maximum par réponse, avec une courte légende.
        """.trimIndent()
    }

    @GetMapping("/chat")
    fun nouvelle(model: Model, auth: Authentication): String {
        preparer(model, auth, null)
        return "chat"
    }

    @GetMapping("/chat/{id}")
    fun ouvrir(@PathVariable id: Long, model: Model, auth: Authentication): String {
        preparer(model, auth, conversationDe(id, auth))
        return "chat"
    }

    @PostMapping("/chat/{id}/supprimer")
    fun supprimer(@PathVariable id: Long, auth: Authentication): String {
        conversationDAO.delete(conversationDe(id, auth))
        return "redirect:/chat"
    }

    private fun conversationDe(id: Long, auth: Authentication): Conversation {
        val c = conversationDAO.findById(id).orElse(null)
        if (c == null || c.utilisateur?.email != auth.name) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return c
    }

    private fun preparer(model: Model, auth: Authentication, conversation: Conversation?) {
        val utilisateur = utilisateurDAO.findByEmailIgnoreCase(auth.name)
        val debutJour = LocalDate.now().atStartOfDay()
        val (aujourdhui, avant) = (utilisateur?.let { conversationDAO.findByUtilisateur(it) } ?: emptyList())
            .sortedByDescending { it.dateModification }
            .partition { it.dateModification >= debutJour }
        model.addAttribute("aujourdhui", aujourdhui)
        model.addAttribute("avant", avant)
        model.addAttribute("fournisseurs", fournisseurDAO.findAll())
        model.addAttribute("conversation", conversation)
        model.addAttribute("messages", conversation?.messages?.sortedBy { it.dateCreation }?.filter { it.typeMessage != TypeMessage.SYSTEME } ?: emptyList<Message>())
        model.addAttribute("fournisseurConversation", conversation?.modele?.fournisseur)
        model.addAttribute("email", auth.name)
        model.addAttribute("estAdmin", auth.authorities.any { it.authority == "ROLE_ADMIN" })
    }
}
