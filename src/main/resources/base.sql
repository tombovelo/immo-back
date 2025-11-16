----------- CREATE role tombovelo login password 'tombovelo';
CREATE role tombovelo LOGIN SUPERUSER PASSWORD 'tombovelo'; 

------- donne le privilege a user tombovelo de cree une base de donne 
ALTER role tombovelo createdb;

------- creation de la base de donnee dbimmobilier
CREATE DATABASE dbimmobilier;

------- permetre a l'utilisateur tombovelo  de creer une table dans la base dbimmobilier 
ALTER DATABASE dbimmobilier OWNER TO tombovelo;

psql -h localhost -U tombovelo -d dbimmobilier;


-- Table des types de transaction
CREATE TABLE type_transaction (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- Table des propriétaires
CREATE TABLE proprietaire (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    adresse VARCHAR(200),
    password VARCHAR(200) NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE Admin (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(200) NOT NULL
);

CREATE TABLE Payement(
    id SERIAL PRIMARY KEY,
    maison_id INTEGER NOT NULL,
    date_payement DATE;
    status VARCHAR(20);
)

-- Table des maisons
CREATE TABLE maison (
    id SERIAL PRIMARY KEY,
    proprietaire_id INTEGER NOT NULL,
    type_transaction_id INTEGER NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    ville VARCHAR(100) NOT NULL,
    code_postal VARCHAR(10) NOT NULL,
    nombre_pieces INTEGER,
    prix DECIMAL(12,2),
    description VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    visible BOOLEAN DEFAULT TRUE, 
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (proprietaire_id) REFERENCES proprietaire(id) ON DELETE CASCADE,
    FOREIGN KEY (type_transaction_id) REFERENCES type_transaction(id)
);

-- Table des albums
CREATE TABLE album (
    id SERIAL PRIMARY KEY,
    maison_id INTEGER NOT NULL,
    nom_album VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maison_id) REFERENCES maison(id) ON DELETE CASCADE
);

-- Table des photos
CREATE TABLE photo (
    id SERIAL PRIMARY KEY,
    album_id INTEGER NOT NULL,
    nom_fichier VARCHAR(255) NOT NULL,
    chemin TEXT NOT NULL,
    description VARCHAR(500),
    ordre INTEGER DEFAULT 0,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE
);

/////////////////////////////////////////////

CREATE TABLE statut_paiement (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(20) NOT NULL UNIQUE -- ex: 'PAYE', 'NON_PAYE', 'EN_ATTENTE'
);

CREATE TABLE mois (
    id SERIAL PRIMARY KEY,
    numero INTEGER NOT NULL CHECK (numero BETWEEN 1 AND 12) UNIQUE,
    nom VARCHAR(20) NOT NULL UNIQUE -- ex: 'Janvier', 'Février', ...
);

CREATE TABLE paiement (
    id SERIAL PRIMARY KEY,
    maison_id INTEGER NOT NULL,
    mois_id INTEGER NOT NULL,              -- référence à la table mois
    annee INTEGER NOT NULL,
    date_payement DATE NOT NULL,
    montant DECIMAL(12,2) NOT NULL,
    statut_id INTEGER NOT NULL,            -- référence à la table statut_paiement
    FOREIGN KEY (maison_id) REFERENCES maison(id) ON DELETE CASCADE,
    FOREIGN KEY (mois_id) REFERENCES mois(id),
    FOREIGN KEY (statut_id) REFERENCES statut_paiement(id),
    UNIQUE(maison_id, mois_id, annee)      -- éviter double paiement du même mois
);

INSERT INTO statut_paiement (nom) VALUES ('PAYE'), ('NON_PAYE'), ('EN_ATTENTE');

INSERT INTO mois (numero, nom) VALUES
(1, 'Janvier'),
(2, 'Février'),
(3, 'Mars'),
(4, 'Avril'),
(5, 'Mai'),
(6, 'Juin'),
(7, 'Juillet'),
(8, 'Août'),
(9, 'Septembre'),
(10, 'Octobre'),
(11, 'Novembre'),
(12, 'Décembre');

-- une seule requête pour voir payées et non payées
SELECT m.id AS maison_id,
       m.adresse,
       p.nom AS nom,
       p.prenom AS prenom,
       p.telephone AS telephone,
    --    p.email AS email,
       COALESCE(sp.nom, 'NON_PAYE') AS statut,
       pay.date_payement,
    --    pay.montant
    pay.mois_id,
    pay.annee,
    m.visible 
FROM maison m
JOIN proprietaire p ON m.proprietaire_id = p.id
LEFT JOIN paiement pay 
       ON pay.maison_id = m.id 
      AND pay.annee = 2025
      AND pay.mois_id = (SELECT id FROM mois WHERE numero = 1) -- Janvier
LEFT JOIN statut_paiement sp ON pay.statut_id = sp.id;
