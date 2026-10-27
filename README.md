# API Servidor

API REST de gerenciamento de **usuários, produtos e pedidos** com controle de estoque, construída com Java e Spring Boot. Projeto de estudo focado em boas práticas de backend: camadas bem separadas, DTOs, validação, tratamento global de erros e regras de negócio dentro do domínio.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-336791)
![Maven](https://img.shields.io/badge/Maven-build-C71A36)

## Funcionalidades

- **Usuários**: cadastro e consulta (e-mail único, senha com mínimo de 8 caracteres).
- **Produtos**: CRUD completo, com entrada e saída manual de estoque.
- **Pedidos**: criação com múltiplos itens, pagamento e cancelamento.
  - O estoque é baixado automaticamente ao criar o pedido.
  - Ao cancelar, o estoque é devolvido.
  - O preço do item é gravado no momento da compra (`unitPrice`) e o total é calculado a partir dos itens.
- **Validação** dos dados de entrada com Bean Validation.
- **Erros padronizados** em JSON (404 para recurso não encontrado, 400 para regra de negócio violada).
- **Documentação interativa** com Swagger UI.

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA) |
| Banco de dados | PostgreSQL |
| Validação | Jakarta Bean Validation |
| Documentação | springdoc-openapi (Swagger UI) |
| Build | Maven (Maven Wrapper incluso) |

## Arquitetura

O projeto segue uma arquitetura em camadas:

```
src/main/java/com/api/arthur
├── controllers/   # Endpoints REST (entrada e saída HTTP)
├── services/      # Casos de uso e transações
├── repositorys/   # Acesso a dados (Spring Data JPA)
├── entitys/       # Entidades JPA e regras de negócio do domínio
├── dtos/          # Records de request/response
├── enums/         # Role e OrderStatus
└── exceptions/    # Exceções de negócio e handler global
```

Algumas decisões de design:

- **DTOs como `record`**: a API nunca expõe as entidades diretamente (a senha do usuário, por exemplo, não aparece nas respostas).
- **Regras de negócio na entidade**: `Order.pay()`, `Order.cancel()` e `Product.removeStock()` validam o próprio estado, em vez de deixar essa lógica espalhada nos services.
- **IDs do tipo `UUID`** em todas as entidades.
- **`@Transactional`** nos services, garantindo que pedido e baixa de estoque sejam gravados juntos ou não sejam gravados.

### Ciclo de vida do pedido

```
CREATED ──pay()──▶ PAID
   │
   └──cancel()──▶ CANCELED   (devolve o estoque)
```

Só pedidos com status `CREATED` podem ser pagos ou cancelados.

## Como executar

### Pré-requisitos

- JDK 21
- PostgreSQL em execução
- (Opcional) Maven instalado. O projeto já inclui o wrapper (`mvnw`)

### 1. Clonar o repositório

```bash
git clone https://github.com/arthurzanellakoerich/api-servidor.git
cd api-servidor
```

### 2. Criar o banco de dados

```sql
CREATE DATABASE api_arthur;
```

### 3. Configurar a senha do banco

A senha é lida da variável de ambiente `DB_PASSWORD` (ela **não** fica no código):

```bash
# Linux / macOS
export DB_PASSWORD=sua_senha

# Windows (PowerShell)
$env:DB_PASSWORD="sua_senha"
```

A URL e o usuário padrão estão em `src/main/resources/application.properties` (`localhost:5432`, usuário `postgres`). Ajuste se o seu ambiente for diferente.

### 4. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. As tabelas (`tb_users`, `tb_products`, `tb_orders`, `tb_order_items`) são criadas automaticamente pelo Hibernate (`ddl-auto=update`).

## Documentação da API

Com a aplicação rodando, acesse o Swagger UI:

**http://localhost:8080/swagger-ui.html**

## Endpoints

### Usuários

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/users` | Lista todos os usuários |
| `GET` | `/users/{id}` | Busca usuário por ID |
| `POST` | `/users` | Cria um usuário |

### Produtos

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/products` | Lista todos os produtos |
| `GET` | `/products/{id}` | Busca produto por ID |
| `POST` | `/products` | Cria um produto |
| `PUT` | `/products/{id}` | Atualiza um produto |
| `DELETE` | `/products/{id}` | Remove um produto |
| `POST` | `/products/{id}/stock-in` | Adiciona estoque |
| `POST` | `/products/{id}/stock-out` | Remove estoque |

### Pedidos

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/orders` | Lista todos os pedidos |
| `GET` | `/orders/{id}` | Busca pedido por ID |
| `POST` | `/orders` | Cria um pedido (baixa o estoque) |
| `POST` | `/orders/{id}/pay` | Marca o pedido como pago |
| `POST` | `/orders/{id}/cancel` | Cancela o pedido (devolve o estoque) |

## Exemplos de uso

**Criar um usuário**

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Maria", "email": "maria@email.com", "password": "senha1234"}'
```

**Criar um produto**

```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado Mecânico", "price": 249.90}'
```

**Adicionar estoque**

```bash
curl -X POST http://localhost:8080/products/{productId}/stock-in \
  -H "Content-Type: application/json" \
  -d '{"quantity": 10}'
```

**Criar um pedido**

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{userId}",
    "items": [
      { "productId": "{productId}", "quantity": 2 }
    ]
  }'
```

**Resposta de erro padronizada** (exemplo: estoque insuficiente)

```json
{
  "timestamp": "2026-10-27T18:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Estoque insuficiente. Disponível: 1",
  "path": "/orders"
}
```

## Próximos passos

- [ ] Autenticação e autorização com Spring Security + JWT
- [ ] Hash de senha com BCrypt (hoje a senha ainda é salva em texto puro, por isso o projeto **não está pronto para produção**)
- [ ] Testes unitários e de integração
- [ ] Dockerfile e `docker-compose` (API + PostgreSQL)
- [ ] Migrations com Flyway no lugar do `ddl-auto=update`
- [ ] Paginação nas listagens
- [ ] Tratamento global de erros de validação (`MethodArgumentNotValidException`)

## Autor

**Arthur Zanella Koerich**
Estudante de Ciência da Computação (FURB) | Desenvolvedor Java backend em formação

[GitHub](https://github.com/arthurzanellakoerich)
