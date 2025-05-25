package com.anahit.pawmatch.models;

public class Pet {
    private String id;
    private String name;
    private String ownerId;
    private String imageUrl;
    private String healthStatus;
    private int age; // Changed to int to match Firebase rules
    private String breed;
    private String bio;

    public Pet() {}

    public Pet(String id, String name, String ownerId, String imageUrl, String healthStatus, int age, String breed, String bio) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
        this.healthStatus = healthStatus;
        this.age = age;
        this.breed = breed;
        this.bio = bio;
    }

    public Pet(String name, String breed, String ownerId, String imageUrl) {
        this.id = null;
        this.name = name;
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
        this.healthStatus = "Unknown";
        this.age = 0; // Default value for age
        this.breed = breed;
        this.bio = "No bio available";
    }

    public Pet(String name, int age, String ownerId, String imageUrl) {
        this.id = null;
        this.name = name;
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
        this.healthStatus = "Unknown";
        this.age = age;
        this.breed = null;
        this.bio = "No bio available";
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}