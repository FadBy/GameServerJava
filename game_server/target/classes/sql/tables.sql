DROP TABLE IF EXISTS money_transaction;

DROP TABLE IF EXISTS player_item_transaction;

DROP TABLE IF EXISTS loot_drop;

DROP TABLE IF EXISTS drop_source;

DROP TABLE IF EXISTS player_inventory;

DROP TABLE IF EXISTS item;

DROP TABLE IF EXISTS player_action;

DROP TABLE IF EXISTS action_type;

DROP TABLE IF EXISTS player_movement;

DROP TABLE IF EXISTS player_profile;

DROP TABLE IF EXISTS money_source;


CREATE TABLE IF NOT EXISTS player_profile (
    id SERIAL PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    register_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP CHECK (register_date <= CURRENT_TIMESTAMP),
    last_login_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP CHECK (last_login_date >= register_date),
    ban_status BOOLEAN NOT NULL DEFAULT false,
    money_amount REAL CHECK(money_amount >= 0) NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS player_profile_nickname_idx ON player_profile(nickname);

CREATE TABLE IF NOT EXISTS player_movement (
    id BIGSERIAL PRIMARY KEY,
    player_profile_id INTEGER NOT NULL REFERENCES player_profile(id) ON DELETE CASCADE ON UPDATE CASCADE,
    move_distance REAL NOT NULL CHECK (move_distance >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP CHECK (created_at <= CURRENT_TIMESTAMP)
);

CREATE INDEX IF NOT EXISTS player_movement_player_profile_id_idx ON player_movement(player_profile_id);

CREATE TABLE IF NOT EXISTS action_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS player_action (
    id BIGSERIAL PRIMARY KEY,
    action_type_id INTEGER NOT NULL REFERENCES action_type(id) ON DELETE CASCADE ON UPDATE CASCADE,
    player_profile_id INTEGER NOT NULL REFERENCES player_profile(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP CHECK (created_at <= CURRENT_TIMESTAMP)
);

CREATE INDEX IF NOT EXISTS player_action_player_profile_id ON player_action(player_profile_id); 

CREATE TABLE IF NOT EXISTS item (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS player_inventory (
    id SERIAL PRIMARY KEY,
    player_profile_id INTEGER NOT NULL REFERENCES player_profile(id) ON DELETE CASCADE ON UPDATE CASCADE,
    item_id BIGINT NOT NULL REFERENCES item(id) ON DELETE CASCADE ON UPDATE CASCADE 
);

CREATE INDEX IF NOT EXISTS player_inventory_player_profile_id_idx ON player_inventory(player_profile_id);

CREATE TABLE IF NOT EXISTS drop_source (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS loot_drop (
    id SERIAL PRIMARY KEY,
    drop_source_id INTEGER NOT NULL REFERENCES drop_source(id) ON DELETE CASCADE ON UPDATE CASCADE,
    item_id INTEGER NOT NULL REFERENCES item(id) ON DELETE CASCADE ON UPDATE CASCADE,
    chance REAL NOT NULL CHECK (chance > 0) 
);

CREATE TABLE IF NOT EXISTS player_item_transaction (
    id BIGSERIAL PRIMARY KEY,
    player_profile_id INTEGER NOT NULL REFERENCES player_profile(id) ON DELETE CASCADE ON UPDATE CASCADE,
    item_id INTEGER NOT NULL REFERENCES item(id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP CHECK (created_at <= CURRENT_TIMESTAMP)
);

CREATE INDEX IF NOT EXISTS player_item_transaction_player_profile_id_idx ON player_item_transaction(player_profile_id);

CREATE TABLE IF NOT EXISTS money_source (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    money_amount REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS money_transaction (
    id BIGSERIAL NOT NULL,
    player_profile_id INTEGER REFERENCES player_profile(id) ON DELETE CASCADE ON UPDATE CASCADE,
    money_source_id INTEGER REFERENCES money_source ON DELETE CASCADE ON UPDATE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP CHECK (created_at <= CURRENT_TIMESTAMP)
);

CREATE INDEX IF NOT EXISTS money_transaction_player_profile_id_idx ON money_transaction(player_profile_id);
