package com.novalearn.entity;

public class Genre {
    private int id;
    private String libelle;
    private String description;

    public Genre() {
    }

    public Genre(int id, String libelle, String description) {
        this.id = id;
        this.libelle = libelle;
        this.description = description;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return libelle;
    }
} 