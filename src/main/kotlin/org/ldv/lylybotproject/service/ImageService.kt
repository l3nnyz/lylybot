package org.ldv.lylybotproject.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URI

@Service
class ImageService(restClientBuilder: RestClient.Builder) {

    private val client = restClientBuilder.defaultHeader("User-Agent", "Lylybot/1.0 (projet etudiant BTS SIO)").build()
    private val regexImage = Regex("""!\[([^\]]*)]\(\s*<?([^)\s>]+(?:\([^)\s]*\)[^)\s>]*)*)>?\s*\)""")
    private val cache = mutableMapOf<String, String?>()

    fun verifierImages(markdown: String): String =
        regexImage.replace(markdown) { m ->
            val legende = m.groupValues[1]
            val cible = m.groupValues[2]
            val url = if (cible.startsWith("wiki:")) rechercherCommons(cible.removePrefix("wiki:")) else verifierUrl(cible)
            if (url != null) "![$legende](<$url>)" else ""
        }

    private fun rechercherCommons(recherche: String): String? = cache.getOrPut("wiki:$recherche") {
        try {
            val uri = URI("https", "commons.wikimedia.org", "/w/api.php",
                "action=query&format=json&generator=search&gsrnamespace=6&gsrlimit=1" +
                    "&gsrsearch=filetype:bitmap ${recherche.replace('_', ' ')}&prop=imageinfo&iiprop=url&iiurlwidth=640",
                null)
            val reponse = client.get().uri(uri).retrieve().body(Map::class.java)
            val pages = (reponse?.get("query") as? Map<*, *>)?.get("pages") as? Map<*, *>
            val info = (pages?.values?.firstOrNull() as? Map<*, *>)?.get("imageinfo") as? List<*>
            val premiere = info?.firstOrNull() as? Map<*, *>
            (premiere?.get("thumburl") ?: premiere?.get("url")) as? String
        } catch (e: Exception) {
            null
        }
    }

    private fun verifierUrl(url: String): String? = cache.getOrPut(url) {
        if (!url.startsWith("https://")) return@getOrPut null
        try {
            val statut = client.get().uri(URI(url)).header("Range", "bytes=0-0")
                .retrieve().toBodilessEntity().statusCode
            if (statut.is2xxSuccessful) url else null
        } catch (e: Exception) {
            null
        }
    }
}
