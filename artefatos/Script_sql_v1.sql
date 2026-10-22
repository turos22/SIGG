-- ============================================================================
-- SIGG - Sistema Integrado de Gestão Guayporé (Grupo Escoteiro Guayporé 186/SP)
-- DUMP PARA AMBIENTE DE DESENVOLVIMENTO  (schema + dados fictícios)
--
-- SGBD alvo : PostgreSQL 15+   (conforme ERS, seção 3.1.3)
-- Origem    : Script_sql_v1.sql (Oracle SQL Developer Data Modeler)
-- Data-base : 22/10/2026  (os dados de mensalidade/frequência vão de jan a out/2026)
-- Encoding  : UTF-8  (há identificadores com acento: "responsável")
--
-- Como carregar:
--   createdb sigg_dev
--   psql -d sigg_dev -v ON_ERROR_STOP=1 -f dump_dev_sigg.sql
-- O script é re-executável: ele derruba as tabelas antes de recriá-las.
--
-- ATENÇÃO: todos os dados são FICTÍCIOS (nomes, CPFs com dígito verificador
-- válido, e-mails @example.com, telefones numa faixa fictícia 189999xxxxx).
-- Mesmo assim, desative/mocke as integrações externas (WhatsApp Business e
-- Paxtu) no ambiente de desenvolvimento.
--
-- ----------------------------------------------------------------------------
-- CONVENÇÕES ADOTADAS NOS DADOS (o modelo não define estes pontos)
-- ----------------------------------------------------------------------------
-- * Códigos usam faixas distintas para evitar confundir JOINs:
--     mem_cod 1..N | esc_cod 2001.. | chefe_cod 3001.. | pres_cod 4001..
--     fin_cod 5001.. | resp_cod 6001..
--   (mensalidade e frequencia referenciam esc_cod, NÃO mem_cod.)
-- * Chefes e presidentes também são linhas de "escoteiro" (FK do modelo). Eles
--   ficam em patrulhas "Chefia ..." (pat_cod 7..9) e na categoria 5 (isento).
-- * categoria_mensalidade: cat_mens_qtd_irmao = nº de irmãos do escoteiro no
--   grupo (3 = "3 ou mais"). Categoria 5 = isento (voluntários/diretoria).
-- * escoteiro.escoteiro_esc_cod (NOT NULL, auto-referência) = esc_cod do ANJO
--   que apadrinha o escoteiro. Quem não tem Anjo aponta para si mesmo.
-- * escoteiro.esc_sit_fin: 0 = em dia, 1 = inadimplente (marcado pelo financeiro).
-- * patrulha.pat_lider_cod / pat_sublider_cod guardam esc_cod (o modelo não
--   declara FK para essas colunas).
-- * frequencia.freq_status: 'P' presente | 'F' falta | 'J' falta justificada.
-- * frequencia e contas_receber exigem uma atividade_especial (NOT NULL):
--     - cada reunião de sábado é uma atividade_especial com atv_valor NULL;
--     - atv_cod 1 ("Mensalidade (sem atividade)") é um registro técnico usado
--       nas contas_receber de mensalidade.
-- * contas_receber exige também uma mensalidade: a cobrança de uma atividade
--   paga fica atrelada à mensalidade do mês da atividade (ou do último mês
--   lançado, no caso de atividades futuras).
-- * pagamento = parcela/baixa da conta a receber:
--     pg_dt_pagto NULL  -> ainda não pago (pg_valor_pago = 0)
--     pago a menor      -> saldo = cr_valor - SUM(pg_valor_pago)
--     tp_cod 4          -> mensalidade quitada pelo Anjo (apadrinhamento)
--
-- ----------------------------------------------------------------------------
-- CENÁRIOS DE TESTE EMBUTIDOS
-- ----------------------------------------------------------------------------
-- * Inadimplentes (esc_sit_fin = 1): Davi Pires Santos (ago-out), Matheus
--   Henrique Lima (jul-out), Larissa Moraes Teixeira (set-out).
-- * Só outubro em aberto, ainda não marcados: Isabela e Enzo Mota Rocha, Helena.
-- * Pagamento parcial: Gabriel Alves Oliveira, set/2026 (pagou 40 de 70).
-- * Anjo: Henrique Barbosa Neves apadrinha Rafael Costa e Sofia Pereira
--   (mensalidades quitadas com tp_cod 4).
-- * Ex-membro: Bruno Sérgio Gomes (saiu em 31/08/2026); ex-presidente, ex-chefe
--   e ex-financeiro com mem_dt_fim preenchido.
-- * Irmãos em seções diferentes (família Oliveira, 3 filhos) -> categoria 3.
-- * Atividades pagas: duas já realizadas (com pagamentos), Jamboree Regional
--   com cobranças em aberto a vencer (e 2 já pagas) e Ceia de Natal só com
--   cobranças em aberto.
-- ============================================================================

SET client_encoding = 'UTF8';
BEGIN;

-- ----------------------------------------------------------------------------
-- RESET (apenas ambiente de desenvolvimento!)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS pagamento              CASCADE;
DROP TABLE IF EXISTS contas_receber         CASCADE;
DROP TABLE IF EXISTS mensalidade            CASCADE;
DROP TABLE IF EXISTS frequencia             CASCADE;
DROP TABLE IF EXISTS escoteiro_responsavel  CASCADE;
DROP TABLE IF EXISTS chefe                  CASCADE;
DROP TABLE IF EXISTS presidente             CASCADE;
DROP TABLE IF EXISTS financeiro             CASCADE;
DROP TABLE IF EXISTS escoteiro              CASCADE;
DROP TABLE IF EXISTS responsável            CASCADE;
DROP TABLE IF EXISTS membro                 CASCADE;
DROP TABLE IF EXISTS patrulha               CASCADE;
DROP TABLE IF EXISTS tropa                  CASCADE;
DROP TABLE IF EXISTS atividade_especial     CASCADE;
DROP TABLE IF EXISTS categoria_mensalidade  CASCADE;
DROP TABLE IF EXISTS tipo_pagamento         CASCADE;

-- ----------------------------------------------------------------------------
-- ESTRUTURA (idêntica ao Script_sql_v1.sql; apenas sem os comentários
-- "ERROR ... maximum allowed length(30)", que são limite do Oracle 11g e não
-- se aplicam ao PostgreSQL, cujo limite de identificador é 63 bytes)
-- ----------------------------------------------------------------------------
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

-- Chaves estrangeiras
ALTER TABLE chefe
    ADD CONSTRAINT chefe_escoteiro_fk FOREIGN KEY ( mem_cod )
        REFERENCES escoteiro ( mem_cod );

ALTER TABLE chefe
    ADD CONSTRAINT chefe_tropa_fk FOREIGN KEY ( tropa_tro_cod )
        REFERENCES tropa ( tro_cod );

ALTER TABLE contas_receber
    ADD CONSTRAINT contas_receber_atividade_especial_fk FOREIGN KEY ( atividade_especial_atv_cod )
        REFERENCES atividade_especial ( atv_cod );

ALTER TABLE contas_receber
    ADD CONSTRAINT contas_receber_mensalidade_fk FOREIGN KEY ( mensalidade_mens_cod )
        REFERENCES mensalidade ( mens_cod );

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

ALTER TABLE escoteiro_responsavel
    ADD CONSTRAINT escoteiro_responsavel_escoteiro_fk FOREIGN KEY ( escoteiro_mem_cod )
        REFERENCES escoteiro ( mem_cod );

ALTER TABLE escoteiro_responsavel
    ADD CONSTRAINT escoteiro_responsavel_responsável_fk FOREIGN KEY ( responsável_mem_cod )
        REFERENCES responsável ( mem_cod );

ALTER TABLE financeiro
    ADD CONSTRAINT financeiro_membro_fk FOREIGN KEY ( mem_cod )
        REFERENCES membro ( mem_cod );

ALTER TABLE frequencia
    ADD CONSTRAINT frequencia_atividade_especial_fk FOREIGN KEY ( atividade_especial_atv_cod )
        REFERENCES atividade_especial ( atv_cod );

ALTER TABLE frequencia
    ADD CONSTRAINT frequencia_escoteiro_fk FOREIGN KEY ( escoteiro_esc_cod )
        REFERENCES escoteiro ( esc_cod );

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


-- ----------------------------------------------------------------------
-- DADOS PARA AMBIENTE DE DESENVOLVIMENTO!!!!
-- COPIAR > ABRIR AMBIENTE DE SGBD > COLAR E EXECUTAR
-- ----------------------------------------------------------------------

INSERT INTO tropa (tro_cod, tro_nome) VALUES
    (1, 'Alcateia'),
    (2, 'Tropa Escoteira'),
    (3, 'Tropa Sênior');

INSERT INTO patrulha (pat_cod, pat_nome, pat_lider_cod, pat_sublider_cod, tropa_tro_cod) VALUES
    (1, 'Seis Cinzento', 2021, 2017, 1),
    (2, 'Seis Marrom', 2026, 2011, 1),
    (3, 'Águia', 2007, 2014, 2),
    (4, 'Pantera', 2020, 2024, 2),
    (5, 'Falcão', 2009, 2019, 3),
    (6, 'Jaguar', 2023, NULL, 3),
    (7, 'Chefia Alcateia', 2004, NULL, 1),
    (8, 'Chefia Escoteira', 2005, NULL, 2),
    (9, 'Chefia Sênior', 2006, NULL, 3);

INSERT INTO categoria_mensalidade (cat_mens_cod, cat_mens_valor, cat_mens_qtd_irmao) VALUES
    (1, 90.00, 0),
    (2, 80.00, 1),
    (3, 70.00, 2),
    (4, 60.00, 3),
    (5, 0.00, 0);

INSERT INTO tipo_pagamento (tp_cod, tp_desc) VALUES
    (1, 'Dinheiro'),
    (2, 'PIX'),
    (3, 'Cartão'),
    (4, 'Apadrinhamento (Anjo)');

