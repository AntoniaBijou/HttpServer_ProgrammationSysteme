create database Login;
use Login;

create table utilisateur (
    idUtilisateur int auto_increment primary key,
    email varchar(50),
    mdp varchar(50)
);

insert into utilisateur (email,mdp) values ('antoniabijou@gmail.com','1234');