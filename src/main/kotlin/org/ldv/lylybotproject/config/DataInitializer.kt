package org.ldv.lylybotproject.config

import org.ldv.lylybotproject.model.dao.ConversationDAO
import org.ldv.lylybotproject.model.dao.FourniseurIADAO
import org.ldv.lylybotproject.model.dao.ModeleIADAO
import org.ldv.lylybotproject.model.dao.UtilisateurDAO
import org.ldv.lylybotproject.model.entity.Conversation
import org.ldv.lylybotproject.model.entity.FourniseurIA
import org.ldv.lylybotproject.model.entity.Message
import org.ldv.lylybotproject.model.entity.ModeleIA
import org.ldv.lylybotproject.model.entity.TypeMessage
import org.ldv.lylybotproject.model.entity.NomRole
import org.ldv.lylybotproject.model.entity.Utilisateur
import java.time.LocalDateTime
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataInitializer(
    private val fournisseurDAO: FourniseurIADAO,
    private val modeleDAO: ModeleIADAO,
    private val conversationDAO: ConversationDAO,
    private val utilisateurDAO: UtilisateurDAO,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        if (utilisateurDAO.count() == 0L) {
            utilisateurDAO.saveAll(
                listOf(
                    Utilisateur(
                        email = "admin@lylybot.fr",
                        prenom = "Lenny",
                        motDePasse = passwordEncoder.encode("Admin123!")!!,
                        roles = mutableListOf(NomRole.ROLE_USER, NomRole.ROLE_ADMIN),
                    ),
                    Utilisateur(email = "lea.martin@lylybot.fr", prenom = "Léa", motDePasse = passwordEncoder.encode("Lea12345!")!!),
                    Utilisateur(email = "hugo.bernard@lylybot.fr", prenom = "Hugo", motDePasse = passwordEncoder.encode("Hugo12345!")!!),
                    Utilisateur(email = "lenny.zachelin@lylybot.fr", prenom = "Lenny", motDePasse = passwordEncoder.encode("test")!!),
                    )
            )
        }

        val prenoms = mapOf("admin@lylybot.fr" to "Admin", "lea.martin@lylybot.fr" to "Léa", "hugo.bernard@lylybot.fr" to "Hugo")
        utilisateurDAO.findAll().filter { it.prenom == null && it.email in prenoms }.forEach { it.prenom = prenoms[it.email] }

        conversationDAO.deleteAll(conversationDAO.findByUtilisateurIsNull())
        conversationDAO.deleteAll(conversationDAO.findByModeleIsNull())
        modeleDAO.deleteAll(modeleDAO.findByFournisseurIsNull())
        fournisseurDAO.deleteAll(fournisseurDAO.findAll().filter { it.modeles.isEmpty() })

        modeleDAO.findAll().filter { it.identifiantAPI == "gemini-2.5-flash" }.forEach {
            it.nom = "Gemini 3.5 Flash Lite"
            it.identifiantAPI = "gemini-3.5-flash-lite"
            it.urlAPI = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent"
        }

        if (fournisseurDAO.count() == 0L) {
            val mistral = FourniseurIA(nom = "Mistral", description = "Fournisseur français de modèles de langage.")
            val gemini = FourniseurIA(nom = "Gemini", description = "Famille de modèles de langage proposée par Google.")
            val urlMistral = "https://api.mistral.ai/v1/chat/completions"
            mistral.modeles.add(ModeleIA(
                nom = "Mistral Small", identifiantAPI = "mistral-small-latest", urlAPI = urlMistral, fournisseur = mistral,
                description = "Modèle rapide et économique pour les échanges courants.",
            ))
            mistral.modeles.add(ModeleIA(
                nom = "Mistral Large", identifiantAPI = "mistral-large-latest", urlAPI = urlMistral, fournisseur = mistral,
                description = "Modèle le plus complet de Mistral, pour les questions complexes.",
            ))
            gemini.modeles.add(ModeleIA(
                nom = "Gemini 3.5 Flash Lite", identifiantAPI = "gemini-3.5-flash-lite", fournisseur = gemini,
                urlAPI = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent",
                description = "Modèle rapide de Gemini pour les échanges courants.",
            ))
            fournisseurDAO.saveAll(listOf(mistral, gemini))
        }

        if (fournisseurDAO.findAll().none { it.nom == "OpenRouter" }) {
            val openRouter = FourniseurIA(nom = "OpenRouter", description = "Passerelle vers de nombreux modèles, dont des modèles gratuits.")
            openRouter.modeles.add(ModeleIA(
                nom = "Nemotron 3.5 Lightning", identifiantAPI = "nvidia/nemotron-3.5-lightning:free", fournisseur = openRouter,
                urlAPI = "https://openrouter.ai/api/v1/chat/completions",
                description = "Modèle gratuit de NVIDIA servi par OpenRouter.",
            ))
            fournisseurDAO.save(openRouter)
        }

        if (conversationDAO.count() == 0L) {
            val lea = utilisateurDAO.findByEmailIgnoreCase("lea.martin@lylybot.fr")
            val hugo = utilisateurDAO.findByEmailIgnoreCase("hugo.bernard@lylybot.fr")
            val modeles = modeleDAO.findAll()
            val small = modeles.first { it.identifiantAPI == "mistral-small-latest" }
            val large = modeles.first { it.identifiantAPI == "mistral-large-latest" }
            val flash = modeles.first { it.fournisseur?.nom == "Gemini" }

            fun conversation(
                titre: String, proprietaire: Utilisateur?, modele: ModeleIA,
                echange: List<Pair<TypeMessage, String>>, ilYaJours: Long = 0,
            ): Conversation {
                val date = LocalDateTime.now().minusDays(ilYaJours)
                val c = Conversation(titre = titre, dateCreation = date, dateModification = date, utilisateur = proprietaire, modele = modele)
                echange.forEachIndexed { i, (type, texte) ->
                    c.messages.add(Message(contenu = texte, typeMessage = type, dateCreation = date.plusSeconds(i * 20L), conversation = c))
                }
                return c
            }

            conversationDAO.saveAll(
                listOf(
                    conversation("Idées de repas pour la semaine", lea, small, listOf(
                        TypeMessage.UTILISATEUR to "Donne-moi des idées de repas simples pour la semaine.",
                        TypeMessage.ASSISTANT to "Lundi : pâtes au pesto. Mardi : curry de pois chiches. Mercredi : omelette et salade. Jeudi : soupe de légumes. Vendredi : pizza maison.",
                    )),
                    conversation("Résumé d'un article sur le climat", lea, large, listOf(
                        TypeMessage.UTILISATEUR to "Résume-moi cet article sur le réchauffement climatique.",
                        TypeMessage.ASSISTANT to "Collez le texte de l'article et je vous en fais un résumé en quelques points clés.",
                    )),
                    conversation("Préparer un entretien d'embauche", lea, flash, listOf(
                        TypeMessage.UTILISATEUR to "Comment me préparer pour un entretien de développeur ?",
                        TypeMessage.ASSISTANT to "Relisez l'offre, préparez deux projets à présenter, entraînez-vous aux questions classiques et préparez vos propres questions.",
                    )),
                    conversation("Comment organiser mes révisions ?", lea, small, listOf(
                        TypeMessage.UTILISATEUR to "Comment organiser mes révisions avant les examens ?",
                        TypeMessage.ASSISTANT to "Faites un planning par matière, alternez les sujets, révisez par sessions de 45 minutes et testez-vous régulièrement.",
                    ), ilYaJours = 1),
                    conversation("Plan de la semaine à la salle", hugo, small, listOf(
                        TypeMessage.UTILISATEUR to "Propose-moi un programme de musculation sur 3 jours.",
                        TypeMessage.ASSISTANT to "Jour 1 : pousser. Jour 2 : tirer. Jour 3 : jambes et abdominaux.",
                    )),
                )
            )
        }
    }
}
