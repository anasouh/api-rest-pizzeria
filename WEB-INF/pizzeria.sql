drop table if exists contains;
drop table if exists orders;
drop table if exists compose;
drop table if exists pizzas;
drop table if exists ingredients;
drop table if exists users;

CREATE TABLE IF NOT EXISTS users (
    login VARCHAR(25) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ingredients (
    iid SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS pizzas (
    pid SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    basePrice DECIMAL(10, 2) NOT NULL,
    dough VARCHAR(25) NOT NULL DEFAULT 'classic'
);

CREATE TABLE IF NOT EXISTS compose (
    pid INT,
    iid INT,
    CONSTRAINT pk_compose PRIMARY KEY (pid, iid),
    CONSTRAINT fk_pizzas FOREIGN KEY (pid) REFERENCES pizzas(pid) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ingredients FOREIGN KEY (iid) REFERENCES ingredients(iid) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    oid SERIAL PRIMARY KEY,
    login VARCHAR(25) REFERENCES users(login),
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS contains (
    oid INT,
    pid INT,
    quantity INT NOT NULL,
    CONSTRAINT pk_contains PRIMARY KEY (oid, pid),
    CONSTRAINT fk_orders FOREIGN KEY (oid) REFERENCES orders(oid) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pizzas_contains FOREIGN KEY (pid) REFERENCES pizzas(pid) ON DELETE CASCADE ON UPDATE CASCADE
);

insert into users (login, password) values ('user', 'user');