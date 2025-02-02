## Лабораторная работа Конюхова Матвея "Курс Java-разработчик"

### О приложении

RESTful приложение для перевода текста с использованием стороннего сервиса перевода. Я использовал Yandex Translator.

### Инструкции по запуску

Для запуска этого проекта вам понадобится:

- Java 17 или выше
- Maven 3.6.3 или выше

1. Склонируйте репозиторий:

    ```sh
    git clone https://github.com/translationApp/translationApp.git
    ```

2. Далее проект можно открыть либо в IDE, либо перейти в дерикторию проекта через командную:

    ```sh
    cd translationApp
    ```

3. Настройте файл `application.properties`:

    Важно отметить, что при разработке я использовал несколько `properties` файлов: один для разработки, а другой для гита без конфиденциальной информации в виде токенов для работы приложения. `Properties` файл с токенами я сразу добавил в `.gitignore`. Я осознанно добавляю в README токены лишь для того, чтобы проект был проверен и оценен.

    Добавьте в файл конфигурации следующие строки:

    ```properties
    yandex.oauth.token=y0_AgAAAABVE6PxAATuwQAAAAEMTzCYAACcOtIer9RCwYJNwLblAXGSCi7N4g
    folder.id=b1gloo1se7vi30bsl33b
    ```

4. Либо запустите проект с использованием средств IDE, либо переходите к следующему шагу.

5. Соберите проект с помощью Maven:

    ```sh
    mvn clean install
    ```

6. Запустите приложение:

    ```sh
    mvn spring-boot:run
    ```

### Инструкции по использованию
Для ознакомления с документацией к REST API, включая примеры запросов и коды ошибок, перейдите по [ссылке](https://documenter.getpostman.com/view/26186639/2sA3rzJrx1).

### Реализация

- При разработке была создана отдельная ветка для разработки на GitHub. Итоговую версию слил с веткой `main`.
- Проект структурно разделен на модули по причине того, что получение IAM-токена для работы с API Яндекса обновляется автоматически (первый раз при запуске приложения, далее - каждый час). Это отдельный процесс, который выполняется в отдельном потоке. Исходя из этого, логику работы с переводом и получения токена я разделил.
- Для каждого модуля в `pom.xml` файле указаны только необходимые для его работы зависимости. Общие зависимости двух модулей вынесены в общий `pom.xml` файл.
- Реализована кастомная обработка исключений. Несмотря на то, что в задании указан пример "Пример 3: http 400 Ошибка доступа к ресурсу перевода", я сделал более информативные ошибки для лучшего понимания работы приложения. Также проверил на исключение при выключенном соединении с интернетом.
- Рассуждая в рамках микросервисной архитектуры, для того чтобы не нагружать высокоуровневый сервис (API Яндекса), проверка на корректность языков перевода происходит на уровне моего приложения.
- В качестве реляционной базы данных использовалась H2, так как она является интегрируемой и легковесной, а также удобна для разработки.
- Дополнительные библиотеки, использованные в ходе разработки: Lombok, Jackson.
