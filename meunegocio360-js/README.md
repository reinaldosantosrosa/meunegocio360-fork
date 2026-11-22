<<<<<<< HEAD
# MeuNegocio360 — frontend
=======
<<<<<<< HEAD
# Frontend MeuNegocio360
>>>>>>> feat/rf-be-004-cadastro-de-fornecedor

Frontend do MeuNegocio360 em React 19, Vite e Bootstrap 5.

## Desenvolvimento local

Na raiz do repositório, configure as variáveis conforme `.env.example` e inicie o backend Núcleo. Em seguida:

```powershell
<<<<<<< HEAD
cd meunegocio360-js
=======
=======
# MeuNegocio360 — frontend

Frontend do MeuNegocio360 em React 19, Vite e Bootstrap 5.

## Desenvolvimento local

Na raiz do repositório, configure as variáveis conforme `.env.example` e inicie o backend Núcleo. Em seguida:

```powershell
cd meunegocio360-js
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
>>>>>>> feat/rf-be-004-cadastro-de-fornecedor
npm install
npm run dev
```

<<<<<<< HEAD
O Vite encaminha `/api` para `http://localhost:8080`; o frontend usa a URL relativa `/api/v1` para manter as chamadas na mesma origem. Variáveis `VITE_*` são públicas e não podem conter segredos.
=======
<<<<<<< HEAD
O Vite encaminha `/api` para `http://localhost:8080`. A URL base do frontend deve permanecer relativa (`VITE_API_BASE_URL=/api/v1`); consulte [`.env.example`](./.env.example). Não coloque segredos em variáveis `VITE_*`.

O arquivo `public/_redirects` permite abrir diretamente as rotas da SPA em hospedagem estática. Em produção, configure também o repasse de `/api/*` para a URL do serviço Render, conforme a [seção de implantação](../README.md#72-frontend-no-netlify).
>>>>>>> feat/rf-be-004-cadastro-de-fornecedor

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

O fluxo de login e as convenções de interface estão especificados em [`RF-FE-001.md`](../docs/requisitos/frontend/RF-FE-001.md). Os requisitos gerais estão em [`GERAIS.md`](../docs/requisitos/GERAIS.md).
=======
<<<<<<< HEAD
=======

O fluxo de login e as convenções de interface estão especificados em [`docs/requisitos-fe.md`](../docs/requisitos-fe.md#rf-fe-001--fluxo-de-login-do-usuário). Os requisitos gerais estão em [`docs/requisitos-gerais.md`](../docs/requisitos-gerais.md).
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
>>>>>>> feat/rf-be-004-cadastro-de-fornecedor
