package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    private static ItemPedido item(long preco, int quantidade, int estoque, int peso) {
        return new ItemPedido("SKU-1", preco, quantidade, estoque, peso, false);
    }

    @Test
    @DisplayName("SKU nulo é rejeitado sem avaliar isBlank (curto-circuito do ||)")
    void deveRejeitarSkuNulo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(null, 100, 1, 1, 100, false));
        assertEquals("SKU obrigatório", erro.getMessage());
    }

    @Test
    @DisplayName("SKU em branco é rejeitado (operando direito do || avaliado)")
    void deveRejeitarSkuEmBranco() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("   ", 100, 1, 1, 100, false));
    }

    @ParameterizedTest(name = "preço {0} centavos é inválido")
    @CsvSource({"0", "-1", "1000001"})
    void deveRejeitarPrecoForaDoIntervalo(long preco) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> item(preco, 1, 1, 100));
        assertEquals("Preço inválido", erro.getMessage());
    }

    @ParameterizedTest(name = "preço {0} centavos é aceito")
    @CsvSource({"1", "1000000"})
    void deveAceitarLimitesDoPreco(long preco) {
        assertEquals(preco, item(preco, 1, 1, 100).precoCentavos());
    }

    @ParameterizedTest(name = "quantidade {0} é inválida")
    @CsvSource({"-1", "101"})
    void deveRejeitarQuantidadeForaDoIntervalo(int quantidade) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> item(100, quantidade, 200, 100));
        assertEquals("Quantidade inválida", erro.getMessage());
    }

    @ParameterizedTest(name = "quantidade {0} é aceita")
    @CsvSource({"0", "100"})
    void deveAceitarLimitesDaQuantidade(int quantidade) {
        assertEquals(quantidade, item(100, quantidade, 200, 100).quantidade());
    }

    @Test
    @DisplayName("Estoque -1 é rejeitado e estoque 0 é aceito")
    void deveValidarLimiteDoEstoque() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> item(100, 0, -1, 100));
        assertEquals("Estoque inválido", erro.getMessage());
        assertEquals(0, item(100, 0, 0, 100).estoque());
    }

    @ParameterizedTest(name = "peso {0} g é inválido")
    @CsvSource({"0", "100001"})
    void deveRejeitarPesoForaDoIntervalo(int peso) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> item(100, 1, 1, peso));
        assertEquals("Peso inválido", erro.getMessage());
    }

    @ParameterizedTest(name = "peso {0} g é aceito")
    @CsvSource({"1", "100000"})
    void deveAceitarLimitesDoPeso(int peso) {
        assertEquals(peso, item(100, 1, 1, peso).pesoGramas());
    }

    @Test
    @DisplayName("Total da linha = preço × quantidade")
    void deveCalcularTotalDaLinha() {
        assertEquals(750L, item(250, 3, 5, 100).totalCentavos());
        assertEquals(0L, item(250, 0, 5, 100).totalCentavos());
    }

    @ParameterizedTest(name = "quantidade {0} com estoque {1} → disponível = {2}")
    @CsvSource({
        "2, 3, true",   // abaixo do estoque
        "3, 3, true",   // igual ao estoque (limite)
        "4, 3, false",  // acima do estoque
        "0, 0, true"    // linha inativa sem estoque
    })
    void deveVerificarDisponibilidade(int quantidade, int estoque, boolean esperado) {
        assertEquals(esperado, item(100, quantidade, estoque, 100).disponivel());
    }
}
