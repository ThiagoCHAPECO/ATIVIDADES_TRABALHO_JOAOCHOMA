package br.edu.ifpr.grafos;

/** Métodos dos exercícios de Grafo de Fluxo de Controle, transcritos sem alteração. */
public class Exercicios {

    // Exercício 1 — Classificação de pedido
    public String classificarPedido(
            double valor,
            boolean clienteVip,
            boolean pagamentoAprovado) {

        double desconto = 0;

        if (valor >= 500) {
            desconto = 10;
        }

        if (clienteVip) {
            desconto += 5;
        }

        if (!pagamentoAprovado) {
            return "PAGAMENTO RECUSADO";
        }

        double valorFinal = valor - (valor * desconto / 100);
        return "PEDIDO APROVADO: " + valorFinal;
    }

    // Exercício 2 — Análise de leituras de temperatura
    public int contarAlertas(double[] temperaturas) {
        int alertas = 0;
        int i = 0;

        while (i < temperaturas.length) {
            if (temperaturas[i] < 0) {
                alertas += 2;
            } else if (temperaturas[i] > 35) {
                alertas++;
            }

            i++;
        }

        return alertas;
    }
}
