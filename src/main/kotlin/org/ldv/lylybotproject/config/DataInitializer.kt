package org.ldv.lylybotproject.config

import org.ldv.lylybotproject.model.dao.ConversationDAO
import org.ldv.lylybotproject.model.dao.FourniseurIADAO
import org.ldv.lylybotproject.model.dao.ModeleIADAO
import org.ldv.lylybotproject.model.entity.Conversation
import org.ldv.lylybotproject.model.entity.FourniseurIA
import org.ldv.lylybotproject.model.entity.ModeleIA
import java.time.LocalDateTime
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * Insère les données de départ au lancement de l'application.
 * Chaque table n'est remplie que si elle est vide, donc pas de doublons au redémarrage.
 * Les relations (modèle -> fournisseur, etc.) seront ajoutées avec les relations entre entités.
 */
@Component
class DataInitializer(
    private val fournisseurDAO: FourniseurIADAO,
    private val modeleDAO: ModeleIADAO,
    private val conversationDAO: ConversationDAO,
) : CommandLineRunner {

    override fun run(vararg args: String) {
        if (fournisseurDAO.count() == 0L) {
            fournisseurDAO.saveAll(
                listOf(
                    FourniseurIA(
                        nom = "Mistral",
                        description = "Fournisseur français de modèles de langage.",
                    ),
                    FourniseurIA(
                        nom = "Gemini",
                        description = "Famille de modèles de langage proposée par Google.",
                    ),
                )
            )
        }

        if (modeleDAO.count() == 0L) {
            modeleDAO.saveAll(
                listOf(
                    ModeleIA(
                        nom = "mistral-small-latest",
                        urlAPI = "https://api.mistral.ai/v1/chat/completions",
                        description = "Modèle rapide et économique pour les échanges courants.",
                    ),
                    ModeleIA(
                        nom = "mistral-large-latest",
                        urlAPI = "https://api.mistral.ai/v1/chat/completions",
                        description = "Modèle le plus complet de Mistral, pour les questions complexes.",
                    ),
                    ModeleIA(
                        nom = "gemini-2.5-flash",
                        urlAPI = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
                        description = "Modèle rapide de Gemini pour les échanges courants.",
                    ),
                )
            )
        }

        if (conversationDAO.count() == 0L) {
            conversationDAO.saveAll(
                listOf(
                    Conversation(titre = "Idées de repas pour la semaine"),
                    Conversation(titre = "Résumé d'un article sur le climat"),
                    Conversation(titre = "Préparer un entretien d'embauche"),
                    Conversation(
                        titre = "Comment organiser mes révisions ?",
                        dateCreation = LocalDateTime.now().minusDays(1),
                        dateModification = LocalDateTime.now().minusDays(1),
                    ),
                )
            )
        }
    }
}
