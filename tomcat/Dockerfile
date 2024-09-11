FROM tomcat:latest

# Встановлюємо GSON бібліотеку, PostgreSQL драйвер, Maven і curl в одному кроці, а потім очищуємо кеші
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    maven \
    curl && \
    wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar -P /usr/local/tomcat/lib/ && \
    wget https://jdbc.postgresql.org/download/postgresql-42.6.0.jar -P /usr/local/tomcat/lib/ && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Визначаємо робочу директорію
WORKDIR /usr/src/app

# Копіюємо файли проекту до робочої директорії
COPY . .

# Збираємо проект за допомогою Maven
RUN mvn clean package

# Копіюємо зібраний WAR файл до веб-додатків Tomcat
RUN cp target/*.war /usr/local/tomcat/webapps/ROOT.war

# Копіюємо конфігурацію Tomcat
COPY ./tomcat/ROOT.xml /usr/local/tomcat/conf/Catalina/localhost/

# Вказуємо порт, який буде використовувати контейнер
EXPOSE 8080

# Запускаємо Tomcat
CMD ["catalina.sh", "run"]