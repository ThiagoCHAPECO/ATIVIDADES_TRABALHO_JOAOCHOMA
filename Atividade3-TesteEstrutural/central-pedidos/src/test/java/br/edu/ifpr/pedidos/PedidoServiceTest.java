package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de colaboração de PedidoService.fechar (V(G) = 9).
 * Os identificadores S1..S9 correspondem à base de caminhos do RELATORIO.md.
 */
class PedidoServiceTest {

    /** Registra cada cobrança e responde conforme a sequência programada. */
    private static ProcessadorPagamento processador(List<Long> cobrancas, Object... respostas) {
        List<Object> fila = new ArrayList<>(List.of(respostas));
        return total -> {
            cobrancas.add(total);
            Object resposta = fila.remove(0);
            if (resposta instanceof RuntimeException erro) throw erro;
            return (Boolean) resposta;
        };
    }

    private static ItemPedido item(long preco, int quantidade, int estoque) {
        return new ItemPedido("ITEM", preco, quantidade, estoque, 1_000, false);
    }

    private static void assertResultado(ResultadoPedido r, String status, long subtotal,
                                        long desconto, long frete, long total) {
        assertAll(
            () -> assertEquals(status, r.status()),
            () -> assertEquals(subtotal, r.subtotalCentavos()),
            () -> assertEquals(desconto, r.descontoCentavos()),
            () -> assertEquals(frete, r.freteCentavos()),
            () -> assertEquals(total, r.totalCentavos())
        );
    }

    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        // 1. Preparar: cliente comum, uma compra anterior e item disponível de R$ 100,00.
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        // Simula o pagamento e registra as cobranças, sem banco ou serviço externo.
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        // 2. Executar: percorrer um caminho completo do fechamento.
        ResultadoPedido resultado = service.fechar(pedido, cliente);

        // 3. Verificar: sem desconto; frete de R$ 12,00; total de R$ 112,00.
        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            // A lista comprova uma única cobrança, com o valor correto.
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    @DisplayName("Processador nulo causa NullPointerException na construção")
    void processadorNulo() {
        assertThrows(NullPointerException.class, () -> new PedidoService(null));
    }

