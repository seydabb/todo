FROM openjdk:8-jre

EXPOSE 9000

WORKDIR /todoapp
ADD ./target/universal/todoapp-1.0.zip /todoapp
RUN unzip todoapp-1.0.zip

WORKDIR /todoapp/todoapp-1.0
CMD ./bin/todoapp
