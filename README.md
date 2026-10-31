# Spring Boot List — guia de estudo

Aplicação web de cadastro de produtos. Ela demonstra o fluxo completo de uma
aplicação Spring MVC tradicional: o navegador envia uma requisição, um
controller decide o que fazer, um service aplica as regras, um repository
acessa o banco de dados e uma página Thymeleaf apresenta a resposta.

Este guia descreve o que o código faz hoje, explica as responsabilidades de
cada camada e fornece um roteiro para executar e estudar o projeto.

**Autores: Lucas Cordeiro e Lucas Kauan.**

## 1. Visão geral do fluxo

```text
Navegador
   │ HTTP + autenticação HTTP Basic
   ▼
ProductController ── recebe a requisição, valida DTOs e prepara o Model
   │ chama os casos de uso
   ▼
ProductService ───── aplica regras e coordena as transações
   │ usa
   ▼
ProductRepository ── Spring Data JPA
   │ Hibernate/JDBC converte entidades em SQL
   ▼
MySQL ─────────────── persiste as linhas de produtos

Resposta:
MySQL → Repository → Entidade Product → ProductResponseDTO
       → Controller/Model → Thymeleaf → HTML → Navegador
```

O controller não deve montar SQL nem conter regras de persistência. O
repository não deve decidir qual página será exibida. Cada camada tem uma
responsabilidade e conversa com a próxima por métodos Java e objetos.

### Fluxo de dados em cada operação

- **Listar:** navegador → controller → service → repository → banco; as
  entidades voltam como `ProductResponseDTO`, são colocadas no `Model` e
  Thymeleaf as transforma na tabela HTML.
- **Cadastrar/editar:** o formulário HTML envia um `ProductRequestDTO` por
  POST; o controller valida os campos, o service cria ou atualiza a entidade,
  e o repository persiste a alteração no banco. O navegador recebe um
  redirecionamento para a lista.
- **Excluir:** o formulário envia POST com token CSRF; controller → service →
  repository remove a entidade, e o browser volta à lista.

## 2. Tecnologias principais

- **Java 21**: linguagem da aplicação.
- **Maven**: dependências, compilação e execução dos testes.
- **Spring Boot**: configuração automática e inicialização da aplicação.
- **Spring MVC**: roteamento de URLs e controllers web.
- **Thymeleaf**: renderização de HTML no servidor.
- **Spring Data JPA / Hibernate**: persistência de objetos Java em tabelas.
- **MySQL**: banco de dados relacional.
- **Jakarta Bean Validation**: validação de dados recebidos pelos formulários.
- **Bootstrap**: estilos e componentes visuais das páginas.
- **Lombok**: geração de métodos comuns na entidade `Product`.

## 3. Estrutura de pastas

```text
src/
├── main/
│   ├── java/com/lab/mvc/springbootlist/
│   │   ├── SpringBootListApplication.java  # ponto de entrada
│   │   ├── controller/                     # rotas HTTP e páginas
│   │   ├── dto/                            # dados de entrada e saída
│   │   ├── model/                          # entidade persistida
│   │   ├── repository/                     # acesso a dados
│   │   └── service/                        # operações e regras de negócio
│   └── resources/
│       ├── application.properties           # configurações
│       └── templates/                       # páginas Thymeleaf
└── test/
    └── java/com/lab/mvc/springbootlist/     # testes automatizados
```

### Ponto de entrada

`SpringBootListApplication` contém o método `main`. A anotação
`@SpringBootApplication` habilita a configuração automática do Spring Boot e a
busca de componentes a partir do pacote da classe. Por isso, controller,
service e repository ficam em subpacotes desse pacote principal.

### Controller

`ProductController` recebe requisições em `/products`. As anotações
`@GetMapping` e `@PostMapping` ligam métodos Java a caminhos e verbos HTTP. O
controller prepara o `Model` com os dados que o template usará e retorna o
nome da página, como `"index"`.

As rotas exigem autenticação. A exclusão é POST, não GET, porque acessar um
link GET não deve alterar dados.

### Service

`ProductService` concentra operações do catálogo: listar, localizar, salvar e
excluir. Ele é o lugar adequado para regras que não pertencem à apresentação
nem ao mecanismo de banco. `@Service` registra a classe como componente do
Spring. `@Transactional` define os limites das operações de persistência.

### Repository

`ProductRepository` estende `JpaRepository<Product, Long>`. O Spring cria a
implementação automaticamente. Isso fornece operações como `findAll`,
`findById`, `save` e `delete` sem escrever SQL para esses casos básicos.

### Model / entidade

`Product` representa um produto e uma linha da tabela gerenciada pelo JPA.
`@Entity` marca a classe como persistente, `@Id` indica a chave primária e
`@GeneratedValue` deixa o banco gerar o identificador. `@Column` define
restrições de coluna. As anotações de validação na entidade protegem também
eventuais fluxos que não passem pelo formulário web.

### DTOs

