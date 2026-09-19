/**
 * Testes funcionais (caixa-preta) — Cadastro de senha (/senha)
 *
 * Técnicas aplicadas:
 *   - Particionamento em Classes de Equivalência (CE)
 *   - Análise de Valor-Limite (AVL)
 *
 * Especificação (extraída da própria interface):
 *   R1. A senha deve ter de 8 a 20 caracteres.
 *   R2. Deve conter ao menos uma letra maiúscula.
 *   R3. Deve conter ao menos uma letra minúscula.
 *   R4. Deve conter ao menos um número.
 *   R5. Espaços não são permitidos.
 *   R6. A confirmação deve ser idêntica à senha.
 *
 * O plano de testes completo está em PLANO_DE_TESTES.md.
 */
import { test, expect, type Page } from '@playwright/test';

type Caso = {
  id: string;
  senha: string;
  esperado: string;
  descricao: string;
};

const SUCESSO = 'Senha cadastrada';
const FORA_DO_PADRAO = 'Senha fora do padrão';
const NAO_COINCIDEM = 'As senhas não coincidem';

async function cadastrarSenha(page: Page, senha: string, confirmacao: string) {
  await page.goto('/senha');
  await page.getByLabel('Nova senha').fill(senha);
  await page.getByLabel('Confirmar senha').fill(confirmacao);
  await page.getByRole('button', { name: 'Cadastrar senha' }).click();
  return page.locator('#resultado');
}

function executar(casos: Caso[]) {
  for (const caso of casos) {
    test(`${caso.id} — ${caso.descricao} (senha="${caso.senha}")`, async ({ page }) => {
      const resultado = await cadastrarSenha(page, caso.senha, caso.senha);
      const aceito = caso.esperado === SUCESSO;

      await expect(resultado).toBeVisible();
      await expect(resultado).toHaveText(caso.esperado);
      await expect(resultado).toHaveAttribute('role', aceito ? 'status' : 'alert');
    });
  }
}

test.describe('Senha — classes de equivalência válidas', () => {
  executar([
    { id: 'CT-S01', senha: 'Senha123', esperado: SUCESSO, descricao: 'CE1: atende a todos os requisitos' },
    { id: 'CT-S02', senha: 'SenhaForte2026', esperado: SUCESSO, descricao: 'CE1: tamanho intermediário' },
    { id: 'CT-S03', senha: 'Senha@123!', esperado: SUCESSO, descricao: 'CE1: com caracteres especiais' },
  ]);
});

test.describe('Senha — valores-limite do tamanho (R1: 8 a 20)', () => {
  executar([
    { id: 'CT-S04', senha: 'Abcde12', esperado: FORA_DO_PADRAO, descricao: 'AVL: 7 caracteres (mínimo - 1)' },
    { id: 'CT-S05', senha: 'Abcdef12', esperado: SUCESSO, descricao: 'AVL: 8 caracteres (mínimo)' },
    { id: 'CT-S06', senha: 'Abcdefg12', esperado: SUCESSO, descricao: 'AVL: 9 caracteres (mínimo + 1)' },
    { id: 'CT-S07', senha: 'Abcdefghijklmnopq12', esperado: SUCESSO, descricao: 'AVL: 19 caracteres (máximo - 1)' },
    { id: 'CT-S08', senha: 'Abcdefghijklmnopqr12', esperado: SUCESSO, descricao: 'AVL: 20 caracteres (máximo)' },
    { id: 'CT-S09', senha: 'Abcdefghijklmnopqrs12', esperado: FORA_DO_PADRAO, descricao: 'AVL: 21 caracteres (máximo + 1)' },
  ]);
});

test.describe('Senha — classes de equivalência inválidas de composição', () => {
  executar([
    { id: 'CT-S10', senha: 'senha123', esperado: FORA_DO_PADRAO, descricao: 'CE2: sem letra maiúscula (R2)' },
    { id: 'CT-S11', senha: 'SENHA123', esperado: FORA_DO_PADRAO, descricao: 'CE3: sem letra minúscula (R3)' },
    { id: 'CT-S12', senha: 'SenhaSemNumero', esperado: FORA_DO_PADRAO, descricao: 'CE4: sem número (R4)' },
    { id: 'CT-S13', senha: 'Senha 123', esperado: FORA_DO_PADRAO, descricao: 'CE5: espaço no meio (R5)' },
    { id: 'CT-S14', senha: ' Senha123', esperado: FORA_DO_PADRAO, descricao: 'CE5: espaço no início (R5)' },
    { id: 'CT-S15', senha: '12345678', esperado: FORA_DO_PADRAO, descricao: 'CE6: somente números (R2, R3)' },
    { id: 'CT-S16', senha: '', esperado: FORA_DO_PADRAO, descricao: 'CE7: campo vazio (R1)' },
  ]);
});

test.describe('Senha — confirmação (R6)', () => {
  test('CT-S17 — CE8: confirmação diferente da senha é rejeitada', async ({ page }) => {
    const resultado = await cadastrarSenha(page, 'Senha123', 'Senha124');
    await expect(resultado).toHaveText(NAO_COINCIDEM);
    await expect(resultado).toHaveAttribute('role', 'alert');
  });

  test('CT-S18 — CE9: confirmação vazia é rejeitada', async ({ page }) => {
    const resultado = await cadastrarSenha(page, 'Senha123', '');
    await expect(resultado).toHaveText(NAO_COINCIDEM);
    await expect(resultado).toHaveAttribute('role', 'alert');
  });

  test('CT-S19 — o formato é validado antes da confirmação', async ({ page }) => {
    const resultado = await cadastrarSenha(page, 'curta', 'outra');
    await expect(resultado).toHaveText(FORA_DO_PADRAO);
  });
});

test.describe('Senha — pós-condição do cadastro', () => {
  test('CT-S20 — formulário é limpo após cadastro bem-sucedido', async ({ page }) => {
    const resultado = await cadastrarSenha(page, 'Senha123', 'Senha123');
    await expect(resultado).toHaveText(SUCESSO);
    await expect(page.getByLabel('Nova senha')).toHaveValue('');
    await expect(page.getByLabel('Confirmar senha')).toHaveValue('');
  });
});