    @Test
    @DisplayName("S1/S2 — pedido ou cliente nulos causam NullPointerException")
    void s1s2ReferenciasNulas() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        Pedido pedido = new Pedido(List.of(item(1_000, 1, 1)), "PR", false, null);
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(NullPointerException.class, () -> service.fechar(null, cliente));
        assertThrows(NullPointerException.class, () -> service.fechar(pedido, null));
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    @DisplayName("S3 — bloqueado retorna BLOQUEADO antes de avaliar itens e cupom")
    void s3ClienteBloqueado() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        // Lista vazia e cupom desconhecido lançariam exceção se fossem avaliados.
        Pedido pedido = new Pedido(List.of(), "PR", false, "INEXISTENTE");

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, true, 5));

        assertResultado(r, "BLOQUEADO", 0, 0, 0, 0);
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    @DisplayName("S4 — pedido sem itens ativos lança exceção")
    void s4SemItensAtivos() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        Cliente cliente = new Cliente(false, false, 1);

        assertThrows(IllegalArgumentException.class,
            () -> service.fechar(new Pedido(List.of(), "PR", false, null), cliente));
        assertThrows(IllegalArgumentException.class,
            () -> service.fechar(new Pedido(List.of(item(1_000, 0, 0)), "PR", false, null), cliente));
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    @DisplayName("S5 — falta de estoque retorna SEM_ESTOQUE antes de avaliar o cupom")
    void s5SemEstoque() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        Pedido pedido = new Pedido(List.of(item(1_000, 3, 2)), "PR", false, "INEXISTENTE");

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, false, 1));

        assertResultado(r, "SEM_ESTOQUE", 0, 0, 0, 0);
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    @DisplayName("S6 — cupom desconhecido interrompe o fechamento sem cobrança")
    void s6CupomDesconhecido() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        Pedido pedido = new Pedido(List.of(item(10_000, 1, 1)), "PR", false, "PROMO");

        assertThrows(IllegalArgumentException.class,
            () -> service.fechar(pedido, new Cliente(false, false, 1)));
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    @DisplayName("S7 — cliente novo com expresso vai para REVISAO, com valores e sem cobrança")
    void s7RevisaoPorExpresso() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        Pedido pedido = new Pedido(List.of(item(10_000, 1, 1)), "PR", true, null);

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, false, 0));

        // Frete: 1.200 + 1.500 (expresso) = 2.700; total 12.700.
        assertResultado(r, "REVISAO", 10_000, 0, 2_700, 12_700);
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    @DisplayName("S7' — recorrente não VIP acima de R$ 5.000,00 vai para REVISAO")
    void revisaoPorValor() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        Pedido pedido = new Pedido(List.of(item(1_000_000, 1, 1)), "PR", false, null);

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, false, 2));

        // Desconto 5% = 50.000; líquido 950.000 ≥ 30.000 → frete grátis.
        assertResultado(r, "REVISAO", 1_000_000, 50_000, 0, 950_000);
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    @DisplayName("S8 — caminho completo de cliente VIP com cupom, peso, frágil e item inativo")
    void s8PagoVipComCupom() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas, true));
        ItemPedido livros = new ItemPedido("LIVRO", 20_000, 2, 2, 1_500, false);
        ItemPedido vaso = new ItemPedido("VASO", 5_000, 1, 1, 500, true);
        ItemPedido inativo = new ItemPedido("PIANO", 99_999, 0, 0, 90_000, true);
        Pedido pedido = new Pedido(List.of(livros, vaso, inativo), "SP", false, " extra10 ");

        ResultadoPedido r = service.fechar(pedido, new Cliente(true, false, 3));

        // Subtotal 45.000; desconto 4.500 (VIP) + 4.500 (EXTRA10) = 9.000 (= teto);
        // líquido 36.000 ≥ 30.000 → base zerada; VIP 0; frágil +500; total 36.500.
        assertResultado(r, "PAGO", 45_000, 9_000, 500, 36_500);
        assertEquals(List.of(36_500L), cobrancas);
    }

    @Test
    @DisplayName("Cliente novo com BEMVINDO é aprovado e pago")
    void pagoComBemVindo() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas, true));
        Pedido pedido = new Pedido(List.of(item(10_000, 1, 1)), "PR", false, "bemvindo");

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, false, 0));

        assertResultado(r, "PAGO", 10_000, 2_000, 1_200, 9_200);
        assertEquals(List.of(9_200L), cobrancas);
    }

    @Test
    @DisplayName("S9 — pagamento recusado retorna PAGAMENTO_RECUSADO com os valores calculados")
    void s9PagamentoRecusado() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas, false));
        Pedido pedido = new Pedido(List.of(item(10_000, 1, 1)), "PR", false, null);

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, false, 1));

        assertResultado(r, "PAGAMENTO_RECUSADO", 10_000, 0, 1_200, 11_200);
        assertEquals(List.of(11_200L), cobrancas);
    }

    @Test
    @DisplayName("Indisponibilidade temporária: nova tentativa e pagamento aprovado")
    void pagoAposIndisponibilidade() {
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(
            processador(cobrancas, new IllegalStateException("fora do ar"), true));
        Pedido pedido = new Pedido(List.of(item(10_000, 1, 1)), "PR", false, null);

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, false, 1));

        assertEquals("PAGO", r.status());
        assertEquals(List.of(11_200L, 11_200L), cobrancas);
    }

    @Test
    @DisplayName("O serviço usa no máximo 3 tentativas")
    void esgotaTresTentativasNoServico() {
        List<Long> cobrancas = new ArrayList<>();
        IllegalStateException fora = new IllegalStateException("fora do ar");
        PedidoService service = new PedidoService(processador(cobrancas, fora, fora, fora));
        Pedido pedido = new Pedido(List.of(item(10_000, 1, 1)), "PR", false, null);

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, false, 1));

        assertEquals("PAGAMENTO_RECUSADO", r.status());
        assertEquals(3, cobrancas.size());
    }

    @Test
    @DisplayName("Caminho RECUSADO de AnaliseRisco é inviável pelo serviço")
    void recusadoInviavelPeloServico() {
        // Na unidade, cliente bloqueado gera RECUSADO; no serviço, o retorno antecipado
        // de BLOQUEADO acontece antes da análise de risco.
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(processador(cobrancas));
        Pedido pedido = new Pedido(List.of(item(10_000, 1, 1)), "PR", false, null);

        ResultadoPedido r = service.fechar(pedido, new Cliente(false, true, 1));

        assertEquals("BLOQUEADO", r.status());
        assertNotEquals("RECUSADO", r.status());
    }
}
