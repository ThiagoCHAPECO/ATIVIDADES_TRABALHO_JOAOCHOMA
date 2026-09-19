package br.edu.ifpr.pedidos;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base de caminhos independentes de CalculadoraFrete.calcular (V(G) = 11).
 * Cada caminho F3..F11 altera uma decisão em relação ao caminho de referência F2.
 */
class CalculadoraFreteTest {

    private static final Cliente COMUM = new Cliente(false, false, 1);
    private static final Cliente VIP = new Cliente(true, false, 1);

    private final CalculadoraFrete calculadora = new CalculadoraFrete();

    private static Pedido pedido(String uf, int pesoGramas, boolean expresso, boolean fragil) {
        ItemPedido item = new ItemPedido("ITEM", 10_000, 1, 10, pesoGramas, fragil);
        return new Pedido(List.of(item), uf, expresso, null);
    }

    @Test
    @DisplayName("F1 — valor líquido negativo lança exceção")
    void f1LiquidoNegativo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> calculadora.calcular(pedido("PR", 1_000, false, false), COMUM, -1));
        assertEquals("Valor líquido negativo", erro.getMessage());
    }

    @Test
    @DisplayName("F2 — referência: PR, 1 kg, normal, comum, sem frágil = R$ 12,00")
    void f2Referencia() {
        assertEquals(1_200L, calculadora.calcular(pedido("PR", 1_000, false, false), COMUM, 10_000));
    }

    @ParameterizedTest(name = "F3–F5 — UF {0} tem frete base de {1} centavos")
    @CsvSource({"SP, 2000", "RJ, 2000", "AM, 3000"})
    void f3aF5TarifaPorUf(String uf, long esperado) {
        assertEquals(esperado, calculadora.calcular(pedido(uf, 1_000, false, false), COMUM, 10_000));
    }

    @ParameterizedTest(name = "F6 — peso {0} g: {1} iterações do while → {2} centavos")
    @CsvSource({
        "2000, 0, 1200",  // exatamente 2 kg: nenhuma iteração
        "2001, 1, 1500",  // 1 g de excedente conta como fração de kg
        "3000, 1, 1500",  // 1 kg exato de excedente
        "3001, 2, 1800",  // 1 kg e fração
        "4500, 3, 2100"   // várias iterações
    })
    void f6AdicionalPorPeso(int peso, int iteracoes, long esperado) {
        assertEquals(esperado, calculadora.calcular(pedido("PR", peso, false, false), COMUM, 10_000));
    }

    @Test
    @DisplayName("F7 — líquido = R$ 300,00 (limite) e entrega normal: frete grátis")
    void f7FreteGratis() {
        assertEquals(0L, calculadora.calcular(pedido("PR", 5_000, false, false), COMUM, 30_000));
    }

    @Test
    @DisplayName("Líquido R$ 299,99 (limite - 1) paga frete")
    void liquidoAbaixoDoLimiteDeGratuidade() {
        assertEquals(1_200L, calculadora.calcular(pedido("PR", 1_000, false, false), COMUM, 29_999));
    }

    @Test
    @DisplayName("F8 — líquido ≥ R$ 300,00 com expresso: não zera (&& falso no 2º operando)")
    void f8ExpressoNaoZera() {
        assertEquals(2_700L, calculadora.calcular(pedido("PR", 1_000, true, false), COMUM, 30_000));
    }

    @Test
    @DisplayName("F9 — VIP paga metade")
    void f9Vip() {
        assertEquals(600L, calculadora.calcular(pedido("PR", 1_000, false, false), VIP, 10_000));
    }

    @Test
    @DisplayName("F10 — expresso acrescenta R$ 15,00")
    void f10Expresso() {
        assertEquals(2_700L, calculadora.calcular(pedido("PR", 1_000, true, false), COMUM, 10_000));
    }

    @Test
    @DisplayName("F11 — item frágil ativo acrescenta R$ 5,00")
    void f11Fragil() {
        assertEquals(1_700L, calculadora.calcular(pedido("PR", 1_000, false, true), COMUM, 10_000));
    }

    @Test
    @DisplayName("Frágil é cobrado uma única vez, mesmo com vários itens frágeis")
    void fragilCobradoUmaVez() {
        ItemPedido a = new ItemPedido("A", 1_000, 1, 5, 500, true);
        ItemPedido b = new ItemPedido("B", 1_000, 1, 5, 500, true);
        Pedido pedido = new Pedido(List.of(a, b), "PR", false, null);
        assertEquals(1_700L, calculadora.calcular(pedido, COMUM, 2_000));
    }

    @Test
    @DisplayName("Item frágil inativo não gera adicional nem peso")
    void fragilInativoIgnorado() {
        ItemPedido ativo = new ItemPedido("A", 1_000, 1, 5, 1_000, false);
        ItemPedido inativo = new ItemPedido("B", 1_000, 0, 0, 90_000, true);
        Pedido pedido = new Pedido(List.of(ativo, inativo), "PR", false, null);
        assertEquals(1_200L, calculadora.calcular(pedido, COMUM, 1_000));
    }

    @Test
    @DisplayName("Adicionais incidem mesmo com a base zerada: VIP grátis + frágil = R$ 5,00")
    void adicionalAposGratuidade() {
        assertEquals(500L, calculadora.calcular(pedido("PR", 1_000, false, true), VIP, 30_000));
    }

    @Test
    @DisplayName("Todas as regras juntas: AM, 3.001 g, VIP, expresso e frágil")
    void todasAsRegras() {
        // (3.000 + 2 × 300) / 2 + 1.500 + 500 = 3.800
        assertEquals(3_800L, calculadora.calcular(pedido("AM", 3_001, true, true), VIP, 50_000));
    }
}
