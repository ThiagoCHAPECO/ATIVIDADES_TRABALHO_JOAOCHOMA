package br.edu.ifpr.grafos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/** Exercício 1 — executa cada caminho da base independente (C1..C4). */
class ClassificarPedidoTest {

    private final Exercicios exercicios = new Exercicios();

    @Test
    @DisplayName("C1 — 1→3→5→7→8: sem desconto, pagamento aprovado")
    void c1SemDesconto() {
        assertEquals("PEDIDO APROVADO: 100.0", exercicios.classificarPedido(100, false, true));
    }

    @Test
    @DisplayName("C2 — 1→2→3→5→7→8: valor = 500 (limite) recebe 10%")
    void c2DescontoPorValor() {
        assertEquals("PEDIDO APROVADO: 450.0", exercicios.classificarPedido(500, false, true));
    }

    @Test
    @DisplayName("C3 — 1→3→4→5→7→8: cliente VIP recebe 5%")
    void c3DescontoVip() {
        assertEquals("PEDIDO APROVADO: 95.0", exercicios.classificarPedido(100, true, true));
    }

    @Test
    @DisplayName("C4 — 1→3→5→6→8: pagamento recusado (return antecipado)")
    void c4PagamentoRecusado() {
        assertEquals("PAGAMENTO RECUSADO", exercicios.classificarPedido(100, false, false));
    }

    @ParameterizedTest(name = "combinação valor={0}, vip={1}, aprovado={2} → {3}")
    @CsvSource(delimiter = ';', value = {
        "1000; true; true; PEDIDO APROVADO: 850.0",
        "1000; true; false; PAGAMENTO RECUSADO",
        "1000; false; false; PAGAMENTO RECUSADO",
        "100; true; false; PAGAMENTO RECUSADO",
        "499.99; false; true; PEDIDO APROVADO: 499.99"
    })
    @DisplayName("Demais combinações e limite 499,99")
    void outrasCombinacoes(double valor, boolean vip, boolean aprovado, String esperado) {
        assertEquals(esperado, exercicios.classificarPedido(valor, vip, aprovado));
    }
}
