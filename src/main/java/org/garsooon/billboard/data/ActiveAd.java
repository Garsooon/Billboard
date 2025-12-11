package org.garsooon.billboard.data;

import java.util.HashMap;
import java.util.Map;

public class ActiveAd {
    private String playerName;
    private String message;
    private long expiryTime;

    public ActiveAd(String playerName, String message, int durationDays) {
        this.playerName = playerName;
        this.message = message;
        this.expiryTime = System.currentTimeMillis() + (durationDays * 24L * 60L * 60L * 1000L);
    }

    public ActiveAd(Map<String, Object> map) {
        this.playerName = (String) map.get("playerName");
        this.message = (String) map.get("message");

        Object expiryObj = map.get("expiryTime");
        this.expiryTime = expiryObj instanceof Number ? ((Number) expiryObj).longValue() : 0L;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("playerName", playerName);
        map.put("message", message);
        map.put("expiryTime", expiryTime);
        return map;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired(long currentTime) {
        return currentTime >= expiryTime;
    }

    public long getRemainingDays() {
        long remaining = expiryTime - System.currentTimeMillis();
        return Math.max(0, remaining / (24L * 60L * 60L * 1000L));
    }

    public long getRemainingHours() {
        long remaining = expiryTime - System.currentTimeMillis();
        return Math.max(0, remaining / (60L * 60L * 1000L));
    }
}