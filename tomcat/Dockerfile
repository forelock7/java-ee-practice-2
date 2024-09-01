FROM tomcat:latest

# Встановлюємо GSON бібліотеку та PostgreSQL драйвер
RUN apt-get update && apt-get install -y \
    wget && \
    wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar -P /usr/local/tomcat/lib/ && \
    wget https://jdbc.postgresql.org/download/postgresql-42.6.0.jar -P /usr/local/tomcat/lib/

# Встановлюємо Maven
RUN apt-get update && apt-get install -y maven

# Встановлюємо curl для тестування контейнера
RUN apt-get update && apt-get install -y curl

# Визначаємо робочу директорію
WORKDIR /usr/src/app

# Копіюємо все до робочої директорії
COPY . .

# Збираємо проект за допомогою Maven
RUN mvn clean package

# Копіюємо зібраний WAR файл до веб-додатків Tomcat
RUN #cp target/*.war /usr/local/tomcat/webapps/myapp.war

COPY ./tomcat/ROOT.xml /usr/local/tomcat/conf/Catalina/localhost/
COPY ../target/*.war /usr/local/tomcat/myApp/myapp.war

# Вказуємо порт, який буде використовувати контейнер
EXPOSE 8080

# Запускаємо Tomcat
CMD ["catalina.sh", "run"]