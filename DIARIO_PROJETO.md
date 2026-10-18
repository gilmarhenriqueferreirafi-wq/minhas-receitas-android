# Diário do projeto — Minhas Receitas

Última atualização: 18/10/2026

## Objetivo

Aplicativo pessoal de receitas, com armazenamento local, edição de receitas, fotos, cálculo de porções, busca, favoritos, categorias e modo de cozinhar com temporizadores.

O visual definitivo deve seguir o prompt visual e as imagens de referência, mas sem transformar as imagens em telas ou fundos. A interface deve ser construída com componentes Android reais.

## Prompts que devem ser usados

### Prompt funcional inicial

Arquivo:

`referencias_app_receitas/PROMPT_CODEX.md`

Usar para:

- Regras de funcionamento do app.
- Modelo das receitas.
- Persistência local.
- Cadastro e edição.
- Cálculo de porções.
- Busca, favoritos e categorias.
- Modo de cozinhar e temporizadores.

### Prompt visual e de arquitetura

Arquivo:

`referencias_app_receitas/novo prompt_codex.md`

Usar para:

- Layout final das telas.
- Identidade visual escura gastronômica.
- Home personalizável.
- Navegação inferior.
- Tela de detalhes em leitura contínua.
- Modo de cozinhar visual.
- Editor visual de receitas.
- Configurações, backup e preferências.
- Migração gradual para XML + Kotlin quando essa etapa for iniciada.

Não aplicar esse prompt para apagar dados ou recomeçar o projeto. Ele deve orientar a evolução do protótipo existente.

## Imagens de referência do layout

### Referências visuais originais

- `referencias_app_receitas/referencias_visuais/home_conceito_visual.png`
- `referencias_app_receitas/referencias_visuais/referencia_leitura_receita_01.jpg`
- `referencias_app_receitas/referencias_visuais/referencia_leitura_receita_02.jpg`
- `referencias_app_receitas/referencias_visuais/referencia_leitura_receita_03.jpg`

### Montagens visuais enviadas posteriormente

- `C:/Users/gilma/Downloads/3119e86a-95ab-491c-869f-a055433a7a8b.png`
- `C:/Users/gilma/Downloads/4fb74e8b-d03c-4163-8c03-9bfde2264f21.png`

Essas duas montagens mostram a direção visual final das telas:

1. Home.
2. Visualização da receita.
3. Modo de cozinhar.
4. Adicionar e editar receita.
5. Buscar e explorar.
6. Favoritos.
7. Configurações e personalização.

### Captura do estado atual da Home

- `C:/Users/gilma/Downloads/Screenshot_20261018_151158.png`

Essa captura registra a primeira versão da Home em XML. Ela serviu para identificar que a navegação inferior estava com apenas quatro opções; a opção Configurações foi adicionada depois.

## O que já foi feito

### Base e persistência

- Criados os modelos `Recipe`, `Ingredient` e `RecipeStep`.
- Criado `RecipeStore` com persistência em `SharedPreferences` e JSON.
- Receitas sobrevivem ao fechamento e reabertura do app.
- Receita de exemplo de lasanha criada automaticamente quando não existem receitas.
- Dados existentes foram preservados durante as alterações.

### Receitas

- Cadastro de receita.
- Edição de receita.
- Exclusão de receita com confirmação.
- Título, descrição, categoria e porções.
- Ingredientes com quantidade, unidade, nome e observação.
- Ingredientes numéricos continuam escaláveis.
- Ingredientes como “sal a gosto” não recebem cálculo artificial.
- Etapas de preparo editáveis individualmente.
- Cada etapa possui texto e tempo opcional em minutos.
- Adição e remoção de ingredientes.
- Adição e remoção de etapas.
- Fotos de banner por seletor de imagem.
- Fotos de banner tiradas pela câmera.
- Fotos salvas em arquivo local ou por URI persistente.

### Visualização e preparo

- Tela de detalhes da receita.
- Ajuste de porções com recálculo dos ingredientes.
- Botão de favoritos.
- Modo de cozinhar com uma etapa por vez.
- Navegação anterior e próxima.
- Marcação de etapa concluída.
- Temporizadores baseados em horário de término.
- Progresso do temporizador preservado ao sair e retornar.
- Corrigido o fechamento do app ao abrir o modo de cozinhar.
- Todas as telas travadas em modo retrato.

### Busca, favoritos e categorias

- Busca por título, descrição e categoria.
- Busca sem diferenciar maiúsculas, minúsculas e acentos.
- Busca também pelos nomes e observações dos ingredientes.
- Lista de favoritos.
- Categorias abertas em lista de receitas.
- Renomear categoria.
- Excluir categoria sem excluir as receitas.

