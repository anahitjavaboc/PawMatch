package com.anahit.pawmatch.models;

public class User {
    private String name;
    private String email;
    private Integer age; // Changed from String to Integer
    private String gender; // Added gender field

    public User() {
        // Required for Firebase
    }

    public User(String name, String email, Integer age, String gender) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}