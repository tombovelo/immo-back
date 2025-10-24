-- Activer l'extension PostGIS pour les types géométriques
CREATE EXTENSION IF NOT EXISTS postgis;

-- Supprimer toutes les tables existantes
DROP TABLE IF EXISTS paiement CASCADE;
DROP TABLE IF EXISTS photo CASCADE;
DROP TABLE IF EXISTS album CASCADE;
DROP TABLE IF EXISTS maison CASCADE;
DROP TABLE IF EXISTS proprietaire CASCADE;
DROP TABLE IF EXISTS admin CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS type_transaction CASCADE;
DROP TABLE IF EXISTS mois CASCADE;
DROP TABLE IF EXISTS statut_paiement CASCADE;

-- Table users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(255)
);

-- Table admin
CREATE TABLE admin (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(200) NOT NULL
);

-- Table proprietaire
CREATE TABLE proprietaire (
    id BIGSERIAL PRIMARY KEY,
    adresse VARCHAR(200),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    telephone VARCHAR(20) NOT NULL,
    user_id BIGINT UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Table type_transaction
CREATE TABLE type_transaction (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- Table maison avec coordonnees spatiales
CREATE TABLE maison (
    id BIGSERIAL PRIMARY KEY,
    adresse VARCHAR(255),
    code_postal VARCHAR(10),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500),
    coordonnees GEOMETRY(Point, 4326) NOT NULL, -- latitude et longitude combinés
    nombre_pieces INTEGER NOT NULL,
    prix DOUBLE PRECISION NOT NULL,
    ville VARCHAR(100) NOT NULL,
    visible BOOLEAN NOT NULL DEFAULT true,
    proprietaire_id BIGINT NOT NULL,
    type_transaction_id BIGINT NOT NULL,
    FOREIGN KEY (proprietaire_id) REFERENCES proprietaire(id) ON DELETE CASCADE,
    FOREIGN KEY (type_transaction_id) REFERENCES type_transaction(id)
);

-- Table album
CREATE TABLE album (
    id BIGSERIAL PRIMARY KEY,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP,
    description VARCHAR(500),
    nom_album VARCHAR(100) NOT NULL,
    path VARCHAR(100) NOT NULL,
    maison_id BIGINT NOT NULL,
    FOREIGN KEY (maison_id) REFERENCES maison(id) ON DELETE CASCADE
);

-- Table photo
CREATE TABLE photo (
    id BIGSERIAL PRIMARY KEY,
    cloudinary_public_id VARCHAR(255) NOT NULL,
    cloudinary_url VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP,
    description VARCHAR(500),
    nom_fichier VARCHAR(255) NOT NULL,
    ordre INTEGER DEFAULT 0,
    album_id BIGINT NOT NULL,
    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

-- Table mois
CREATE TABLE mois (
    id SERIAL PRIMARY KEY,
    numero INTEGER NOT NULL UNIQUE,
    nom VARCHAR(20) NOT NULL UNIQUE,
    CHECK (numero >= 1 AND numero <= 12)
);

-- Table statut_paiement
CREATE TABLE statut_paiement (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(20) NOT NULL UNIQUE
);

-- Table paiement
CREATE TABLE paiement (
    id SERIAL PRIMARY KEY,
    maison_id BIGINT NOT NULL,
    mois_id INTEGER NOT NULL,
    annee INTEGER NOT NULL,
    date_payement DATE NOT NULL,
    montant NUMERIC(12,2) NOT NULL,
    statut_id INTEGER NOT NULL,
    UNIQUE (maison_id, mois_id, annee),
    FOREIGN KEY (maison_id) REFERENCES maison(id) ON DELETE CASCADE,
    FOREIGN KEY (mois_id) REFERENCES mois(id),
    FOREIGN KEY (statut_id) REFERENCES statut_paiement(id)
);

-- Insertion données type_transaction
INSERT INTO type_transaction (nom, description) VALUES
('Location', 'Transaction pour louer une maison'),
('Vente', 'Transaction pour vendre une maison');

-- Insertion données mois
INSERT INTO mois (numero, nom) VALUES
(1, 'Janvier'), (2, 'Février'), (3, 'Mars'), (4, 'Avril'),
(5, 'Mai'), (6, 'Juin'), (7, 'Juillet'), (8, 'Août'),
(9, 'Septembre'), (10, 'Octobre'), (11, 'Novembre'), (12, 'Décembre');

-- Insertion données statut_paiement
INSERT INTO statut_paiement (nom) VALUES
('PAYE'), ('NON_PAYE'), ('EN_ATTENTE');