### Home e navegação

- Contagem de acessos por receita.
- Destaque da receita mais acessada.
- Home antiga em Canvas preservada como `MainActivity`.
- Criada nova `HomeActivity` usando layout XML.
- Nova Home com cabeçalho, destaque, grade e navegação inferior.
- Navegação inferior com Início, Buscar, Favoritos, Categorias e Configurações.
- Cards preparados para receber fotos reais das receitas.
- Cores, superfícies e cantos arredondados aproximados das referências.

### Organização do projeto

- O app continua local e sem login ou servidor.
- O projeto não foi enviado para o GitHub como produto final.
- As alterações recentes de layout ainda não foram compiladas nem instaladas no telefone porque foi solicitado não executar o assembler.

## Estado atual importante

- O projeto original ainda possui código Java e telas programáticas.
- A nova Home já possui XML e `HomeActivity.java`.
- A tela de detalhes, o modo de cozinhar e o editor ainda precisam receber a mesma reformulação visual da Home.
- O prompt visual recomenda Kotlin + XML, mas a migração completa para Kotlin ainda não foi feita.
- Não executar o assembler automaticamente até ser solicitado novamente.
- Usar o telefone conectado via ADB para testes quando o usuário autorizar ou quando a etapa de validação chegar.

## O que falta fazer

### Próxima etapa: completar a Home visual

- Usar fotos reais de receitas nos cards quando houver `photoUri`.
- Criar estado vazio quando não houver receitas.
- Melhorar o botão de Configurações para abrir uma tela real.
- Ajustar espaçamentos e tamanhos comparando com as duas montagens enviadas.
- Verificar a Home em tamanhos diferentes de tela.

### Tela de visualização da receita

- Migrar para o layout visual da referência.
- Cabeçalho compacto com voltar, favorito e opções.
- Banner da receita no topo quando existir.
- Informações de tempo e porções.
- Lista contínua de ingredientes.
- Etapas numeradas sem cartões exagerados.
- Botão de cozinhar com destaque.
- Ações de editar e excluir em menu apropriado.

### Modo de cozinhar

- Barra de progresso visual.
- Layout com etapa atual centralizada.
- Próximas etapas resumidas.
- Pausar, retomar e reiniciar temporizador.
- Alerta visual ao finalizar o timer.
- Acesso aos ingredientes durante o preparo.

### Editor

- Transformar o editor atual em XML mais próximo da referência.
- Abas ou alternância entre Ingredientes e Modo de preparo.
- Seletor de categoria.
- Campos de tempo total e tempo de cozimento.
- Reordenação de ingredientes e etapas.
- Confirmação ao sair com alterações não salvas.

### Configurações

- Criar `SettingsActivity` real.
- Tema escuro, claro ou sistema.
- Cor de destaque configurável.
- Tamanho do texto.
- Preferências da Home.
- Mostrar ou ocultar categorias e favoritos.
- Escolher grade ou lista.
- Restaurar configuração padrão.

### Backup local

- Exportar receitas para JSON.
- Importar receitas de JSON.
- Confirmar substituição ou mesclagem dos dados.
- Criar backup local sem enviar receitas para a nuvem.

### Arquitetura futura

- Migrar gradualmente a lógica para Kotlin.
- Separar telas, dados e domínio.
- Usar layouts XML por tela.
- Considerar Room quando a quantidade de dados justificar a migração.
- Manter compatibilidade com os dados JSON existentes durante a migração.

## Ordem recomendada daqui para frente

1. Finalizar a Home visual usando as imagens como referência.
2. Reformular a tela de detalhes da receita.
3. Reformular o modo de cozinhar.
4. Reformular o editor.
5. Criar Configurações.
6. Criar exportação e importação.
7. Testar todos os fluxos no telefone.
8. Só depois revisar a migração completa para Kotlin/XML e a entrega final.

## Regra para os próximos trabalhos

Sempre preservar as receitas existentes. Não usar as montagens como imagens de fundo. Não declarar uma função pronta sem testar. Ao chegar na fase de layout, comparar cada tela com:

- `3119e86a-95ab-491c-869f-a055433a7a8b.png`
- `4fb74e8b-d03c-4163-8c03-9bfde2264f21.png`

## Atualização da etapa visual — 18/10/2026

- Criados os layouts XML da Home, da visualização da receita e do modo de cozinhar.
- A Home recebeu navegação inferior com cinco opções e suporte a fotos nos cards.
- A visualização da receita recebeu banner, porções, ingredientes, etapas e ações.
- O modo de cozinhar recebeu barra de progresso, timer e controles de navegação.
- O assembler não foi executado nesta etapa para preservar o limite solicitado.

