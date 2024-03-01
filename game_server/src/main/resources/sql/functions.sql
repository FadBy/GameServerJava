CREATE OR REPLACE PROCEDURE insert_player_movement_with_name(player_profile_name VARCHAR, move_distance REAL)
    LANGUAGE plpgsql
    AS $$
        DECLARE
            player_profile_id INTEGER; 
        BEGIN
            SELECT id INTO player_profile_id
            FROM player_profile
            WHERE nickname = player_profile_name;

            IF player_profile_id IS NOT NULL THEN
                INSERT INTO player_movement (player_profile_id, move_distance) VALUES (player_profile_id, move_distance);
            ELSE
                RAISE EXCEPTION 'player_profile.nickname not found';
            END IF;
        END;
    $$;

CREATE OR REPLACE PROCEDURE insert_player_action_with_name(player_profile_name VARCHAR, action_type_name VARCHAR)
    LANGUAGE plpgsql
    AS $$
        DECLARE
            player_profile_id INTEGER;
            action_type_id INTEGER;
        BEGIN
            SELECT id INTO player_profile_id
            FROM player_profile
            WHERE nickname = player_profile_name;

            SELECT id INTO action_type_id
            FROM action_type
            WHERE action_type.name = action_type_name;

            IF player_profile_id IS NOT NULL THEN
                INSERT INTO player_action (player_profile_id, action_type_id) VALUES (player_profile_id, action_type_id);
            ELSE
                RAISE EXCEPTION 'player_profile.nickname not found';
            END IF;
        END;
    $$;

CREATE OR REPLACE FUNCTION find_player_moves(player_profile_id INTEGER)
    RETURNS TABLE(id BIGINT, move_distance REAL, created_at TIMESTAMP)
    AS $$
        BEGIN
            RETURN QUERY SELECT id, move_distance, created_at FROM player_movement WHERE player_movement.player_profile_id = player_profile_id;
        END;
    $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION find_player_actions(player_profile_id INTEGER)
    RETURNS TABLE(id BIGINT, action_type_name VARCHAR, created_at TIMESTAMP)
    AS $$
        BEGIN
            RETURN QUERY SELECT id, action_type.name, created_at FROM player_action LEFT JOIN action_type ON player_action.action_type_id = action_type.id WHERE player_movement.player_profile_id = player_profile_id;
        END;
    $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_items_for_player(player_nickname VARCHAR)
    RETURNS TABLE(id INTEGER, item_name VARCHAR)
    AS $$
    DECLARE
        player_profile_id1 INTEGER;
    BEGIN
        SELECT player_profile.id INTO player_profile_id1
        FROM player_profile
        WHERE nickname = player_nickname;

        IF player_profile_id1 IS NOT NULL THEN
            RETURN QUERY
            SELECT player_inventory.id, item.name
            FROM player_inventory
            INNER JOIN item ON player_inventory.item_id = item.id
            WHERE player_inventory.player_profile_id = player_profile_id1;
        ELSE
            RAISE EXCEPTION 'player_profile.nickname not found';
        END IF;
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE insert_player_inventory_with_names(player_nickname VARCHAR, item_name VARCHAR)
LANGUAGE plpgsql
AS $$
DECLARE
    player_profile_id BIGINT;
    item_id BIGINT;
BEGIN
    SELECT id INTO player_profile_id
    FROM player_profile
    WHERE nickname = player_nickname;

    SELECT id INTO item_id
    FROM item
    WHERE name = item_name;

    IF player_profile_id IS NOT NULL AND item_id IS NOT NULL THEN
        INSERT INTO player_inventory (player_profile_id, item_id)
        VALUES (player_profile_id, item_id);
    ELSE
        IF player_profile_id IS NULL THEN
            RAISE EXCEPTION 'player_profile.nickname not found';
        ELSIF item_id IS NULL THEN
            RAISE EXCEPTION 'item.name not found';
        END IF;
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE insert_loot_drop_with_names(drop_source_name VARCHAR, item_name VARCHAR, drop_chance REAL)
LANGUAGE plpgsql
AS $$
DECLARE
    drop_source_id INTEGER;
    item_id INTEGER;
