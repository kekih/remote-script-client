# Remote Script Client (Kotlin / Android)

Android-клиент для удалённого запуска Python и Node.js скриптов через FastAPI сервер.

## Возможности

- Подключение к серверу по URL + API Key
- Проверка health (наличие Python / Node.js)
- Выбор языка: Python / Node.js
- Редактор кода + timeout
- Отображение stdout / stderr / exit code

## Сборка

### Локально

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions

При пуше в `main` автоматически собирается debug APK.  
Артефакт доступен во вкладке **Actions → Build APK → Artifacts**.

Можно запустить вручную: **Actions → Build APK → Run workflow**.

## Использование

1. Запусти сервер (см. отдельный репозиторий / папку `remote-script-server`)
2. В приложении укажи:
   - **Server URL**: `http://IP_СЕРВЕРА:8000`  
     (для эмулятора Android: `http://10.0.2.2:8000`)
   - **API Key**: тот же, что в `API_KEY` на сервере
3. Нажми «Проверить» → затем пиши код и «Запустить»

## Безопасность

Клиент просто отправляет код на сервер. Вся опасность — на стороне сервера.  
**Никогда не открывай сервер в публичный интернет без сильного ключа и песочницы.**

## Тема

Тёмная тема вдохновлена палитрой Aether (ink / amber / warm paper).
