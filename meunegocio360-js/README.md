<<<<<<< HEAD
# Frontend MeuNegocio360

Frontend React com Vite. A tela de login, a restauração de sessão e a página inicial protegida seguem o [RF-FE-001](../docs/requisitos-fe.md#rf-fe-001--fluxo-de-login-do-usuário).

## Desenvolvimento local

Na raiz do repositório, inicie o banco e o backend conforme o [README principal](../README.md#5-primeiros-passos). Em seguida, neste diretório:

```powershell
=======
# MeuNegocio360 — frontend

Frontend do MeuNegocio360 em React 19, Vite e Bootstrap 5.

## Desenvolvimento local

Na raiz do repositório, configure as variáveis conforme `.env.example` e inicie o backend Núcleo. Em seguida:

```powershell
cd meunegocio360-js
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
npm install
npm run dev
```

<<<<<<< HEAD
O Vite encaminha `/api` para `http://localhost:8080`. A URL base do frontend deve permanecer relativa (`VITE_API_BASE_URL=/api/v1`); consulte [`.env.example`](./.env.example). Não coloque segredos em variáveis `VITE_*`.

O arquivo `public/_redirects` permite abrir diretamente as rotas da SPA em hospedagem estática. Em produção, configure também o repasse de `/api/*` para a URL do serviço Render, conforme a [seção de implantação](../README.md#72-frontend-no-netlify).

## Comandos

=======
O Vite encaminha `/api` para `http://localhost:8080`; o frontend usa a URL relativa `/api/v1` para manter as chamadas na mesma origem. Variáveis `VITE_*` são públicas e não podem conter segredos.

## Comandos

>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
```powershell
npm run lint
npm run format
npm run build
```
<<<<<<< HEAD
=======

O fluxo de login e as convenções de interface estão especificados em [`docs/requisitos-fe.md`](../docs/requisitos-fe.md#rf-fe-001--fluxo-de-login-do-usuário). Os requisitos gerais estão em [`docs/requisitos-gerais.md`](../docs/requisitos-gerais.md).
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
