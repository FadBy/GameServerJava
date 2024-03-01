DROP TRIGGER IF EXISTS trigger_prevent_change_of_register_date ON player_profile;
DROP TRIGGER IF EXISTS trigger_add_movement_to_player_action ON player_movement;
DROP TRIGGER IF EXISTS trigger_prevent_duplicate_item ON player_inventory;
DROP TRIGGER IF EXISTS trigger_add_item_transaction ON player_inventory;

CREATE OR REPLACE FUNCTION prevent_change_of_register_date()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.register_date IS DISTINCT FROM NEW.register_date THEN
        RAISE EXCEPTION 'Cannot update player_profile.register_date';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_change_of_register_date
BEFORE UPDATE ON player_profile FOR EACH ROW
EXECUTE FUNCTION prevent_change_of_register_date();

CREATE OR REPLACE FUNCTION add_movement_to_player_action()
    RETURNS TRIGGER AS $$
DECLARE
    action_type_id INTEGER;
BEGIN
    SELECT id INTO action_type_id FROM action_type WHERE name = 'move';

    IF action_type_id IS NULL THEN
        INSERT INTO action_type (name) VALUES ('move');
        SELECT id INTO action_type_id FROM action_type WHERE name = 'move';
    END IF;

    INSERT INTO player_action (action_type_id, player_profile_id, created_at)
    VALUES (action_type_id, NEW.player_profile_id, NEW.created_at);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_add_movement_to_player_action
AFTER INSERT ON player_movement
FOR EACH ROW
EXECUTE FUNCTION add_movement_to_player_action();

CREATE OR REPLACE FUNCTION prevent_duplicate_item()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM player_inventory
        WHERE player_profile_id = NEW.player_profile_id
          AND item_id = NEW.item_id
    ) THEN
        RAISE EXCEPTION 'player_profile already owns this item';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_duplicate_item
BEFORE INSERT ON player_inventory
FOR EACH ROW
EXECUTE FUNCTION prevent_duplicate_item();

CREATE OR REPLACE FUNCTION add_item_transaction()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO player_item_transaction(player_profile_id, item_id)
    VALUES (NEW.player_profile_id, NEW.item_id);

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_add_item_transaction
AFTER INSERT ON player_inventory
FOR EACH ROW
EXECUTE FUNCTION add_item_transaction();