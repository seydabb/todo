#-- comments schema

# --- !Ups

CREATE TABLE IF NOT EXISTS comments
(
  id CHARACTER VARYING(96) NOT NULL,
  fkTodoId CHARACTER VARYING(96) NOT NULL,
  comment CHARACTER VARYING(500) NOT NULL,
  createdAt TIMESTAMP NOT NULL,
  updatedAt TIMESTAMP NOT NULL,
  CONSTRAINT comments_pkey PRIMARY KEY (ID),
  CONSTRAINT todo_fk_constraint FOREIGN KEY (fkTodoId) REFERENCES todos (id)
);

# --- !Downs

drop table if exists comments;
