# Casos de Teste — Sistema de Reserva de Salas

A massa de dados usada (salas, turmas e usuários) está definida na seção 7 do Plano de Teste (Plano-de-Teste.pdf).

**Pré-condição geral:** banco de teste restaurado, sem reservas cadastradas, a não ser as indicadas em cada caso.

---

## CT-01 — Reservar sala disponível para turma compatível

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-01, RNF-02 |
| Prioridade | Alta |
| Pré-condições | Logado como **prof.carlos**; S101 livre em 10/09/2026 |
| Dados | Sala S101 · 10/09/2026 · 19h00–20h40 · turma ESOFT5A (40) · responsável prof.carlos |

**Passos:** 1. Acessar "Nova reserva". 2. Informar os dados acima. 3. Clicar em "Reservar".
**Resultado esperado:** mensagem "Reserva realizada com sucesso"; a reserva aparece na agenda da S101; um registro de criação aparece na auditoria.
**Resultado real:** ______ **Status:** ______

---

## CT-02 — Impedir sobreposição parcial de horário na mesma sala

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-02 / RS-01 |
| Prioridade | Alta |
| Pré-condições | Existe reserva da S101 em 10/09/2026 das 19h00 às 20h40; logado como **prof.marina** |
| Dados | Sala S101 · 10/09/2026 · **20h00–21h00** · turma ADS3A |

**Passos:** 1. Acessar "Nova reserva". 2. Informar os dados. 3. Clicar em "Reservar".
**Resultado esperado:** reserva recusada com a mensagem "Sala já reservada neste horário"; a agenda mantém apenas a reserva original.
**Resultado real:** ______ **Status:** ______

---

## CT-03 — Permitir reserva em horário imediatamente seguinte (limite do conflito)

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-01, RF-02 / RS-01 |
| Prioridade | Média |
| Pré-condições | Existe reserva da S101 em 10/09/2026 das 19h00 às 20h40; logado como **prof.marina** |
| Dados | Sala S101 · 10/09/2026 · **20h40–22h20** · turma ADS3A |

**Passos:** 1. Acessar "Nova reserva". 2. Informar os dados. 3. Clicar em "Reservar".
**Resultado esperado:** reserva aceita, porque os intervalos apenas se encostam e não se sobrepõem.
**Resultado real:** ______ **Status:** ______

---

## CT-04 — Turma com exatamente a capacidade da sala (limite)

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-03 / RS-02 |
| Prioridade | Alta |
| Pré-condições | Logado como **prof.carlos**; S101 livre |
| Dados | Sala S101 (capacidade 40) · 11/09/2026 · 08h00–09h40 · turma **ESOFT5A (40 alunos)** |

**Passos:** 1. Acessar "Nova reserva". 2. Informar os dados. 3. Clicar em "Reservar".
**Resultado esperado:** reserva aceita.
**Resultado real:** ______ **Status:** ______

---

## CT-05 — Impedir turma maior que a capacidade (limite + 1)

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-03 / RS-02 |
| Prioridade | Alta |
| Pré-condições | Logado como **prof.carlos**; S101 livre |
| Dados | Sala S101 (capacidade 40) · 11/09/2026 · 10h00–11h40 · turma **ESOFT5B (41 alunos)** |

**Passos:** 1. Acessar "Nova reserva". 2. Informar os dados. 3. Clicar em "Reservar".
**Resultado esperado:** reserva recusada com a mensagem "Capacidade da sala insuficiente para a turma".
**Resultado real:** ______ **Status:** ______

---

## CT-06 — Bloquear reserva de sala em manutenção

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-04 / RS-05 |
| Prioridade | Média |
| Pré-condições | LAB01 com status "Em manutenção"; logado como **prof.carlos** |
| Dados | Sala LAB01 · 12/09/2026 · 14h00–15h40 · turma ADS3A (25) |

**Passos:** 1. Acessar "Nova reserva". 2. Selecionar LAB01. 3. Informar os dados. 4. Clicar em "Reservar".
**Resultado esperado:** reserva recusada com a mensagem "Sala indisponível: em manutenção" (ou sala não disponível para seleção).
**Resultado real:** ______ **Status:** ______

---

## CT-07 — Valores-limite da janela de horário (07h30 às 22h30)

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-05 / RS-06 |
| Prioridade | Média |
| Pré-condições | Logado como **prof.carlos**; S102 livre em 14/09/2026 |

