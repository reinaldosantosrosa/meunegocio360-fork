# MeuNegocio360 — frontend

Frontend do MeuNegocio360 em React 19, Vite e Bootstrap 5.

## Desenvolvimento local

Na raiz do repositório, configure as variáveis conforme `.env.example` e inicie o backend Núcleo. Em seguida:

```powershell
cd meunegocio360-js
npm install
npm run dev
```

O Vite encaminha `/api` para `http://localhost:8080`; o frontend usa a URL relativa `/api/v1` para manter as chamadas na mesma origem. Variáveis `VITE_*` são públicas e não podem conter segredos.

## Comandos

```powershell
npm run lint
npm run format
npm run build
```

O fluxo de login e as convenções de interface estão especificados em [`RF-FE-001.md`](../docs/requisitos/frontend/RF-FE-001.md). Os requisitos gerais estão em [`GERAIS.md`](../docs/requisitos/GERAIS.md).

## Empresa, navegação e acesso

O fluxo de cadastro inicial, consulta, edição e situação da empresa, assim como
as guardas de rota, está descrito em
[`RF-FE-002.md`](../docs/requisitos/frontend/RF-FE-002.md). O contrato HTTP é
definido por [`RF-BE-002.md`](../docs/requisitos/backend/RF-BE-002.md), e a
paleta e os requisitos de acessibilidade estão em
[`RNF-FE.md`](../docs/requisitos/frontend/RNF-FE.md).

O menu exibe **Início** e **Empresa**. Em telas grandes, a marca fica à esquerda
dos itens; em telas móveis, fica centralizada entre o botão de menu e o ícone
visual de pesquisa, que ainda não executa uma busca.

### Verificações manuais no navegador

Use os usuários de desenvolvimento definidos pelo backend e mantenha os
segredos nos arquivos/variáveis locais, sem registrá-los neste documento.

1. **ADMIN — cadastro e manutenção:** com nenhuma empresa ativa, entre como
   administrador e confirme o redirecionamento para `/empresa/cadastro`.
   Envie o CNPJ inválido `11.222.333/0001-80` e confirme o erro sem requisição;
   depois cadastre com `11.222.333/0001-81`, confira o envio somente de dígitos,
   o aviso de sucesso e o nome na barra. Edite o nome fantasia, confira a
   confirmação de atualização, inative e cancele uma vez; confirme que cancelar
   não altera o estado. Inative novamente, recarregue `/empresa/cadastro`,
   localize a empresa na lista paginada de inativas e reative-a.
2. **OPERADOR_COMERCIAL — empresa ausente:** sem empresa ativa, entre como
   operador comercial. Confirme a mensagem **Aguardando o cadastro da empresa**,
   tente abrir `/` e `/empresa/cadastro` diretamente e verifique que nenhuma
   tela de negócio é exibida e que o cadastro é negado.
3. **OPERADOR_FINANCEIRO — consulta e responsividade:** com empresa ativa, abra
   `/empresa` e confirme que os dados são somente leitura e que não há ações de
   edição ou situação. Em viewport desktop, confirme a marca à esquerda do
   menu; em viewport de 360 px, confirme a marca centralizada entre menu e
   pesquisa visual, navegue por teclado e verifique que não há rolagem
   horizontal.

Com nenhuma empresa ativa, o cadastro usa o endpoint administrativo paginado
`GET /api/v1/empresa/inativas` para localizar empresas históricas e permitir
reativá-las mesmo após recarregar a página. O contrato está documentado em
[`RF-BE-002.md`](../docs/requisitos/backend/RF-BE-002.md).

## Clientes

Cadastro, consulta, busca, filtros, paginação, edição, situação e exclusão de
clientes seguem [`RF-FE-003.md`](../docs/requisitos/frontend/RF-FE-003.md) e o
contrato da API em
[`RF-BE-003.md`](../docs/requisitos/backend/RF-BE-003.md). A lista e o seletor
reutilizável usam busca paginada no servidor; filtros e página permanecem na
URL. A rota de demonstração do `ClienteSelect` existe somente no modo de
desenvolvimento: `/dev/cliente-select`.

### Verificações manuais no navegador

1. **Busca, filtros e paginação:** entre como `OPERADOR_COMERCIAL` ou `ADMIN`,
   abra `/clientes`, pesquise um nome com acentos e parte de um CPF/CNPJ,
   confirme o debounce de 400 ms e que `q` permanece na URL após recarregar.
   Troque Situação entre **Ativos**, **Inativos** e **Todos**, navegue entre
   páginas, e teste busca sem resultados e **Limpar filtros**. Como
   `OPERADOR_FINANCEIRO`, confirme que `/clientes` mostra **Acesso negado**.
2. **Cadastro e edição:** crie pessoa física com CPF válido e pessoa jurídica
   com CNPJ válido; confira a máscara na tela e os dígitos sem máscara nas
   requisições. Tente documento inválido e duplicado, alterne o tipo de pessoa
   com um documento preenchido e confirme que ele é limpo com aviso; depois
   edite nome/telefone e verifique a mensagem de sucesso.
3. **Situação, exclusão e seletor:** inative um cliente e confirme a ação; ele
   deve sair de **Ativos** e aparecer em **Inativos**, de onde pode ser
   reativado. Exclua um cliente sem histórico; para um cliente com histórico,
   confirme que o modal informa o motivo e oferece **Inativar cliente**. Em
   `/dev/cliente-select`, busque por nome/documento e selecione uma sugestão
   ativa com setas e `Enter`; confira também `Escape` e `Tab`.
