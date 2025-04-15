ALTER TABLE reclamation
ADD COLUMN genre_id INT,
ADD FOREIGN KEY (genre_id) REFERENCES genre(id); 