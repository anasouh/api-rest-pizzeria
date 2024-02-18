CREATE TABLE IF NOT EXISTS users (
    uid SERIAL PRIMARY KEY,
    login VARCHAR(255) NOT NULL,
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
    basePrice DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS compose (
    pid INT REFERENCES pizzas(pid),
    iid INT REFERENCES ingredients(iid),
    PRIMARY KEY (pid, iid),
    CONSTRAINT fk_pizzas FOREIGN KEY (pid) REFERENCES pizzas(pid),
    CONSTRAINT fk_ingredients FOREIGN KEY (iid) REFERENCES ingredients(iid)
);

CREATE TABLE IF NOT EXISTS orders (
    oid SERIAL PRIMARY KEY,
    uid INT REFERENCES users(uid)
);

CREATE TABLE IF NOT EXISTS contains (
    oid INT REFERENCES orders(oid),
    pid INT REFERENCES pizzas(pid),
    quantity INT NOT NULL,
    PRIMARY KEY (oid, pid),
    CONSTRAINT fk_orders FOREIGN KEY (oid) REFERENCES orders(oid),
    CONSTRAINT fk_pizzas_contains FOREIGN KEY (pid) REFERENCES pizzas(pid)
);