INSERT INTO membro (mem_cod, mem_nome, mem_dt_nascto, mem_cpf, mem_rg, mem_cep, mem_end, mem_end_num, mem_end_bairro, mem_cidade, mem_estado, mem_tel, mem_email, mem_dt_ini, mem_dt_fim) VALUES
    (1, 'Sandra Regina Pacheco', '1968-04-12', '56871710370', '581750767', '19025080', 'Rua Antônio Marcondes', 140, 'Jardim Paulista', 'Presidente Prudente', 'SP', '18999900001', 'sandra.pacheco@example.com', '2015-03-07', '2025-06-30'),
    (2, 'Cláudio Roberto Menezes', '1971-09-03', '72918514160', '118239008', '19030100', 'Avenida Joaquim Constantino', 1900, 'Vila Furquim', 'Presidente Prudente', 'SP', '18999900002', 'claudio.menezes@example.com', '2012-02-04', NULL),
    (3, 'Wagner Luiz Tavares', '1979-06-22', '84031264178', '684378685', '19015250', 'Rua Mato Grosso', 312, 'Vila Euclides', 'Presidente Prudente', 'SP', '18999900003', 'wagner.tavares@example.com', '2018-03-03', '2025-12-31'),
    (4, 'Teresa Cristina Vilela', '1983-11-15', '88216169329', '256214494', '19050210', 'Rua Bahia', 870, 'Jardim Bongiovani', 'Presidente Prudente', 'SP', '18999900004', 'teresa.vilela@example.com', '2021-02-06', NULL),
    (5, 'Anderson Moreira Lopes', '1990-08-30', '78280164766', '510244771', '19013300', 'Rua Paraná', 45, 'Vila Marcondes', 'Presidente Prudente', 'SP', '18999900005', 'anderson.lopes@example.com', '2019-03-02', '2023-01-31'),
    (6, 'Renata Cavalcante Dias', '1992-02-14', '80891222138', '052000999', '19020150', 'Rua Goiás', 1203, 'Vila Formosa', 'Presidente Prudente', 'SP', '18999900006', 'renata.dias@example.com', '2022-02-05', NULL),
    (7, 'Felipe Andrade Souza', '1994-12-09', '86832984189', '039748628', '19010320', 'Rua Espírito Santo', 678, 'Vila Nova', 'Presidente Prudente', 'SP', '18999900007', 'felipe.souza@example.com', '2020-03-07', NULL),
    (8, 'Henrique Barbosa Neves', '1989-05-18', '10689770944', '583265910', '19015400', 'Avenida Cel. José Soares Marcondes', 2750, 'Jardim Aviação', 'Presidente Prudente', 'SP', '18999900008', 'henrique.neves@example.com', '2017-03-04', NULL),
    (9, 'Carla Ramos Silva', '1982-05-17', '76088162015', '204416027', '19015120', 'Rua Tenente Nicolau Maffei', 215, 'Vila Euclides', 'Presidente Prudente', 'SP', '18999900009', 'carla.silva@example.com', '2022-03-05', NULL),
    (10, 'Marcos Antônio Silva', '1980-11-02', '88594886721', '389911431', '19015120', 'Rua Tenente Nicolau Maffei', 215, 'Vila Euclides', 'Presidente Prudente', 'SP', '18999900010', 'marcos.silva@example.com', '2022-03-05', NULL),
    (11, 'Pedro Silva Ramos', '2013-03-14', '64045110534', '299712242', '19015120', 'Rua Tenente Nicolau Maffei', 215, 'Vila Euclides', 'Presidente Prudente', 'SP', '18999900011', NULL, '2022-03-05', NULL),
    (12, 'Laura Silva Ramos', '2017-08-02', '13493162022', '234953302', '19015120', 'Rua Tenente Nicolau Maffei', 215, 'Vila Euclides', 'Presidente Prudente', 'SP', NULL, NULL, '2024-03-02', NULL),
    (13, 'Fernanda Alves Oliveira', '1981-01-23', '96149392570', '572292646', '19050010', 'Avenida Washington Luís', 1840, 'Jardim Bongiovani', 'Presidente Prudente', 'SP', '18999900013', 'fernanda.oliveira@example.com', '2020-03-07', NULL),
    (14, 'Lucas Alves Oliveira', '2010-05-21', '20461756625', '239597180', '19050010', 'Avenida Washington Luís', 1840, 'Jardim Bongiovani', 'Presidente Prudente', 'SP', '18999900014', 'lucas.oliveira@example.com', '2020-03-07', NULL),
    (15, 'Mariana Alves Oliveira', '2014-11-30', '63498727702', '085300860', '19050010', 'Avenida Washington Luís', 1840, 'Jardim Bongiovani', 'Presidente Prudente', 'SP', NULL, NULL, '2022-03-05', NULL),
    (16, 'Gabriel Alves Oliveira', '2018-02-09', '53400718556', '475350107', '19050010', 'Avenida Washington Luís', 1840, 'Jardim Bongiovani', 'Presidente Prudente', 'SP', NULL, NULL, '2025-02-08', NULL),
    (17, 'Roberto Carlos Santos', '1984-07-09', '27208949158', '660669405', '19013050', 'Rua Siqueira Campos', 602, 'Vila Marcondes', 'Presidente Prudente', 'SP', '18999900017', 'roberto.santos@example.com', '2024-03-02', NULL),
    (18, 'Juliana Pires Santos', '1986-03-30', '71101854600', '329699321', '19013050', 'Rua Siqueira Campos', 602, 'Vila Marcondes', 'Presidente Prudente', 'SP', '18999900018', 'juliana.santos@example.com', '2024-03-02', NULL),
    (19, 'Davi Pires Santos', '2017-04-17', '90312934823', '864514954', '19013050', 'Rua Siqueira Campos', 602, 'Vila Marcondes', 'Presidente Prudente', 'SP', NULL, NULL, '2024-03-02', NULL),
    (20, 'Patrícia Lúcia Almeida', '1983-12-12', '15884635591', '841320624', '19020340', 'Rua Quintino Bocaiúva', 77, 'Vila Formosa', 'Presidente Prudente', 'SP', '18999900020', 'patricia.almeida@example.com', '2021-03-06', NULL),
    (21, 'Beatriz Almeida', '2011-01-19', '07017858700', '121746141', '19020340', 'Rua Quintino Bocaiúva', 77, 'Vila Formosa', 'Presidente Prudente', 'SP', '18999900021', 'beatriz.almeida@example.com', '2021-03-06', NULL),
    (22, 'Thiago Almeida', '2013-09-05', '50098874870', '663070476', '19020340', 'Rua Quintino Bocaiúva', 77, 'Vila Formosa', 'Presidente Prudente', 'SP', '18999900022', NULL, '2022-03-05', NULL),
    (23, 'Ricardo Augusto Costa', '1978-02-28', '90373614390', '649836557', '19060080', 'Avenida Brasil', 950, 'Parque Cedral', 'Presidente Prudente', 'SP', '18999900023', 'ricardo.costa@example.com', '2023-03-04', NULL),
    (24, 'Rafael Augusto Costa', '2012-07-25', '11506264107', '887343773', '19060080', 'Avenida Brasil', 950, 'Parque Cedral', 'Presidente Prudente', 'SP', '18999900024', 'rafael.costa@example.com', '2023-03-04', NULL),
    (25, 'Aline Cristina Pereira', '1990-06-14', '09949626145', '341459061', '19050680', 'Rua José Bongiovani', 1320, 'Cidade Universitária', 'Presidente Prudente', 'SP', '18999900025', 'aline.pereira@example.com', '2025-05-03', NULL),
    (26, 'Sofia Cristina Pereira', '2018-10-11', '62796025594', '588684935', '19050680', 'Rua José Bongiovani', 1320, 'Cidade Universitária', 'Presidente Prudente', 'SP', NULL, NULL, '2025-05-03', NULL),
    (27, 'Marcelo Henrique Rocha', '1985-08-21', '88682990385', '635188521', '19025210', 'Rua Ângelo Rotta', 455, 'Jardim Paulista', 'Presidente Prudente', 'SP', '18999900027', 'marcelo.rocha@example.com', '2023-07-01', NULL),
    (28, 'Tatiane Mota Rocha', '1987-10-05', '51738953734', '460429398', '19025210', 'Rua Ângelo Rotta', 455, 'Jardim Paulista', 'Presidente Prudente', 'SP', '18999900028', 'tatiane.rocha@example.com', '2023-07-01', NULL),
    (29, 'Isabela Mota Rocha', '2016-12-03', '05563219283', '284920731', '19025210', 'Rua Ângelo Rotta', 455, 'Jardim Paulista', 'Presidente Prudente', 'SP', NULL, NULL, '2023-07-01', NULL),
    (30, 'Enzo Mota Rocha', '2019-03-28', '86887229358', '671399867', '19025210', 'Rua Ângelo Rotta', 455, 'Jardim Paulista', 'Presidente Prudente', 'SP', NULL, NULL, '2026-02-14', NULL),
    (31, 'Cláudia Regina Lima', '1979-04-04', '70288133471', '098456648', '19010100', 'Avenida Manoel Goulart', 2100, 'Vila Santa Helena', 'Presidente Prudente', 'SP', '18999900031', 'claudia.lima@example.com', '2021-03-06', NULL),
    (32, 'Matheus Henrique Lima', '2010-09-14', '51325754048', '967258358', '19010100', 'Avenida Manoel Goulart', 2100, 'Vila Santa Helena', 'Presidente Prudente', 'SP', '18999900032', 'matheus.lima@example.com', '2021-03-06', NULL),
    (33, 'Sérgio Luiz Fernandes', '1977-09-18', '63138643523', '720238390', '19015300', 'Rua Dr. Gurgel', 318, 'Jardim Aviação', 'Presidente Prudente', 'SP', '18999900033', 'sergio.fernandes@example.com', '2023-03-04', NULL),
    (34, 'Helena Souza Fernandes', '2013-06-08', '86273287066', '311939727', '19015300', 'Rua Dr. Gurgel', 318, 'Jardim Aviação', 'Presidente Prudente', 'SP', '18999900034', NULL, '2023-03-04', NULL),
    (35, 'Daniela Prado Martins', '1988-02-11', '14900024724', '772160549', '19030400', 'Rua Joaquim Constantino', 89, 'Vila Furquim', 'Presidente Prudente', 'SP', '18999900035', 'daniela.martins@example.com', '2023-03-04', NULL),
    (36, 'Arthur Prado Martins', '2016-05-20', '75746900303', '268077087', '19030400', 'Rua Joaquim Constantino', 89, 'Vila Furquim', 'Presidente Prudente', 'SP', NULL, NULL, '2023-03-04', NULL),
    (37, 'Valentina Prado Martins', '2018-07-01', '91746683155', '767371396', '19030400', 'Rua Joaquim Constantino', 89, 'Vila Furquim', 'Presidente Prudente', 'SP', NULL, NULL, '2026-01-10', NULL),
    (38, 'Eduardo José Barros', '1975-10-30', '95974231970', '445260764', '19010200', 'Rua Tiradentes', 1150, 'Vila Nova', 'Presidente Prudente', 'SP', '18999900038', 'eduardo.barros@example.com', '2019-03-02', NULL),
    (39, 'João Pedro Barros', '2009-11-12', '61961062283', '178520610', '19010200', 'Rua Tiradentes', 1150, 'Vila Nova', 'Presidente Prudente', 'SP', '18999900039', 'joao.barros@example.com', '2019-03-02', NULL),
    (40, 'Vanessa Moraes Teixeira', '1989-05-25', '42888491524', '926605029', '19031150', 'Rua Sete de Setembro', 734, 'Jardim Guanabara', 'Presidente Prudente', 'SP', '18999900040', 'vanessa.teixeira@example.com', '2024-03-02', NULL),
    (41, 'Larissa Moraes Teixeira', '2014-02-17', '59710710338', '600571358', '19031150', 'Rua Sete de Setembro', 734, 'Jardim Guanabara', 'Presidente Prudente', 'SP', '18999900041', NULL, '2024-03-02', NULL),
    (42, 'Paulo Sérgio Gomes', '1980-01-08', '49050096930', '839691808', '19061200', 'Alameda Santa Catarina', 24, 'Parque Residencial Cambuci', 'Presidente Prudente', 'SP', '18999900042', 'paulo.gomes@example.com', '2022-03-05', NULL),
    (43, 'Bruno Sérgio Gomes', '2012-04-10', '74912044490', '404325248', '19061200', 'Alameda Santa Catarina', 24, 'Parque Residencial Cambuci', 'Presidente Prudente', 'SP', '18999900043', 'bruno.gomes@example.com', '2022-03-05', '2026-08-31'),
    (44, 'Camila Torres Nunes', '1991-09-02', '23465702735', '574651745', '19013200', 'Rua Rosa Pereira', 560, 'Vila Marcondes', 'Presidente Prudente', 'SP', '18999900044', 'camila.nunes@example.com', '2026-03-07', NULL),
    (45, 'Miguel Torres Nunes', '2017-01-26', '81482579650', '858848304', '19013200', 'Rua Rosa Pereira', 560, 'Vila Marcondes', 'Presidente Prudente', 'SP', NULL, NULL, '2026-03-07', NULL);

INSERT INTO escoteiro (mem_cod, esc_cod, categoria_mensalidade_cat_mens_cod, escoteiro_esc_cod, patrulha_pat_cod2, esc_sit_fin) VALUES
    (1, 2001, 5, 2001, 8, 0),
    (2, 2002, 5, 2002, 8, 0),
    (5, 2003, 5, 2003, 8, 0),
    (6, 2004, 5, 2004, 7, 0),
    (7, 2005, 5, 2005, 8, 0),
    (8, 2006, 5, 2006, 9, 0),
    (11, 2007, 2, 2007, 3, 0),
    (12, 2008, 2, 2008, 1, 0),
    (14, 2009, 3, 2009, 5, 0),
    (15, 2010, 3, 2010, 4, 0),
    (16, 2011, 3, 2011, 2, 0),
    (19, 2012, 1, 2012, 1, 1),
    (21, 2013, 2, 2013, 6, 0),
    (22, 2014, 2, 2014, 3, 0),
    (24, 2015, 1, 2006, 3, 0),
    (26, 2016, 1, 2006, 2, 0),
    (29, 2017, 2, 2017, 1, 0),
    (30, 2018, 2, 2018, 2, 0),
    (32, 2019, 1, 2019, 5, 1),
    (34, 2020, 1, 2020, 4, 0),
    (36, 2021, 2, 2021, 1, 0),
    (37, 2022, 2, 2022, 2, 0),
    (39, 2023, 1, 2023, 6, 0),
    (41, 2024, 1, 2024, 4, 1),
    (43, 2025, 1, 2025, 3, 0),
    (45, 2026, 1, 2026, 2, 0);

INSERT INTO responsável (mem_cod, resp_cod) VALUES
    (9, 6001),
    (10, 6002),
    (13, 6003),
    (17, 6004),
    (18, 6005),
    (20, 6006),
    (23, 6007),
    (25, 6008),
    (27, 6009),
    (28, 6010),
    (31, 6011),
    (33, 6012),
    (35, 6013),
    (38, 6014),
    (40, 6015),
    (42, 6016),
    (44, 6017);

INSERT INTO escoteiro_responsavel (responsável_mem_cod, escoteiro_mem_cod) VALUES
    (9, 11),
    (10, 11),
    (9, 12),
    (10, 12),
    (13, 14),
    (13, 15),
    (13, 16),
    (17, 19),
    (18, 19),
    (20, 21),
    (20, 22),
    (23, 24),
    (25, 26),
    (27, 29),
    (28, 29),
    (27, 30),
    (28, 30),
    (31, 32),
    (33, 34),
    (35, 36),
    (35, 37),
    (38, 39),
    (40, 41),
    (42, 43),
    (44, 45);

INSERT INTO chefe (mem_cod, chefe_cod, chefe_gestao_ini, chefe_gestao_fim, tropa_tro_cod) VALUES
    (5, 3001, '2020-02-01', '2022-12-31', 2),
    (6, 3002, '2024-02-03', NULL, 1),
    (7, 3003, '2023-02-04', NULL, 2),
    (8, 3004, '2025-02-01', NULL, 3);

