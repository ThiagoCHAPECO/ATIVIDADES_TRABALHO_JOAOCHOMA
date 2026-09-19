# Plano de Testes Funcionais — Frete e Senha

**Disciplina:** Teste de Software — Semana 04
**Ferramenta:** Playwright (`@playwright/test`)
**Tipo de teste:** Funcional, caixa-preta, ponta a ponta (E2E)

## 1. Objetivo

Verificar se as interfaces de **cálculo de frete** (`/frete`) e **cadastro de senha** (`/senha`) se comportam de acordo com as regras descritas nas próprias páginas. Os casos de teste foram derivados por duas técnicas de caixa-preta:

- **Particionamento em Classes de Equivalência (CE):** o domínio de entrada é dividido em classes cujos elementos devem ser tratados da mesma forma pelo sistema, e cada classe é representada por pelo menos um caso.
- **Análise de Valor-Limite (AVL):** como os defeitos se concentram nas fronteiras das classes, testa-se o limite exato e os valores imediatamente abaixo e acima dele.

## 2. Escopo

| Incluído | Não incluído |
|---|---|
| Validação das entradas e mensagens exibidas | Testes de desempenho e carga |
| Regras de negócio do frete e da senha | Segurança (armazenamento da senha) |
| Atributo de acessibilidade `role` (`status`/`alert`) | Compatibilidade com dispositivos móveis |

## 3. Critério de aprovação

Um caso de teste é aprovado quando o elemento `#resultado` fica visível, exibe exatamente o texto esperado e tem `role="status"` quando a entrada é aceita ou `role="alert"` quando é rejeitada.

---

## 4. Calculadora de frete

### 4.1 Requisitos

| ID | Requisito |
|---|---|
| R1 | O CEP deve conter exatamente 8 dígitos numéricos |
| R2 | O valor deve ser numérico, maior que zero, com até 2 casas decimais |
| R3 | CEP iniciado por 8: frete de R$ 15,00 |
| R4 | Demais CEPs: frete de R$ 25,00 |
| R5 | Pedidos a partir de R$ 200,00 têm frete grátis |

### 4.2 Classes de equivalência

| Entrada | Classes válidas | Classes inválidas |
|---|---|---|
| CEP | CE1: 8 dígitos iniciados por 8; CE2: 8 dígitos iniciados por outro dígito | CE4: quantidade de dígitos ≠ 8; CE5: caracteres não numéricos; CE6: vazio |
| Valor | CE3: número > 0 com até 2 casas decimais (ponto ou vírgula) | CE7: negativo; CE8: não numérico; CE9: mais de 2 casas decimais; CE10: vazio |

### 4.3 Valores-limite

| Fronteira | Abaixo | No limite | Acima |
|---|---|---|---|
| Frete grátis (200,00) | 199,99 → cobra frete | 200,00 → grátis | 200,01 → grátis |
| Valor mínimo (> 0) | 0,00 → inválido | — | 0,01 → válido |
| Tamanho do CEP (8) | 7 dígitos → inválido | 8 dígitos → válido | 9 dígitos → inválido |

### 4.4 Casos de teste

| ID | CEP | Valor | Técnica | Resultado esperado |
|---|---|---|---|---|
| CT-F01 | 80000000 | 100 | CE1 | Frete: R$ 15,00 |
| CT-F02 | 01000000 | 100 | CE2 | Frete: R$ 25,00 |
| CT-F03 | 90000000 | 100 | CE2 | Frete: R$ 25,00 |
| CT-F04 | 80000000 | 150,50 | CE3 | Frete: R$ 15,00 |
| CT-F05 | 01000000 | 150.50 | CE3 | Frete: R$ 25,00 |
| CT-F06 | 80000000 | 199,99 | AVL | Frete: R$ 15,00 |
| CT-F07 | 01000000 | 199,99 | AVL | Frete: R$ 25,00 |
| CT-F08 | 80000000 | 200 | AVL | Frete grátis |
| CT-F09 | 01000000 | 200,00 | AVL | Frete grátis |
| CT-F10 | 01000000 | 200,01 | AVL | Frete grátis |
| CT-F11 | 01000000 | 0,01 | AVL | Frete: R$ 25,00 |
| CT-F12 | 01000000 | 0 | AVL | Dados inválidos |
| CT-F13 | 01000000 | 0,00 | AVL | Dados inválidos |
| CT-F14 | 8000000 | 100 | CE4 / AVL | Dados inválidos |
| CT-F15 | 800000000 | 100 | CE4 / AVL | Dados inválidos |
| CT-F16 | 80000-000 | 100 | CE5 | Dados inválidos |
| CT-F17 | 8000000a | 100 | CE5 | Dados inválidos |
| CT-F18 | (vazio) | 100 | CE6 | Dados inválidos |
| CT-F19 | 80000000 | -10 | CE7 | Dados inválidos |
| CT-F20 | 80000000 | cem | CE8 | Dados inválidos |
| CT-F21 | 80000000 | 10,999 | CE9 | Dados inválidos |
| CT-F22 | 80000000 | (vazio) | CE10 | Dados inválidos |
| CT-F23 | — | — | Estado inicial | Resultado oculto antes do envio |

