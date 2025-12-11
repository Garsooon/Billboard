package org.garsooon.billboard.data;

import java.util.HashMap;
import java.util.Map;

public class AdRequest {
    private String playerName;
    private String message;
    private int durationDays;

    public AdRequest(String playerName, String message, int durationDays) {
        this.playerName = playerName;
        this.message = message;
        this.durationDays = durationDays;
    }

    public AdRequest(Map<String, Object> map) {
        this.playerName = (String) map.get("playerName");
        this.message = (String) map.get("message");

        Object durationObj = map.get("durationDays");
        this.durationDays = durationObj instanceof Number ? ((Number) durationObj).intValue() : 0;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("playerName", playerName);
        map.put("message", message);
        map.put("durationDays", durationDays);
        return map;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }

    public int getDurationDays() {
        return durationDays;
    }
}