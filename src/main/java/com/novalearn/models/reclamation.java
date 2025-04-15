package com.novalearn.models;


public class reclamation {
    private int id;
    private String nom;
    private String lettre;

    public reclamation(int id, String nom, String lettre) {
        this.id = id;
        this.nom = nom;
        this.lettre = lettre;
    }

    // Getters & Setters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getLettre() { return getLettre(); }

    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setLettre(String lettre) { this.lettre = lettre; }
}