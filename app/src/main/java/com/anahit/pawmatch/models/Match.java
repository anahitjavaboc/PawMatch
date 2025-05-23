package com.anahit.pawmatch.models;

public class Match {
    private String matchId;
    private String userId1;
    private String userId2;
    private String petId;
    private String petName;
    private String ownerName;
    private String petImageUrl;
    private long timestamp; // New field for match timestamp
    private String status;  // New field for match status

    public Match() {}

    public Match(String matchId, String userId1, String userId2, String petId, String petName, String ownerName, String petImageUrl, long timestamp, String status) {
        this.matchId = matchId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.petId = petId;
        this.petName = petName;
        this.ownerName = ownerName;
        this.petImageUrl = petImageUrl;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getUserId1() { return userId1; }
    public void setUserId1(String userId1) { this.userId1 = userId1; }
    public String getUserId2() { return userId2; }
    public void setUserId2(String userId2) { this.userId2 = userId2; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; } // Added setter
    public String getPetImageUrl() { return petImageUrl; }
    public void setPetImageUrl(String petImageUrl) { this.petImageUrl = petImageUrl; } // Added setter
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}