BEGIN
    SELECT id INTO drop_source_id
    FROM drop_source
    WHERE name = drop_source_name;

    SELECT id INTO item_id
    FROM item
    WHERE name = item_name;

    IF drop_source_id IS NOT NULL AND item_id IS NOT NULL THEN
        INSERT INTO loot_drop (drop_source_id, item_id, chance)
        VALUES (drop_source_id, item_id, drop_chance);
    ELSE
        IF drop_source_id IS NULL THEN
            RAISE EXCEPTION 'drop_source.name not found';
        ELSIF item_id IS NULL THEN
            RAISE EXCEPTION 'item.name not found';
        END IF;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION find_player_item_transactions(player_profile_id INTEGER)
    RETURNS TABLE(id BIGINT, item_name VARCHAR, created_at TIMESTAMP)
    AS $$
        BEGIN
            RETURN QUERY SELECT id, item.name, created_at FROM player_item_transaction LEFT JOIN item ON player_item_transaction.item_id = item.id WHERE player_item_transaction.player_profile_id = player_profile_id;
        END;
    $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION find_player_money_transaction(player_profile_id INTEGER)
    RETURNS TABLE(id BIGINT, money_source_name VARCHAR, money_amount REAL, created_at TIMESTAMP)
    AS $$
        BEGIN
            RETURN QUERY SELECT id, money_source.name, money_source.money_amount, created_at FROM money_transaction LEFT JOIN money_source ON money_transaction.money_source_id = money_source.id WHERE money_transaction.player_profile_id = player_profile_id;
        END;
    $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION has_enough_money(player_profile_id INTEGER, amount_needed REAL)
RETURNS BOOLEAN AS $$
DECLARE
    result BOOLEAN;
BEGIN
    SELECT money_amount >= amount_needed INTO result
    FROM player_profile
    WHERE id = player_profile_id;

    RETURN result;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE PROCEDURE process_money_transaction(player_profile_name VARCHAR, money_source_name VARCHAR)
LANGUAGE plpgsql
AS $$
DECLARE
    money_source_id INT;
    player_profile_id INT;
    source_amount REAL;
    player_amount REAL;
    has_enough BOOLEAN;
BEGIN
    SELECT id, money_amount INTO money_source_id, source_amount
    FROM money_source
    WHERE name = money_source_name;

    SELECT id, money_amount INTO player_profile_id, player_amount
    FROM player_profile
    WHERE nickname = player_profile_name;

    IF has_enough_money_names(player_profile_name, money_source_name) THEN
        UPDATE player_profile
        SET money_amount = player_amount - source_amount
        WHERE id = player_profile_id;

        INSERT INTO money_transaction (player_profile_id, money_source_id)
        VALUES (player_profile_id, money_source_id);
    ELSE
        RAISE EXCEPTION 'Player does not have enough money for the transaction';
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION has_enough_money_names(player_profile_name VARCHAR, money_source_name VARCHAR)
RETURNS BOOLEAN AS $$
DECLARE
    player_profile_id INTEGER;
    amount_needed REAL;
    result BOOLEAN;
BEGIN
    SELECT id INTO player_profile_id FROM player_profile WHERE nickname = player_profile_name;
    SELECT money_amount INTO amount_needed FROM money_source WHERE name = money_source_name;

    SELECT has_enough_money(player_profile_id, amount_needed) INTO result;

    RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION select_drop_chances_with_name(drop_source_name VARCHAR)
RETURNS TABLE(item_name VARCHAR, chance REAL) AS $$
DECLARE
    drop_source_id INTEGER;
BEGIN
    SELECT id INTO drop_source_id FROM drop_source WHERE name = drop_source_name;

    RETURN QUERY SELECT item.name, loot_drop.chance FROM loot_drop INNER JOIN item ON loot_drop.item_id = item.id;
END;
$$ LANGUAGE plpgsql;