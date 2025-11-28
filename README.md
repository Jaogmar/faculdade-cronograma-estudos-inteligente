# 📚 Sistema de Planejamento de Estudos com IA Gemini

Sistema web completo desenvolvido em Java/Spring Boot que auxilia estudantes no planejamento e execução de estudos, utilizando Google Gemini IA para sugestões inteligentes de conteúdo e distribuição de carga horária.

## 🚀 Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot 3.2.0
- **Persistência**: Spring Data JPA + MySQL
- **Frontend**: Thymeleaf + Tailwind CSS
- **Segurança**: Spring Security
- **IA**: Google Gemini API
- **Build**: Maven

## ⚙️ Configuração do Ambiente

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- MySQL 8.0+ rodando em `localhost:3306`
- Conta Google Cloud com acesso à API Gemini

### 1. Configurar MySQL

```bash
# Conectar ao MySQL
mysql -u root -p

# Criar o banco de dados (ou deixar o Spring criar automaticamente)
CREATE DATABASE planner_estudos;
```

### 2. Configurar API Key do Google Gemini

#### Como Obter a API Key:

1. Acesse [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Faça login com sua conta Google
3. Clique em "Get API Key" ou "Create API Key"
4. Copie a chave gerada

#### Configurar no Projeto:

Edite o arquivo `src/main/resources/application.properties`:

```properties
# Substituir YOUR_API_KEY_HERE pela sua chave
gemini.api.key=SUA_CHAVE_AQUI
```

**Exemplo:**
```properties
gemini.api.key=AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz1234567
```

### 3. Configurar Credenciais do MySQL

No mesmo arquivo `application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

Ajuste conforme suas credenciais do MySQL.

## 🏃 Como Executar

### Via Maven (Desenvolvimento)

```bash
# Compilar e executar
mvn spring-boot:run
```

### Via JAR (Produção)

```bash
# Compilar
mvn clean package

# Executar
java -jar target/planner-1.0.0.jar
```

A aplicação estará disponível em: **http://localhost:8080**

## 📋 Como Usar

### 1. Primeiro Acesso

1. Acesse http://localhost:8080
2. Clique em "**Cadastrar**"
3. Preencha: Nome, Email e Senha
4. Faça login com suas credenciais

### 2. Criar um Objetivo de Estudo

1. No dashboard, clique em "**Criar Novo Objetivo**"
2. Siga o wizard de 4 etapas:

#### **Etapa 1**: Definir Tema e Prazo
- Digite o tema (ex: "Java Spring Boot", "Inglês Intermediário")
- Defina a data limite

#### **Etapa 2**: Sugestões da IA
- ✨ **A IA Gemini sugerirá automaticamente 10 mini-temas**
- Selecione/deselecione os que desejar
- Clique em "**Adicionar Personalizado**" para criar temas manualmente

#### **Etapa 3**: Ajustar Carga Horária
- Revise e ajuste as horas estimadas para cada tema
- O sistema calcula o total automaticamente

#### **Etapa 4**: Definir Rotina
- Escolha quantas horas/dia pode estudar (slider)
- Selecione os dias da semana disponíveis
- **O sistema valida em tempo real** se é viável
- Se não for viável, ajuste a rotina ou remova temas

### 3. Acompanhar Progresso

- **Dashboard**: Visualize estatísticas, tarefas do dia e atrasadas
- **Detalhes do Objetivo**: Veja progresso, gráficos e mini-temas
- **Marcar Tarefas**: Clique no checkbox para concluir tarefas

## 🎯 Funcionalidades Principais

### ✅ Autenticação Completa
- Cadastro de usuários com hash BCrypt
- Login seguro com Spring Security
- Sessões gerenciadas

### 🤖 Integração com IA Gemini
- Sugestões automáticas de mini-temas
- Análise inteligente do tema de estudo
- Estimativa de carga horária

### 📊 Algoritmo Inteligente de Distribuição
- **Espaçamento**: Sessões do mesmo tema espaçadas
- **Blocos Otimizados**: Sessões de 1-2h (limite de atenção)
- **Progressão**: Fundamentos → Aprofundamento → Revisão
- **Balanceamento**: Evita sobrecarga cognitiva diária

### 📈 Visualização de Progresso
- Barras de progresso por objetivo
- Gráficos interativos (Chart.js)
- Indicadores de tarefas concluídas/pendentes/atrasadas

### ⏰ Gestão de Tarefas
- Tarefas distribuídas automaticamente no calendário
- Checkbox para marcar como concluída
- Indicador visual de tarefas em atraso

## 🔧 Estrutura do Projeto

```
src/main/java/com/estudos/planner/
├── config/              # Configurações (Security, etc)
├── controller/          # Controllers MVC
├── dto/                 # Data Transfer Objects
├── model/               # Entidades JPA
├── repository/          # Repositories Spring Data
└── service/             # Lógica de negócio
    ├── GeminiService.java           # Integração com IA
    ├── CronogramaService.java       # Algoritmo inteligente
    ├── ObjetivoService.java         # Gestão de objetivos
    └── TarefaService.java           # Gestão de tarefas

src/main/resources/
├── templates/           # Templates Thymeleaf
│   ├── objetivo/        # Wizard de 4 etapas
│   ├── dashboard.html
│   ├── login.html
│   └── cadastro.html
└── static/              # CSS, JS, imagens
```

## 🐛 Troubleshooting

### ❌ Erro: "models/gemini-pro is not found"

**Causa:** O Google atualizou os modelos disponíveis.

**Solução:**
- A URL correta agora é: `gemini-1.5-flash` (já corrigido no projeto)
- Se o erro persistir, verifique se sua API Key está ativa

### ❌ Erro: "IA Gemini não está respondendo"

**Possíveis causas:**
1. API Key inválida ou não configurada
2. Quota da API Gemini excedida
3. Sem conexão com a internet
4. Modelo desatualizado (use `gemini-1.5-flash`)

**Solução:**
- Verifique a API Key no `application.properties`
- Certifique-se de usar: `gemini-1.5-flash:generateContent`
- Teste a chave em: https://aistudio.google.com/app/apikey
- Use a opção "Continuar Manualmente" para adicionar temas

### ❌ Erro: "Access denied for user 'root'@'localhost'"

**Solução:**
- Verifique as credenciais do MySQL no `application.properties`
- Teste a conexão: `mysql -u root -proot`

### ❌ Erro: "Table 'planner_estudos.usuarios' doesn't exist"

**Solução:**
- O banco será criado automaticamente na primeira execução
- Aguarde o Spring Boot iniciar completamente
- Verifique os logs: `spring.jpa.hibernate.ddl-auto=update`

## 📝 Notas de Desenvolvimento

- **CSRF**: Desabilitado para simplificar requisições AJAX (habilite em produção)
- **Logs**: Nível DEBUG ativo para desenvolvimento
- **Hot Reload**: DevTools ativado (ctrl+F9 no IntelliJ)

## 🎨 Personalização

### Alterar Cores do Tailwind

Edite o arquivo de templates para customizar:

```javascript
tailwind.config = {
    theme: {
        extend: {
            colors: {
                primary: '#3B82F6',  // Altere aqui
                secondary: '#8B5CF6',
            }
        }
    }
}
```

### Ajustar Algoritmo de Distribuição

Edite `CronogramaService.java` método `dividirEmSessoes()` para alterar:
- Percentuais: Fundamentos (40%), Aprofundamento (40%), Revisão (20%)
- Duração das sessões (1-2h)

## 📄 Licença

Projeto educacional - Livre para uso e modificação

## 👨‍💻 Autor

Sistema desenvolvido com Spring Boot e Google Gemini IA

---

**Dúvidas?** Abra uma issue no repositório!
