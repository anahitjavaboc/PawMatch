package com.anahit.pawmatch.models;

public class Match {
    private String id;
    private String user1;
    private String user2;
    private String petId;
    private String petName;
    private String ownerName;
    private String petImageUrl;
    private long timestamp;
    private String status;

    public Match() {}

    public Match(String id, String user1, String user2, String petId, String petName, String ownerName,
                 String petImageUrl, long timestamp, String status) {
        this.id = id;
        this.user1 = user1;
        this.user2 = user2;
        this.petId = petId;
        this.petName = petName;
        this.ownerName = ownerName;
        this.petImageUrl = petImageUrl;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUser1() { return user1; }
    public void setUser1(String user1) { this.user1 = user1; }
    public String getUser2() { return user2; }
    public void setUser2(String user2) { this.user2 = user2; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getPetImageUrl() { return petImageUrl; }
    public void setPetImageUrl(String petImageUrl) { this.petImageUrl = petImageUrl; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}