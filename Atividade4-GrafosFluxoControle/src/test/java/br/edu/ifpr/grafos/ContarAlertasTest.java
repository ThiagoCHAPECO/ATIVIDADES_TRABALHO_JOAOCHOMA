package br.edu.ifpr.grafos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Exercício 2 — executa cada caminho da base independente (C1..C4). */
class ContarAlertasTest {

    private final Exercicios exercicios = new Exercicios();

    @Test
    @DisplayName("C1 — 1→2→8→9: vetor vazio, laço não executa")
    void c1VetorVazio() {
        assertEquals(0, exercicios.contarAlertas(new double[] {}));
    }

    @Test
    @DisplayName("C2 — 1→2→3→4→7→2→8→9: temperatura negativa soma 2")
    void c2Negativa() {
        assertEquals(2, exercicios.contarAlertas(new double[] {-5}));
    }

    @Test
    @DisplayName("C3 — 1→2→3→5→6→7→2→8→9: temperatura acima de 35 soma 1")
    void c3AcimaDe35() {
        assertEquals(1, exercicios.contarAlertas(new double[] {40}));
    }

    @Test
    @DisplayName("C4 — 1→2→3→5→7→2→8→9: temperatura entre 0 e 35 não soma")
    void c4Normal() {
        assertEquals(0, exercicios.contarAlertas(new double[] {20}));
    }

    @Test
    @DisplayName("Fronteiras: 0 e 35 não geram alerta; -0,1 e 35,1 geram")
    void fronteiras() {
        assertEquals(0, exercicios.contarAlertas(new double[] {0}));
        assertEquals(0, exercicios.contarAlertas(new double[] {35}));
        assertEquals(2, exercicios.contarAlertas(new double[] {-0.1}));
        assertEquals(1, exercicios.contarAlertas(new double[] {35.1}));
    }

    @Test
    @DisplayName("Várias iterações repetem partes do grafo")
    void variasIteracoes() {
        // -3 → +2; 0 → 0; 35 → 0; 36 → +1; -0,5 → +2 = 5
        assertEquals(5, exercicios.contarAlertas(new double[] {-3, 0, 35, 36, -0.5}));
    }
}
