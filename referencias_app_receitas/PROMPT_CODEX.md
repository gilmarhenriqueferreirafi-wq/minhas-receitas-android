# PROMPT PARA CODEX — APP PESSOAL DE RECEITAS COM REFERÊNCIAS VISUAIS

Use as imagens da pasta `referencias_visuais/` como referências visuais. Primeiro inspecione o projeto existente, identifique a tecnologia, a estrutura, a navegação e o armazenamento. Preserve o código e as funcionalidades que já funcionam. Não recrie tudo do zero sem necessidade.

## Regra essencial para as referências
As imagens são referências de design, não telas prontas para usar como imagem de fundo. Recrie a interface com componentes reais, textos reais, botões funcionais, listas acessíveis e layout responsivo. Não copie a marca ou a interface proprietária do Claude; use apenas a ideia de leitura contínua e organização tipográfica como inspiração.

## 1. Identidade visual
- Tema escuro por padrão, com preto, grafite e cinza.
- Cor de destaque quente, como vermelho queimado ou laranja, configurável.
- Texto claro, tipografia legível e hierarquia visual evidente.
- Estilo gastronômico na Home, com fotos opcionais de pratos.
- Sem anúncios nem elementos decorativos desnecessários.
- Priorizar celulares, adaptando corretamente a outras larguras.

## 2. Home personalizável
Criar blocos configuráveis: cabeçalho com menu/título/configurações; receita em destaque; grade ou lista de receitas; atalho para adicionar receita; blocos opcionais para busca, favoritos e categorias; navegação inferior quando adequada.

Permitir ao usuário escolher a cor de destaque, alternar grade/lista, alterar número de colunas e tamanho dos cartões, mostrar/ocultar e reorganizar blocos, escolher manualmente a receita em destaque, configurar atalhos e restaurar o layout padrão. Salvar preferências e restaurá-las ao reabrir. A Home nunca deve ficar sem acesso às receitas e às configurações necessárias para recuperar um layout utilizável. Se não houver arrastar e soltar, fornecer controles de mover para cima/baixo.

## 3. Tela de leitura da receita
Usar as capturas como referência para uma tela de documento contínuo: ação para voltar; título grande; descrição opcional; foto opcional sem espaço reservado quando ausente; informações complementares; controle de porções com menos/mais; botão “Começar a cozinhar”; seção “INGREDIENTES”; lista simples; seção “MODO DE PREPARO”; etapas numeradas; temporizadores opcionais; menu para editar, favoritar, duplicar, compartilhar se suportado e excluir com confirmação.

Não transformar cada ingrediente ou etapa em cartão. Usar fluxo vertical contínuo, títulos de seção, espaçamento confortável e divisões discretas. Evitar excesso de botões. Receitas longas devem continuar fáceis de percorrer.

## 4. Dados da receita
Cada receita deve armazenar identificador único, título, descrição opcional, foto opcional, porções originais, ingredientes, etapas, tempos opcionais, categoria, favorito, contador de acessos e datas de criação/edição.

Cada ingrediente deve suportar quantidade numérica quando aplicável, unidade, nome e observações. Suportar “sal a gosto”, “quanto baste”, quantidades aproximadas e texto original sem forçar conversões numéricas. Cada etapa deve ter texto, ordem e duração opcional.

## 5. Recálculo de porções
Calcular sempre a partir dos valores originais, nunca das quantidades já recalculadas:

`quantidade exibida = quantidade original × (porções desejadas / porções originais)`

Não alterar permanentemente a receita original. Evitar erros acumulados, tratar divisão por zero e rendimentos inválidos, preservar unidades e observações, manter ingredientes “a gosto” sem alteração e apresentar frações/decimais de maneira legível. Não converter unidades quando a conversão não for confiável.

## 6. Cadastro e edição
Permitir criar, editar, salvar e cancelar receitas. Ingredientes e etapas podem ser adicionados, removidos e reordenados. Validar título e porções originais. Avisar sobre alterações não salvas ao sair do editor. Preservar dados não modificados.

## 7. Modo de cozinhar
Ao tocar em “Começar a cozinhar”, oferecer uma etapa em destaque por vez, avançar/voltar, marcação de etapas concluídas, progresso, temporizadores independentes com iniciar/pausar/reiniciar, acesso aos ingredientes sem perder a etapa atual e preservação do progresso durante a sessão.

Calcular tempo restante usando um horário de término previsto, não apenas decrementando um contador visual. Tratar retorno à tela e temporizadores simultâneos. Notificação sonora/visual deve respeitar os recursos e permissões da plataforma.

## 8. Busca e organização
Implementar busca por título e ingredientes, favoritos, categorias e ordenação útil. Incluir estados vazios e, quando viável, busca tolerante a maiúsculas/minúsculas e acentos.

## 9. Persistência e arquitetura
Usar armazenamento adequado à tecnologia existente. Separar receitas, preferências, layout da Home e estado da sessão. Criar componentes reutilizáveis. Preservar dados ao fechar/reabrir. Não apagar dados em inicialização ou migrações. Tratar falhas de leitura/gravação. Não adicionar servidor, login, sincronização em nuvem ou dependências sem necessidade justificada.

## 10. Testes e entrega
Testar criação/edição/exclusão, persistência, recálculo de porções e retorno ao original, ingredientes não numéricos, personalização/reorganização/restauração da Home, temporizadores, receitas longas e diferentes larguras de tela.

Trabalhar por etapas: primeiro inspecionar o projeto e apresentar um plano; depois implementar dados e leitura, cadastro, cálculo de porções, Home, modo de cozinhar, busca e refinamentos. Executar os testes disponíveis. Ao final, informar arquivos alterados, testes realmente executados e limitações. Não declarar como funcional algo que não foi verificado.
