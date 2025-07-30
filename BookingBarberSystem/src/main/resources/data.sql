-- =======================================================
-- SCRIPT PARA POVOAR E SINCRONIZAR O BANCO DE DADOS
-- =======================================================
/*
-- 1. INSERINDO ROLES
INSERT INTO tb_role (id, authority) VALUES (1, 'ROLE_CLIENTE') ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_role (id, authority) VALUES (2, 'ROLE_PROFISSIONAL') ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_role (id, authority) VALUES (3, 'ROLE_ADMIN') ON CONFLICT (id) DO NOTHING;

-- 2. INSERINDO ESPECIALIDADES
INSERT INTO tb_especialidade (id, nome, tipo) VALUES (1, 'Corte Masculino', 'Cabelo') ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_especialidade (id, nome, tipo) VALUES (2, 'Barba Terapia', 'Barba') ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_especialidade (id, nome, tipo) VALUES (3, 'Coloração', 'Cabelo') ON CONFLICT (id) DO NOTHING;

-- 3. INSERINDO SERVIÇOS
INSERT INTO tb_servico (id, nome, descricao, valor, duracao) VALUES (1, 'Corte de Cabelo Simples', 'Corte clássico na máquina e tesoura.', 50.00, 30) ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_servico (id, nome, descricao, valor, duracao) VALUES (2, 'Corte e Barba', 'Pacote completo de corte de cabelo e design de barba.', 85.00, 60) ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_servico (id, nome, descricao, valor, duracao) VALUES (3, 'Coloração Capilar', 'Aplicação de tintura para cobrir fios brancos ou mudar a cor.', 120.00, 90) ON CONFLICT (id) DO NOTHING;

-- 4. INSERINDO USUARIOS (Clientes e Profissionais)
-- Senha para todos é 'senha123' criptografada com BCrypt
-- Cliente 1 (ID 1)
INSERT INTO tb_usuario (id, nome, sobrenome, cpf, email, password, url_foto_perfil, ativo) VALUES (1, 'Ana', 'Silva', '111.222.333-44', 'ana.silva@email.com', '$2a$10$g.wT4U0sXp.iU3w3i9A5AuF8s3B/LhB2w.e.x/Xy.xZ.Y.z.Y.z.Y', 'url_foto_ana.png', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_cliente (usuario_id, data_criacao, ultima_visita, pontos_fidelidade) VALUES (1, '2025-07-28', null, 50) ON CONFLICT (usuario_id) DO NOTHING;
INSERT INTO usuario_role (usuario_id, role_id) VALUES (1, 1) ON CONFLICT (usuario_id, role_id) DO NOTHING;

-- Cliente 2 (ID 2)
INSERT INTO tb_usuario (id, nome, sobrenome, cpf, email, password, url_foto_perfil, ativo) VALUES (2, 'Bruno', 'Costa', '222.333.444-55', 'bruno.costa@email.com', '$2a$10$g.wT4U0sXp.iU3w3i9A5AuF8s3B/LhB2w.e.x/Xy.xZ.Y.z.Y.z.Y', 'url_foto_bruno.png', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_cliente (usuario_id, data_criacao, ultima_visita, pontos_fidelidade) VALUES (2, '2025-06-15', null, 120) ON CONFLICT (usuario_id) DO NOTHING;
INSERT INTO usuario_role (usuario_id, role_id) VALUES (2, 1) ON CONFLICT (usuario_id, role_id) DO NOTHING;

-- Profissional 1 (ID 3)
INSERT INTO tb_usuario (id, nome, sobrenome, cpf, email, password, url_foto_perfil, ativo) VALUES (3, 'Carlos', 'Souza', '333.444.555-66', 'carlos.souza@barber.com', '$2a$10$g.wT4U0sXp.iU3w3i9A5AuF8s3B/LhB2w.e.x/Xy.xZ.Y.z.Y.z.Y', 'url_foto_carlos.png', true) ON CONFLICT (id) DO NOTHING;
INSERT INTO tb_profissional (usuario_id, numero_registro, data_contratacao, percentual_comissao) VALUES (3, 'REG-12345', '2024-01-10', 0.40) ON CONFLICT (usuario_id) DO NOTHING;
INSERT INTO usuario_role (usuario_id, role_id) VALUES (3, 2) ON CONFLICT (usuario_id, role_id) DO NOTHING;
INSERT INTO profissional_especialidade (profissional_id, especialidade_id) VALUES (3, 1) ON CONFLICT (profissional_id, especialidade_id) DO NOTHING;
INSERT INTO profissional_especialidade (profissional_id, especialidade_id) VALUES (3, 2) ON CONFLICT (profissional_id, especialidade_id) DO NOTHING;


-- 5. SINCRONIZANDO AS SEQUÊNCIAS (A PARTE MAIS IMPORTANTE!)
-- Garante que o próximo ID gerado pela aplicação será maior que o último ID inserido manualmente.
SELECT setval('role_id_seq', (SELECT MAX(id) FROM tb_role));
SELECT setval('especialidade_id_seq', (SELECT MAX(id) FROM tb_especialidade));
SELECT setval('servico_id_seq', (SELECT MAX(id) FROM tb_servico));
SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM tb_usuario));

-- Tabelas sem ID próprio (como cliente e profissional) não possuem sequência própria, pois usam o ID de 'usuario'.
-- Tabelas de junção (como usuario_role) também não possuem sequência.*/

