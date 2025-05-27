package com.anahit.pawmatch.models;

import java.util.HashMap;
import java.util.Map;

public class Pet {
    private String id;
    private String name;
    private String ownerId;
    private String imageUrl;
    private String healthStatus;
    private int age;
    private String breed;
    private String bio;
    private String ownerName;
    private String species;
    private Map<String, Vaccination> vaccinationRecords;
    private Map<String, String> medicalHistory;
    private Map<String, Medication> medications;
    private Map<String, VetAppointment> vetAppointments;

    // Default constructor (required for Firebase deserialization)
    public Pet() {
        this.id = null;
        this.name = null;
        this.ownerId = null;
        this.imageUrl = null;
        this.healthStatus = "Unknown";
        this.age = 0;
        this.breed = null;
        this.bio = "No bio available";
        this.ownerName = null;
        this.species = null;
        this.vaccinationRecords = new HashMap<>();
        this.medicalHistory = new HashMap<>();
        this.medications = new HashMap<>();
        this.vetAppointments = new HashMap<>();
    }

    // New constructor to accept name, age, ownerId, and imageUrl
    public Pet(String name, int age, String ownerId, String imageUrl) {
        this.id = null;
        this.name = name;
        this.ownerId = ownerId;
        this.imageUrl = imageUrl;
        this.healthStatus = "Unknown";
        this.age = age;
        this.breed = null;
        this.bio = "No bio available";
        this.ownerName = null;
        this.species = null;
        this.vaccinationRecords = new HashMap<>();
        this.medicalHistory = new HashMap<>();
        this.medications = new HashMap<>();
        this.vetAppointments = new HashMap<>();
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
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public Map<String, Vaccination> getVaccinationRecords() { return vaccinationRecords; }
    public void setVaccinationRecords(Map<String, Vaccination> vaccinationRecords) { this.vaccinationRecords = vaccinationRecords; }
    public Map<String, String> getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(Map<String, String> medicalHistory) { this.medicalHistory = medicalHistory; }
    public Map<String, Medication> getMedications() { return medications; }
    public void setMedications(Map<String, Medication> medications) { this.medications = medications; }
    public Map<String, VetAppointment> getVetAppointments() { return vetAppointments; }
    public void setVetAppointments(Map<String, VetAppointment> vetAppointments) { this.vetAppointments = vetAppointments; }

    // Inner classes for nested objects
    public static class Vaccination {
        private String date;
        private String type;
        private boolean isUpcoming;
        private long reminderTimestamp;

        public Vaccination() {}
        public Vaccination(String date, String type, boolean isUpcoming, long reminderTimestamp) {
            this.date = date;
            this.type = type;
            this.isUpcoming = isUpcoming;
            this.reminderTimestamp = reminderTimestamp;
        }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public boolean isUpcoming() { return isUpcoming; }
        public void setUpcoming(boolean upcoming) { this.isUpcoming = upcoming; }
        public long getReminderTimestamp() { return reminderTimestamp; }
        public void setReminderTimestamp(long reminderTimestamp) { this.reminderTimestamp = reminderTimestamp; }
    }

    public static class Medication {
        private String dosage;
        private String frequency;
        private long reminderTimestamp;

        public Medication() {}
        public Medication(String dosage, String frequency, long reminderTimestamp) {
            this.dosage = dosage;
            this.frequency = frequency;
            this.reminderTimestamp = reminderTimestamp;
        }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public long getReminderTimestamp() { return reminderTimestamp; }
        public void setReminderTimestamp(long reminderTimestamp) { this.reminderTimestamp = reminderTimestamp; }
    }

    public static class VetAppointment {
        private String date;
        private String time;
        private String location;
        private long reminderTimestamp;

        public VetAppointment() {}
        public VetAppointment(String date, String time, String location, long reminderTimestamp) {
            this.date = date;
            this.time = time;
            this.location = location;
            this.reminderTimestamp = reminderTimestamp;
        }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public long getReminderTimestamp() { return reminderTimestamp; }
        public void setReminderTimestamp(long reminderTimestamp) { this.reminderTimestamp = reminderTimestamp; }
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", healthStatus='" + healthStatus + '\'' +
                ", age=" + age +
                ", breed='" + breed + '\'' +
                ", bio='" + bio + '\'' +
                ", ownerName='" + ownerName + '\'' +
                ", species='" + species + '\'' +
                ", vaccinationRecords=" + vaccinationRecords +
                ", medicalHistory=" + medicalHistory +
                ", medications=" + medications +
                ", vetAppointments=" + vetAppointments +
                '}';
    }
}