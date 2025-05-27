package com.anahit.pawmatch.models;

public class Match {
    private String id;
    private String userId;
    private String petId;
    private String petOwnerId;
    private String petName;
    private String ownerName;
    private String petImageUrl;
    private long timestamp;
    private String status;

    public Match() {}

    public Match(String id, String userId, String petId, String petOwnerId, String petName, String ownerName,
                 String petImageUrl, long timestamp, String status) {
        this.id = id != null ? id : "";
        this.userId = userId != null ? userId : "";
        this.petId = petId != null ? petId : "";
        this.petOwnerId = petOwnerId != null ? petOwnerId : "";
        this.petName = petName != null ? petName : "";
        this.ownerName = ownerName != null ? ownerName : "";
        this.petImageUrl = petImageUrl != null ? petImageUrl : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
        this.status = status != null ? status : "pending";
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id != null ? id : ""; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId != null ? userId : ""; }
    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId != null ? petId : ""; }
    public String getPetOwnerId() { return petOwnerId; }
    public void setPetOwnerId(String petOwnerId) { this.petOwnerId = petOwnerId != null ? petOwnerId : ""; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName != null ? petName : ""; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName != null ? ownerName : ""; }
    public String getPetImageUrl() { return petImageUrl; }
    public void setPetImageUrl(String petImageUrl) { this.petImageUrl = petImageUrl != null ? petImageUrl : ""; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis(); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status != null ? status : "pending"; }
}