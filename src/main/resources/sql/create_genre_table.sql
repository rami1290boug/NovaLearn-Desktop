CREATE TABLE IF NOT EXISTS genre (
    id INT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Insertion des genres par défaut
INSERT INTO genre (libelle, description) VALUES
('Technique', 'Problèmes techniques liés à l''application'),
('Contenu', 'Problèmes liés au contenu des cours'),
('Paiement', 'Problèmes liés aux paiements'),
('Compte', 'Problèmes liés au compte utilisateur'),
('Autre', 'Autres types de problèmes'); 