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
