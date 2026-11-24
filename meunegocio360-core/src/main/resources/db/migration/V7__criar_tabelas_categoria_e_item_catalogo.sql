CREATE TABLE categoria (
    id BINARY(16) NOT NULL,
    empresa_id BINARY(16) NOT NULL,
    nome VARCHAR(80) COLLATE utf8mb4_0900_ai_ci NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT uk_categoria_empresa_nome UNIQUE (empresa_id, nome)
);

CREATE TABLE item_catalogo (
    id BINARY(16) NOT NULL,
    empresa_id BINARY(16) NOT NULL,
    categoria_id BINARY(16) NULL,
    tipo VARCHAR(10) NOT NULL,
    codigo VARCHAR(40) COLLATE utf8mb4_0900_ai_ci NULL,
    nome VARCHAR(150) COLLATE utf8mb4_0900_ai_ci NOT NULL,
    descricao VARCHAR(500) NULL,
    unidade VARCHAR(6) NOT NULL,
    preco DECIMAL(12,2) NOT NULL,
    estoque_minimo DECIMAL(15,3) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME(6) NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_item_catalogo PRIMARY KEY (id),
    CONSTRAINT fk_item_catalogo_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_item_catalogo_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT uk_item_catalogo_empresa_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT chk_item_catalogo_preco CHECK (preco >= 0),
    CONSTRAINT chk_item_catalogo_estoque_minimo CHECK (estoque_minimo >= 0),
    INDEX idx_item_catalogo_empresa_tipo_ativo (empresa_id, tipo, ativo),
    INDEX idx_item_catalogo_empresa_nome (empresa_id, nome),
    INDEX idx_item_catalogo_categoria (categoria_id)
);
