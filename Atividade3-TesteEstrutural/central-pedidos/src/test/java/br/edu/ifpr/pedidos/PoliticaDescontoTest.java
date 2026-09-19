package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base de caminhos independentes de PoliticaDesconto.calcular (V(G) = 12).
 * Os identificadores D1..D12 correspondem à tabela do RELATORIO.md.
 */
class PoliticaDescontoTest {

    private static final Cliente VIP_NOVO = new Cliente(true, false, 0);
    private static final Cliente COMUM_NOVO = new Cliente(false, false, 0);
    private static final Cliente COMUM_RECORRENTE = new Cliente(false, false, 1);

    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    @DisplayName("D1 — subtotal negativo lança exceção")
    void d1SubtotalNegativo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> politica.calcular(COMUM_NOVO, -1, null));
        assertEquals("Subtotal negativo", erro.getMessage());
    }

    @Test
    @DisplayName("D2 — VIP sem cupom recebe 10%")
    void d2VipSemCupom() {
        assertEquals(1_000L, politica.calcular(VIP_NOVO, 10_000, null));
    }

    @Test
    @DisplayName("D3 — comum com subtotal = R$ 500,00 (limite) recebe 5%")
    void d3ComumNoLimiteDe500() {
        assertEquals(2_500L, politica.calcular(COMUM_RECORRENTE, 50_000, null));
    }

    @Test
    @DisplayName("D4 — comum com subtotal R$ 499,99 (limite - 1) não recebe desconto")
    void d4ComumAbaixoDe500() {
        assertEquals(0L, politica.calcular(COMUM_RECORRENTE, 49_999, null));
    }

    @Test
    @DisplayName("Truncamento: 5% de 50.001 centavos = 2.500 (e não 2.500,05)")
    void deveTruncarPercentual() {
        assertEquals(2_500L, politica.calcular(COMUM_RECORRENTE, 50_001, null));
    }

    @ParameterizedTest(name = "D5 — cupom em branco \"{0}\" mantém o desconto base")
    @ValueSource(strings = {"", "   "})
    void d5CupomEmBranco(String cupom) {
        assertEquals(1_000L, politica.calcular(VIP_NOVO, 10_000, cupom));
    }

    @Test
    @DisplayName("D6 — BEMVINDO: sem compras e subtotal = R$ 100,00 soma R$ 20,00")
    void d6BemVindoElegivel() {
        assertEquals(2_000L, politica.calcular(COMUM_NOVO, 10_000, "BEMVINDO"));
    }

    @Test
    @DisplayName("D7 — BEMVINDO: cliente com compras anteriores não recebe (curto-circuito do &&)")
    void d7BemVindoComHistorico() {
        assertEquals(0L, politica.calcular(COMUM_RECORRENTE, 10_000, "BEMVINDO"));
    }

    @Test
    @DisplayName("D8 — BEMVINDO: subtotal R$ 99,99 (limite - 1) não recebe")
    void d8BemVindoAbaixoDoMinimo() {
        assertEquals(0L, politica.calcular(COMUM_NOVO, 9_999, "BEMVINDO"));
    }

    @Test
    @DisplayName("D9 — EXTRA10: subtotal = R$ 200,00 (limite) soma 10%")
    void d9Extra10Elegivel() {
        assertEquals(2_000L, politica.calcular(COMUM_RECORRENTE, 20_000, "EXTRA10"));
    }

    @Test
    @DisplayName("D10 — EXTRA10: subtotal R$ 199,99 (limite - 1) não soma")
    void d10Extra10AbaixoDoMinimo() {
        assertEquals(0L, politica.calcular(COMUM_RECORRENTE, 19_999, "EXTRA10"));
    }

    @ParameterizedTest(name = "D11 — cupom desconhecido \"{0}\" lança exceção")
    @ValueSource(strings = {"PROMO", "BEM VINDO", "EXTRA 10"})
    void d11CupomDesconhecido(String cupom) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> politica.calcular(COMUM_NOVO, 10_000, cupom));
        assertEquals("Cupom desconhecido", erro.getMessage());
    }

    @Test
    @DisplayName("D12 — VIP + BEMVINDO ultrapassa o teto: 1.000 + 2.000 limitado a 20% = 2.000")
    void d12TetoDeVintePorCento() {
        assertEquals(2_000L, politica.calcular(VIP_NOVO, 10_000, "BEMVINDO"));
    }

    @Test
    @DisplayName("Desconto igual ao teto é mantido (VIP + EXTRA10 = exatamente 20%)")
    void descontoIgualAoTeto() {
        assertEquals(20_000L, politica.calcular(VIP_NOVO, 100_000, "EXTRA10"));
    }

    @Test
    @DisplayName("Comum ≥ R$ 500,00 com EXTRA10: 5% + 10% = 15%, abaixo do teto")
    void cincoMaisDezPorCento() {
        assertEquals(7_500L, politica.calcular(COMUM_RECORRENTE, 50_000, "EXTRA10"));
    }

    @Test
    @DisplayName("Truncamento no teto: VIP + EXTRA10 em 20.005 = 2.000 + 2.000 (teto 4.001)")
    void truncamentoComTeto() {
        assertEquals(4_000L, politica.calcular(VIP_NOVO, 20_005, "EXTRA10"));
    }

    @ParameterizedTest(name = "cupom \"{0}\" é normalizado com trim e maiúsculas")
    @ValueSource(strings = {"bemvindo", "  BemVindo  ", "\tBEMVINDO\n"})
    void deveNormalizarCupom(String cupom) {
        assertEquals(2_000L, politica.calcular(COMUM_NOVO, 10_000, cupom));
    }
}
