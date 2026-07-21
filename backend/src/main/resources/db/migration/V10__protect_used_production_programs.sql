CREATE OR REPLACE FUNCTION protect_used_production_program_parameters()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM production_cycles WHERE program_id = OLD.id)
       AND (
           NEW.stage IS DISTINCT FROM OLD.stage
           OR NEW.required_machine_type IS DISTINCT FROM OLD.required_machine_type
           OR NEW.duration_minutes IS DISTINCT FROM OLD.duration_minutes
           OR NEW.max_temperature_c IS DISTINCT FROM OLD.max_temperature_c
           OR NEW.gentle IS DISTINCT FROM OLD.gentle
           OR NEW.uses_softener IS DISTINCT FROM OLD.uses_softener
           OR NEW.fragrance_policy IS DISTINCT FROM OLD.fragrance_policy
       ) THEN
        RAISE EXCEPTION 'Los parámetros técnicos de un programa utilizado son inmutables'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_protect_used_production_program_parameters
BEFORE UPDATE ON production_programs
FOR EACH ROW
EXECUTE FUNCTION protect_used_production_program_parameters();
