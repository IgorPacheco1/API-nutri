package br.com.igorpacheco.apinutri.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS liberado só para as origens configuradas em apinutri.cors.origens
 * (variável de ambiente APINUTRI_CORS_ORIGENS), separadas por vírgula.
 * Requisições de navegador vindas de outras origens recebem 403.
 * Chamadas servidor a servidor (sem cabeçalho Origin) não são afetadas.
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(@Value("${apinutri.cors.origens:http://localhost:5173}") String origens) {
        List<String> origensPermitidas = separar(origens);
        log.info("CORS liberado para: {}", origensPermitidas.isEmpty() ? "(nenhuma origem)" : origensPermitidas);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origensPermitidas);
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    static List<String> separar(String origens) {
        if (origens == null) {
            return List.of();
        }
        return Arrays.stream(origens.split(","))
                .map(String::trim)
                .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                .filter(o -> !o.isEmpty())
                .toList();
    }
}
