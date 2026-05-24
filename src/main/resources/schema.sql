CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS graphs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS nodes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    graph_id INT NOT NULL,
    label VARCHAR(255),
    x DOUBLE,
    y DOUBLE,
    FOREIGN KEY (graph_id) REFERENCES graphs(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS edges (
    id INT PRIMARY KEY AUTO_INCREMENT,
    source_node_id INT NOT NULL,
    target_node_id INT NOT NULL,
    graph_id INT NOT NULL,
    weight DOUBLE,
    FOREIGN KEY (source_node_id) REFERENCES nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (target_node_id) REFERENCES nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (graph_id) REFERENCES graphs(id) ON DELETE CASCADE
    );