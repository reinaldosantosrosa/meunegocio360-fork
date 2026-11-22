CREATE TABLE fornecedor (
    id BINARY(16) NOT NULL,
    empresa_id BINARY(16) NOT NULL,
    tipo_pessoa VARCHAR(10) NOT NULL,
    nome VARCHAR(150) COLLATE utf8mb4_0900_ai_ci NOT NULL,
    documento VARCHAR(14) NOT NULL,
    email VARCHAR(160) NULL,
    telefone VARCHAR(11) NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_fornecedor PRIMARY KEY (id),
    CONSTRAINT fk_fornecedor_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT uk_fornecedor_empresa_documento UNIQUE (empresa_id, documento),
    INDEX idx_fornecedor_empresa_nome (empresa_id, nome),
    INDEX idx_fornecedor_empresa_ativo (empresa_id, ativo)
);
