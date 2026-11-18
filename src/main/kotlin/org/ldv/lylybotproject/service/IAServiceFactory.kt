package org.ldv.lylybotproject.service

import org.ldv.lylybotproject.model.entity.ModeleIA
import org.springframework.stereotype.Component

@Component
class IAServiceFactory(
    private val services: List<IAService>,
) {
    fun getService(modele: ModeleIA): IAService {
        val fournisseur = modele.fournisseur?.nom
            ?: throw IllegalArgumentException("Aucun fournisseur associé au modèle")
        return services.firstOrNull { it.getFournisseur().equals(fournisseur, ignoreCase = true) }
            ?: throw IllegalArgumentException("Aucun service disponible pour $fournisseur")
    }
}
