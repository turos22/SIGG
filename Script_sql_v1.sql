-- Gerado por Oracle SQL Developer Data Modeler 23.1.0.087.0806
--   em:        2026-10-22 19:17:06 BRT
--   site:      Oracle Database 11g
--   tipo:      Oracle Database 11g



-- predefined type, no DDL - MDSYS.SDO_GEOMETRY

-- predefined type, no DDL - XMLTYPE

CREATE TABLE atividade_especial (
    atv_cod   int4 NOT NULL,
    atv_nome  VARCHAR(30) NOT NULL,
    atv_desc  VARCHAR(200),
    atv_data  DATE NOT NULL,
    atv_valor FLOAT
);

ALTER TABLE atividade_especial ADD CONSTRAINT atividade_especial_pk PRIMARY KEY ( atv_cod );

CREATE TABLE categoria_mensalidade (
    cat_mens_cod       int4 NOT NULL,
    cat_mens_valor     FLOAT NOT NULL,
    cat_mens_qtd_irmao int4 NOT NULL
);

ALTER TABLE categoria_mensalidade ADD CONSTRAINT categoria_mensalidade_pk PRIMARY KEY ( cat_mens_cod );

CREATE TABLE chefe (
    mem_cod          int4 NOT NULL,
    chefe_cod        int4 NOT NULL,
    chefe_gestao_ini DATE,
    chefe_gestao_fim DATE,
    tropa_tro_cod    int4 NOT NULL
);

ALTER TABLE chefe ADD CONSTRAINT chefe_pk PRIMARY KEY ( mem_cod );

ALTER TABLE chefe ADD CONSTRAINT chefe_pkv1 UNIQUE ( chefe_cod );

CREATE TABLE contas_receber (
    cr_cod                     int4 NOT NULL,
    cr_valor                   FLOAT NOT NULL,
    cr_desc                    VARCHAR(30),
    atividade_especial_atv_cod int4 NOT NULL,
    mensalidade_mens_cod       int4 NOT NULL
);

ALTER TABLE contas_receber ADD CONSTRAINT contas_receber_pk PRIMARY KEY ( cr_cod );

CREATE TABLE escoteiro (
    mem_cod                            int4 NOT NULL,
    esc_cod                            int4 NOT NULL, 
--  ERROR: Column name length exceeds maximum allowed length(30) 
    categoria_mensalidade_cat_mens_cod int4 NOT NULL,
    escoteiro_esc_cod                  int4 NOT NULL,
    patrulha_pat_cod2                  int4 NOT NULL,
    esc_sit_fin                        int4
);

ALTER TABLE escoteiro ADD CONSTRAINT escoteiro_pk PRIMARY KEY ( mem_cod );

ALTER TABLE escoteiro ADD CONSTRAINT escoteiro_pkv1 UNIQUE ( esc_cod );

CREATE TABLE escoteiro_responsavel (
    responsável_mem_cod int4 NOT NULL,
    escoteiro_mem_cod   int4 NOT NULL
);

ALTER TABLE escoteiro_responsavel ADD CONSTRAINT escoteiro_responsavel_pk PRIMARY KEY ( responsável_mem_cod,
                                                                                        escoteiro_mem_cod );

CREATE TABLE financeiro (
    mem_cod int4 NOT NULL,
    fin_cod int4 NOT NULL
);

ALTER TABLE financeiro ADD CONSTRAINT financeiro_pk PRIMARY KEY ( mem_cod );

ALTER TABLE financeiro ADD CONSTRAINT financeiro_fin_cod_un UNIQUE ( fin_cod );

CREATE TABLE frequencia (
    freq_cod                   int4 NOT NULL,
    freq_data                  DATE NOT NULL,
    freq_status                VARCHAR(1) NOT NULL,
    escoteiro_esc_cod          int4 NOT NULL,
    atividade_especial_atv_cod int4 NOT NULL
);

ALTER TABLE frequencia ADD CONSTRAINT frequencia_pk PRIMARY KEY ( freq_cod );

