# 🧩 Desafio Técnico Fullstack – JTech
### Sistema TODO List Multiusuário com Arquitetura Avançada

---

## 🚀 Visão Geral da Arquitetura

O projeto foi desenvolvido seguindo uma **arquitetura modular e em camadas**, garantindo organização, escalabilidade e facilidade de manutenção.  
A separação entre **frontend e backend** foi mantida conforme solicitado no desafio, permitindo o desacoplamento total entre a interface e as regras de negócio.

No **backend**, a aplicação foi construída em **Spring Boot (Java 17+)**, adotando uma estrutura limpa e padronizada composta por camadas bem definidas:

- **Controller (adapters/input):** expõe os endpoints REST e intermedia as requisições HTTP.
- **Service (core/domain):** concentra as regras de negócio, validações e orquestra as operações.
- **Repository (adapters/output):** realiza o acesso e manipulação de dados via JPA/Hibernate.
- **Security:** autenticação e autorização com JWT e Spring Security.
- **Exception Handling:** tratamento centralizado de erros para respostas consistentes.
- **DTOs e Validation:** validação de dados com Spring Validation.

O backend segue o padrão **RESTful**, garantindo previsibilidade nas rotas e retornos.  
A configuração de **CORS**, autenticação e rotas protegidas foram implementadas conforme solicitado, permitindo comunicação segura entre o **Vue.js** e o **Spring Boot**.

No **frontend**, foi utilizado **Vue.js 3**, seguindo os padrões definidos no desafio.  
A aplicação foi construída com **componentes reutilizáveis**, **stores centralizadas (Pinia)** e **rotas protegidas**, garantindo uma navegação fluida e segura.  
Para melhorar a experiência visual e responsividade, foi adicionado o **Bootstrap**, mantendo o layout moderno e consistente sem alterar as dependências exigidas.

Toda a arquitetura prioriza **simplicidade, clareza e boas práticas**, com autenticação baseada em JWT, proteção de rotas, isolamento de responsabilidades e cobertura de testes automatizados.

---

## ⚙️ Stack Tecnológica

O projeto foi desenvolvido com uma stack moderna, priorizando desempenho, segurança e boas práticas.

### 🖥️ Backend
- **Linguagem:** Java 17+
- **Framework:** Spring Boot
- **Segurança:** Spring Security + JWT + BCrypt
- **Persistência:** Spring Data JPA + Hibernate
- **Banco de Dados:** PostgreSQL *(sugerido no desafio)*
- **Testes:** JUnit 5, Mockito e Spring Boot Test

### 💻 Frontend
- **Framework:** Vue.js 3 (Composition API)
- **Gerenciamento de Estado:** Pinia
- **Roteamento:** Vue Router
- **UI/UX:** Bootstrap 5  Vuetify
- **Build Tool:** Vite + TypeScript

---

## 🧰 Como Rodar Localmente

Primeiro, baixe o projeto no link disponibilizado.  
Verifique se o **Java 17 ou superior** está instalado requisito fundamental para o backend funcionar corretamente.


Antes de rodar o projeto, configure o banco de dados via Docker.  
O arquivo `docker-compose.yml` está localizado em:

```
jtech-tasklist-backend/composer/docker-compose.yml
```

Execute os comandos abaixo:

```bash
cd fullstack2\jtech-tasklist-backend\composer
docker compose up -d
```

Após o container do PostgreSQL estar ativo, crie a estrutura do banco:

```bash
docker exec -i tasklist_db psql -U postgres -d tasklist < db/001_init.sql
```

O script de inicialização está localizado em:
```
fullstack2\jtech-tasklist-backend\db\001_init.sql
```

Com o ambiente pronto, gere o build do backend com o comando:

```bash
gradle build
```

Com tudo configurado, o backend já pode ser executado.  
As configurações de conexão com o banco local (Docker) estão definidas no arquivo `application.yml`.

Para o **frontend**, é necessário ter o **Node.js** instalado.  
Dentro da pasta do projeto frontend, execute:

```bash
npm install
npm run dev
```

A aplicação ficará disponível localmente, permitindo cadastro e login de usuários.  
Para ativar o modo **mock**, altere a variável:
```
VITE_USE_MOCK=false → true
```

---

## 🧪 Como Rodar os Testes

Os testes automatizados do backend foram desenvolvidos com **JUnit 5**, **Mockito** e **Spring Boot Test**.  
Para executá-los, basta rodar no terminal, dentro da raiz do backend:

```bash
gradle test
```

Se quiser limpar e gerar novamente o relatório completo, use:

```bash
gradle clean test
```

O relatório detalhado é gerado automaticamente em:

