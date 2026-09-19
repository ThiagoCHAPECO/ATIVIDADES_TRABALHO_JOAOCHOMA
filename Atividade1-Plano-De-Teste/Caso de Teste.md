# Casos de Teste - Sistema de Reserva de Salas

**Equipe:** Thiago Gimenes e ______________________

Os dados de teste (salas, turmas, usuários e unidades) estão definidos na seção 7 do **Plano de Teste** (`Plano-de-Teste.pdf`). Antes de cada caso, o banco de teste é restaurado e contém apenas as reservas indicadas nas pré-condições.

| Caso | Funcionalidade | Requisitos | Riscos | Prioridade |
|---|---|---|---|---|
| CT-01 | Reservar sala disponível | RF-01, RNF-02 | - | Alta |
| CT-02 | Conflito de horário na mesma sala | RF-02 | RS-01 | Alta |
| CT-03 | Capacidade da sala | RF-03 | RS-02 | Alta |
| CT-04 | Sala em manutenção | RF-04 | RS-05 | Média |
| CT-05 | Horário de funcionamento | RF-05 | RS-06 | Média |
| CT-06 | Alteração de reserva por perfil | RF-06, RF-08, RNF-02 | RS-03, RS-04 | Alta |
| CT-07 | Cancelamento de reserva | RF-07, RF-08, RNF-02 | RS-04, RS-07 | Alta |
| CT-08 | Desempenho da busca | RNF-01 | RS-10 | Baixa |
| CT-09 | Trilha de auditoria | RNF-02 | RS-08 | Média |
| CT-10 | Acesso por unidade | RNF-03 | RS-09 | Média |

---

## Caso de Teste 01 - Reservar sala disponível

**Descrição:** verificar se o sistema permite reservar uma sala disponível para uma turma compatível e se exige todos os dados da reserva (data, horário, turma e responsável).

**Pré-condições:** o usuário "prof.carlos" está logado; a sala S101 está livre em 10/09/2026.

**Passos:**
1. Acessar a página "Nova reserva".
2. Preencher os campos com os dados do cenário.
3. Clicar no botão "Reservar".

**Cenário 1 - Reserva com todos os dados válidos:**
- Dados de Teste:
  - Sala: S101 · Data: 10/09/2026 · Horário: 19h00 às 20h40
  - Turma: ESOFT5A (40 alunos) · Responsável: prof.carlos
- Resultado Esperado:
  - O sistema exibe "Reserva realizada com sucesso".
  - A reserva aparece na agenda da S101.
  - A criação é registrada na auditoria (usuário, data e hora).

**Cenário 2 - Turma não informada:**
- Dados de Teste:
  - Sala: S101 · Data: 10/09/2026 · Horário: 19h00 às 20h40
  - Turma: (em branco) · Responsável: prof.carlos
- Resultado Esperado:
  - O sistema exibe uma mensagem indicando que o campo "Turma" é obrigatório.
  - Nenhuma reserva é criada.

**Pós-condições:** no Cenário 1, a S101 fica ocupada no horário reservado; no Cenário 2, a agenda permanece inalterada.

---

## Caso de Teste 02 - Conflito de horário na mesma sala

**Descrição:** verificar se o sistema impede a sobreposição de reservas na mesma sala (classes de equivalência de horário).

**Pré-condições:** existe uma reserva da S101 em 10/09/2026 das 19h00 às 20h40 (turma ESOFT5A); o usuário "prof.marina" está logado.

**Passos:**
1. Acessar a página "Nova reserva".
2. Informar a sala S101, a data 10/09/2026, a turma ADS3A e o horário do cenário.
3. Clicar no botão "Reservar".

**Cenário 1 - Mesmo horário (sobreposição total):**
- Dados de Teste:
  - Horário: 19h00 às 20h40
- Resultado Esperado:
  - O sistema exibe "Sala já reservada neste horário".
  - A agenda mantém apenas a reserva original.

**Cenário 2 - Sobreposição parcial:**
- Dados de Teste:
  - Horário: 20h00 às 21h00
- Resultado Esperado:
  - O sistema exibe "Sala já reservada neste horário".

**Cenário 3 - Horário contido na reserva existente:**
- Dados de Teste:
  - Horário: 19h30 às 20h00
- Resultado Esperado:
  - O sistema exibe "Sala já reservada neste horário".

**Cenário 4 - Horário imediatamente seguinte (intervalos apenas se encostam):**
- Dados de Teste:
  - Horário: 20h40 às 22h20
- Resultado Esperado:
  - A reserva é aceita, pois não há sobreposição.

**Pós-condições:** nos Cenários 1 a 3, só existe a reserva original; no Cenário 4, existem duas reservas consecutivas.

---

## Caso de Teste 03 - Capacidade da sala

**Descrição:** verificar se o sistema impede reservar uma sala para uma turma maior que a sua capacidade (análise de valor-limite).

**Pré-condições:** o usuário "prof.carlos" está logado; a sala S101 (capacidade 40) está livre em 11/09/2026.

**Passos:**
1. Acessar a página "Nova reserva".
2. Informar a sala S101, a data 11/09/2026, o horário 08h00 às 09h40 e a turma do cenário.
3. Clicar no botão "Reservar".