- `ProductRequestDTO` representa os campos recebidos do formulário. Suas
  anotações de validação permitem rejeitar dados inválidos antes da chamada ao
  service.
- `ProductResponseDTO` representa os dados expostos para a listagem e para
  preencher a edição. O construtor que recebe `Product` faz a conversão de
  entidade para DTO.

DTO significa *Data Transfer Object*. Separar DTO e entidade evita vincular
diretamente o formato da tela ao formato interno do banco e permite controlar
quais dados entram e saem.

### Templates

- `index.html`: tabela do catálogo e links de ação.
- `new_product.html`: formulário de cadastro.
- `edit_product.html`: formulário de edição com o ID somente para leitura.
- `validacao.html`: fragmento para exibir erros do formulário.
- `alert.html`: mensagens de sucesso e falha transferidas entre requisições.

Thymeleaf usa atributos `th:*` para inserir dados e URLs no HTML. Por exemplo,
`th:each` percorre a lista de produtos, `th:text` insere texto escapado e
`th:field` conecta um campo do formulário a uma propriedade do objeto
`product`.

## 4. Caminhos de uso e ciclo de requisição

| Ação | Método e caminho | Resultado |
| --- | --- | --- |
| Listar produtos | `GET /products` | Busca produtos e renderiza `index.html`. |
| Abrir cadastro | `GET /products/new` | Cria DTO vazio e renderiza `new_product.html`. |
| Criar ou atualizar | `POST /products/save` | Valida o DTO, persiste e redireciona à lista. |
| Abrir edição | `GET /products/edit/{id}` | Busca produto e renderiza `edit_product.html`. |
| Excluir | `POST /products/delete/{id}` | Exige autenticação e token CSRF, exclui e redireciona à lista. |

### Exemplo: cadastro

1. O usuário abre `/products/new`.
2. `showCreateForm` adiciona ao `Model` um `ProductRequestDTO` vazio.
3. Thymeleaf usa `th:object="${product}"` e os `th:field` para montar o
   formulário.
4. O navegador envia os dados para `/products/save` usando POST.
5. `@Valid` executa as validações do DTO. `BindingResult`, que precisa vir
   imediatamente depois do argumento validado, contém os erros.
6. Se houver erros, o controller volta ao template do formulário para mostrar
   as mensagens. Se não houver, o service cria a entidade e o repository a
   persiste.
7. O controller usa o padrão **Post/Redirect/Get**: após salvar, redireciona
   para `/products`, evitando que atualizar a página repita o POST.
8. A mensagem `success` é um *flash attribute*: sobrevive ao redirecionamento
   seguinte e depois desaparece.

### Exemplo: edição

`showEditProductPage` busca o produto pelo ID, recebe um DTO de resposta,
converte-o para DTO de formulário e o coloca no `Model`. Ao enviar, o ID
existente indica ao service que a intenção é atualizar. O service carrega a
entidade persistida e altera seus campos; um ID inexistente gera
`EntityNotFoundException`, em vez de criar silenciosamente outro produto.

### Exemplo: listagem

O service converte cada `Product` em `ProductResponseDTO`. O template percorre
`listProducts` com `th:each` e apresenta os valores. Essa conversão mantém a
camada de apresentação independente da entidade JPA.

## 5. Validação e tratamento de inexistência

As restrições no DTO incluem campos obrigatórios, tamanhos de texto e formato
numérico do preço. `@Valid` aciona essas regras durante a vinculação do POST.
O HTML também informa limites como `min` e `step`, mas validação no navegador é
apenas conveniência: ela pode ser desativada, por isso as regras do servidor
são essenciais.

O service lança `EntityNotFoundException` quando uma consulta por ID não
encontra produto. O controller transforma essa situação em HTTP 404. Isso se
aplica à abertura da edição, à atualização com ID inválido e à exclusão.

## 6. Configuração e execução local

### Pré-requisitos

1. JDK 21.
2. MySQL em execução.
3. Um usuário MySQL dedicado, com apenas as permissões necessárias no banco.

Por padrão, o projeto tenta conectar a
`jdbc:mysql://localhost:3306/sales?createDatabaseIfNotExist=true`. Não há mais
usuário ou senha MySQL padrão: configure as variáveis antes de executar:

```powershell
$env:DB_USERNAME = "usuario_do_banco"
$env:DB_PASSWORD = "senha_do_banco"
$env:DB_URL = "jdbc:mysql://localhost:3306/sales?createDatabaseIfNotExist=true"
$env:APP_SECURITY_USERNAME = "administrador"
$env:APP_SECURITY_PASSWORD = "uma_senha_forte"
```

As credenciais são lidas de variáveis de ambiente. Não grave senhas reais em
arquivos versionados nem as envie ao GitHub. O `.gitignore` exclui arquivos
`.env` e configurações locais; ainda assim, confira os arquivos staged antes
de cada commit. A aplicação falha na inicialização se as credenciais de
administração estiverem ausentes, em vez de iniciar sem proteção.