INSERT INTO presidente (mem_cod, pres_cod, pres_gestao_ini, pres_gestao_fim) VALUES
    (1, 4001, '2022-01-01', '2024-12-31'),
    (2, 4002, '2025-01-01', '2027-12-31');

INSERT INTO financeiro (mem_cod, fin_cod) VALUES
    (3, 5001),
    (4, 5002);

INSERT INTO atividade_especial (atv_cod, atv_nome, atv_desc, atv_data, atv_valor) VALUES
    (1, 'Mensalidade (sem atividade)', 'Registro técnico: atividade usada em contas_receber de mensalidade (FK obrigatória no modelo).', '2026-01-01', NULL),
    (2, 'Reunião Semanal 01/08', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-08-01', NULL),
    (3, 'Reunião Semanal 08/08', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-08-08', NULL),
    (4, 'Reunião Semanal 15/08', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-08-15', NULL),
    (5, 'Reunião Semanal 22/08', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-08-22', NULL),
    (6, 'Passeio ao Zoológico', 'Passeio recreativo ao zoológico municipal, com almoço incluso.', '2026-08-23', 45.00),
    (7, 'Reunião Semanal 29/08', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-08-29', NULL),
    (8, 'Reunião Semanal 05/09', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-09-05', NULL),
    (9, 'Reunião Semanal 12/09', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-09-12', NULL),
    (10, 'Acampamento de Primavera', 'Acampamento de fim de semana (19 e 20/09) com todas as seções.', '2026-09-19', 120.00),
    (11, 'Reunião Semanal 26/09', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-09-26', NULL),
    (12, 'Reunião Semanal 03/10', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-10-03', NULL),
    (13, 'Reunião Semanal 10/10', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-10-10', NULL),
    (14, 'Reunião Semanal 17/10', 'Reunião ordinária do Grupo na Casa do Escoteiro (sábado à tarde).', '2026-10-17', NULL),
    (15, 'Jamboree Regional 2026', 'Encontro regional de escoteiros. Inscrições abertas até 06/11.', '2026-11-14', 250.00),
    (16, 'Ceia de Natal do Grupo', 'Confraternização de fim de ano do Grupo.', '2026-12-12', 35.00);

INSERT INTO frequencia (freq_cod, freq_data, freq_status, escoteiro_esc_cod, atividade_especial_atv_cod) VALUES
    (1, '2026-08-01', 'P', 2007, 2),
    (2, '2026-08-01', 'P', 2008, 2),
    (3, '2026-08-01', 'P', 2009, 2),
    (4, '2026-08-01', 'P', 2010, 2),
    (5, '2026-08-01', 'P', 2011, 2),
    (6, '2026-08-01', 'P', 2012, 2),
    (7, '2026-08-01', 'F', 2013, 2),
    (8, '2026-08-01', 'P', 2014, 2),
    (9, '2026-08-01', 'P', 2015, 2),
    (10, '2026-08-01', 'P', 2016, 2),
    (11, '2026-08-01', 'P', 2017, 2),
    (12, '2026-08-01', 'P', 2018, 2),
    (13, '2026-08-01', 'F', 2019, 2),
    (14, '2026-08-01', 'P', 2020, 2),
    (15, '2026-08-01', 'P', 2021, 2),
    (16, '2026-08-01', 'P', 2022, 2),
    (17, '2026-08-01', 'P', 2023, 2),
    (18, '2026-08-01', 'P', 2024, 2),
    (19, '2026-08-01', 'P', 2025, 2),
    (20, '2026-08-01', 'P', 2026, 2),
    (21, '2026-08-01', 'P', 2004, 2),
    (22, '2026-08-01', 'P', 2005, 2),
    (23, '2026-08-01', 'P', 2006, 2),
    (24, '2026-08-08', 'P', 2007, 3),
    (25, '2026-08-08', 'F', 2008, 3),
    (26, '2026-08-08', 'P', 2009, 3),
    (27, '2026-08-08', 'P', 2010, 3),
    (28, '2026-08-08', 'P', 2011, 3),
    (29, '2026-08-08', 'P', 2012, 3),
    (30, '2026-08-08', 'P', 2013, 3),
    (31, '2026-08-08', 'P', 2014, 3),
    (32, '2026-08-08', 'P', 2015, 3),
    (33, '2026-08-08', 'P', 2016, 3),
    (34, '2026-08-08', 'P', 2017, 3),
    (35, '2026-08-08', 'P', 2018, 3),
    (36, '2026-08-08', 'F', 2019, 3),
    (37, '2026-08-08', 'J', 2020, 3),
    (38, '2026-08-08', 'P', 2021, 3),
    (39, '2026-08-08', 'P', 2022, 3),
    (40, '2026-08-08', 'F', 2023, 3),
    (41, '2026-08-08', 'F', 2024, 3),
    (42, '2026-08-08', 'P', 2025, 3),
    (43, '2026-08-08', 'P', 2026, 3),
    (44, '2026-08-08', 'P', 2004, 3),
    (45, '2026-08-08', 'P', 2005, 3),
    (46, '2026-08-08', 'P', 2006, 3),
    (47, '2026-08-15', 'F', 2007, 4),
    (48, '2026-08-15', 'P', 2008, 4),
    (49, '2026-08-15', 'P', 2009, 4),
    (50, '2026-08-15', 'P', 2010, 4);

INSERT INTO frequencia (freq_cod, freq_data, freq_status, escoteiro_esc_cod, atividade_especial_atv_cod) VALUES
    (51, '2026-08-15', 'P', 2011, 4),
    (52, '2026-08-15', 'P', 2012, 4),
    (53, '2026-08-15', 'P', 2013, 4),
    (54, '2026-08-15', 'P', 2014, 4),
    (55, '2026-08-15', 'P', 2015, 4),
    (56, '2026-08-15', 'J', 2016, 4),
    (57, '2026-08-15', 'P', 2017, 4),
    (58, '2026-08-15', 'P', 2018, 4),
    (59, '2026-08-15', 'P', 2019, 4),
    (60, '2026-08-15', 'P', 2020, 4),
    (61, '2026-08-15', 'P', 2021, 4),
    (62, '2026-08-15', 'P', 2022, 4),
    (63, '2026-08-15', 'P', 2023, 4),
    (64, '2026-08-15', 'P', 2024, 4),
    (65, '2026-08-15', 'P', 2025, 4),
    (66, '2026-08-15', 'P', 2026, 4),
    (67, '2026-08-15', 'P', 2004, 4),
    (68, '2026-08-15', 'P', 2005, 4),
    (69, '2026-08-15', 'P', 2006, 4),
    (70, '2026-08-22', 'P', 2007, 5),
    (71, '2026-08-22', 'P', 2008, 5),
    (72, '2026-08-22', 'F', 2009, 5),
    (73, '2026-08-22', 'P', 2010, 5),
    (74, '2026-08-22', 'P', 2011, 5),
    (75, '2026-08-22', 'P', 2012, 5),
    (76, '2026-08-22', 'P', 2013, 5),
    (77, '2026-08-22', 'P', 2014, 5),
    (78, '2026-08-22', 'P', 2015, 5),
    (79, '2026-08-22', 'P', 2016, 5),
    (80, '2026-08-22', 'P', 2017, 5),
    (81, '2026-08-22', 'P', 2018, 5),
    (82, '2026-08-22', 'P', 2019, 5),
    (83, '2026-08-22', 'P', 2020, 5),
    (84, '2026-08-22', 'P', 2021, 5),
    (85, '2026-08-22', 'P', 2022, 5),
    (86, '2026-08-22', 'P', 2023, 5),
    (87, '2026-08-22', 'P', 2024, 5),
    (88, '2026-08-22', 'P', 2025, 5),
    (89, '2026-08-22', 'P', 2026, 5),
    (90, '2026-08-22', 'P', 2004, 5),
    (91, '2026-08-22', 'P', 2005, 5),
    (92, '2026-08-22', 'P', 2006, 5),
    (93, '2026-08-23', 'P', 2007, 6),
    (94, '2026-08-23', 'P', 2008, 6),
    (95, '2026-08-23', 'P', 2009, 6),
    (96, '2026-08-23', 'P', 2010, 6),
    (97, '2026-08-23', 'P', 2013, 6),
    (98, '2026-08-23', 'P', 2014, 6),
    (99, '2026-08-23', 'P', 2017, 6),
    (100, '2026-08-23', 'P', 2018, 6);

INSERT INTO frequencia (freq_cod, freq_data, freq_status, escoteiro_esc_cod, atividade_especial_atv_cod) VALUES
    (101, '2026-08-23', 'P', 2020, 6),
    (102, '2026-08-23', 'P', 2021, 6),
    (103, '2026-08-23', 'P', 2022, 6),
    (104, '2026-08-23', 'P', 2023, 6),
    (105, '2026-08-23', 'P', 2024, 6),
    (106, '2026-08-23', 'P', 2026, 6),
    (107, '2026-08-23', 'P', 2004, 6),
    (108, '2026-08-23', 'P', 2005, 6),
    (109, '2026-08-23', 'P', 2006, 6),
    (110, '2026-08-29', 'J', 2007, 7),
    (111, '2026-08-29', 'P', 2008, 7),
    (112, '2026-08-29', 'P', 2009, 7),
    (113, '2026-08-29', 'P', 2010, 7),
    (114, '2026-08-29', 'P', 2011, 7),
    (115, '2026-08-29', 'F', 2012, 7),
    (116, '2026-08-29', 'P', 2013, 7),
    (117, '2026-08-29', 'F', 2014, 7),
    (118, '2026-08-29', 'J', 2015, 7),
    (119, '2026-08-29', 'P', 2016, 7),
    (120, '2026-08-29', 'P', 2017, 7),
    (121, '2026-08-29', 'J', 2018, 7),
    (122, '2026-08-29', 'P', 2019, 7),
    (123, '2026-08-29', 'P', 2020, 7),
    (124, '2026-08-29', 'P', 2021, 7),
    (125, '2026-08-29', 'P', 2022, 7),
    (126, '2026-08-29', 'P', 2023, 7),
    (127, '2026-08-29', 'P', 2024, 7),
    (128, '2026-08-29', 'J', 2025, 7),
    (129, '2026-08-29', 'P', 2026, 7),
    (130, '2026-08-29', 'P', 2004, 7),
    (131, '2026-08-29', 'P', 2005, 7),
    (132, '2026-08-29', 'P', 2006, 7),
    (133, '2026-09-05', 'P', 2007, 8),
    (134, '2026-09-05', 'P', 2008, 8),
    (135, '2026-09-05', 'P', 2009, 8),
    (136, '2026-09-05', 'P', 2010, 8),
    (137, '2026-09-05', 'J', 2011, 8),
    (138, '2026-09-05', 'P', 2012, 8),
    (139, '2026-09-05', 'P', 2013, 8),
    (140, '2026-09-05', 'P', 2014, 8),
    (141, '2026-09-05', 'P', 2015, 8),
    (142, '2026-09-05', 'P', 2016, 8),
    (143, '2026-09-05', 'P', 2017, 8),
    (144, '2026-09-05', 'P', 2018, 8),
    (145, '2026-09-05', 'P', 2019, 8),
    (146, '2026-09-05', 'P', 2020, 8),
    (147, '2026-09-05', 'P', 2021, 8),
    (148, '2026-09-05', 'P', 2022, 8),
    (149, '2026-09-05', 'P', 2023, 8),
    (150, '2026-09-05', 'P', 2024, 8);

INSERT INTO frequencia (freq_cod, freq_data, freq_status, escoteiro_esc_cod, atividade_especial_atv_cod) VALUES
    (151, '2026-09-05', 'P', 2026, 8),
    (152, '2026-09-05', 'P', 2004, 8),
    (153, '2026-09-05', 'P', 2005, 8),
    (154, '2026-09-05', 'P', 2006, 8),
    (155, '2026-09-12', 'P', 2007, 9),
    (156, '2026-09-12', 'P', 2008, 9),
    (157, '2026-09-12', 'P', 2009, 9),
    (158, '2026-09-12', 'P', 2010, 9),
    (159, '2026-09-12', 'P', 2011, 9),
    (160, '2026-09-12', 'P', 2012, 9),
    (161, '2026-09-12', 'P', 2013, 9),
    (162, '2026-09-12', 'P', 2014, 9),
    (163, '2026-09-12', 'P', 2015, 9),
    (164, '2026-09-12', 'P', 2016, 9),
    (165, '2026-09-12', 'P', 2017, 9),
    (166, '2026-09-12', 'P', 2018, 9),
    (167, '2026-09-12', 'P', 2019, 9),
    (168, '2026-09-12', 'P', 2020, 9),
    (169, '2026-09-12', 'P', 2021, 9),
    (170, '2026-09-12', 'P', 2022, 9),
    (171, '2026-09-12', 'P', 2023, 9),
    (172, '2026-09-12', 'P', 2024, 9),
    (173, '2026-09-12', 'P', 2026, 9),
    (174, '2026-09-12', 'P', 2004, 9),
    (175, '2026-09-12', 'P', 2005, 9),
    (176, '2026-09-12', 'P', 2006, 9),
    (177, '2026-09-19', 'P', 2008, 10),
    (178, '2026-09-19', 'P', 2010, 10),
    (179, '2026-09-19', 'P', 2013, 10),
    (180, '2026-09-19', 'P', 2014, 10),
    (181, '2026-09-19', 'P', 2018, 10),
    (182, '2026-09-19', 'P', 2020, 10),
    (183, '2026-09-19', 'P', 2021, 10),
    (184, '2026-09-19', 'P', 2022, 10),
    (185, '2026-09-19', 'P', 2023, 10),
    (186, '2026-09-19', 'P', 2004, 10),
    (187, '2026-09-19', 'P', 2005, 10),
    (188, '2026-09-19', 'P', 2006, 10),
    (189, '2026-09-26', 'P', 2007, 11),
    (190, '2026-09-26', 'P', 2008, 11),
    (191, '2026-09-26', 'P', 2009, 11),
    (192, '2026-09-26', 'P', 2010, 11),
    (193, '2026-09-26', 'P', 2011, 11),
    (194, '2026-09-26', 'P', 2012, 11),
    (195, '2026-09-26', 'P', 2013, 11),
    (196, '2026-09-26', 'P', 2014, 11),
    (197, '2026-09-26', 'J', 2015, 11),
    (198, '2026-09-26', 'F', 2016, 11),
    (199, '2026-09-26', 'P', 2017, 11),
    (200, '2026-09-26', 'P', 2018, 11);

INSERT INTO frequencia (freq_cod, freq_data, freq_status, escoteiro_esc_cod, atividade_especial_atv_cod) VALUES
    (201, '2026-09-26', 'J', 2019, 11),
    (202, '2026-09-26', 'P', 2020, 11),
    (203, '2026-09-26', 'P', 2021, 11),
    (204, '2026-09-26', 'P', 2022, 11),
    (205, '2026-09-26', 'P', 2023, 11),
    (206, '2026-09-26', 'J', 2024, 11),
    (207, '2026-09-26', 'P', 2026, 11),
    (208, '2026-09-26', 'P', 2004, 11),
    (209, '2026-09-26', 'P', 2005, 11),
    (210, '2026-09-26', 'P', 2006, 11),
    (211, '2026-10-03', 'J', 2007, 12),
    (212, '2026-10-03', 'F', 2008, 12),
    (213, '2026-10-03', 'P', 2009, 12),
    (214, '2026-10-03', 'P', 2010, 12),
    (215, '2026-10-03', 'P', 2011, 12),
    (216, '2026-10-03', 'P', 2012, 12),
    (217, '2026-10-03', 'P', 2013, 12),
    (218, '2026-10-03', 'P', 2014, 12),
    (219, '2026-10-03', 'P', 2015, 12),
    (220, '2026-10-03', 'P', 2016, 12),
    (221, '2026-10-03', 'P', 2017, 12),
    (222, '2026-10-03', 'F', 2018, 12),
    (223, '2026-10-03', 'P', 2019, 12),
    (224, '2026-10-03', 'P', 2020, 12),
    (225, '2026-10-03', 'P', 2021, 12),
    (226, '2026-10-03', 'P', 2022, 12),
    (227, '2026-10-03', 'P', 2023, 12),
    (228, '2026-10-03', 'F', 2024, 12),
    (229, '2026-10-03', 'P', 2026, 12),
    (230, '2026-10-03', 'P', 2004, 12),
    (231, '2026-10-03', 'P', 2005, 12),
    (232, '2026-10-03', 'P', 2006, 12),
    (233, '2026-10-10', 'P', 2007, 13),
    (234, '2026-10-10', 'P', 2008, 13),
    (235, '2026-10-10', 'P', 2009, 13),
    (236, '2026-10-10', 'P', 2010, 13),
    (237, '2026-10-10', 'P', 2011, 13),
    (238, '2026-10-10', 'F', 2012, 13),
    (239, '2026-10-10', 'P', 2013, 13),
    (240, '2026-10-10', 'P', 2014, 13),
    (241, '2026-10-10', 'P', 2015, 13),
    (242, '2026-10-10', 'P', 2016, 13),
    (243, '2026-10-10', 'P', 2017, 13),
    (244, '2026-10-10', 'P', 2018, 13),
    (245, '2026-10-10', 'P', 2019, 13),
    (246, '2026-10-10', 'J', 2020, 13),
    (247, '2026-10-10', 'P', 2021, 13),
    (248, '2026-10-10', 'F', 2022, 13),
    (249, '2026-10-10', 'P', 2023, 13),
    (250, '2026-10-10', 'P', 2024, 13);

INSERT INTO frequencia (freq_cod, freq_data, freq_status, escoteiro_esc_cod, atividade_especial_atv_cod) VALUES
    (251, '2026-10-10', 'P', 2026, 13),
    (252, '2026-10-10', 'P', 2004, 13),
    (253, '2026-10-10', 'P', 2005, 13),
    (254, '2026-10-10', 'P', 2006, 13),
    (255, '2026-10-17', 'P', 2007, 14),
    (256, '2026-10-17', 'P', 2008, 14),
    (257, '2026-10-17', 'P', 2009, 14),
    (258, '2026-10-17', 'P', 2010, 14),
    (259, '2026-10-17', 'P', 2011, 14),
    (260, '2026-10-17', 'J', 2012, 14),
    (261, '2026-10-17', 'P', 2013, 14),
    (262, '2026-10-17', 'P', 2014, 14),
    (263, '2026-10-17', 'F', 2015, 14),
    (264, '2026-10-17', 'P', 2016, 14),
    (265, '2026-10-17', 'P', 2017, 14),
    (266, '2026-10-17', 'P', 2018, 14),
    (267, '2026-10-17', 'P', 2019, 14),
    (268, '2026-10-17', 'P', 2020, 14),
    (269, '2026-10-17', 'P', 2021, 14),
    (270, '2026-10-17', 'P', 2022, 14),
    (271, '2026-10-17', 'P', 2023, 14),
    (272, '2026-10-17', 'J', 2024, 14),
    (273, '2026-10-17', 'P', 2026, 14),
    (274, '2026-10-17', 'P', 2004, 14),
    (275, '2026-10-17', 'P', 2005, 14),
    (276, '2026-10-17', 'P', 2006, 14);

INSERT INTO mensalidade (mens_cod, mens_mes, mens_ano, mens_dt_lancto, mens_valor, escoteiro_esc_cod, categoria_mensalidade_cat_mens_cod) VALUES
    (1, 1, 2026, '2026-01-01', 80.00, 2007, 2),
    (2, 1, 2026, '2026-01-01', 80.00, 2008, 2),
    (3, 1, 2026, '2026-01-01', 70.00, 2009, 3),
    (4, 1, 2026, '2026-01-01', 70.00, 2010, 3),
    (5, 1, 2026, '2026-01-01', 70.00, 2011, 3),
    (6, 1, 2026, '2026-01-01', 90.00, 2012, 1),
    (7, 1, 2026, '2026-01-01', 80.00, 2013, 2),
    (8, 1, 2026, '2026-01-01', 80.00, 2014, 2),
    (9, 1, 2026, '2026-01-01', 90.00, 2015, 1),
    (10, 1, 2026, '2026-01-01', 90.00, 2016, 1),
    (11, 1, 2026, '2026-01-01', 80.00, 2017, 2),
    (12, 1, 2026, '2026-01-01', 90.00, 2019, 1),
    (13, 1, 2026, '2026-01-01', 90.00, 2020, 1),
    (14, 1, 2026, '2026-01-01', 80.00, 2021, 2),
    (15, 1, 2026, '2026-01-01', 90.00, 2023, 1),
    (16, 1, 2026, '2026-01-01', 90.00, 2024, 1),
    (17, 1, 2026, '2026-01-01', 90.00, 2025, 1),
    (18, 2, 2026, '2026-02-01', 80.00, 2007, 2),
    (19, 2, 2026, '2026-02-01', 80.00, 2008, 2),
    (20, 2, 2026, '2026-02-01', 70.00, 2009, 3),
    (21, 2, 2026, '2026-02-01', 70.00, 2010, 3),
    (22, 2, 2026, '2026-02-01', 70.00, 2011, 3),
    (23, 2, 2026, '2026-02-01', 90.00, 2012, 1),
    (24, 2, 2026, '2026-02-01', 80.00, 2013, 2),
    (25, 2, 2026, '2026-02-01', 80.00, 2014, 2),
    (26, 2, 2026, '2026-02-01', 90.00, 2015, 1),
    (27, 2, 2026, '2026-02-01', 90.00, 2016, 1),
    (28, 2, 2026, '2026-02-01', 80.00, 2017, 2),
    (29, 2, 2026, '2026-02-01', 90.00, 2019, 1),
    (30, 2, 2026, '2026-02-01', 90.00, 2020, 1),
    (31, 2, 2026, '2026-02-01', 80.00, 2021, 2),
    (32, 2, 2026, '2026-02-01', 80.00, 2022, 2),
    (33, 2, 2026, '2026-02-01', 90.00, 2023, 1),
    (34, 2, 2026, '2026-02-01', 90.00, 2024, 1),
    (35, 2, 2026, '2026-02-01', 90.00, 2025, 1),
    (36, 3, 2026, '2026-03-01', 80.00, 2007, 2),
    (37, 3, 2026, '2026-03-01', 80.00, 2008, 2),
    (38, 3, 2026, '2026-03-01', 70.00, 2009, 3),
    (39, 3, 2026, '2026-03-01', 70.00, 2010, 3),
    (40, 3, 2026, '2026-03-01', 70.00, 2011, 3),
    (41, 3, 2026, '2026-03-01', 90.00, 2012, 1),
    (42, 3, 2026, '2026-03-01', 80.00, 2013, 2),
    (43, 3, 2026, '2026-03-01', 80.00, 2014, 2),
    (44, 3, 2026, '2026-03-01', 90.00, 2015, 1),
    (45, 3, 2026, '2026-03-01', 90.00, 2016, 1),
    (46, 3, 2026, '2026-03-01', 80.00, 2017, 2),
    (47, 3, 2026, '2026-03-01', 80.00, 2018, 2),
    (48, 3, 2026, '2026-03-01', 90.00, 2019, 1),
    (49, 3, 2026, '2026-03-01', 90.00, 2020, 1),
    (50, 3, 2026, '2026-03-01', 80.00, 2021, 2);

INSERT INTO mensalidade (mens_cod, mens_mes, mens_ano, mens_dt_lancto, mens_valor, escoteiro_esc_cod, categoria_mensalidade_cat_mens_cod) VALUES
    (51, 3, 2026, '2026-03-01', 80.00, 2022, 2),
    (52, 3, 2026, '2026-03-01', 90.00, 2023, 1),
    (53, 3, 2026, '2026-03-01', 90.00, 2024, 1),
    (54, 3, 2026, '2026-03-01', 90.00, 2025, 1),
    (55, 4, 2026, '2026-04-01', 80.00, 2007, 2),
    (56, 4, 2026, '2026-04-01', 80.00, 2008, 2),
    (57, 4, 2026, '2026-04-01', 70.00, 2009, 3),
    (58, 4, 2026, '2026-04-01', 70.00, 2010, 3),
    (59, 4, 2026, '2026-04-01', 70.00, 2011, 3),
    (60, 4, 2026, '2026-04-01', 90.00, 2012, 1),
    (61, 4, 2026, '2026-04-01', 80.00, 2013, 2),
    (62, 4, 2026, '2026-04-01', 80.00, 2014, 2),
    (63, 4, 2026, '2026-04-01', 90.00, 2015, 1),
    (64, 4, 2026, '2026-04-01', 90.00, 2016, 1),
    (65, 4, 2026, '2026-04-01', 80.00, 2017, 2),
    (66, 4, 2026, '2026-04-01', 80.00, 2018, 2),
    (67, 4, 2026, '2026-04-01', 90.00, 2019, 1),
    (68, 4, 2026, '2026-04-01', 90.00, 2020, 1),
    (69, 4, 2026, '2026-04-01', 80.00, 2021, 2),
    (70, 4, 2026, '2026-04-01', 80.00, 2022, 2),
    (71, 4, 2026, '2026-04-01', 90.00, 2023, 1),
    (72, 4, 2026, '2026-04-01', 90.00, 2024, 1),
    (73, 4, 2026, '2026-04-01', 90.00, 2025, 1),
    (74, 4, 2026, '2026-04-01', 90.00, 2026, 1),
    (75, 5, 2026, '2026-05-01', 80.00, 2007, 2),
    (76, 5, 2026, '2026-05-01', 80.00, 2008, 2),
    (77, 5, 2026, '2026-05-01', 70.00, 2009, 3),
    (78, 5, 2026, '2026-05-01', 70.00, 2010, 3),
    (79, 5, 2026, '2026-05-01', 70.00, 2011, 3),
    (80, 5, 2026, '2026-05-01', 90.00, 2012, 1),
    (81, 5, 2026, '2026-05-01', 80.00, 2013, 2),
    (82, 5, 2026, '2026-05-01', 80.00, 2014, 2),
    (83, 5, 2026, '2026-05-01', 90.00, 2015, 1),
    (84, 5, 2026, '2026-05-01', 90.00, 2016, 1),
    (85, 5, 2026, '2026-05-01', 80.00, 2017, 2),
    (86, 5, 2026, '2026-05-01', 80.00, 2018, 2),
    (87, 5, 2026, '2026-05-01', 90.00, 2019, 1),
    (88, 5, 2026, '2026-05-01', 90.00, 2020, 1),
    (89, 5, 2026, '2026-05-01', 80.00, 2021, 2),
    (90, 5, 2026, '2026-05-01', 80.00, 2022, 2),
    (91, 5, 2026, '2026-05-01', 90.00, 2023, 1),
    (92, 5, 2026, '2026-05-01', 90.00, 2024, 1),
    (93, 5, 2026, '2026-05-01', 90.00, 2025, 1),
    (94, 5, 2026, '2026-05-01', 90.00, 2026, 1),
    (95, 6, 2026, '2026-06-01', 80.00, 2007, 2),
    (96, 6, 2026, '2026-06-01', 80.00, 2008, 2),
    (97, 6, 2026, '2026-06-01', 70.00, 2009, 3),
    (98, 6, 2026, '2026-06-01', 70.00, 2010, 3),
    (99, 6, 2026, '2026-06-01', 70.00, 2011, 3),
    (100, 6, 2026, '2026-06-01', 90.00, 2012, 1);

INSERT INTO mensalidade (mens_cod, mens_mes, mens_ano, mens_dt_lancto, mens_valor, escoteiro_esc_cod, categoria_mensalidade_cat_mens_cod) VALUES
    (101, 6, 2026, '2026-06-01', 80.00, 2013, 2),
    (102, 6, 2026, '2026-06-01', 80.00, 2014, 2),
    (103, 6, 2026, '2026-06-01', 90.00, 2015, 1),
    (104, 6, 2026, '2026-06-01', 90.00, 2016, 1),
    (105, 6, 2026, '2026-06-01', 80.00, 2017, 2),
    (106, 6, 2026, '2026-06-01', 80.00, 2018, 2),
    (107, 6, 2026, '2026-06-01', 90.00, 2019, 1),
    (108, 6, 2026, '2026-06-01', 90.00, 2020, 1),
    (109, 6, 2026, '2026-06-01', 80.00, 2021, 2),
    (110, 6, 2026, '2026-06-01', 80.00, 2022, 2),
    (111, 6, 2026, '2026-06-01', 90.00, 2023, 1),
    (112, 6, 2026, '2026-06-01', 90.00, 2024, 1),
    (113, 6, 2026, '2026-06-01', 90.00, 2025, 1),
    (114, 6, 2026, '2026-06-01', 90.00, 2026, 1),
    (115, 7, 2026, '2026-07-01', 80.00, 2007, 2),
    (116, 7, 2026, '2026-07-01', 80.00, 2008, 2),
    (117, 7, 2026, '2026-07-01', 70.00, 2009, 3),
    (118, 7, 2026, '2026-07-01', 70.00, 2010, 3),
    (119, 7, 2026, '2026-07-01', 70.00, 2011, 3),
    (120, 7, 2026, '2026-07-01', 90.00, 2012, 1),
    (121, 7, 2026, '2026-07-01', 80.00, 2013, 2),
    (122, 7, 2026, '2026-07-01', 80.00, 2014, 2),
    (123, 7, 2026, '2026-07-01', 90.00, 2015, 1),
    (124, 7, 2026, '2026-07-01', 90.00, 2016, 1),
    (125, 7, 2026, '2026-07-01', 80.00, 2017, 2),
    (126, 7, 2026, '2026-07-01', 80.00, 2018, 2),
    (127, 7, 2026, '2026-07-01', 90.00, 2019, 1),
    (128, 7, 2026, '2026-07-01', 90.00, 2020, 1),
    (129, 7, 2026, '2026-07-01', 80.00, 2021, 2),
    (130, 7, 2026, '2026-07-01', 80.00, 2022, 2),
    (131, 7, 2026, '2026-07-01', 90.00, 2023, 1),
    (132, 7, 2026, '2026-07-01', 90.00, 2024, 1),
    (133, 7, 2026, '2026-07-01', 90.00, 2025, 1),
    (134, 7, 2026, '2026-07-01', 90.00, 2026, 1),
    (135, 8, 2026, '2026-08-01', 80.00, 2007, 2),
    (136, 8, 2026, '2026-08-01', 80.00, 2008, 2),
    (137, 8, 2026, '2026-08-01', 70.00, 2009, 3),
    (138, 8, 2026, '2026-08-01', 70.00, 2010, 3),
    (139, 8, 2026, '2026-08-01', 70.00, 2011, 3),
    (140, 8, 2026, '2026-08-01', 90.00, 2012, 1),
    (141, 8, 2026, '2026-08-01', 80.00, 2013, 2),
    (142, 8, 2026, '2026-08-01', 80.00, 2014, 2),
    (143, 8, 2026, '2026-08-01', 90.00, 2015, 1),
    (144, 8, 2026, '2026-08-01', 90.00, 2016, 1),
    (145, 8, 2026, '2026-08-01', 80.00, 2017, 2),
    (146, 8, 2026, '2026-08-01', 80.00, 2018, 2),
    (147, 8, 2026, '2026-08-01', 90.00, 2019, 1),
    (148, 8, 2026, '2026-08-01', 90.00, 2020, 1),
    (149, 8, 2026, '2026-08-01', 80.00, 2021, 2),
    (150, 8, 2026, '2026-08-01', 80.00, 2022, 2);

INSERT INTO mensalidade (mens_cod, mens_mes, mens_ano, mens_dt_lancto, mens_valor, escoteiro_esc_cod, categoria_mensalidade_cat_mens_cod) VALUES
    (151, 8, 2026, '2026-08-01', 90.00, 2023, 1),
    (152, 8, 2026, '2026-08-01', 90.00, 2024, 1),
    (153, 8, 2026, '2026-08-01', 90.00, 2025, 1),
    (154, 8, 2026, '2026-08-01', 90.00, 2026, 1),
    (155, 9, 2026, '2026-09-01', 80.00, 2007, 2),
    (156, 9, 2026, '2026-09-01', 80.00, 2008, 2),
    (157, 9, 2026, '2026-09-01', 70.00, 2009, 3),
    (158, 9, 2026, '2026-09-01', 70.00, 2010, 3),
    (159, 9, 2026, '2026-09-01', 70.00, 2011, 3),
    (160, 9, 2026, '2026-09-01', 90.00, 2012, 1),
    (161, 9, 2026, '2026-09-01', 80.00, 2013, 2),
    (162, 9, 2026, '2026-09-01', 80.00, 2014, 2),
    (163, 9, 2026, '2026-09-01', 90.00, 2015, 1),
    (164, 9, 2026, '2026-09-01', 90.00, 2016, 1),
    (165, 9, 2026, '2026-09-01', 80.00, 2017, 2),
    (166, 9, 2026, '2026-09-01', 80.00, 2018, 2),
    (167, 9, 2026, '2026-09-01', 90.00, 2019, 1),
    (168, 9, 2026, '2026-09-01', 90.00, 2020, 1),
    (169, 9, 2026, '2026-09-01', 80.00, 2021, 2),
    (170, 9, 2026, '2026-09-01', 80.00, 2022, 2),
    (171, 9, 2026, '2026-09-01', 90.00, 2023, 1),
    (172, 9, 2026, '2026-09-01', 90.00, 2024, 1),
    (173, 9, 2026, '2026-09-01', 90.00, 2026, 1),
    (174, 10, 2026, '2026-10-01', 80.00, 2007, 2),
    (175, 10, 2026, '2026-10-01', 80.00, 2008, 2),
    (176, 10, 2026, '2026-10-01', 70.00, 2009, 3),
    (177, 10, 2026, '2026-10-01', 70.00, 2010, 3),
    (178, 10, 2026, '2026-10-01', 70.00, 2011, 3),
    (179, 10, 2026, '2026-10-01', 90.00, 2012, 1),
    (180, 10, 2026, '2026-10-01', 80.00, 2013, 2),
    (181, 10, 2026, '2026-10-01', 80.00, 2014, 2),
    (182, 10, 2026, '2026-10-01', 90.00, 2015, 1),
    (183, 10, 2026, '2026-10-01', 90.00, 2016, 1),
    (184, 10, 2026, '2026-10-01', 80.00, 2017, 2),
    (185, 10, 2026, '2026-10-01', 80.00, 2018, 2),
    (186, 10, 2026, '2026-10-01', 90.00, 2019, 1),
    (187, 10, 2026, '2026-10-01', 90.00, 2020, 1),
    (188, 10, 2026, '2026-10-01', 80.00, 2021, 2),
    (189, 10, 2026, '2026-10-01', 80.00, 2022, 2),
    (190, 10, 2026, '2026-10-01', 90.00, 2023, 1),
    (191, 10, 2026, '2026-10-01', 90.00, 2024, 1),
    (192, 10, 2026, '2026-10-01', 90.00, 2026, 1);

INSERT INTO contas_receber (cr_cod, cr_valor, cr_desc, atividade_especial_atv_cod, mensalidade_mens_cod) VALUES
    (1, 80.00, 'Mensalidade 01/2026', 1, 1),
    (2, 80.00, 'Mensalidade 01/2026', 1, 2),
    (3, 70.00, 'Mensalidade 01/2026', 1, 3),
    (4, 70.00, 'Mensalidade 01/2026', 1, 4),
    (5, 70.00, 'Mensalidade 01/2026', 1, 5),
    (6, 90.00, 'Mensalidade 01/2026', 1, 6),
    (7, 80.00, 'Mensalidade 01/2026', 1, 7),
    (8, 80.00, 'Mensalidade 01/2026', 1, 8),
    (9, 90.00, 'Mensalidade 01/2026', 1, 9),
    (10, 90.00, 'Mensalidade 01/2026', 1, 10),
    (11, 80.00, 'Mensalidade 01/2026', 1, 11),
    (12, 90.00, 'Mensalidade 01/2026', 1, 12),
    (13, 90.00, 'Mensalidade 01/2026', 1, 13),
    (14, 80.00, 'Mensalidade 01/2026', 1, 14),
    (15, 90.00, 'Mensalidade 01/2026', 1, 15),
    (16, 90.00, 'Mensalidade 01/2026', 1, 16),
    (17, 90.00, 'Mensalidade 01/2026', 1, 17),
    (18, 80.00, 'Mensalidade 02/2026', 1, 18),
    (19, 80.00, 'Mensalidade 02/2026', 1, 19),
    (20, 70.00, 'Mensalidade 02/2026', 1, 20),
    (21, 70.00, 'Mensalidade 02/2026', 1, 21),
    (22, 70.00, 'Mensalidade 02/2026', 1, 22),
    (23, 90.00, 'Mensalidade 02/2026', 1, 23),
    (24, 80.00, 'Mensalidade 02/2026', 1, 24),
    (25, 80.00, 'Mensalidade 02/2026', 1, 25),
    (26, 90.00, 'Mensalidade 02/2026', 1, 26),
    (27, 90.00, 'Mensalidade 02/2026', 1, 27),
    (28, 80.00, 'Mensalidade 02/2026', 1, 28),
    (29, 90.00, 'Mensalidade 02/2026', 1, 29),
    (30, 90.00, 'Mensalidade 02/2026', 1, 30),
    (31, 80.00, 'Mensalidade 02/2026', 1, 31),
    (32, 80.00, 'Mensalidade 02/2026', 1, 32),
    (33, 90.00, 'Mensalidade 02/2026', 1, 33),
    (34, 90.00, 'Mensalidade 02/2026', 1, 34),
    (35, 90.00, 'Mensalidade 02/2026', 1, 35),
    (36, 80.00, 'Mensalidade 03/2026', 1, 36),
    (37, 80.00, 'Mensalidade 03/2026', 1, 37),
    (38, 70.00, 'Mensalidade 03/2026', 1, 38),
    (39, 70.00, 'Mensalidade 03/2026', 1, 39),
    (40, 70.00, 'Mensalidade 03/2026', 1, 40),
    (41, 90.00, 'Mensalidade 03/2026', 1, 41),
    (42, 80.00, 'Mensalidade 03/2026', 1, 42),
    (43, 80.00, 'Mensalidade 03/2026', 1, 43),
    (44, 90.00, 'Mensalidade 03/2026', 1, 44),
    (45, 90.00, 'Mensalidade 03/2026', 1, 45),
    (46, 80.00, 'Mensalidade 03/2026', 1, 46),
    (47, 80.00, 'Mensalidade 03/2026', 1, 47),
    (48, 90.00, 'Mensalidade 03/2026', 1, 48),
    (49, 90.00, 'Mensalidade 03/2026', 1, 49),
    (50, 80.00, 'Mensalidade 03/2026', 1, 50);

INSERT INTO contas_receber (cr_cod, cr_valor, cr_desc, atividade_especial_atv_cod, mensalidade_mens_cod) VALUES
    (51, 80.00, 'Mensalidade 03/2026', 1, 51),
    (52, 90.00, 'Mensalidade 03/2026', 1, 52),
    (53, 90.00, 'Mensalidade 03/2026', 1, 53),
    (54, 90.00, 'Mensalidade 03/2026', 1, 54),
    (55, 80.00, 'Mensalidade 04/2026', 1, 55),
    (56, 80.00, 'Mensalidade 04/2026', 1, 56),
    (57, 70.00, 'Mensalidade 04/2026', 1, 57),
    (58, 70.00, 'Mensalidade 04/2026', 1, 58),
    (59, 70.00, 'Mensalidade 04/2026', 1, 59),
    (60, 90.00, 'Mensalidade 04/2026', 1, 60),
    (61, 80.00, 'Mensalidade 04/2026', 1, 61),
    (62, 80.00, 'Mensalidade 04/2026', 1, 62),
    (63, 90.00, 'Mensalidade 04/2026', 1, 63),
    (64, 90.00, 'Mensalidade 04/2026', 1, 64),
    (65, 80.00, 'Mensalidade 04/2026', 1, 65),
    (66, 80.00, 'Mensalidade 04/2026', 1, 66),
    (67, 90.00, 'Mensalidade 04/2026', 1, 67),
    (68, 90.00, 'Mensalidade 04/2026', 1, 68),
    (69, 80.00, 'Mensalidade 04/2026', 1, 69),
    (70, 80.00, 'Mensalidade 04/2026', 1, 70),
    (71, 90.00, 'Mensalidade 04/2026', 1, 71),
    (72, 90.00, 'Mensalidade 04/2026', 1, 72),
    (73, 90.00, 'Mensalidade 04/2026', 1, 73),
    (74, 90.00, 'Mensalidade 04/2026', 1, 74),
    (75, 80.00, 'Mensalidade 05/2026', 1, 75),
    (76, 80.00, 'Mensalidade 05/2026', 1, 76),
    (77, 70.00, 'Mensalidade 05/2026', 1, 77),
    (78, 70.00, 'Mensalidade 05/2026', 1, 78),
    (79, 70.00, 'Mensalidade 05/2026', 1, 79),
    (80, 90.00, 'Mensalidade 05/2026', 1, 80),
    (81, 80.00, 'Mensalidade 05/2026', 1, 81),
    (82, 80.00, 'Mensalidade 05/2026', 1, 82),
    (83, 90.00, 'Mensalidade 05/2026', 1, 83),
    (84, 90.00, 'Mensalidade 05/2026', 1, 84),
    (85, 80.00, 'Mensalidade 05/2026', 1, 85),
    (86, 80.00, 'Mensalidade 05/2026', 1, 86),
    (87, 90.00, 'Mensalidade 05/2026', 1, 87),
    (88, 90.00, 'Mensalidade 05/2026', 1, 88),
    (89, 80.00, 'Mensalidade 05/2026', 1, 89),
    (90, 80.00, 'Mensalidade 05/2026', 1, 90),
    (91, 90.00, 'Mensalidade 05/2026', 1, 91),
    (92, 90.00, 'Mensalidade 05/2026', 1, 92),
    (93, 90.00, 'Mensalidade 05/2026', 1, 93),
    (94, 90.00, 'Mensalidade 05/2026', 1, 94),
    (95, 80.00, 'Mensalidade 06/2026', 1, 95),
    (96, 80.00, 'Mensalidade 06/2026', 1, 96),
    (97, 70.00, 'Mensalidade 06/2026', 1, 97),
    (98, 70.00, 'Mensalidade 06/2026', 1, 98),
    (99, 70.00, 'Mensalidade 06/2026', 1, 99),
    (100, 90.00, 'Mensalidade 06/2026', 1, 100);

INSERT INTO contas_receber (cr_cod, cr_valor, cr_desc, atividade_especial_atv_cod, mensalidade_mens_cod) VALUES
    (101, 80.00, 'Mensalidade 06/2026', 1, 101),
    (102, 80.00, 'Mensalidade 06/2026', 1, 102),
    (103, 90.00, 'Mensalidade 06/2026', 1, 103),
    (104, 90.00, 'Mensalidade 06/2026', 1, 104),
    (105, 80.00, 'Mensalidade 06/2026', 1, 105),
    (106, 80.00, 'Mensalidade 06/2026', 1, 106),
    (107, 90.00, 'Mensalidade 06/2026', 1, 107),
    (108, 90.00, 'Mensalidade 06/2026', 1, 108),
    (109, 80.00, 'Mensalidade 06/2026', 1, 109),
    (110, 80.00, 'Mensalidade 06/2026', 1, 110),
    (111, 90.00, 'Mensalidade 06/2026', 1, 111),
    (112, 90.00, 'Mensalidade 06/2026', 1, 112),
    (113, 90.00, 'Mensalidade 06/2026', 1, 113),
    (114, 90.00, 'Mensalidade 06/2026', 1, 114),
    (115, 80.00, 'Mensalidade 07/2026', 1, 115),
    (116, 80.00, 'Mensalidade 07/2026', 1, 116),
    (117, 70.00, 'Mensalidade 07/2026', 1, 117),
    (118, 70.00, 'Mensalidade 07/2026', 1, 118),
    (119, 70.00, 'Mensalidade 07/2026', 1, 119),
    (120, 90.00, 'Mensalidade 07/2026', 1, 120),
    (121, 80.00, 'Mensalidade 07/2026', 1, 121),
    (122, 80.00, 'Mensalidade 07/2026', 1, 122),
    (123, 90.00, 'Mensalidade 07/2026', 1, 123),
    (124, 90.00, 'Mensalidade 07/2026', 1, 124),
    (125, 80.00, 'Mensalidade 07/2026', 1, 125),
    (126, 80.00, 'Mensalidade 07/2026', 1, 126),
    (127, 90.00, 'Mensalidade 07/2026', 1, 127),
    (128, 90.00, 'Mensalidade 07/2026', 1, 128),
    (129, 80.00, 'Mensalidade 07/2026', 1, 129),
    (130, 80.00, 'Mensalidade 07/2026', 1, 130),
    (131, 90.00, 'Mensalidade 07/2026', 1, 131),
    (132, 90.00, 'Mensalidade 07/2026', 1, 132),
    (133, 90.00, 'Mensalidade 07/2026', 1, 133),
    (134, 90.00, 'Mensalidade 07/2026', 1, 134),
    (135, 80.00, 'Mensalidade 08/2026', 1, 135),
    (136, 80.00, 'Mensalidade 08/2026', 1, 136),
    (137, 70.00, 'Mensalidade 08/2026', 1, 137),
    (138, 70.00, 'Mensalidade 08/2026', 1, 138),
    (139, 70.00, 'Mensalidade 08/2026', 1, 139),
    (140, 90.00, 'Mensalidade 08/2026', 1, 140),
    (141, 80.00, 'Mensalidade 08/2026', 1, 141),
    (142, 80.00, 'Mensalidade 08/2026', 1, 142),
    (143, 90.00, 'Mensalidade 08/2026', 1, 143),
    (144, 90.00, 'Mensalidade 08/2026', 1, 144),
    (145, 80.00, 'Mensalidade 08/2026', 1, 145),
    (146, 80.00, 'Mensalidade 08/2026', 1, 146),
    (147, 90.00, 'Mensalidade 08/2026', 1, 147),
    (148, 90.00, 'Mensalidade 08/2026', 1, 148),
    (149, 80.00, 'Mensalidade 08/2026', 1, 149),
    (150, 80.00, 'Mensalidade 08/2026', 1, 150);

INSERT INTO contas_receber (cr_cod, cr_valor, cr_desc, atividade_especial_atv_cod, mensalidade_mens_cod) VALUES
    (151, 90.00, 'Mensalidade 08/2026', 1, 151),
    (152, 90.00, 'Mensalidade 08/2026', 1, 152),
    (153, 90.00, 'Mensalidade 08/2026', 1, 153),
    (154, 90.00, 'Mensalidade 08/2026', 1, 154),
    (155, 80.00, 'Mensalidade 09/2026', 1, 155),
    (156, 80.00, 'Mensalidade 09/2026', 1, 156),
    (157, 70.00, 'Mensalidade 09/2026', 1, 157),
    (158, 70.00, 'Mensalidade 09/2026', 1, 158),
    (159, 70.00, 'Mensalidade 09/2026', 1, 159),
    (160, 90.00, 'Mensalidade 09/2026', 1, 160),
    (161, 80.00, 'Mensalidade 09/2026', 1, 161),
    (162, 80.00, 'Mensalidade 09/2026', 1, 162),
    (163, 90.00, 'Mensalidade 09/2026', 1, 163),
    (164, 90.00, 'Mensalidade 09/2026', 1, 164),
    (165, 80.00, 'Mensalidade 09/2026', 1, 165),
    (166, 80.00, 'Mensalidade 09/2026', 1, 166),
    (167, 90.00, 'Mensalidade 09/2026', 1, 167),
    (168, 90.00, 'Mensalidade 09/2026', 1, 168),
    (169, 80.00, 'Mensalidade 09/2026', 1, 169),
    (170, 80.00, 'Mensalidade 09/2026', 1, 170),
    (171, 90.00, 'Mensalidade 09/2026', 1, 171),
    (172, 90.00, 'Mensalidade 09/2026', 1, 172),
    (173, 90.00, 'Mensalidade 09/2026', 1, 173),
    (174, 80.00, 'Mensalidade 10/2026', 1, 174),
    (175, 80.00, 'Mensalidade 10/2026', 1, 175),
    (176, 70.00, 'Mensalidade 10/2026', 1, 176),
    (177, 70.00, 'Mensalidade 10/2026', 1, 177),
    (178, 70.00, 'Mensalidade 10/2026', 1, 178),
    (179, 90.00, 'Mensalidade 10/2026', 1, 179),
    (180, 80.00, 'Mensalidade 10/2026', 1, 180),
    (181, 80.00, 'Mensalidade 10/2026', 1, 181),
    (182, 90.00, 'Mensalidade 10/2026', 1, 182),
    (183, 90.00, 'Mensalidade 10/2026', 1, 183),
    (184, 80.00, 'Mensalidade 10/2026', 1, 184),
    (185, 80.00, 'Mensalidade 10/2026', 1, 185),
    (186, 90.00, 'Mensalidade 10/2026', 1, 186),
    (187, 90.00, 'Mensalidade 10/2026', 1, 187),
    (188, 80.00, 'Mensalidade 10/2026', 1, 188),
    (189, 80.00, 'Mensalidade 10/2026', 1, 189),
    (190, 90.00, 'Mensalidade 10/2026', 1, 190),
    (191, 90.00, 'Mensalidade 10/2026', 1, 191),
    (192, 90.00, 'Mensalidade 10/2026', 1, 192),
    (193, 45.00, 'Passeio Zoológico', 6, 135),
    (194, 45.00, 'Passeio Zoológico', 6, 136),
    (195, 45.00, 'Passeio Zoológico', 6, 137),
    (196, 45.00, 'Passeio Zoológico', 6, 138),
    (197, 45.00, 'Passeio Zoológico', 6, 141),
    (198, 45.00, 'Passeio Zoológico', 6, 142),
    (199, 45.00, 'Passeio Zoológico', 6, 145),
    (200, 45.00, 'Passeio Zoológico', 6, 146);

INSERT INTO contas_receber (cr_cod, cr_valor, cr_desc, atividade_especial_atv_cod, mensalidade_mens_cod) VALUES
    (201, 45.00, 'Passeio Zoológico', 6, 148),
    (202, 45.00, 'Passeio Zoológico', 6, 149),
    (203, 45.00, 'Passeio Zoológico', 6, 150),
    (204, 45.00, 'Passeio Zoológico', 6, 151),
    (205, 45.00, 'Passeio Zoológico', 6, 152),
    (206, 45.00, 'Passeio Zoológico', 6, 154),
    (207, 120.00, 'Acampamento Primavera', 10, 156),
    (208, 120.00, 'Acampamento Primavera', 10, 158),
    (209, 120.00, 'Acampamento Primavera', 10, 161),
    (210, 120.00, 'Acampamento Primavera', 10, 162),
    (211, 120.00, 'Acampamento Primavera', 10, 166),
    (212, 120.00, 'Acampamento Primavera', 10, 168),
    (213, 120.00, 'Acampamento Primavera', 10, 169),
    (214, 120.00, 'Acampamento Primavera', 10, 170),
    (215, 120.00, 'Acampamento Primavera', 10, 171),
    (216, 250.00, 'Jamboree Regional', 15, 177),
    (217, 250.00, 'Jamboree Regional', 15, 180),
    (218, 250.00, 'Jamboree Regional', 15, 181),
    (219, 250.00, 'Jamboree Regional', 15, 188),
    (220, 250.00, 'Jamboree Regional', 15, 189),
    (221, 250.00, 'Jamboree Regional', 15, 190),
    (222, 35.00, 'Ceia de Natal', 16, 174),
    (223, 35.00, 'Ceia de Natal', 16, 181),
    (224, 35.00, 'Ceia de Natal', 16, 189),
    (225, 35.00, 'Ceia de Natal', 16, 190),
    (226, 35.00, 'Ceia de Natal', 16, 192);

INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod, contas_receber_cr_cod, pg_valor_pago) VALUES
    (1, '2026-01-05', '2026-01-10', 'Mensalidade 01/2026', 1, 1, 80.00),
    (2, '2026-01-14', '2026-01-10', 'Mensalidade 01/2026', 1, 2, 80.00),
    (3, '2026-01-04', '2026-01-10', 'Mensalidade 01/2026', 2, 3, 70.00),
    (4, '2026-01-09', '2026-01-10', 'Mensalidade 01/2026', 3, 4, 70.00),
    (5, '2026-01-03', '2026-01-10', 'Mensalidade 01/2026', 1, 5, 70.00),
    (6, '2026-01-07', '2026-01-10', 'Mensalidade 01/2026', 2, 6, 90.00),
    (7, '2026-01-05', '2026-01-10', 'Mensalidade 01/2026', 3, 7, 80.00),
    (8, '2026-01-07', '2026-01-10', 'Mensalidade 01/2026', 3, 8, 80.00),
    (9, '2026-01-05', '2026-01-10', 'Apadrinhamento 01/2026', 4, 9, 90.00),
    (10, '2026-01-05', '2026-01-10', 'Apadrinhamento 01/2026', 4, 10, 90.00),
    (11, '2026-01-04', '2026-01-10', 'Mensalidade 01/2026', 3, 11, 80.00),
    (12, '2026-01-12', '2026-01-10', 'Mensalidade 01/2026', 2, 12, 90.00),
    (13, '2026-01-05', '2026-01-10', 'Mensalidade 01/2026', 1, 13, 90.00),
    (14, '2026-01-07', '2026-01-10', 'Mensalidade 01/2026', 2, 14, 80.00),
    (15, '2026-01-12', '2026-01-10', 'Mensalidade 01/2026', 2, 15, 90.00),
    (16, '2026-01-13', '2026-01-10', 'Mensalidade 01/2026', 2, 16, 90.00),
    (17, '2026-01-14', '2026-01-10', 'Mensalidade 01/2026', 2, 17, 90.00),
    (18, '2026-02-10', '2026-02-10', 'Mensalidade 02/2026', 2, 18, 80.00),
    (19, '2026-02-09', '2026-02-10', 'Mensalidade 02/2026', 1, 19, 80.00),
    (20, '2026-02-03', '2026-02-10', 'Mensalidade 02/2026', 2, 20, 70.00),
    (21, '2026-02-03', '2026-02-10', 'Mensalidade 02/2026', 2, 21, 70.00),
    (22, '2026-02-06', '2026-02-10', 'Mensalidade 02/2026', 2, 22, 70.00),
    (23, '2026-02-04', '2026-02-10', 'Mensalidade 02/2026', 2, 23, 90.00),
    (24, '2026-02-07', '2026-02-10', 'Mensalidade 02/2026', 2, 24, 80.00),
    (25, '2026-02-03', '2026-02-10', 'Mensalidade 02/2026', 1, 25, 80.00),
    (26, '2026-02-05', '2026-02-10', 'Apadrinhamento 02/2026', 4, 26, 90.00),
    (27, '2026-02-05', '2026-02-10', 'Apadrinhamento 02/2026', 4, 27, 90.00),
    (28, '2026-02-05', '2026-02-10', 'Mensalidade 02/2026', 2, 28, 80.00),
    (29, '2026-02-02', '2026-02-10', 'Mensalidade 02/2026', 2, 29, 90.00),
    (30, '2026-02-08', '2026-02-10', 'Mensalidade 02/2026', 2, 30, 90.00),
    (31, '2026-02-12', '2026-02-10', 'Mensalidade 02/2026', 2, 31, 80.00),
    (32, '2026-02-11', '2026-02-10', 'Mensalidade 02/2026', 2, 32, 80.00),
    (33, '2026-02-05', '2026-02-10', 'Mensalidade 02/2026', 1, 33, 90.00),
    (34, '2026-02-09', '2026-02-10', 'Mensalidade 02/2026', 2, 34, 90.00),
    (35, '2026-02-06', '2026-02-10', 'Mensalidade 02/2026', 2, 35, 90.00),
    (36, '2026-03-04', '2026-03-10', 'Mensalidade 03/2026', 2, 36, 80.00),
    (37, '2026-03-03', '2026-03-10', 'Mensalidade 03/2026', 2, 37, 80.00),
    (38, '2026-03-07', '2026-03-10', 'Mensalidade 03/2026', 3, 38, 70.00),
    (39, '2026-03-06', '2026-03-10', 'Mensalidade 03/2026', 1, 39, 70.00),
    (40, '2026-03-09', '2026-03-10', 'Mensalidade 03/2026', 3, 40, 70.00),
    (41, '2026-03-04', '2026-03-10', 'Mensalidade 03/2026', 3, 41, 90.00),
    (42, '2026-03-09', '2026-03-10', 'Mensalidade 03/2026', 3, 42, 80.00),
    (43, '2026-03-06', '2026-03-10', 'Mensalidade 03/2026', 2, 43, 80.00),
    (44, '2026-03-05', '2026-03-10', 'Apadrinhamento 03/2026', 4, 44, 90.00),
    (45, '2026-03-05', '2026-03-10', 'Apadrinhamento 03/2026', 4, 45, 90.00),
    (46, '2026-03-14', '2026-03-10', 'Mensalidade 03/2026', 2, 46, 80.00),
    (47, '2026-03-04', '2026-03-10', 'Mensalidade 03/2026', 2, 47, 80.00),
    (48, '2026-03-08', '2026-03-10', 'Mensalidade 03/2026', 2, 48, 90.00),
    (49, '2026-03-07', '2026-03-10', 'Mensalidade 03/2026', 2, 49, 90.00),
    (50, '2026-03-14', '2026-03-10', 'Mensalidade 03/2026', 1, 50, 80.00);

INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod, contas_receber_cr_cod, pg_valor_pago) VALUES
    (51, '2026-03-13', '2026-03-10', 'Mensalidade 03/2026', 3, 51, 80.00),
    (52, '2026-03-14', '2026-03-10', 'Mensalidade 03/2026', 2, 52, 90.00),
    (53, '2026-03-11', '2026-03-10', 'Mensalidade 03/2026', 2, 53, 90.00),
    (54, '2026-03-02', '2026-03-10', 'Mensalidade 03/2026', 2, 54, 90.00),
    (55, '2026-04-13', '2026-04-10', 'Mensalidade 04/2026', 2, 55, 80.00),
    (56, '2026-04-06', '2026-04-10', 'Mensalidade 04/2026', 3, 56, 80.00),
    (57, '2026-04-13', '2026-04-10', 'Mensalidade 04/2026', 1, 57, 70.00),
    (58, '2026-04-03', '2026-04-10', 'Mensalidade 04/2026', 2, 58, 70.00),
    (59, '2026-04-02', '2026-04-10', 'Mensalidade 04/2026', 1, 59, 70.00),
    (60, '2026-04-13', '2026-04-10', 'Mensalidade 04/2026', 1, 60, 90.00),
    (61, '2026-04-04', '2026-04-10', 'Mensalidade 04/2026', 2, 61, 80.00),
    (62, '2026-04-14', '2026-04-10', 'Mensalidade 04/2026', 1, 62, 80.00),
    (63, '2026-04-05', '2026-04-10', 'Apadrinhamento 04/2026', 4, 63, 90.00),
    (64, '2026-04-05', '2026-04-10', 'Apadrinhamento 04/2026', 4, 64, 90.00),
    (65, '2026-04-09', '2026-04-10', 'Mensalidade 04/2026', 2, 65, 80.00),
    (66, '2026-04-13', '2026-04-10', 'Mensalidade 04/2026', 1, 66, 80.00),
    (67, '2026-04-10', '2026-04-10', 'Mensalidade 04/2026', 3, 67, 90.00),
    (68, '2026-04-05', '2026-04-10', 'Mensalidade 04/2026', 2, 68, 90.00),
    (69, '2026-04-03', '2026-04-10', 'Mensalidade 04/2026', 2, 69, 80.00),
    (70, '2026-04-14', '2026-04-10', 'Mensalidade 04/2026', 2, 70, 80.00),
    (71, '2026-04-10', '2026-04-10', 'Mensalidade 04/2026', 2, 71, 90.00),
    (72, '2026-04-11', '2026-04-10', 'Mensalidade 04/2026', 3, 72, 90.00),
    (73, '2026-04-14', '2026-04-10', 'Mensalidade 04/2026', 1, 73, 90.00),
    (74, '2026-04-05', '2026-04-10', 'Mensalidade 04/2026', 2, 74, 90.00),
    (75, '2026-05-09', '2026-05-10', 'Mensalidade 05/2026', 3, 75, 80.00),
    (76, '2026-05-08', '2026-05-10', 'Mensalidade 05/2026', 2, 76, 80.00),
    (77, '2026-05-10', '2026-05-10', 'Mensalidade 05/2026', 3, 77, 70.00),
    (78, '2026-05-06', '2026-05-10', 'Mensalidade 05/2026', 1, 78, 70.00),
    (79, '2026-05-06', '2026-05-10', 'Mensalidade 05/2026', 2, 79, 70.00),
    (80, '2026-05-13', '2026-05-10', 'Mensalidade 05/2026', 2, 80, 90.00),
    (81, '2026-05-11', '2026-05-10', 'Mensalidade 05/2026', 1, 81, 80.00),
    (82, '2026-05-14', '2026-05-10', 'Mensalidade 05/2026', 1, 82, 80.00),
    (83, '2026-05-05', '2026-05-10', 'Apadrinhamento 05/2026', 4, 83, 90.00),
    (84, '2026-05-05', '2026-05-10', 'Apadrinhamento 05/2026', 4, 84, 90.00),
    (85, '2026-05-10', '2026-05-10', 'Mensalidade 05/2026', 2, 85, 80.00),
    (86, '2026-05-10', '2026-05-10', 'Mensalidade 05/2026', 2, 86, 80.00),
    (87, '2026-05-02', '2026-05-10', 'Mensalidade 05/2026', 2, 87, 90.00),
    (88, '2026-05-09', '2026-05-10', 'Mensalidade 05/2026', 1, 88, 90.00),
    (89, '2026-05-11', '2026-05-10', 'Mensalidade 05/2026', 2, 89, 80.00),
    (90, '2026-05-10', '2026-05-10', 'Mensalidade 05/2026', 3, 90, 80.00),
    (91, '2026-05-06', '2026-05-10', 'Mensalidade 05/2026', 1, 91, 90.00),
    (92, '2026-05-07', '2026-05-10', 'Mensalidade 05/2026', 2, 92, 90.00),
    (93, '2026-05-10', '2026-05-10', 'Mensalidade 05/2026', 3, 93, 90.00),
    (94, '2026-05-11', '2026-05-10', 'Mensalidade 05/2026', 2, 94, 90.00),
    (95, '2026-06-12', '2026-06-10', 'Mensalidade 06/2026', 2, 95, 80.00),
    (96, '2026-06-11', '2026-06-10', 'Mensalidade 06/2026', 2, 96, 80.00),
    (97, '2026-06-11', '2026-06-10', 'Mensalidade 06/2026', 3, 97, 70.00),
    (98, '2026-06-06', '2026-06-10', 'Mensalidade 06/2026', 1, 98, 70.00),
    (99, '2026-06-04', '2026-06-10', 'Mensalidade 06/2026', 2, 99, 70.00),
    (100, '2026-06-11', '2026-06-10', 'Mensalidade 06/2026', 2, 100, 90.00);

INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod, contas_receber_cr_cod, pg_valor_pago) VALUES
    (101, '2026-06-08', '2026-06-10', 'Mensalidade 06/2026', 2, 101, 80.00),
    (102, '2026-06-02', '2026-06-10', 'Mensalidade 06/2026', 2, 102, 80.00),
    (103, '2026-06-05', '2026-06-10', 'Apadrinhamento 06/2026', 4, 103, 90.00),
    (104, '2026-06-05', '2026-06-10', 'Apadrinhamento 06/2026', 4, 104, 90.00),
    (105, '2026-06-11', '2026-06-10', 'Mensalidade 06/2026', 2, 105, 80.00),
    (106, '2026-06-14', '2026-06-10', 'Mensalidade 06/2026', 3, 106, 80.00),
    (107, '2026-06-11', '2026-06-10', 'Mensalidade 06/2026', 3, 107, 90.00),
    (108, '2026-06-02', '2026-06-10', 'Mensalidade 06/2026', 2, 108, 90.00),
    (109, '2026-06-03', '2026-06-10', 'Mensalidade 06/2026', 2, 109, 80.00),
    (110, '2026-06-12', '2026-06-10', 'Mensalidade 06/2026', 3, 110, 80.00),
    (111, '2026-06-12', '2026-06-10', 'Mensalidade 06/2026', 2, 111, 90.00),
    (112, '2026-06-11', '2026-06-10', 'Mensalidade 06/2026', 2, 112, 90.00),
    (113, '2026-06-12', '2026-06-10', 'Mensalidade 06/2026', 3, 113, 90.00),
    (114, '2026-06-07', '2026-06-10', 'Mensalidade 06/2026', 2, 114, 90.00),
    (115, '2026-07-10', '2026-07-10', 'Mensalidade 07/2026', 1, 115, 80.00),
    (116, '2026-07-12', '2026-07-10', 'Mensalidade 07/2026', 3, 116, 80.00),
    (117, '2026-07-02', '2026-07-10', 'Mensalidade 07/2026', 3, 117, 70.00),
    (118, '2026-07-05', '2026-07-10', 'Mensalidade 07/2026', 2, 118, 70.00),
    (119, '2026-07-07', '2026-07-10', 'Mensalidade 07/2026', 3, 119, 70.00),
    (120, '2026-07-02', '2026-07-10', 'Mensalidade 07/2026', 1, 120, 90.00),
    (121, '2026-07-12', '2026-07-10', 'Mensalidade 07/2026', 2, 121, 80.00),
    (122, '2026-07-02', '2026-07-10', 'Mensalidade 07/2026', 2, 122, 80.00),
    (123, '2026-07-05', '2026-07-10', 'Apadrinhamento 07/2026', 4, 123, 90.00),
    (124, '2026-07-05', '2026-07-10', 'Apadrinhamento 07/2026', 4, 124, 90.00),
    (125, '2026-07-10', '2026-07-10', 'Mensalidade 07/2026', 2, 125, 80.00),
    (126, '2026-07-03', '2026-07-10', 'Mensalidade 07/2026', 3, 126, 80.00),
    (127, NULL, '2026-07-10', 'Mensalidade 07/2026', 2, 127, 0.00),
    (128, '2026-07-04', '2026-07-10', 'Mensalidade 07/2026', 2, 128, 90.00),
    (129, '2026-07-11', '2026-07-10', 'Mensalidade 07/2026', 2, 129, 80.00),
    (130, '2026-07-05', '2026-07-10', 'Mensalidade 07/2026', 2, 130, 80.00),
    (131, '2026-07-14', '2026-07-10', 'Mensalidade 07/2026', 1, 131, 90.00),
    (132, '2026-07-14', '2026-07-10', 'Mensalidade 07/2026', 3, 132, 90.00),
    (133, '2026-07-13', '2026-07-10', 'Mensalidade 07/2026', 1, 133, 90.00),
    (134, '2026-07-05', '2026-07-10', 'Mensalidade 07/2026', 1, 134, 90.00),
    (135, '2026-08-11', '2026-08-10', 'Mensalidade 08/2026', 3, 135, 80.00),
    (136, '2026-08-13', '2026-08-10', 'Mensalidade 08/2026', 1, 136, 80.00),
    (137, '2026-08-13', '2026-08-10', 'Mensalidade 08/2026', 2, 137, 70.00),
    (138, '2026-08-13', '2026-08-10', 'Mensalidade 08/2026', 2, 138, 70.00),
    (139, '2026-08-08', '2026-08-10', 'Mensalidade 08/2026', 3, 139, 70.00),
    (140, NULL, '2026-08-10', 'Mensalidade 08/2026', 3, 140, 0.00),
    (141, '2026-08-08', '2026-08-10', 'Mensalidade 08/2026', 1, 141, 80.00),
    (142, '2026-08-11', '2026-08-10', 'Mensalidade 08/2026', 3, 142, 80.00),
    (143, '2026-08-05', '2026-08-10', 'Apadrinhamento 08/2026', 4, 143, 90.00),
    (144, '2026-08-05', '2026-08-10', 'Apadrinhamento 08/2026', 4, 144, 90.00),
    (145, '2026-08-02', '2026-08-10', 'Mensalidade 08/2026', 3, 145, 80.00),
    (146, '2026-08-11', '2026-08-10', 'Mensalidade 08/2026', 3, 146, 80.00),
    (147, NULL, '2026-08-10', 'Mensalidade 08/2026', 3, 147, 0.00),
    (148, '2026-08-11', '2026-08-10', 'Mensalidade 08/2026', 2, 148, 90.00),
    (149, '2026-08-14', '2026-08-10', 'Mensalidade 08/2026', 2, 149, 80.00),
    (150, '2026-08-02', '2026-08-10', 'Mensalidade 08/2026', 1, 150, 80.00);

INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod, contas_receber_cr_cod, pg_valor_pago) VALUES
    (151, '2026-08-06', '2026-08-10', 'Mensalidade 08/2026', 3, 151, 90.00),
    (152, '2026-08-07', '2026-08-10', 'Mensalidade 08/2026', 1, 152, 90.00),
    (153, '2026-08-03', '2026-08-10', 'Mensalidade 08/2026', 1, 153, 90.00),
    (154, '2026-08-06', '2026-08-10', 'Mensalidade 08/2026', 2, 154, 90.00),
    (155, '2026-09-06', '2026-09-10', 'Mensalidade 09/2026', 1, 155, 80.00),
    (156, '2026-09-13', '2026-09-10', 'Mensalidade 09/2026', 3, 156, 80.00),
    (157, '2026-09-12', '2026-09-10', 'Mensalidade 09/2026', 2, 157, 70.00),
    (158, '2026-09-04', '2026-09-10', 'Mensalidade 09/2026', 2, 158, 70.00),
    (159, '2026-09-09', '2026-09-10', 'Mensalidade 09/2026', 1, 159, 40.00),
    (160, NULL, '2026-09-10', 'Mensalidade 09/2026', 2, 160, 0.00),
    (161, '2026-09-03', '2026-09-10', 'Mensalidade 09/2026', 2, 161, 80.00),
    (162, '2026-09-13', '2026-09-10', 'Mensalidade 09/2026', 1, 162, 80.00),
    (163, '2026-09-05', '2026-09-10', 'Apadrinhamento 09/2026', 4, 163, 90.00),
    (164, '2026-09-05', '2026-09-10', 'Apadrinhamento 09/2026', 4, 164, 90.00),
    (165, '2026-09-04', '2026-09-10', 'Mensalidade 09/2026', 3, 165, 80.00),
    (166, '2026-09-05', '2026-09-10', 'Mensalidade 09/2026', 2, 166, 80.00),
    (167, NULL, '2026-09-10', 'Mensalidade 09/2026', 1, 167, 0.00),
    (168, '2026-09-04', '2026-09-10', 'Mensalidade 09/2026', 1, 168, 90.00),
    (169, '2026-09-11', '2026-09-10', 'Mensalidade 09/2026', 1, 169, 80.00),
    (170, '2026-09-02', '2026-09-10', 'Mensalidade 09/2026', 1, 170, 80.00),
    (171, '2026-09-09', '2026-09-10', 'Mensalidade 09/2026', 2, 171, 90.00),
    (172, NULL, '2026-09-10', 'Mensalidade 09/2026', 2, 172, 0.00),
    (173, '2026-09-04', '2026-09-10', 'Mensalidade 09/2026', 3, 173, 90.00),
    (174, '2026-10-04', '2026-10-10', 'Mensalidade 10/2026', 2, 174, 80.00),
    (175, '2026-10-02', '2026-10-10', 'Mensalidade 10/2026', 2, 175, 80.00),
    (176, '2026-10-06', '2026-10-10', 'Mensalidade 10/2026', 2, 176, 70.00),
    (177, '2026-10-05', '2026-10-10', 'Mensalidade 10/2026', 2, 177, 70.00),
    (178, '2026-10-11', '2026-10-10', 'Mensalidade 10/2026', 1, 178, 70.00),
    (179, NULL, '2026-10-10', 'Mensalidade 10/2026', 2, 179, 0.00),
    (180, '2026-10-05', '2026-10-10', 'Mensalidade 10/2026', 1, 180, 80.00),
    (181, '2026-10-11', '2026-10-10', 'Mensalidade 10/2026', 3, 181, 80.00),
    (182, '2026-10-05', '2026-10-10', 'Apadrinhamento 10/2026', 4, 182, 90.00),
    (183, '2026-10-05', '2026-10-10', 'Apadrinhamento 10/2026', 4, 183, 90.00),
    (184, NULL, '2026-10-10', 'Mensalidade 10/2026', 2, 184, 0.00),
    (185, NULL, '2026-10-10', 'Mensalidade 10/2026', 2, 185, 0.00),
    (186, NULL, '2026-10-10', 'Mensalidade 10/2026', 2, 186, 0.00),
    (187, NULL, '2026-10-10', 'Mensalidade 10/2026', 3, 187, 0.00),
    (188, '2026-10-12', '2026-10-10', 'Mensalidade 10/2026', 2, 188, 80.00),
    (189, '2026-10-14', '2026-10-10', 'Mensalidade 10/2026', 1, 189, 80.00),
    (190, '2026-10-13', '2026-10-10', 'Mensalidade 10/2026', 2, 190, 90.00),
    (191, NULL, '2026-10-10', 'Mensalidade 10/2026', 2, 191, 0.00),
    (192, '2026-10-08', '2026-10-10', 'Mensalidade 10/2026', 1, 192, 90.00),
    (193, '2026-08-16', '2026-08-16', 'Pgto Passeio Zoológico', 1, 193, 45.00),
    (194, '2026-08-13', '2026-08-16', 'Pgto Passeio Zoológico', 1, 194, 45.00),
    (195, '2026-08-14', '2026-08-16', 'Pgto Passeio Zoológico', 2, 195, 45.00),
    (196, '2026-08-15', '2026-08-16', 'Pgto Passeio Zoológico', 2, 196, 45.00),
    (197, '2026-08-10', '2026-08-16', 'Pgto Passeio Zoológico', 1, 197, 45.00),
    (198, '2026-08-06', '2026-08-16', 'Pgto Passeio Zoológico', 2, 198, 45.00),
    (199, '2026-08-13', '2026-08-16', 'Pgto Passeio Zoológico', 2, 199, 45.00),
    (200, '2026-08-11', '2026-08-16', 'Pgto Passeio Zoológico', 2, 200, 45.00);

INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod, contas_receber_cr_cod, pg_valor_pago) VALUES
    (201, '2026-08-13', '2026-08-16', 'Pgto Passeio Zoológico', 1, 201, 45.00),
    (202, '2026-08-13', '2026-08-16', 'Pgto Passeio Zoológico', 1, 202, 45.00),
    (203, '2026-08-15', '2026-08-16', 'Pgto Passeio Zoológico', 2, 203, 45.00),
    (204, '2026-08-15', '2026-08-16', 'Pgto Passeio Zoológico', 3, 204, 45.00),
    (205, '2026-08-17', '2026-08-16', 'Pgto Passeio Zoológico', 2, 205, 45.00),
    (206, '2026-08-09', '2026-08-16', 'Pgto Passeio Zoológico', 3, 206, 45.00),
    (207, '2026-09-03', '2026-09-12', 'Pgto Acampamento Primavera', 2, 207, 120.00),
    (208, '2026-09-03', '2026-09-12', 'Pgto Acampamento Primavera', 1, 208, 120.00),
    (209, '2026-09-12', '2026-09-12', 'Pgto Acampamento Primavera', 2, 209, 120.00),
    (210, '2026-09-05', '2026-09-12', 'Pgto Acampamento Primavera', 2, 210, 120.00),
    (211, '2026-09-01', '2026-09-12', 'Pgto Acampamento Primavera', 3, 211, 120.00),
    (212, '2026-08-31', '2026-09-12', 'Pgto Acampamento Primavera', 2, 212, 120.00),
    (213, '2026-09-01', '2026-09-12', 'Pgto Acampamento Primavera', 1, 213, 120.00),
    (214, '2026-09-11', '2026-09-12', 'Pgto Acampamento Primavera', 2, 214, 120.00),
    (215, '2026-09-05', '2026-09-12', 'Pgto Acampamento Primavera', 2, 215, 120.00),
    (216, '2026-10-09', '2026-11-06', 'Pgto Jamboree Regional', 2, 216, 250.00),
    (217, '2026-10-09', '2026-11-06', 'Pgto Jamboree Regional', 2, 217, 250.00),
    (218, NULL, '2026-11-06', 'Pgto Jamboree Regional', 3, 218, 0.00),
    (219, NULL, '2026-11-06', 'Pgto Jamboree Regional', 1, 219, 0.00),
    (220, NULL, '2026-11-06', 'Pgto Jamboree Regional', 2, 220, 0.00),
    (221, NULL, '2026-11-06', 'Pgto Jamboree Regional', 2, 221, 0.00),
    (222, NULL, '2026-12-04', 'Pgto Ceia de Natal', 2, 222, 0.00),
    (223, NULL, '2026-12-04', 'Pgto Ceia de Natal', 3, 223, 0.00),
    (224, NULL, '2026-12-04', 'Pgto Ceia de Natal', 2, 224, 0.00),
    (225, NULL, '2026-12-04', 'Pgto Ceia de Natal', 2, 225, 0.00),
    (226, NULL, '2026-12-04', 'Pgto Ceia de Natal', 2, 226, 0.00);

COMMIT;