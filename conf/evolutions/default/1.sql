# -- todos schema

# --- !Ups

CREATE TABLE IF NOT EXISTS todos
(
  id CHARACTER VARYING(96) NOT NULL,
  todo CHARACTER VARYING(500) NOT NULL,
  isDone BOOLEAN NOT NULL,
  createdAt TIMESTAMP NOT NULL,
  updatedAt TIMESTAMP NOT NULL,
  CONSTRAINT todos_pkey PRIMARY KEY (id)
);


# --- !Downs

drop table if exists todos;