O próximo arquivo recomendado para trabalhar é a tela visual de detalhes da receita.
### Atualização de layout — 18/10/2026

- Home, detalhe da receita, modo de cozinhar e lista de receitas passaram a usar layouts XML com a identidade visual escura e destaque coral/laranja.
- A Home agora tem cinco destinos na navegação inferior: início, busca, favoritos, categorias e configurações.
- Categorias ganhou uma tela XML com navegação consistente e manteve renomear/excluir.
- Configurações foi criada com exportação/importação das receitas em JSON e restauração da receita de exemplo.
- Cards e itens de lista receberam superfícies arredondadas; a Home usa a imagem de banner salva na receita quando disponível.

### Próxima etapa

- Revisar visualmente as telas no telefone e ajustar espaçamentos, ícones e textos conforme as imagens de referência.
- Migrar o editor de receita para XML quando a edição dinâmica de ingredientes e etapas estiver estável.
- Validar compilação/instalação e testar exportação/importação no dispositivo.
### Imagens separadas na criação/edição

- A receita agora armazena uma foto principal (`photoUri`) e um banner (`bannerUri`) em campos independentes.
- O editor permite escolher ou fotografar cada imagem sem substituir a outra.
- Os cards usam a foto principal; o detalhe prioriza o banner e mantém compatibilidade com receitas antigas que só tinham `photoUri`.
## Regra de continuidade para o Codex

Quando o Gilmar disser que o limite está acabando, que vai parar/pausar o projeto ou pedir para continuar depois, atualizar este diário antes de encerrar o trabalho. Registrar o que foi feito na sessão, arquivos alterados, o que falta e o próximo passo concreto. Não depender apenas do histórico da conversa para retomar.

## Atualização — foto principal e banner

- O formulário de receita agora tem campos independentes para foto principal e banner, ambos com opção de escolher imagem ou tirar foto.
- `Recipe` persiste os dois endereços separadamente (`photoUri` e `bannerUri`).
- Os cards usam a foto principal; a tela de detalhes usa o banner, com fallback para receitas antigas que só têm `photoUri`.
- Alterados `Recipe.java`, `RecipeEditorActivity.java`, `RecipeDetailActivity.java` e `activity_recipe_editor.xml`.
- Ainda falta compilar/instalar e conferir os dois fluxos no telefone.
## Categorias e associação múltipla — 18/10/2026

- O botão no cabeçalho de Categorias agora cria categorias.
- O botão `+` de cada categoria abre a seleção múltipla de receitas para associar ou remover receitas daquela categoria.
- Receitas agora persistem uma lista de categorias, preservando o campo antigo para compatibilidade com dados existentes.
- Categorias criadas sem receita também são salvas localmente; renomear/excluir atualiza as associações.
- A busca por categoria aceita qualquer categoria associada à receita.
- Ainda falta compilar/instalar e validar a seleção, renomeação e exclusão no telefone.
## Categorias e associação múltipla — atualização

- O cabeçalho da aba Categorias usa `+` para criar uma categoria.
- O `+` ao lado de cada categoria abre a seleção múltipla de receitas para vincular/remover receitas.
- Ao abrir uma categoria, a tela de resultados também mostra um `+` no cabeçalho para editar as receitas vinculadas.
- O modelo de receita persiste várias categorias; filtros e busca consideram todas as categorias associadas.
- As categorias criadas ficam salvas mesmo antes de receber receitas.
- Não compilei nem instalei esta alteração; validar os fluxos no telefone ainda é necessário.
## Ajuste da navegação e do seletor de categorias

- Tocar no nome da categoria agora abre uma tela de conteúdo dentro da aba Categorias, sem encaminhar para Buscar.
- A tela da categoria lista as receitas associadas e oferece `+` no cabeçalho para adicionar/remover receitas.
- A seleção de receitas virou uma tela própria com cartões e caixas de seleção no tema escuro; suporta várias receitas e categorias por receita.
- A aba Categorias continua selecionada durante a navegação interna; botão voltar e voltar do Android retornam à lista.
- Ainda falta compilar/instalar e conferir esse fluxo no telefone.
## AtualizaÃ§Ã£o — acabamento visual e preparaÃ§Ã£o para entrega — 18/10/2026

