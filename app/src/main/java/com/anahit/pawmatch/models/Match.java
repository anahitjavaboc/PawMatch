package com.anahit.pawmatch.models;

public class Match {
    private String matchId;
    private String userId1;
    private String userId2;
    private String petId;
    private String petName;
    private String ownerName;
    private String petImageUrl;

    public Match() {}

    public Match(String matchId, String userId1, String userId2, String petId, String petName) {
        this.matchId = matchId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.petId = petId;
        this.petName = petName;
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
    public String getOwnerName() {
        return ownerName;
    }

    public String getPetImageUrl() {
        return petImageUrl;
    }

}