CREATE TABLE membro (
    mem_cod        int4 NOT NULL,
    mem_nome       VARCHAR(30) NOT NULL,
    mem_dt_nascto  DATE NOT NULL,
    mem_cpf        VARCHAR(11) NOT NULL,
    mem_rg         VARCHAR(10) NOT NULL,
    mem_cep        VARCHAR(8) NOT NULL,
    mem_end        VARCHAR(50) NOT NULL,
    mem_end_num    int4 NOT NULL,
    mem_end_bairro VARCHAR(50) NOT NULL,
    mem_cidade     VARCHAR(50) NOT NULL,
    mem_estado     VARCHAR(30) NOT NULL,
    mem_tel        VARCHAR(11),
    mem_email      VARCHAR(40),
    mem_dt_ini     DATE NOT NULL,
    mem_dt_fim     DATE
);

ALTER TABLE membro ADD CONSTRAINT membro_pk PRIMARY KEY ( mem_cod );

CREATE TABLE mensalidade (
    mens_cod                           int4 NOT NULL,
    mens_mes                           int4 NOT NULL,
    mens_ano                           int4 NOT NULL,
    mens_dt_lancto                     DATE NOT NULL,
    mens_valor                         FLOAT NOT NULL,
    escoteiro_esc_cod                  int4 NOT NULL, 
--  ERROR: Column name length exceeds maximum allowed length(30) 
    categoria_mensalidade_cat_mens_cod int4 NOT NULL
);

ALTER TABLE mensalidade ADD CONSTRAINT mensalidade_pk PRIMARY KEY ( mens_cod );

CREATE TABLE pagamento (
    pg_cod                int4 NOT NULL,
    pg_dt_pagto           DATE,
    pg_dt_vencto          DATE NOT NULL,
    pg_desc               VARCHAR(30) NOT NULL,
    tipo_pagamento_tp_cod int4 NOT NULL,
    contas_receber_cr_cod int4 NOT NULL,
    pg_valor_pago         FLOAT NOT NULL
);

ALTER TABLE pagamento ADD CONSTRAINT pagamento_pk PRIMARY KEY ( pg_cod );

CREATE TABLE patrulha (
    pat_cod          int4 NOT NULL,
    pat_nome         VARCHAR(20) NOT NULL,
    pat_lider_cod    int4 NOT NULL,
    pat_sublider_cod int4,
    tropa_tro_cod    int4 NOT NULL
);

ALTER TABLE patrulha ADD CONSTRAINT patrulha_pk PRIMARY KEY ( pat_cod );

CREATE TABLE presidente (
    mem_cod         int4 NOT NULL,
    pres_cod        int4 NOT NULL,
    pres_gestao_ini DATE NOT NULL,
    pres_gestao_fim DATE NOT NULL
);

ALTER TABLE presidente ADD CONSTRAINT presidente_pk PRIMARY KEY ( mem_cod );

ALTER TABLE presidente ADD CONSTRAINT presidente_pkv1 UNIQUE ( pres_cod );

CREATE TABLE responsável (
    mem_cod  int4 NOT NULL,
    resp_cod int4 NOT NULL
);

ALTER TABLE responsável ADD CONSTRAINT responsável_pk PRIMARY KEY ( mem_cod );

ALTER TABLE responsável ADD CONSTRAINT responsável_pkv1 UNIQUE ( resp_cod );

CREATE TABLE tipo_pagamento (
    tp_cod  int4 NOT NULL,
    tp_desc VARCHAR(30) NOT NULL
);

ALTER TABLE tipo_pagamento ADD CONSTRAINT tipo_pagamento_pk PRIMARY KEY ( tp_cod );

CREATE TABLE tropa (
    tro_cod  int4 NOT NULL,
    tro_nome VARCHAR(20) NOT NULL
);

ALTER TABLE tropa ADD CONSTRAINT tropa_pk PRIMARY KEY ( tro_cod );

ALTER TABLE chefe
    ADD CONSTRAINT chefe_escoteiro_fk FOREIGN KEY ( mem_cod )
        REFERENCES escoteiro ( mem_cod );

ALTER TABLE chefe
    ADD CONSTRAINT chefe_tropa_fk FOREIGN KEY ( tropa_tro_cod )
        REFERENCES tropa ( tro_cod );