O acesso web usa HTTP Basic e um usuário administrador em memória. Em cada
inicialização, o Spring cria esse usuário a partir de
`APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD`, armazenando a senha
codificada com um `PasswordEncoder`. O token CSRF fica habilitado e é inserido
nos formulários POST renderizados por Thymeleaf. Como HTTP Basic transmite
credenciais em cada requisição, use HTTPS sempre que a aplicação não estiver
restrita a um ambiente local confiável. O usuário em memória é adequado para
estudo; uma aplicação multiusuário deve usar armazenamento de identidades e
políticas de autorização apropriados.

Os testes utilizam credenciais fictícias (`test-admin` e
`test-only-password`) apenas no arquivo de configuração de teste, junto com um
banco H2 descartável. Elas não são credenciais de execução nem devem ser
reutilizadas.

`spring.jpa.show-sql=false` evita imprimir consultas por padrão. O
`spring.jpa.hibernate.ddl-auto=update` continua sendo uma conveniência de
desenvolvimento; para produção, prefira migrações versionadas, por exemplo
Flyway ou Liquibase, e uma estratégia explícita de backup/migração.

### Executar

Na raiz do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

Depois, abra `http://localhost:8080/products`.

### Verificar compilação e testes

```powershell
.\mvnw.cmd test
```

Os testes usam um banco H2 em memória, configurado somente em
`src/test/resources/application.properties`. Assim, nem os testes unitários do
service nem o teste de contexto da aplicação (`SpringBootListApplicationTests`)
precisam de um servidor MySQL. A configuração H2 não substitui nem altera o
MySQL usado quando a aplicação é executada normalmente.

## 7. Decisões importantes para estudo

- **Injeção de dependência por construtor**: as dependências são obrigatórias,
  explícitas e fáceis de substituir em testes.
- **`BigDecimal` para dinheiro**: evita erros de precisão binária comuns em
  `float` e `double`.
- **Persistência de atualização**: carregar a entidade existente antes de
  alterar os campos distingue atualização de inserção e permite detectar IDs
  inválidos.
- **Transações**: uma operação anotada com `@Transactional` executa como uma
  unidade no banco. Consultas usam `readOnly = true` para expressar intenção.
- **Validação em camadas**: formulário melhora a experiência; validação
  servidor protege a aplicação independentemente do cliente.
- **Respostas HTTP**: `GET` consulta ou apresenta; `POST` envia dados que
  alteram o estado. A exclusão usa POST e a proteção CSRF ajuda a evitar que
  outro site induza o navegador autenticado a enviar uma ação sem consentimento.
- **Autenticação**: HTTP Basic protege todas as rotas; segredos são configurados
  fora do código. Não exponha Basic Auth sem HTTPS.
- **Mensagens de erro**: neste exemplo, uma entidade inexistente é retornada
  como HTTP 404. Uma aplicação maior pode criar uma página de erro amigável e
  um tratamento global com `@ControllerAdvice`.
- **`ddl-auto=update`**: útil no início, mas não substitui migrações e não deve
  ser tratado como estratégia de evolução de esquema em produção.

## 8. Roteiro sugerido para continuar aprendendo

1. Depure um GET de listagem e observe a chamada do controller ao service.
2. Coloque breakpoints no POST de cadastro e inspecione `ProductRequestDTO`,
   `BindingResult` e o objeto `Product` antes do `save`.
3. Envie campos vazios e observe como os erros aparecem no formulário.
4. Experimente um ID inexistente e confirme o status 404.
5. Adicione um campo (por exemplo, descrição) em entidade, DTOs, formulários,
   tabela e testes; acompanhe todas as camadas afetadas.
6. Crie testes de controller com MockMvc e testes de persistência com um banco
   de teste separado.
7. Evolua o usuário administrativo em memória para identidades persistidas,
   papéis e permissões, e use migrações para o esquema.

## 9. Diagnóstico rápido

- **Falha de conexão**: confira se o MySQL está ativo, se a porta/URL, usuário e
  senha correspondem às variáveis `DB_*` e se o usuário pode acessar o banco.
- **Erro de compilação em `dto`**: os DTOs precisam estar em arquivos `.java`
  dentro de `src/main/java`, como ocorre neste projeto.
- **Erro de validação**: confira o texto exibido no formulário e as restrições
  no `ProductRequestDTO`; os limites do banco também devem ser compatíveis.
- **Página não encontrada**: confira o caminho da rota e o nome do template
  retornado pelo controller. Os templates ficam em `resources/templates` e
  normalmente não incluem a extensão `.html` no nome retornado.
- **Mudanças de template não aparecem**: a configuração local desativa o cache
  do Thymeleaf; atualize a página e confira se o servidor recompilou/recarregou.
- **HTTP 401 ao abrir produtos**: informe o usuário e a senha definidos em
  `APP_SECURITY_USERNAME` e `APP_SECURITY_PASSWORD` no diálogo de autenticação.
- **HTTP 403 ao enviar formulário**: confirme que o formulário usa POST e que
  está sendo renderizado com `th:action`, para que o token CSRF seja incluído.
