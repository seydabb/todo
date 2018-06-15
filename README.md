ToDo List App
===================

**Starting the App Via Docker**

```
sbt dist
docker-compose build
docker-compose up

```
PostgreSQL will run on localhost:5432
Application in production mode will run on localhost:9001

You can find the credentials in .env file

**Starting the App Via Sbt**

Alternatively, you can start application in dev mode
```
sbt run
```
Application will run on localhost:9000

**Tech Stack**

- Scala 2.12.6
- Play 2.6.15
- SBT 0.13.15
- PostgreSQL 10.4
- Anorm
- Docker

**Notes**

- When you run the application, 2 tables will be created automatically (todos and comments) via evolutions
- It uses PostgreSQL's default database
- You can find the API doc on Postman https://documenter.getpostman.com/view/74682/todoapp/RWEduLWt
- You can use postman export for testing. Here is the export link (https://www.getpostman.com/collections/51c7e13756275aac155d)