**Cenário 1 - Turma menor que a capacidade:**
- Dados de Teste:
  - Turma: ADS3A (25 alunos)
- Resultado Esperado:
  - A reserva é aceita.

**Cenário 2 - Turma igual à capacidade (limite):**
- Dados de Teste:
  - Turma: ESOFT5A (40 alunos)
- Resultado Esperado:
  - A reserva é aceita.

**Cenário 3 - Turma com um aluno a mais que a capacidade (limite + 1):**
- Dados de Teste:
  - Turma: ESOFT5B (41 alunos)
- Resultado Esperado:
  - O sistema exibe "Capacidade da sala insuficiente para a turma".
  - Nenhuma reserva é criada.

**Pós-condições:** as reservas aceitas são canceladas antes do próximo cenário.

---

## Caso de Teste 04 - Sala em manutenção

**Descrição:** verificar se o sistema bloqueia reservas de salas em manutenção.

**Pré-condições:** a sala LAB01 está com status "Em manutenção"; o usuário "prof.carlos" está logado.

**Passos:**
1. Acessar a página "Nova reserva".
2. Selecionar a sala LAB01 e informar data 12/09/2026, horário 14h00 às 15h40 e turma ADS3A.
3. Clicar no botão "Reservar".

**Cenário 1 - Sala em manutenção:**
- Dados de Teste:
  - Sala: LAB01 (em manutenção)
- Resultado Esperado:
  - O sistema exibe "Sala indisponível: em manutenção" ou não permite selecionar a sala.
  - Nenhuma reserva é criada.

**Cenário 2 - Sala liberada após a manutenção:**
- Dados de Teste:
  - Sala: LAB01, com o status alterado para "Disponível" pela coordenação antes do passo 1
- Resultado Esperado:
  - A reserva é aceita.

**Pós-condições:** o status da LAB01 é restaurado para "Em manutenção" ao final.

---

## Caso de Teste 05 - Horário de funcionamento

**Descrição:** verificar se o sistema só permite reservas entre 07h30 e 22h30 (análise de valor-limite).

**Pré-condições:** o usuário "prof.carlos" está logado; a sala S102 está livre em 14/09/2026.

**Passos:**
1. Acessar a página "Nova reserva".
2. Informar a sala S102, a data 14/09/2026, a turma ADS3A e o horário do cenário.
3. Clicar no botão "Reservar".

**Cenário 1 - Início um minuto antes da abertura:**
- Dados de Teste:
  - Horário: 07h29 às 08h30
- Resultado Esperado:
  - O sistema exibe "Horário fora do período permitido".

**Cenário 2 - Início exatamente na abertura:**
- Dados de Teste:
  - Horário: 07h30 às 08h30
- Resultado Esperado:
  - A reserva é aceita.

**Cenário 3 - Término exatamente no encerramento:**
- Dados de Teste:
  - Horário: 21h30 às 22h30
- Resultado Esperado:
  - A reserva é aceita.

**Cenário 4 - Término um minuto após o encerramento:**
- Dados de Teste:
  - Horário: 21h30 às 22h31
- Resultado Esperado:
  - O sistema exibe "Horário fora do período permitido".

**Pós-condições:** as reservas aceitas são canceladas antes do próximo cenário.

---

## Caso de Teste 06 - Alteração de reserva por perfil

**Descrição:** verificar se somente a coordenação pode alterar a reserva de outro professor e se toda alteração gera notificação e registro de auditoria (tabela de decisão perfil × dono da reserva).

**Pré-condições:** existe uma reserva da S102 em 15/09/2026, das 19h00 às 20h40, criada por "prof.carlos".

**Passos:**
1. Entrar com o usuário do cenário.
2. Abrir a reserva de prof.carlos.
3. Alterar o horário para 20h40 às 22h20 e clicar em "Salvar".
4. Entrar como "prof.carlos" e abrir a caixa de notificações.

**Cenário 1 - Professor altera a própria reserva:**
- Dados de Teste:
  - Usuário: prof.carlos
- Resultado Esperado:
  - A alteração é salva.
  - A auditoria registra a alteração com os valores antigo e novo.

**Cenário 2 - Professor tenta alterar a reserva de outro professor:**
- Dados de Teste:
  - Usuário: prof.marina
- Resultado Esperado:
  - O sistema exibe "Sem permissão para alterar esta reserva".
  - A reserva permanece inalterada.
  - A tentativa negada é registrada na auditoria.

**Cenário 3 - Coordenação altera a reserva de um professor:**
- Dados de Teste:
  - Usuário: coord.paula
- Resultado Esperado:
  - A alteração é salva.
  - prof.carlos recebe a notificação "Sua reserva da S102 em 15/09/2026 foi alterada".
  - A auditoria registra usuário, data e hora e os valores antigo e novo.

**Pós-condições:** a reserva é restaurada para 19h00 às 20h40 antes do próximo cenário.

---

## Caso de Teste 07 - Cancelamento de reserva