- Adicionada borda sutil nas superfÃ­cies principais arredondadas em `bg_surface.xml`.
- Criado o `README.md` com descriÃ§Ã£o, recursos, requisitos e instruÃ§Ãµes de compilaÃ§Ã£o.
- Adicionado `.gitignore` para evitar arquivos locais, caches e builds intermediÃ¡rios.
- Adicionada licenÃ§a MIT em `LICENSE`.
- Preparado o primeiro repositÃ³rio GitHub do projeto e uma pasta `releases/` para o APK de demonstraÃ§Ã£o.

### PendÃªncias atuais

- Compilar o APK de debug e conferir se a build termina sem erros.
- Instalar/testar no telefone a versÃ£o com a nova borda e os Ãºltimos ajustes.
- Validar no telefone o editor, o seletor flutuante de categorias/unidades, o carrossel da Home e o modo de cozinhar.
- Continuar a reformulaÃ§Ã£o visual da tela de detalhes, do modo de cozinhar e do editor conforme as referÃªncias.
- Revisar configuraÃ§Ãµes, backup e compatibilidade em diferentes telas.

## Primeiras animaÃ§Ãµes — 18/10/2026

- Adicionadas transiÃ§Ãµes suaves de entrada e saÃ­da para as telas usando animaÃ§Ãµes de opacidade.
- A Home agora anima a troca dos itens do carrossel com um pequeno deslocamento vertical e retorno de opacidade.
- Ativadas animaÃ§Ãµes automÃ¡ticas de mudanÃ§a de layout na Home.
- Mantida uma abordagem discreta para preservar desempenho e legibilidade.
- A compilaÃ§Ã£o limpa foi validada com sucesso apÃ³s os ajustes.

### PrÃ³ximo passo

- Observar as animaÃ§Ãµes no telefone e ajustar a velocidade caso alguma transiÃ§Ã£o pareÃ§a rÃ¡pida ou lenta.
- Depois iniciar a segunda etapa de acabamento estÃ©tico: espaÃ§amentos, hierarquia visual, Ã­cones e estados dos botÃµes.

## Ajuste das animaÃ§Ãµes da Home — 18/10/2026

- A entrada da Home agora revela o destaque e a grade de receitas com deslocamento vertical, opacidade e atraso entre os blocos.
- A animaÃ§Ã£o ficou mais perceptÃ­vel para validaÃ§Ã£o no telefone, mantendo duraÃ§Ã£o curta.

## TransiÃ§Ã£o entre telas — 18/10/2026

- SubstituÃ­da a entrada/saÃ­da apenas por opacidade por uma transiÃ§Ã£o lateral suave.
- Telas novas entram deslizando da direita e a tela anterior recua levemente para a esquerda.
- A transiÃ§Ã£o foi aplicada pelo tema global para manter o comportamento consistente nas Activities.

## MicrointeraÃ§Ãµes — 18/10/2026

- Adicionado pulso de escala ao tocar em favorito e iniciar o modo de cozinhar.
- A etapa atual do modo de cozinhar agora entra com deslocamento lateral e opacidade.
- Criado `MotionUtils` para centralizar animaÃ§Ãµes curtas e manter o comportamento consistente.

### CorreÃ§Ã£o da transiÃ§Ã£o

- Identificado que `BottomNavigation` usava `overridePendingTransition(0, 0)`, desativando as transiÃ§Ãµes entre abas.
- SubstituÃ­do pelo par de animaÃ§Ãµes laterais para que a navegaÃ§Ã£o inferior mostre a transiÃ§Ã£o suave.

### Refinamento da transiÃ§Ã£o

- A navegaÃ§Ã£o entre abas passou a usar crossfade, evitando a sensaÃ§Ã£o de que todos os conteÃºdos vÃªm do mesmo ponto.
- A transiÃ§Ã£o lateral continua disponÃ­vel para telas de detalhe e ediÃ§Ã£o, enquanto a Home troca de aba de forma mais suave.

## AnimaÃ§Ãµes em favoritos, categorias e busca — 18/10/2026

- CartÃµes de favoritos e resultados da busca agora entram em sequência com deslocamento lateral e opacidade.
- Categorias e receitas dentro de uma categoria recebem a mesma entrada escalonada.
- A busca repete a animaÃ§Ã£o quando os resultados sÃ£o atualizados, deixando claro que o filtro foi aplicado.

## Imagens nas abas de Busca e Categorias

- Resultados de busca agora usam foto principal e recorrem ao banner quando a foto principal não existe.
- A lista de categorias exibe uma miniatura de uma receita associada; a tela de cada categoria mostra fotos nos cartões das receitas.
- A seleção de receitas para uma categoria também mostra miniaturas ao lado das caixas de seleção.
