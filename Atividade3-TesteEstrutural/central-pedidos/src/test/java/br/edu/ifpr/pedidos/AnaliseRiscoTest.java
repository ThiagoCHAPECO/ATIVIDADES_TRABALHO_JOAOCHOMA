package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base de caminhos independentes de AnaliseRisco.avaliar (V(G) = 8).
 */
class AnaliseRiscoTest {

    private static final Cliente NOVO = new Cliente(false, false, 0);
    private static final Cliente RECORRENTE = new Cliente(false, false, 3);
    private static final Cliente RECORRENTE_VIP = new Cliente(true, false, 3);
    private static final Cliente BLOQUEADO = new Cliente(false, true, 3);

    private final AnaliseRisco analise = new AnaliseRisco();

    @Test
    @DisplayName("R1 — total negativo lança exceção")
    void r1TotalNegativo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> analise.avaliar(NOVO, -1, false));
        assertEquals("Total negativo", erro.getMessage());
    }

    @Test
    @DisplayName("R2 — cliente bloqueado é recusado (retorno antecipado)")
    void r2Bloqueado() {
        assertEquals("RECUSADO", analise.avaliar(BLOQUEADO, 100, false));
        // Mesmo com valor alto e expresso, a regra do bloqueio vem antes.
        assertEquals("RECUSADO", analise.avaliar(BLOQUEADO, 900_000, true));
    }

    @Test
    @DisplayName("R3 — cliente novo com total R$ 1.000,01 vai para revisão (|| sem avaliar expresso)")
    void r3NovoAcimaDoLimite() {
        assertEquals("REVISAO", analise.avaliar(NOVO, 100_001, false));
    }

    @Test
    @DisplayName("R4 — cliente novo com entrega expressa vai para revisão")
    void r4NovoExpresso() {
        assertEquals("REVISAO", analise.avaliar(NOVO, 1_000, true));
    }

    @Test
    @DisplayName("R5 — cliente novo com total = R$ 1.000,00 (limite) é aprovado")
    void r5NovoNoLimite() {
        assertEquals("APROVADO", analise.avaliar(NOVO, 100_000, false));
    }

    @Test
    @DisplayName("R6 — recorrente com total = R$ 5.000,00 (limite) é aprovado (&& curto-circuito)")
    void r6RecorrenteNoLimite() {
        assertEquals("APROVADO", analise.avaliar(RECORRENTE, 500_000, false));
    }

    @Test
    @DisplayName("R7 — recorrente não VIP com total R$ 5.000,01 vai para revisão")
    void r7RecorrenteAcimaDoLimite() {
        assertEquals("REVISAO", analise.avaliar(RECORRENTE, 500_001, false));
    }

    @Test
    @DisplayName("R8 — recorrente VIP com total R$ 5.000,01 é aprovado")
    void r8RecorrenteVip() {
        assertEquals("APROVADO", analise.avaliar(RECORRENTE_VIP, 500_001, false));
    }

    @Test
    @DisplayName("Expresso não afeta cliente recorrente")
    void expressoNaoAfetaRecorrente() {
        assertEquals("APROVADO", analise.avaliar(RECORRENTE, 1_000, true));
    }

    @Test
    @DisplayName("Total zero é válido")
    void totalZero() {
        assertEquals("APROVADO", analise.avaliar(NOVO, 0, false));
    }
}
