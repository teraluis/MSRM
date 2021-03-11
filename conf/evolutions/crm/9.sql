# --- !Ups

ALTER TABLE orders ADD COLUMN payer_establishment character varying;
ALTER TABLE orders ADD CONSTRAINT payer_establishment_fkey FOREIGN KEY (payer_establishment)
    REFERENCES establishments(uuid) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE orders ADD COLUMN payer_contact character varying;
    ALTER TABLE orders ADD CONSTRAINT payer_contact_fkey FOREIGN KEY (payer_contact)
    REFERENCES people(uuid) ON UPDATE CASCADE ON DELETE CASCADE;

# --- !Downs

ALTER TABLE orders DROP CONSTRAINT IF EXISTS payer_contact_fkey;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS payer_establishment_fkey;
ALTER TABLE orders DROP COLUMN IF EXISTS payer_contact;
ALTER TABLE orders DROP COLUMN IF EXISTS payer_establishment;