---

## 5. Cadastro de senha

### 5.1 Requisitos

| ID | Requisito |
|---|---|
| R1 | De 8 a 20 caracteres |
| R2 | Ao menos uma letra maiúscula |
| R3 | Ao menos uma letra minúscula |
| R4 | Ao menos um número |
| R5 | Espaços não são permitidos |
| R6 | A confirmação deve ser idêntica à senha |

### 5.2 Classes de equivalência

| Classe | Descrição | Tipo |
|---|---|---|
| CE1 | Atende a R1–R5 | Válida |
| CE2 | Sem letra maiúscula | Inválida |
| CE3 | Sem letra minúscula | Inválida |
| CE4 | Sem número | Inválida |
| CE5 | Contém espaço | Inválida |
| CE6 | Somente números | Inválida |
| CE7 | Vazia | Inválida |
| CE8 | Confirmação diferente | Inválida |
| CE9 | Confirmação vazia | Inválida |

### 5.3 Valores-limite do tamanho

| 7 (mín − 1) | 8 (mín) | 9 (mín + 1) | 19 (máx − 1) | 20 (máx) | 21 (máx + 1) |
|---|---|---|---|---|---|
| Rejeitada | Aceita | Aceita | Aceita | Aceita | Rejeitada |

### 5.4 Casos de teste

| ID | Senha | Técnica | Resultado esperado |
|---|---|---|---|
| CT-S01 | Senha123 | CE1 | Senha cadastrada |
| CT-S02 | SenhaForte2026 | CE1 | Senha cadastrada |
| CT-S03 | Senha@123! | CE1 | Senha cadastrada |
| CT-S04 | Abcde12 (7) | AVL | Senha fora do padrão |
| CT-S05 | Abcdef12 (8) | AVL | Senha cadastrada |
| CT-S06 | Abcdefg12 (9) | AVL | Senha cadastrada |
| CT-S07 | 19 caracteres | AVL | Senha cadastrada |
| CT-S08 | 20 caracteres | AVL | Senha cadastrada |
| CT-S09 | 21 caracteres | AVL | Senha fora do padrão |
| CT-S10 | senha123 | CE2 | Senha fora do padrão |
| CT-S11 | SENHA123 | CE3 | Senha fora do padrão |
| CT-S12 | SenhaSemNumero | CE4 | Senha fora do padrão |
| CT-S13 | "Senha 123" | CE5 | Senha fora do padrão |
| CT-S14 | " Senha123" | CE5 | Senha fora do padrão |
| CT-S15 | 12345678 | CE6 | Senha fora do padrão |
| CT-S16 | (vazia) | CE7 | Senha fora do padrão |
| CT-S17 | Senha123 / Senha124 | CE8 | As senhas não coincidem |
| CT-S18 | Senha123 / (vazia) | CE9 | As senhas não coincidem |
| CT-S19 | curta / outra | Precedência | Senha fora do padrão |
| CT-S20 | Senha123 / Senha123 | Pós-condição | Campos limpos após o cadastro |

---

## 6. Execução

```bash
npm install
npm run browsers
npm test
```

Relatório HTML: `npm run report`.

## 7. Resultados

Todos os 54 casos de teste (43 desta atividade e 11 exemplos fornecidos) foram executados no Chromium e **aprovados**. Nenhum defeito foi encontrado nas regras especificadas.
