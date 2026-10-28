# Cadastro, login e autorização

- `POST /usuarios`: nome, email, senha, tipo CLIENTE ou LOJA. LOJA exige whatsapp com 10 a 13 dígitos. ADMIN não pode ser criado por cadastro público.
- `POST /auth/login`: email e senha. Resposta com token, tipo Bearer, expiraEmMs e usuario (id, nome, email, role).
- `GET /auth/me`: usuário autenticado pelo token.

Rotas privadas recebem `Authorization: Bearer <token>`. Token ausente, inválido ou expirado recebe 401; papel sem permissão recebe 403. Senhas são armazenadas com BCrypt; nenhuma senha é devolvida nas respostas.

Anúncios, loja e encontros são administrados por LOJA. O serviço também confere propriedade, independentemente do papel enviado no token. CLIENTE cria pedidos; cliente e loja consultam apenas seus próprios pedidos. Aceite é exclusivo da loja proprietária. Upload exige autenticação.

`JWT_SECRET` é obrigatório fora do modo local; não há chave de produção padrão. `JWT_EXPIRATION_MS` tem padrão de oito horas. O iniciador local gera uma chave aleatória em arquivo ignorado. As chaves fixas em testes são apenas fixtures do banco de testes.

Logout no aplicativo remove o token local, sem revogação no servidor. Não há refresh token. Consulte INTEGRACAO-LOCAL.md e IMPLANTACAO.md na raiz para limites e configuração.