--  ERROR: FK name length exceeds maximum allowed length(30) 
ALTER TABLE contas_receber
    ADD CONSTRAINT contas_receber_atividade_especial_fk FOREIGN KEY ( atividade_especial_atv_cod )
        REFERENCES atividade_especial ( atv_cod );

ALTER TABLE contas_receber
    ADD CONSTRAINT contas_receber_mensalidade_fk FOREIGN KEY ( mensalidade_mens_cod )
        REFERENCES mensalidade ( mens_cod );

--  ERROR: FK name length exceeds maximum allowed length(30) 
ALTER TABLE escoteiro
    ADD CONSTRAINT escoteiro_categoria_mensalidade_fk FOREIGN KEY ( categoria_mensalidade_cat_mens_cod )
        REFERENCES categoria_mensalidade ( cat_mens_cod );

ALTER TABLE escoteiro
    ADD CONSTRAINT escoteiro_escoteiro_fk FOREIGN KEY ( escoteiro_esc_cod )
        REFERENCES escoteiro ( esc_cod );

ALTER TABLE escoteiro
    ADD CONSTRAINT escoteiro_membro_fk FOREIGN KEY ( mem_cod )
        REFERENCES membro ( mem_cod );

ALTER TABLE escoteiro
    ADD CONSTRAINT escoteiro_patrulha_fkv2 FOREIGN KEY ( patrulha_pat_cod2 )
        REFERENCES patrulha ( pat_cod );

--  ERROR: FK name length exceeds maximum allowed length(30) 
ALTER TABLE escoteiro_responsavel
    ADD CONSTRAINT escoteiro_responsavel_escoteiro_fk FOREIGN KEY ( escoteiro_mem_cod )
        REFERENCES escoteiro ( mem_cod );

--  ERROR: FK name length exceeds maximum allowed length(30) 
ALTER TABLE escoteiro_responsavel
    ADD CONSTRAINT escoteiro_responsavel_responsável_fk FOREIGN KEY ( responsável_mem_cod )
        REFERENCES responsável ( mem_cod );

ALTER TABLE financeiro
    ADD CONSTRAINT financeiro_membro_fk FOREIGN KEY ( mem_cod )
        REFERENCES membro ( mem_cod );

--  ERROR: FK name length exceeds maximum allowed length(30) 
ALTER TABLE frequencia
    ADD CONSTRAINT frequencia_atividade_especial_fk FOREIGN KEY ( atividade_especial_atv_cod )
        REFERENCES atividade_especial ( atv_cod );

ALTER TABLE frequencia
    ADD CONSTRAINT frequencia_escoteiro_fk FOREIGN KEY ( escoteiro_esc_cod )
        REFERENCES escoteiro ( esc_cod );

--  ERROR: FK name length exceeds maximum allowed length(30) 
ALTER TABLE mensalidade
    ADD CONSTRAINT mensalidade_categoria_mensalidade_fk FOREIGN KEY ( categoria_mensalidade_cat_mens_cod )
        REFERENCES categoria_mensalidade ( cat_mens_cod );

ALTER TABLE mensalidade
    ADD CONSTRAINT mensalidade_escoteiro_fk FOREIGN KEY ( escoteiro_esc_cod )
        REFERENCES escoteiro ( esc_cod );

ALTER TABLE pagamento
    ADD CONSTRAINT pagamento_contas_receber_fk FOREIGN KEY ( contas_receber_cr_cod )
        REFERENCES contas_receber ( cr_cod );

ALTER TABLE pagamento
    ADD CONSTRAINT pagamento_tipo_pagamento_fk FOREIGN KEY ( tipo_pagamento_tp_cod )
        REFERENCES tipo_pagamento ( tp_cod );

ALTER TABLE patrulha
    ADD CONSTRAINT patrulha_tropa_fk FOREIGN KEY ( tropa_tro_cod )
        REFERENCES tropa ( tro_cod );

ALTER TABLE presidente
    ADD CONSTRAINT presidente_escoteiro_fk FOREIGN KEY ( mem_cod )
        REFERENCES escoteiro ( mem_cod );

ALTER TABLE responsável
    ADD CONSTRAINT responsável_membro_fk FOREIGN KEY ( mem_cod )
        REFERENCES membro ( mem_cod );
