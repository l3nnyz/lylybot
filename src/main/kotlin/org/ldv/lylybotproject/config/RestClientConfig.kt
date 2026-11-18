package org.ldv.lylybotproject.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.time.Duration

@Configuration
class RestClientConfig(
    @Value("\${ia.proxy:}") private val proxyConfig: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun restClientBuilder(): RestClient.Builder {
        val fabrique = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(10))
            setReadTimeout(Duration.ofSeconds(90))
            proxySysteme()?.let { setProxy(it) }
        }
        return RestClient.builder().requestFactory(fabrique)
    }

    private fun proxySysteme(): Proxy? {
        val valeur = proxyConfig.takeIf(String::isNotBlank) ?: listOf("HTTPS_PROXY", "https_proxy", "HTTP_PROXY", "http_proxy")
            .firstNotNullOfOrNull { System.getenv(it)?.takeIf(String::isNotBlank) } ?: return null
        return try {
            val uri = URI(if ("://" in valeur) valeur else "http://$valeur")
            val port = if (uri.port > 0) uri.port else 8080
            log.info("Appels aux API d'IA via le proxy {}:{}", uri.host, port)
            Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, port))
        } catch (e: Exception) {
            log.warn("Variable de proxy illisible, connexion directe : {}", e.message)
            null
        }
    }
}
