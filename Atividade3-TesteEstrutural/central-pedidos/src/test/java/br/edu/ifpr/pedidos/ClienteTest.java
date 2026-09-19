package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    @DisplayName("Histórico zero é o limite válido")
    void deveAceitarHistoricoZero() {
        Cliente cliente = new Cliente(false, false, 0);

        assertAll(
            () -> assertFalse(cliente.vip()),
            () -> assertFalse(cliente.bloqueado()),
            () -> assertEquals(0, cliente.comprasAnteriores())
        );
    }

    @Test
    @DisplayName("Histórico positivo e flags verdadeiras são preservados")
    void devePreservarValoresInformados() {
        Cliente cliente = new Cliente(true, true, 7);

        assertAll(
            () -> assertTrue(cliente.vip()),
            () -> assertTrue(cliente.bloqueado()),
            () -> assertEquals(7, cliente.comprasAnteriores())
        );
    }

    @Test
    @DisplayName("Histórico -1 (limite inferior - 1) é rejeitado")
    void deveRejeitarHistoricoNegativo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
            () -> new Cliente(false, false, -1));
        assertEquals("Histórico inválido", erro.getMessage());
    }
}
