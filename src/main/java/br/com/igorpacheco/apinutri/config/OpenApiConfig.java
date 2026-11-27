package br.com.igorpacheco.apinutri.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiNutriOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Nutricional")
                        .description("""
                                API REST em Spring Boot que fornece dados nutricionais \
                                (calorias, macro e micronutrientes) de alimentos com base na \
                                Tabela TACO (UNICAMP) e calcula os totais nutricionais de uma \
                                refeição a partir das porções informadas.

                                Unidades: valores por 100 g; kcal, macronutrientes em gramas, \
                                sódio e demais micronutrientes em mg (Vitamina A em mcg).

                                O catálogo completo pode ser exportado em JSON, CSV ou como \
                                script MySQL para carga no banco de outra aplicação.

                                A API é stateless: não armazena refeições nem dados de usuário — \
                                autenticação e persistência ficam a cargo da aplicação consumidora.""")
                        .version("1.1.0")
                        .contact(new Contact()
                                .name("Igor Pacheco")
                                .url("https://www.linkedin.com/in/igor-pacheco1/")));
    }
}
