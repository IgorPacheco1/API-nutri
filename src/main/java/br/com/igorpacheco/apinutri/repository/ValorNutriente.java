package br.com.igorpacheco.apinutri.repository;

import java.math.BigDecimal;

/**
 * Projeção leve usada para montar o catálogo: um valor de nutriente de um alimento,
 * sem carregar as entidades inteiras.
 */
public record ValorNutriente(Integer idAlimento, String nomeNutriente, BigDecimal valorPor100g) {
}
