package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private static ItemPedido ativo(long preco, int quantidade, int peso, boolean fragil) {
        return new ItemPedido("ATIVO", preco, quantidade, 100, peso, fragil);
    }

    private static ItemPedido inativo(boolean fragil) {
        return new ItemPedido("INATIVO", 99_999, 0, 0, 5_000, fragil);
    }

    private static Pedido pedido(ItemPedido... itens) {
        return new Pedido(List.of(itens), "PR", false, null);
    }

    // ---------- Construtor ----------

    @Test
    @DisplayName("Lista nula é rejeitada")
    void deveRejeitarListaNula() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> new Pedido(null, "PR", false, null));
        assertEquals("Lista inválida", erro.getMessage());
    }

    @Test
    @DisplayName("100 linhas é o limite aceito; 101 linhas é rejeitado")
    void deveValidarLimiteDeLinhas() {
        ItemPedido item = ativo(100, 1, 100, false);
        assertEquals(100, new Pedido(Collections.nCopies(100, item), "PR", false, null).itens().size());
        assertThrows(IllegalArgumentException.class,
            () -> new Pedido(Collections.nCopies(101, item), "PR", false, null));
    }

    @Test
    @DisplayName("Elemento nulo na lista causa NullPointerException")
    void deveRejeitarElementoNulo() {
        List<ItemPedido> itens = Arrays.asList(ativo(100, 1, 100, false), null);
        assertThrows(NullPointerException.class, () -> new Pedido(itens, "PR", false, null));
    }

    @ParameterizedTest(name = "UF \"{0}\" é inválida")
    @NullSource
    @ValueSource(strings = {"pr", "P", "PRR", "P1", "", "Pr"})
    void deveRejeitarUfInvalida(String uf) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> new Pedido(List.of(), uf, false, null));
        assertEquals("UF inválida", erro.getMessage());
    }

    @Test
    @DisplayName("Qualquer UF com duas letras maiúsculas é aceita")
    void deveAceitarUfDesconhecidaNoFormatoCorreto() {
        assertEquals("AM", new Pedido(List.of(), "AM", false, null).uf());
    }

    @Test
    @DisplayName("A lista é copiada defensivamente e é imutável")
    void deveCopiarListaDefensivamente() {
        List<ItemPedido> original = new ArrayList<>();
        original.add(ativo(1_000, 1, 100, false));
        Pedido pedido = new Pedido(original, "PR", false, null);

        original.add(ativo(5_000, 1, 100, false));

        assertEquals(1, pedido.itens().size());
        assertEquals(1_000L, pedido.subtotalCentavos());
        assertThrows(UnsupportedOperationException.class,
            () -> pedido.itens().add(ativo(1, 1, 1, false)));
    }

    // ---------- subtotalCentavos: for + continue ----------

    @Test
    @DisplayName("Subtotal: zero iterações (lista vazia)")
    void subtotalDeListaVaziaEZero() {
        assertEquals(0L, pedido().subtotalCentavos());
    }

    @Test
    @DisplayName("Subtotal: uma iteração")
    void subtotalDeUmaLinha() {
        assertEquals(3_000L, pedido(ativo(1_500, 2, 100, false)).subtotalCentavos());
    }

    @Test
    @DisplayName("Subtotal: várias iterações com linha inativa (continue)")
    void subtotalIgnoraLinhaInativa() {
        Pedido pedido = pedido(ativo(1_000, 2, 100, false), inativo(false), ativo(500, 3, 100, false));
        assertEquals(3_500L, pedido.subtotalCentavos());
    }

    @Test
    @DisplayName("Subtotal: somente linhas inativas resulta em zero")
    void subtotalSomenteInativos() {
        assertEquals(0L, pedido(inativo(false), inativo(true)).subtotalCentavos());
    }

    // ---------- pesoGramas ----------

    @Test
    @DisplayName("Peso: soma peso × quantidade; inativo não soma")
    void pesoConsideraQuantidade() {
        assertEquals(0, pedido().pesoGramas());
        Pedido pedido = pedido(ativo(100, 3, 700, false), inativo(false), ativo(100, 1, 400, false));
        assertEquals(2_500, pedido.pesoGramas());
    }

    // ---------- temFragil: for com return antecipado ----------

    @Test
    @DisplayName("Frágil: lista vazia não tem item frágil")
    void semItensNaoTemFragil() {
        assertFalse(pedido().temFragil());
    }

    @Test
    @DisplayName("Frágil: item frágil inativo é ignorado (&& curto-circuito na quantidade)")
    void fragilInativoNaoConta() {
        assertFalse(pedido(ativo(100, 1, 100, false), inativo(true)).temFragil());
    }

    @Test
    @DisplayName("Frágil: item frágil ativo no fim da lista é encontrado")
    void fragilAtivoNoFim() {
        assertTrue(pedido(ativo(100, 1, 100, false), ativo(100, 1, 100, true)).temFragil());
    }

    // ---------- estoqueSuficiente: for + break ----------

    @Test
    @DisplayName("Estoque: lista vazia é suficiente (zero iterações)")
    void estoqueDeListaVazia() {
        assertTrue(pedido().estoqueSuficiente());
    }

    @Test
    @DisplayName("Estoque: todas as linhas disponíveis (várias iterações, sem break)")
    void estoqueTodasDisponiveis() {
        assertTrue(pedido(ativo(100, 5, 100, false), inativo(false)).estoqueSuficiente());
    }

    @Test
    @DisplayName("Estoque: falta na primeira linha (break na 1ª iteração)")
    void estoqueFaltaNoInicio() {
        ItemPedido semEstoque = new ItemPedido("X", 100, 3, 2, 100, false);
        assertFalse(pedido(semEstoque, ativo(100, 1, 100, false)).estoqueSuficiente());
    }

    @Test
    @DisplayName("Estoque: falta na última linha (break na última iteração)")
    void estoqueFaltaNoFim() {
        ItemPedido semEstoque = new ItemPedido("X", 100, 3, 2, 100, false);
        assertFalse(pedido(ativo(100, 1, 100, false), semEstoque).estoqueSuficiente());
    }

    @Test
    @DisplayName("Estoque é avaliado por linha, mesmo com SKU repetido")
    void estoquePorLinhaComSkuRepetido() {
        ItemPedido a = new ItemPedido("MESMO", 100, 3, 3, 100, false);
        ItemPedido b = new ItemPedido("MESMO", 100, 3, 3, 100, false);
        assertTrue(pedido(a, b).estoqueSuficiente());
    }
}
