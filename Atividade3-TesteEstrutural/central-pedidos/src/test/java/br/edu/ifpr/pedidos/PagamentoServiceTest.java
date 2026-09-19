package br.edu.ifpr.pedidos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base de caminhos independentes de PagamentoService.pagar (V(G) = 7, modelando o catch).
 */
class PagamentoServiceTest {

    /**
     * Stub do processador: devolve as respostas programadas, em ordem, e registra
     * o valor recebido em cada chamada. Uma resposta pode ser Boolean ou RuntimeException.
     */
    static class StubProcessador implements ProcessadorPagamento {
        private final Deque<Object> respostas = new ArrayDeque<>();
        final List<Long> chamadas = new ArrayList<>();

        StubProcessador(Object... respostas) {
            this.respostas.addAll(List.of(respostas));
        }

        @Override
        public boolean autorizar(long totalCentavos) {
            chamadas.add(totalCentavos);
            Object resposta = respostas.removeFirst();
            if (resposta instanceof RuntimeException erro) throw erro;
            return (Boolean) resposta;
        }
    }

    private static IllegalStateException indisponivel() {
        return new IllegalStateException("indisponível");
    }

    @Test
    @DisplayName("Processador nulo causa NullPointerException")
    void processadorNulo() {
        assertThrows(NullPointerException.class, () -> new PagamentoService(null));
    }

    @ParameterizedTest(name = "P1 — total {0} é rejeitado sem chamar o processador")
    @ValueSource(longs = {0, -1})
    void p1TotalNaoPositivo(long total) {
        StubProcessador stub = new StubProcessador();
        assertThrows(IllegalArgumentException.class, () -> new PagamentoService(stub).pagar(total, 1));
        assertTrue(stub.chamadas.isEmpty());
    }

    @Test
    @DisplayName("P2 — limite 0 de tentativas é rejeitado")
    void p2LimiteAbaixoDoMinimo() {
        StubProcessador stub = new StubProcessador();
        assertThrows(IllegalArgumentException.class, () -> new PagamentoService(stub).pagar(100, 0));
        assertTrue(stub.chamadas.isEmpty());
    }

    @Test
    @DisplayName("P3 — limite 4 de tentativas é rejeitado")
    void p3LimiteAcimaDoMaximo() {
        StubProcessador stub = new StubProcessador();
        assertThrows(IllegalArgumentException.class, () -> new PagamentoService(stub).pagar(100, 4));
        assertTrue(stub.chamadas.isEmpty());
    }

    @Test
    @DisplayName("P4 — aprovação na primeira tentativa (do/while com 1 iteração)")
    void p4AprovadoNaPrimeira() {
        StubProcessador stub = new StubProcessador(true);
        assertTrue(new PagamentoService(stub).pagar(11_200, 3));
        assertEquals(List.of(11_200L), stub.chamadas);
    }

    @Test
    @DisplayName("P4' — recusa definitiva: retorna false sem repetir")
    void recusaNaoRepete() {
        StubProcessador stub = new StubProcessador(false);
        assertFalse(new PagamentoService(stub).pagar(500, 3));
        assertEquals(List.of(500L), stub.chamadas);
    }

    @Test
    @DisplayName("P5 — indisponível com limite 1: esgota e retorna false")
    void p5EsgotaComUmaTentativa() {
        StubProcessador stub = new StubProcessador(indisponivel());
        assertFalse(new PagamentoService(stub).pagar(500, 1));
        assertEquals(1, stub.chamadas.size());
    }

    @Test
    @DisplayName("P6 — indisponível e depois aprovado (do/while com 2 iterações)")
    void p6RepeteAposIndisponibilidade() {
        StubProcessador stub = new StubProcessador(indisponivel(), true);
        assertTrue(new PagamentoService(stub).pagar(700, 3));
        assertEquals(List.of(700L, 700L), stub.chamadas);
    }

    @Test
    @DisplayName("Duas indisponibilidades e aprovação na 3ª tentativa (limite máximo)")
    void aprovaNaTerceira() {
        StubProcessador stub = new StubProcessador(indisponivel(), indisponivel(), true);
        assertTrue(new PagamentoService(stub).pagar(700, 3));
        assertEquals(3, stub.chamadas.size());
    }

    @Test
    @DisplayName("Três indisponibilidades esgotam o limite 3 e retornam false")
    void esgotaTresTentativas() {
        StubProcessador stub = new StubProcessador(indisponivel(), indisponivel(), indisponivel());
        assertFalse(new PagamentoService(stub).pagar(700, 3));
        assertEquals(3, stub.chamadas.size());
    }

    @Test
    @DisplayName("Indisponível e depois recusado: para na recusa")
    void indisponivelDepoisRecusado() {
        StubProcessador stub = new StubProcessador(indisponivel(), false, true);
        assertFalse(new PagamentoService(stub).pagar(700, 3));
        assertEquals(2, stub.chamadas.size());
    }

    @Test
    @DisplayName("P7 — outra exceção é propagada sem nova tentativa")
    void p7OutraExcecaoPropaga() {
        RuntimeException falha = new IllegalArgumentException("cartão inválido");
        StubProcessador stub = new StubProcessador(falha, true);
        RuntimeException lancada = assertThrows(RuntimeException.class,
            () -> new PagamentoService(stub).pagar(700, 3));
        assertSame(falha, lancada);
        assertEquals(1, stub.chamadas.size());
    }
}
