# --- !Ups

ALTER TABLE establishments ADD COLUMN agency_id character varying;
ALTER TABLE establishments ADD CONSTRAINT agency_establishment_fkey FOREIGN KEY (agency_id)
    REFERENCES agencies(uuid) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE orders ADD COLUMN billed_establishment character varying;
ALTER TABLE orders ADD CONSTRAINT billed_establishment_fkey FOREIGN KEY (billed_establishment)
    REFERENCES establishments(uuid) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE orders ADD COLUMN billed_contact character varying;
    ALTER TABLE orders ADD CONSTRAINT billed_contact_fkey FOREIGN KEY (billed_contact)
    REFERENCES people(uuid) ON UPDATE CASCADE ON DELETE CASCADE;
-- Update establishments
UPDATE establishments SET agency_id = 'agency-55c30f66-3b20-4664-9c72-951fad4ddf4a' where 1=1;
ALTER TABLE establishments ADD COLUMN IF NOT EXISTS facturation_analysis varchar;
ALTER TABLE markets ADD COLUMN IF NOT EXISTS facturation_analysis varchar;

ALTER TABLE accounts ADD COLUMN IF NOT EXISTS deleted boolean;
# --- !Downs
ALTER TABLE accounts DROP COLUMN IF EXISTS deleted;
ALTER TABLE establishments DROP CONSTRAINT IF EXISTS agencie_establishment_fkey;
ALTER TABLE establishments DROP COLUMN IF EXISTS agency_id;
ALTER TABLE establishments DROP COLUMN IF EXISTS facturation_analysis;
ALTER TABLE markets DROP COLUMN IF EXISTS facturation_analysis;

ALTER TABLE orders DROP CONSTRAINT IF EXISTS billed_contact_fkey;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS billed_establishment_fkey;
ALTER TABLE orders DROP COLUMN IF EXISTS billed_contact;
ALTER TABLE orders DROP COLUMN IF EXISTS billed_establishment;