| Subcaso | Horário | Resultado esperado |
|---|---|---|
| 07a | **07h29**–08h30 | Recusada: "Horário fora do período permitido" |
| 07b | **07h30**–08h30 | Aceita |
| 07c | 21h30–**22h30** | Aceita |
| 07d | 21h30–**22h31** | Recusada: "Horário fora do período permitido" |

**Passos:** para cada subcaso, criar uma reserva da S102 com a turma ADS3A no horário indicado (cancelar as aceitas antes do próximo subcaso).
**Resultado real:** ______ **Status:** ______

---

## CT-08 — Professor não pode alterar reserva de outro professor

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-06, RNF-02 / RS-03 |
| Prioridade | Alta |
| Pré-condições | Reserva da S102 em 15/09/2026, 19h00–20h40, criada por **prof.carlos**; logado como **prof.marina** |

**Passos:** 1. Abrir a reserva de prof.carlos. 2. Tentar alterar o horário para 20h40–22h20. 3. Salvar.
**Resultado esperado:** operação negada com a mensagem "Sem permissão para alterar esta reserva"; a reserva permanece inalterada; a tentativa fica registrada na auditoria.
**Resultado real:** ______ **Status:** ______

---

## CT-09 — Coordenação altera reserva de professor e gera notificação

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-06, RF-08, RNF-02 / RS-03, RS-04 |
| Prioridade | Alta |
| Pré-condições | Mesma reserva do CT-08; logado como **coord.paula** |

**Passos:** 1. Abrir a reserva de prof.carlos. 2. Alterar o horário para 20h40–22h20. 3. Salvar. 4. Entrar como prof.carlos e abrir as notificações.
**Resultado esperado:** alteração salva; prof.carlos recebe a notificação "Sua reserva da S102 em 15/09/2026 foi alterada"; a auditoria registra usuário, data/hora e os valores antigo e novo.
**Resultado real:** ______ **Status:** ______

---

## CT-10 — Cancelamento libera o horário, registra histórico e notifica

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RF-07, RF-08 / RS-04, RS-07 |
| Prioridade | Alta |
| Pré-condições | Reserva da S101 em 16/09/2026, 19h00–20h40, de **prof.carlos**; logado como prof.carlos |

**Passos:** 1. Cancelar a reserva. 2. Consultar o histórico da reserva. 3. Conferir as notificações. 4. Entrar como prof.marina e reservar a S101 no mesmo horário.
**Resultado esperado:** reserva com status "Cancelada"; histórico com o registro do cancelamento (usuário e data/hora); notificação de cancelamento enviada; nova reserva de prof.marina aceita.
**Resultado real:** ______ **Status:** ______

---

## CT-11 — Tempo de resposta da busca de salas

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RNF-01 / RS-10 |
| Prioridade | Baixa |
| Pré-condições | Base com ao menos 50 salas e 500 reservas; logado como **prof.carlos** |

**Passos:** 1. Buscar salas livres para 17/09/2026, 19h00–20h40, capacidade mínima 30. 2. Medir o tempo até exibir os resultados (DevTools → Network), repetindo 5 vezes.
**Resultado esperado:** todas as buscas respondem em **até 2 segundos**.
**Resultado real:** ______ **Status:** ______

---

## CT-12 — Acesso restrito à unidade autorizada

| Campo | Valor |
|---|---|
| Requisitos / Riscos | RNF-03 / RS-09 |
| Prioridade | Média |
| Pré-condições | Logado como **prof.lucas** (unidade Norte) |

**Passos:** 1. Buscar salas disponíveis. 2. Tentar acessar diretamente a reserva da sala S101 (unidade Centro) pela URL.
**Resultado esperado:** a busca lista apenas as salas da unidade Norte; o acesso à S101 é negado ("Acesso não autorizado").
**Resultado real:** ______ **Status:** ______

---

## Matriz de rastreabilidade

| Requisito | Casos de teste |
|---|---|
| RF-01 | CT-01, CT-03 |
| RF-02 | CT-02, CT-03 |
| RF-03 | CT-04, CT-05 |
| RF-04 | CT-06 |
| RF-05 | CT-07 |
| RF-06 | CT-08, CT-09 |
| RF-07 | CT-10 |
| RF-08 | CT-09, CT-10 |
| RNF-01 | CT-11 |
| RNF-02 | CT-01, CT-08, CT-09 |
| RNF-03 | CT-12 |

Todos os requisitos têm ao menos um caso de teste, e cada risco crítico (RS-01 a RS-04) é coberto por pelo menos dois casos.