**Descrição:** verificar se o cancelamento libera o horário, registra o histórico e gera notificação, e se um professor não pode cancelar a reserva de outro.

**Pré-condições:** existe uma reserva da S101 em 16/09/2026, das 19h00 às 20h40, de "prof.carlos".

**Passos:**
1. Entrar com o usuário do cenário.
2. Abrir a reserva e clicar em "Cancelar reserva".
3. Consultar o histórico da reserva e a caixa de notificações de prof.carlos.

**Cenário 1 - Professor cancela a própria reserva:**
- Dados de Teste:
  - Usuário: prof.carlos
- Resultado Esperado:
  - A reserva passa para o status "Cancelada".
  - O histórico registra o cancelamento (usuário, data e hora).
  - É gerada a notificação de cancelamento.

**Cenário 2 - Professor tenta cancelar a reserva de outro professor:**
- Dados de Teste:
  - Usuário: prof.marina
- Resultado Esperado:
  - O sistema exibe "Sem permissão para cancelar esta reserva".
  - A reserva continua ativa.

**Cenário 3 - Reservar o horário liberado pelo cancelamento:**
- Dados de Teste:
  - Após o Cenário 1, prof.marina reserva a S101 em 16/09/2026, das 19h00 às 20h40, para a turma ADS3A
- Resultado Esperado:
  - A nova reserva é aceita.

**Pós-condições:** a S101 fica reservada para prof.marina no horário liberado.

---

## Caso de Teste 08 - Desempenho da busca

**Descrição:** verificar se a busca de salas responde em até 2 segundos.

**Pré-condições:** a base contém 50 salas e 500 reservas; o usuário "prof.carlos" está logado; o DevTools do navegador está aberto na aba "Network".

**Passos:**
1. Acessar a página "Buscar salas".
2. Informar os filtros do cenário e clicar em "Buscar".
3. Medir o tempo até a exibição dos resultados.
4. Repetir 5 vezes.

**Cenário 1 - Busca com resultados:**
- Dados de Teste:
  - Data: 17/09/2026 · Horário: 19h00 às 20h40 · Capacidade mínima: 30
- Resultado Esperado:
  - As 5 buscas respondem em até 2 segundos.

**Cenário 2 - Busca sem resultados:**
- Dados de Teste:
  - Data: 17/09/2026 · Horário: 19h00 às 20h40 · Capacidade mínima: 500
- Resultado Esperado:
  - O sistema exibe "Nenhuma sala encontrada" em até 2 segundos.

**Pós-condições:** nenhuma alteração nos dados.

---

## Caso de Teste 09 - Trilha de auditoria

**Descrição:** verificar se as operações sobre reservas ficam registradas na trilha de auditoria.

**Pré-condições:** o usuário "coord.paula" tem acesso à tela "Auditoria".

**Passos:**
1. Executar a operação do cenário.
2. Entrar como "coord.paula" e abrir a tela "Auditoria".
3. Filtrar pela data de hoje.

**Cenário 1 - Registro de criação de reserva:**
- Dados de Teste:
  - prof.carlos cria uma reserva da S102 em 18/09/2026, das 08h00 às 09h40
- Resultado Esperado:
  - A auditoria mostra a operação "Criação", o usuário prof.carlos, a data e a hora e os dados da reserva.

**Cenário 2 - Registro de tentativa negada:**
- Dados de Teste:
  - prof.marina tenta alterar a reserva do Cenário 1
- Resultado Esperado:
  - A auditoria mostra a operação "Alteração negada", o usuário prof.marina, a data e a hora.

**Pós-condições:** os registros de auditoria não podem ser editados nem excluídos.

---

## Caso de Teste 10 - Acesso por unidade

**Descrição:** verificar se o usuário só acessa as salas das unidades em que está autorizado.

**Pré-condições:** o usuário "prof.lucas" (unidade Norte) está logado.

**Passos:**
1. Executar a ação do cenário.

**Cenário 1 - Busca lista apenas a unidade autorizada:**
- Dados de Teste:
  - Buscar salas disponíveis sem filtro de unidade
- Resultado Esperado:
  - Só aparecem as salas da unidade Norte (N201).

**Cenário 2 - Acesso direto a sala de outra unidade:**
- Dados de Teste:
  - Abrir diretamente pela URL a página de reserva da S101 (unidade Centro)
- Resultado Esperado:
  - O sistema exibe "Acesso não autorizado".
  - Nenhuma reserva é criada.

**Pós-condições:** nenhuma alteração nos dados.

---

## Matriz de rastreabilidade

| Requisito | Casos de Teste |
|---|---|
| RF-01 | CT-01 |
| RF-02 | CT-02 |
| RF-03 | CT-03 |
| RF-04 | CT-04 |
| RF-05 | CT-05 |
| RF-06 | CT-06 |
| RF-07 | CT-07 |
| RF-08 | CT-06, CT-07 |
| RNF-01 | CT-08 |
| RNF-02 | CT-01, CT-06, CT-07, CT-09 |
| RNF-03 | CT-10 |

Todos os requisitos têm ao menos um caso de teste, e os riscos críticos (RS-01 a RS-04) estão entre os casos de prioridade alta.
