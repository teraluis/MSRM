-- adding new people
INSERT INTO people (uuid, title, lastname, firstname, email, mobilephone, workphone, jobdescription, tenant, workmail) VALUES ('people-2ca5c35d-889b-430d-ac94-7a19f0829454', 'Monsieur', 'BATAILLE','Aymeric', 'bataille@caennaise.com', '06 08 10 26 15', '02 31 46 10 48', 'Technicien Etat des Lieux', 'ADX', null);
INSERT INTO people (uuid, title, lastname, firstname, email, mobilephone, tenant) VALUES ('people-b6dc1663-e98f-4d26-98ae-f4d203ddd636', 'Madame', 'JAIGU','Aurélie', 'ajaigu@caennaise.com', '06 00 00 00 00', 'ADX');
INSERT INTO people (uuid, title, lastname, firstname, email, mobilephone, workphone, jobdescription, tenant, workmail) VALUES ('people-fc9ac79c-fec5-40ca-ac94-962157e05f58', 'Madame', 'LEROISER', 'Aurélie', 'aleroiser@caennaise.com', '06 08 10 25 21', '02 31 46 82 20', 'Responsable du patrimoine', 'ADX', null);

-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('mbarbedienne@allodiag.fr', '0000883', 'Marc', 'BARBEDIENNE', 'CHATEAU-GONTIER', '02 85 40 10 27', 'Assistant (e) Comptable', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('arouillard@allodiag.fr', '0000004', 'Arnaud', 'ROUILLARD', 'CHATEAU-GONTIER', '06 23 17 32 82', 'Responsable Informatique', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('mpicot@allodiag.fr', '0001229', 'Maxence', 'PICOT', 'CHATEAU-GONTIER', null, 'Developpeur', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('econrard@allodiag.fr', '0000390', 'Elodie', 'CONRARD', 'CESSON SEVIGNE', '06 12 66 52 57', 'Conseiller(e) Sedentaire Referent(e)', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('olelievre@allodiag.fr', '0000906', 'Ombeline', 'LELIEVRE', 'CHATEAU-GONTIER', '09 70 69 01 62', 'Conseiller(e) Sedentaire', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('irobinson@allodiag.fr', '0000785', 'Iris', 'ROBINSON', 'CHATEAU-GONTIER', '09 70 69 01 64', 'Conseiller(e) Sedentaire', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('fquetel@allodiag.fr', '0001050', 'Florence', 'QUETEL', 'IFS', '07 61 80 96 45', 'Commercial', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('ctessier@allodiag.fr', '0000003', 'Christine', 'TESSIER', 'CHATEAU-GONTIER', '02 85 40 10 39', 'Assistant (e) Recouvrement', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('sboulday@allodiag.fr', '0000444', 'Sylvain', 'BOULDAY', 'CHATEAU-GONTIER', '06 17 36 31 97', 'Responsable Agence', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('lchapdelaine@allodiag.fr', '0001185', 'Lucas', 'CHAPDELAINE', 'CHATEAU-GONTIER', null, 'Lead Developpeur', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('adxcalypso@allodiag.fr', null, 'adx', 'calypso', null, null, 'Compte Test Calypso pour Rémy et Lucas', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('ccribier@allodiag.fr', '0000726', 'Coraline', 'CRIBIER', 'CHATEAU-GONTIER', '07 61 80 96 49', 'Commercial', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('smorliere@allodiag.fr', null, 'Sebastien', 'MORLIERE', null, null, 'Fabrick', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('cdecheffontaines@allodiag.fr', '0000895', 'Charles', 'DE CHEFFONTAINES', 'CHATEAU-GONTIER', '06 77 57 42 80', 'Directeur Général', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('rdelmotte@allodiag.fr', '0001157', 'Remy', 'DELMOTTE', 'CHATEAU-GONTIER', '06 47 81 58 04', 'Directeur des Systemes d''Information', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('wparaniak@allodiag.fr', '0000367', 'Willy', 'PARANIAK', 'CHATEAU-GONTIER', '06 22 01 13 23', 'Responsable Infrastructure Informatique', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('tandre@allodiag.fr', null, 'Thibault', 'ANDRE', null, null, 'Fabrick', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('lelmoghazli@allodiag.fr', null, 'Lamia', 'ELMOGHAZLI', null, null, 'CGI', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('cdelaunay@allodiag.fr', '0000743', 'Cindy', 'DELAUNAY', 'CHATEAU-GONTIER', '02 85 40 10 31', 'Assistant (e) Recouvrement', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('gsgard@allodiag.fr', null, 'Guillaume', 'SGARD', null, null, 'StelR', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('mpraud@allodiag.fr', '0000774', 'Martin', 'PRAUD', 'CHATEAU-GONTIER', '06 29 39 56 02', 'Chargé projet web', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('rrobin@allodiag.fr', '0000076', 'Rozenn', 'ROBIN', 'CESSON SEVIGNE', '06 17 75 40 23', 'Assistant (e) Administratif (ve) Coordinatrice', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('cbraet@allodiag.fr', null, 'Cedric', 'BRAET', null, null, 'Fabrick', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('jbalavoine@allodiag.fr', '0000499', 'Julien', 'BALAVOINE', 'IFS', '06 11 16 30 27', 'Responsable Agence', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('dkuntz@allodiag.fr', null, 'David', 'KUNTZ', null, null, 'Fabrick', false);
-- INSERT INTO public.users (login, registration_number, first_name, last_name, office, phone, description, deleted) VALUES ('edegend@allodiag.fr', '0001214', 'Emilie', 'DE GEND', 'CHATEAU-GONTIER', null, 'Developpeuse', false);


-- adding La Caennaise
INSERT INTO addresses (uuid, "type", address1, address2, post_code, city, created, tenant) VALUES ('address-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'Postale', '66 Avenue de Thiès', 'BP 75174', '14075', 'CAEN CEDEX', NOW(), 'ADX');
INSERT INTO entities (uuid, "name", corporate_name, "type", siren, "domain", main_address, created, tenant) VALUES ('entity-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'La Caennaise', 'Société Caennaise de Développement Immobilier', 'SAEM', '613820596', 'Privé', 'address-2bf2977f-6c01-48b7-9913-444d8ba1f198', NOW(), 'ADX');
INSERT INTO accounts (uuid, "type", reference, category, commercial, contact, importance, state, entity, max_payment_time, legacy_code, created, tenant) VALUES ('account-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'Professionnel', '613820596', 'Client', 'jbalavoine@allodiag.fr', 'people-fc9ac79c-fec5-40ca-ac94-962157e05f58', 'Primordiale', '0', 'entity-2bf2977f-6c01-48b7-9913-444d8ba1f198', 30, '14A37837', NOW(), 'ADX');
INSERT INTO establishments (uuid, "name", corporate_name, siret, entity, created, tenant, sage_code, bic, iban, facturation_analysis) VALUES ('establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'La Caennaise', 'Société Caennaise de Développement Immobilier', '61382059600039', 'entity-2bf2977f-6c01-48b7-9913-444d8ba1f198', NOW(), 'ADX', '', '', '', '');
INSERT INTO agencies (uuid, "name", manager, tenant, created) VALUES ('agency-55c30f66-3b20-4664-9c72-951fad4ddf4a', 'test', 'jbalavoine@allodiag.fr', 'ADX', now());
INSERT INTO markets (uuid, "name", market_number, agencie_id, tenant) VALUES ('market-ae1dca5a-97ef-4345-a91e-fba4cD86e4bb9', 'La CAENNAISE', 'AL/MB/N°1535', 'agency-55c30f66-3b20-4664-9c72-951fad4ddf4a', 'ADX');
INSERT INTO bpu (uuid, file, tenant, market_id) VALUES ('bpu-62611d4e-7ff5-4aa4-a1c5-43bf49ddd8ff', '2019_BPU_La_Caennaise.pdf', 'ADX', 'market-ae1dca5a-97ef-4345-a91e-fba4cD86e4bb9');
INSERT INTO adnparameters(adnid, "name", address1, address2, zip, city, entity, tenant) VALUES (37837,'LA CAENNAISE', '66 Avenue de Thiès', 'BP 75174', '14075', 'CAEN CEDEX', 'entity-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'ADX');
INSERT INTO accounts (uuid, "type", reference, category, commercial, contact, state, created, tenant) VALUES ('account-2bf2977f-6c01-48b7-9913-444d8ba1f168', 'Particulier', '', 'Autre', 'jbalavoine@allodiag.fr', 'people-2ca5c35d-889b-430d-ac94-7a19f0829454', '0', NOW(), 'ADX');
INSERT INTO accounts (uuid, "type", reference, category, commercial, contact, state, created, tenant) VALUES ('account-1cf2977f-6c01-48b7-9913-444d8ba1f138', 'Particulier', '', 'Autre', 'jbalavoine@allodiag.fr', 'people-b6dc1663-e98f-4d26-98ae-f4d203ddd636', '0', NOW(), 'ADX');
INSERT INTO establishment_accounts(establishment_id, account_id, role) VALUES ('establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'account-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'Payeur');
INSERT INTO establishment_accounts(establishment_id, account_id, role) VALUES ('establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'account-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'Facturé');
INSERT INTO establishment_accounts(establishment_id, account_id, role) VALUES ('establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'account-2bf2977f-6c01-48b7-9913-444d8ba1f168', 'Donneur d''ordre');
INSERT INTO establishment_accounts(establishment_id, account_id, role) VALUES ('establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'account-1cf2977f-6c01-48b7-9913-444d8ba1f138', 'Donneur d''ordre');
INSERT INTO establishment_addresses(establishment_id, address_id, role) VALUES ('establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'address-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'Adresse principale');
INSERT INTO establishment_people(establishment_id, people_id, role) VALUES ('establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'people-fc9ac79c-fec5-40ca-ac94-962157e05f58', 'Contact principal');

-- adding market contacts
INSERT INTO report_destinations(uuid, establishment, dest_people, tenant) VALUES ('reportdestination-24870ff4-9119-44cb-b0db-031ea3d8fc99', 'establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'people-b6dc1663-e98f-4d26-98ae-f4d203ddd636', 'ADX');
INSERT INTO report_destinations(uuid, establishment, dest_mail, tenant) VALUES ('reportdestination-e3157f83-0285-4efd-82e6-d01a43eab95f', 'establishment-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'edl@caennaise.com', 'ADX');

INSERT INTO markets_accounts (markets_id, accounts_id, role) VALUES ('market-ae1dca5a-97ef-4345-a91e-fba4cD86e4bb9', 'account-2bf2977f-6c01-48b7-9913-444d8ba1f198', 'Client');
INSERT INTO markets_people (markets_id, people_id, role) VALUES ('market-ae1dca5a-97ef-4345-a91e-fba4cD86e4bb9', 'people-fc9ac79c-fec5-40ca-ac94-962157e05f58', 'Contact principal');
INSERT INTO markets_users (markets_id, users_id, role) VALUES ('market-ae1dca5a-97ef-4345-a91e-fba4cD86e4bb9', 'jbalavoine@allodiag.fr', 'Commercial Référent');

-- activities
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-907a656a-8b44-464e-8db3-c55bc97e91b3', 'Administrateur de biens', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-4456a003-d2a8-4dd3-86ff-22c12a89e88a', 'Administrateurs judiciaires', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-8cb01157-34d5-487a-b4fd-4556a10c066e', 'Agences immobilières', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-1eb7d6a7-5797-4e95-ad27-8572d05fe7b7', 'Avocats spécialistes en droit immobilier', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-83dba376-bed0-4f71-ad07-4bed3206c557', 'Bâtiment', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-474d1c1a-d8c8-4fcd-913b-40ca6dc1efa6', 'Démolition', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-1b8341dd-5641-4afd-953e-9c955cdd13cb', 'Désamianteur', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-0a36b7a9-f6b0-4efb-a89a-b0966021452c', 'Diagnostics immobiliers', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-e6442792-4b1f-4c2a-aa96-e5dd86312be1', 'Expertise, assurance', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-5ba6f85a-0503-4746-88c0-2bdecdd3af2a', 'Géomètres', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-388114fd-2913-4714-b06d-a116b0707e56', 'Huissiers de justice', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-1808974b-79c5-45af-8039-36c9817fec27', 'Immobilier (locations saisonnières et temporaires)', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-964d352e-2b00-4c80-91d9-2250ee0d0a9e', 'Immobilier (lotisseurs, aménageurs fonciers)', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-6c1e6fd4-7c3e-45c1-92fe-ce76ac5da0fb', 'Ingénierie, bureaux d''études', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-4d91c30a-2aec-4085-8454-77e8c0863fed', 'Mairie', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-e8477c1a-7888-47b8-b6b5-dbef6c06bea1', 'Mandataires judiciaires', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-0d1ee11e-85ad-45d4-9304-c4b093cfc75e', 'Offices et gestion d''HLM', NOW(), 'ADX');
INSERT INTO activities (uuid, "name", created, tenant) VALUES ('activity-206842f7-293d-47cc-aa0f-cb2e913fd231', 'Syndics de copropriétés', NOW(), 'ADX');

