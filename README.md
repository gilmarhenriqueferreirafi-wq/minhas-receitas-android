# Minhas Receitas

Aplicativo Android local para cadastrar, organizar e preparar receitas.

## Recursos

- Cadastro e edição de receitas, ingredientes e etapas.
- Fotos da receita e banner independentes.
- Busca, favoritos e categorias múltiplas.
- Modo de cozinhar com progresso, temporizador e retomada da receita.
- Backup local em JSON.
- Interface escura com destaque coral e navegação inferior.

## Requisitos

- Android Studio recente.
- Android SDK 35.
- JDK compatível com o Gradle do projeto.
- Android 8.0 (API 26) ou superior para executar o app.

## Compilar

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

O APK será gerado em `app/build/outputs/apk/debug/`.

Uma versão de demonstração também está disponível em `releases/`.

## Dados

O aplicativo funciona localmente. As receitas ficam armazenadas no próprio dispositivo; nenhuma conta ou servidor é necessário.

## Licença

Este projeto está disponível sob a licença MIT. Consulte [LICENSE](LICENSE).

