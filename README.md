#Online Library

- реализован функционал импорта\экспорта XML;
- добавлен просмотр сущностей с помощью преобразования XML в HTML с помощью XSLT;
- использованные технологии: EJB3, Hibernate, Bean Validation, WildFly 9, Maven


####Инструкция по натройке:
(Все файлы, используемые при настройке находтся в папке `/installing configuration/`)

WildFly 9.0.2, MySql 5.7

Предварительные настройки:

1) Создать базу данных `library`, используя файл `library.sql`

2) Если не установлен драйвер MySQL, то установить:

В папку `.../wildfly-9.0.2.Final/modules/system/layers/base/com/` скопировать папку `mysql` (она находится в (`/installing configuration/`). В этой папке уже будут находиться модуль и драйвер.

Скачать драйвер с https://downloads.mysql.com/archives/c-j/ и поместите файл `mysql-connector-java-5.1.38-bin.jar` в папку из предыдущего пункта (`.../mysql/main/`).

В консоли открыть `/bin/jboss-cli.bat`, подключиться (`connect`), выполнить следующую команду:

`/subsystem=datasources/jdbc-driver=mysql:add(\
    driver-name=mysql,\
    driver-module-name=com.mysql,\
    driver-class-name=com.mysql.jdbc.Driver,\
    driver-xa-datasource-class-name=com.mysql.jdbc.jdbc2.optional.MysqlXADataSource\
)`

(Подробнее см. http://hpehl.info/jdbc-driver-setup.html пункт `Install as Module`)

3) Скопировать файл standalone-library.xml в папку `.../wildfly-9.0.2.Final/standalone/configuration/`

Для запуска сервера: в командной строке перейти в директорию `.../wildfly-9.0.2.Final/bin/` и запустить сервер с параметром:


`standalone.bat --server-config=standalone-library.xml`


или


`./standalone.sh --server-config=standalone-library.xml` (для линукс)


В консоли администратора для DataSource `MySqlDS` указать свой логин и пароль от СУБД MySQL.

4) Развернуть приложение (Deployments -> Add в консоли администратора WildFly, файл `Lab5-XLibrary-ear-1.0.0.ear`)

5) Запустить приложение в браузере: `http://localhost:8080/library/`

