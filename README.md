# 🚚 Djelog Solutions Back

Backend do sistema **Djelog Solutions**, responsável pela API REST da aplicação logística. A API centraliza autenticação, controle de usuários e operações de negócio relacionadas a profissionais/motoristas, veículos, empresas, viagens, viagens comissionadas, despesas, estadias e relatórios.

## ✨ Principais Recursos

- 🔐 Cadastro e login de usuários.
- 🎫 Autenticação stateless com JWT.
- 🔒 Criptografia de senha com BCrypt.
- 🧾 CRUD de empresas, profissionais, veículos, viagens, viagens comissionadas, despesas e estadias.
- 📈 Relatório de viagens por período.
- 🗄️ Persistência com JPA/Hibernate.
- 🐘 Banco PostgreSQL em ambiente real e H2 para testes.
- 🐳 Dockerfile multi-stage para build e execução em container.

## 🧰 Stack e Frameworks

- Java 21.
- Spring Boot 3.2.2.
- Spring Web.
- Spring Data JPA.
- Spring Security.
- JJWT 0.11.5.
- PostgreSQL Driver.
- H2 Database para testes.
- Maven Wrapper.

## 🗂️ Estrutura Relevante

```text
src/main/java/com/djelog/
  config/          Configurações de ambiente, CORS e segurança
  controllers/     Controllers REST
  dtos/            Objetos de entrada/saída da API
  entities/        Entidades JPA
  repositories/    Repositórios Spring Data
  security/        Filtro JWT e UserDetailsService
  services/        Regras de negócio e serviços auxiliares

src/main/resources/
  application.properties
  db/              Scripts SQL históricos/evolutivos do schema
```

## ✅ Pré-requisitos

- Java 21.
- Maven, ou o Maven Wrapper incluído no projeto.
- PostgreSQL acessível pela aplicação.

## 🔐 Variáveis de Ambiente

A aplicação lê variáveis diretamente do ambiente e também tenta carregar um arquivo `.env` na raiz do projeto (`file:${user.dir}/.env`).

Crie um arquivo `.env` em `djelog-solutions-back` para desenvolvimento local:

```properties
DB_URL=jdbc:postgresql://localhost:5432/djelog
DB_USERNAME=postgres
DB_PASSWORD=postgres

SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=2
SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=30000
SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=1800000
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000

JWT_SECRET=troque_por_um_segredo_com_pelo_menos_32_caracteres
JWT_EXPIRATION_MS=86400000
PORT=8080
```

### 📌 Descrição das Variáveis

- `DB_URL`: URL JDBC do PostgreSQL.
- `DB_USERNAME`: usuário do banco.
- `DB_PASSWORD`: senha do banco.
- `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE`: número máximo de conexões do pool.
- `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE`: conexões ociosas mínimas.
- `SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT`: tempo máximo de ociosidade em milissegundos.
- `SPRING_DATASOURCE_HIKARI_MAX_LIFETIME`: vida máxima de uma conexão em milissegundos.
- `SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT`: timeout para obter conexão em milissegundos.
- `JWT_SECRET`: chave usada para assinar tokens JWT.
- `JWT_EXPIRATION_MS`: tempo de expiração do token em milissegundos.
- `PORT`: porta HTTP da aplicação. O padrão é `8080`.

## 🚀 Como Iniciar em Desenvolvimento

No Windows:

```bash
mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Com `PORT=8080`, a API fica disponível em:

```text
http://localhost:8080
```

## 📦 Build

No Windows:

```bash
mvnw.cmd clean package
```

No Linux/macOS:

```bash
./mvnw clean package
```

O artefato `.jar` será gerado em `target/`.

## 🧪 Testes

No Windows:

```bash
mvnw.cmd test
```

No Linux/macOS:

```bash
./mvnw test
```

Os testes usam `src/test/resources/application-test.properties`, com banco H2 em memória configurado em modo PostgreSQL.

## 🐳 Docker

O projeto possui Dockerfile multi-stage:

- build com `maven:3.9.6-eclipse-temurin-21`;
- runtime com `eclipse-temurin:21-jre-jammy`.

Build da imagem:

```bash
docker build -t djelog-solutions-back .
```

Execução:

```bash
docker run --env-file .env -p 8080:8080 djelog-solutions-back
```

## 🗄️ Banco de Dados

A configuração principal usa PostgreSQL:

```properties
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

O Hibernate atualiza o schema automaticamente em tempo de execução. A pasta `src/main/resources/db` contém scripts SQL históricos/evolutivos que ajudam a entender e preparar a estrutura do banco quando necessário.

## 🔐 Autenticação e Segurança

- `POST /api/auth/register` e `POST /api/auth/login` são públicos.
- Tokens JWT são validados pelo filtro `JwtAuthenticationFilter`.
- A aplicação usa sessão stateless (`SessionCreationPolicy.STATELESS`).
- Senhas são armazenadas com BCrypt.
- O frontend deve enviar o token no header:

```text
Authorization: Bearer <token>
```

## 🌐 CORS

As origens permitidas atualmente são:

- `http://localhost:4200`
- `https://djelogweb.netlify.app`

Caso o frontend seja publicado em outro domínio, adicione a nova origem em `SecurityConfig`.

## 🧭 Endpoints Principais

- `POST /api/auth/register`: cadastra usuário.
- `POST /api/auth/login`: autentica usuário e retorna JWT.
- `PUT /api/auth/usuario/{id}`: atualiza usuário.
- `GET /api/cargo/findAll`: lista cargos.
- `/api/profissional`: consulta e manutenção de profissionais.
- `/api/veiculo`: consulta e manutenção de veículos.
- `/api/empresa`: consulta e manutenção de empresas.
- `/api/viagem`: consulta e manutenção de viagens.
- `GET /api/viagem/excel/dados`: dados de relatório por período.
- `/api/viagem-comissionada`: consulta e manutenção de viagens comissionadas.
- `/api/estadias`: consulta e manutenção de estadias.
- `/api/despesas`: consulta e manutenção de despesas.