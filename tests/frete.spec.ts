/**
 * Testes funcionais (caixa-preta) — Calculadora de frete (/frete)
 *
 * Técnicas aplicadas:
 *   - Particionamento em Classes de Equivalência (CE)
 *   - Análise de Valor-Limite (AVL)
 *
 * Especificação (extraída da própria interface):
 *   R1. O CEP deve conter exatamente 8 dígitos numéricos.
 *   R2. O valor do pedido deve ser numérico, maior que zero, com até 2 casas decimais
 *       (separador ponto ou vírgula).
 *   R3. CEP iniciado por 8: frete de R$ 15,00.
 *   R4. Demais CEPs: frete de R$ 25,00.
 *   R5. Pedidos a partir de R$ 200,00 têm frete grátis.
 *
 * O plano de testes completo está em PLANO_DE_TESTES.md.
 */
import { test, expect, type Page } from '@playwright/test';

type Caso = {
  id: string;
  cep: string;
  valor: string;
  esperado: string;
  descricao: string;
};

const INVALIDO = 'Dados inválidos';

async function calcularFrete(page: Page, cep: string, valor: string) {
  await page.goto('/frete');
  await page.getByLabel('CEP').fill(cep);
  await page.getByLabel('Valor do pedido').fill(valor);
  await page.getByRole('button', { name: 'Calcular frete' }).click();
  return page.locator('#resultado');
}

function executar(casos: Caso[]) {
  for (const caso of casos) {
    test(`${caso.id} — ${caso.descricao} (CEP="${caso.cep}", valor="${caso.valor}")`, async ({ page }) => {
      const resultado = await calcularFrete(page, caso.cep, caso.valor);
      const aceito = caso.esperado !== INVALIDO;

      await expect(resultado).toBeVisible();
      await expect(resultado).toHaveText(caso.esperado);
      await expect(resultado).toHaveAttribute('role', aceito ? 'status' : 'alert');
    });
  }
}

test.describe('Frete — classes de equivalência válidas', () => {
  executar([
    { id: 'CT-F01', cep: '80000000', valor: '100', esperado: 'Frete: R$ 15,00', descricao: 'CE1: CEP iniciado por 8 (R3)' },
    { id: 'CT-F02', cep: '01000000', valor: '100', esperado: 'Frete: R$ 25,00', descricao: 'CE2: CEP de outra região (R4)' },
    { id: 'CT-F03', cep: '90000000', valor: '100', esperado: 'Frete: R$ 25,00', descricao: 'CE2: CEP iniciado por 9 (R4)' },
    { id: 'CT-F04', cep: '80000000', valor: '150,50', esperado: 'Frete: R$ 15,00', descricao: 'CE3: valor decimal com vírgula (R2)' },
    { id: 'CT-F05', cep: '01000000', valor: '150.50', esperado: 'Frete: R$ 25,00', descricao: 'CE3: valor decimal com ponto (R2)' },
  ]);
});

test.describe('Frete — valores-limite do frete grátis (R5: valor >= 200,00)', () => {
  executar([
    { id: 'CT-F06', cep: '80000000', valor: '199,99', esperado: 'Frete: R$ 15,00', descricao: 'AVL: limite - 0,01 com CEP 8' },
    { id: 'CT-F07', cep: '01000000', valor: '199,99', esperado: 'Frete: R$ 25,00', descricao: 'AVL: limite - 0,01 com outro CEP' },
    { id: 'CT-F08', cep: '80000000', valor: '200', esperado: 'Frete grátis', descricao: 'AVL: limite exato com CEP 8' },
    { id: 'CT-F09', cep: '01000000', valor: '200,00', esperado: 'Frete grátis', descricao: 'AVL: limite exato com outro CEP' },
    { id: 'CT-F10', cep: '01000000', valor: '200,01', esperado: 'Frete grátis', descricao: 'AVL: limite + 0,01' },
  ]);
});

test.describe('Frete — valores-limite do valor mínimo (R2: valor > 0)', () => {
  executar([
    { id: 'CT-F11', cep: '01000000', valor: '0,01', esperado: 'Frete: R$ 25,00', descricao: 'AVL: menor valor positivo' },
    { id: 'CT-F12', cep: '01000000', valor: '0', esperado: INVALIDO, descricao: 'AVL: valor igual a zero' },
    { id: 'CT-F13', cep: '01000000', valor: '0,00', esperado: INVALIDO, descricao: 'AVL: zero com casas decimais' },
  ]);
});

test.describe('Frete — classes de equivalência inválidas do CEP (R1)', () => {
  executar([
    { id: 'CT-F14', cep: '8000000', valor: '100', esperado: INVALIDO, descricao: 'CE4 / AVL: 7 dígitos' },
    { id: 'CT-F15', cep: '800000000', valor: '100', esperado: INVALIDO, descricao: 'CE4 / AVL: 9 dígitos' },
    { id: 'CT-F16', cep: '80000-000', valor: '100', esperado: INVALIDO, descricao: 'CE5: formato com hífen' },
    { id: 'CT-F17', cep: '8000000a', valor: '100', esperado: INVALIDO, descricao: 'CE5: caractere não numérico' },
    { id: 'CT-F18', cep: '', valor: '100', esperado: INVALIDO, descricao: 'CE6: campo vazio' },
  ]);
});

test.describe('Frete — classes de equivalência inválidas do valor (R2)', () => {
  executar([
    { id: 'CT-F19', cep: '80000000', valor: '-10', esperado: INVALIDO, descricao: 'CE7: valor negativo' },
    { id: 'CT-F20', cep: '80000000', valor: 'cem', esperado: INVALIDO, descricao: 'CE8: valor não numérico' },
    { id: 'CT-F21', cep: '80000000', valor: '10,999', esperado: INVALIDO, descricao: 'CE9: mais de 2 casas decimais' },
    { id: 'CT-F22', cep: '80000000', valor: '', esperado: INVALIDO, descricao: 'CE10: campo vazio' },
  ]);
});

test.describe('Frete — estado inicial da interface', () => {
  test('CT-F23 — resultado permanece oculto antes do envio', async ({ page }) => {
    await page.goto('/frete');
    await expect(page.locator('#resultado')).toBeHidden();
  });
});
