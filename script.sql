create table person
(
  "CF"      char(16)                   not null
    constraint person_pkey
    primary key,
  birthdate date                       not null,
  name      varchar(100)               not null,
  surname   varchar(100)               not null,
  sex       char(1)                    not null
);

create table login
(
  username     char(16)  not null
    constraint login_pkey
    primary key
    constraint username
    references person
    on delete cascade,
  passwordhash char(128) not null
);

create table operator
(
  login char(16) not null
    constraint operator_pkey
    primary key
    constraint operator_login_fkey
    references person
    on delete cascade
);

create table "user"
(
  sign_update date     not null,
  login       char(16) not null
    constraint user_pkey
    primary key
    constraint "CF"
    references person
    on delete cascade
);