```
/build/reports/tests/test/index.html
```

Esses testes validam autenticação, criação e gerenciamento de tarefas, garantindo a estabilidade e segurança da aplicação.

Para executá-los, basta rodar no terminal, dentro da raiz do frontend:

```bash
npm run test

```

---

## 🗂️ Estrutura de Pastas Detalhada

### 🖥️ Backend – `jtech-tasklist-backend`

Arquitetura modular e em camadas, com foco em clareza e testabilidade.

```
src/
 ├─ main/java/br/com/jtech/tasklist
 │   ├─ adapters
 │   │   ├─ input/controllers      → Controladores REST
 │   │   ├─ input/dtos             → Objetos de transferência de dados
 │   │   ├─ input/protocols        → Contratos de entrada/saída
 │   │   └─ output/repositories    → Implementações dos repositórios JPA
 │   ├─ application
 │   │   ├─ core/ports             → Interfaces (ports) do domínio
 │   │   ├─ config/infra           → Configurações gerais e utilitários
 │   │   ├─ config/security        → Autenticação JWT e filtros
 │   │   └─ usecases               → Casos de uso e orquestração
 │   └─ ...
 ├─ test/java                      → Testes com JUnit e Mockito
 ├─ db/001_init.sql                → Script de criação da base de dados
 ├─ composer/docker-compose.yml    → Subida do PostgreSQL via Docker
 └─ application.yml                → Configuração principal
```

---

### 💻 Frontend – `jtech-tasklist-frontend`

Desenvolvido em Vue.js 3 com Composition API e TypeScript, seguindo uma estrutura modular clara.

### 🧩 Comandos utilizados para criar os componentes do frontend
```
# Criar novo componente Vue
touch src/components/NomeDoComponente.vue

# Criar nova view (página)
touch src/views/NomeDaView.vue

# Criar nova store (gerenciamento de estado Pinia)
touch src/stores/useNomeStore.ts

# Criar novo serviço (API ou lógica de negócio)
touch src/core/services/NomeDoServico.ts

# Criar novo modelo (tipagem TypeScript)
touch src/core/models/NomeDoModelo.ts

# Criar novo teste de componente
touch src/components/__tests__/NomeDoComponente.spec.ts

# Criar nova rota (editar manualmente)
src/router/index.ts

# Rodar o projeto localmente
npm run dev

# Executar testes
npm run test
```

```
src/
 ├─ api/              → Serviços de comunicação (Axios e mocks)
 ├─ app/              → Configurações principais (main.ts, vuetify.ts)
 ├─ assets/           → Arquivos estáticos (imagens, estilos)
 ├─ components/       → Componentes reutilizáveis e testes de UI
 ├─ core/
 │   ├─ models/       → Tipagens e modelos de dados
 │   ├─ services/     → Classes de comunicação e regras de negócio
 │   └─ utils/        → Funções auxiliares (ex.: storage.ts)
 ├─ router/           → Definição e proteção de rotas
 ├─ stores/           → Gerenciamento de estado (Pinia)
 └─ views/            → Páginas principais (Login, Dashboard, Tarefas)
```

---

## 🧠 Decisões Técnicas Aprofundadas

Todas as decisões foram tomadas conforme as diretrizes do desafio.  
O **PostgreSQL** foi adotado conforme a recomendação do enunciado.  
Mantive o **Gradle** em vez de Maven, mesmo sendo meu padrão habitual, para respeitar o projeto base e garantir compatibilidade.  
No **frontend**, utilizei o **Vue.js 3**, mesmo sendo minha primeira experiência prática com o framework. Busquei compreender sua estrutura e aplicar corretamente rotas protegidas, stores e comunicação com o backend.  
Adicionei apenas o **Bootstrap 5** para melhorar a aparência e responsividade, sem alterar o comportamento original.  
Todas as escolhas priorizaram fidelidade ao desafio, clareza e código funcional.

---

## 🚀 Melhorias e Roadmap

Como sugestões de melhoria, proponho evoluir o backend com ajustes de performance, monitoramento e auditoria.  
Já implementei **triggers no banco de dados** para criar uma base simples de auditoria, registrando alterações automaticamente.  
No frontend, recomendo aprimorar a experiência com um tema escuro e notificações em tempo real.  
Pensando em longo prazo, o sistema pode evoluir para uma **arquitetura modular ou baseada em microservices**, facilitando a escalabilidade e o crescimento de novas funcionalidades sem impactar o núcleo principal.

---

✨ **Autor:** Pedro Félix  
🧠 **Desafio Técnico:** Fullstack JTech  
📅 **Entrega:** Projeto completo com backend Spring Boot e frontend Vue.js conforme especificações.  
