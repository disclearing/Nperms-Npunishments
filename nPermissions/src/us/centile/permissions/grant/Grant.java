package us.centile.permissions.grant;

import us.centile.permissions.rank.*;
import java.util.*;

public class Grant
{
    private UUID issuer;
    private UUID rankId;
    private long dateAdded;
    private long duration;
    private String reason;
    private boolean active;
    
    public Grant(final UUID issuer, final Rank rank, final long dateAdded, final long duration, final String reason, final boolean active) {
        this.issuer = issuer;
        this.rankId = rank.getUuid();
        this.dateAdded = dateAdded;
        this.duration = duration;
        this.reason = reason;
        this.active = active;
    }
    
    public Rank getRank() {
        Rank toReturn = Rank.getByUuid(this.rankId);
        if (toReturn == null) {
            this.active = false;
            toReturn = new Rank(UUID.randomUUID(), new ArrayList<UUID>(), new ArrayList<String>(), new RankData("N/A"));
            Rank.getRanks().remove(toReturn);
            return toReturn;
        }
        return toReturn;
    }
    
    public boolean isExpired() {
        return !this.active || System.currentTimeMillis() >= this.dateAdded + this.duration;
    }
    
    public UUID getIssuer() {
        return this.issuer;
    }
    
    public void setIssuer(final UUID issuer) {
        this.issuer = issuer;
    }
    
    public UUID getRankId() {
        return this.rankId;
    }
    
    public void setRankId(final UUID rankId) {
        this.rankId = rankId;
    }
    
    public long getDateAdded() {
        return this.dateAdded;
    }
    
    public void setDateAdded(final long dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    public long getDuration() {
        return this.duration;
    }
    
    public void setDuration(final long duration) {
        this.duration = duration;
    }
    
    public String getReason() {
        return this.reason;
    }
    
    public void setReason(final String reason) {
        this.reason = reason;
    }
    
    public boolean isActive() {
        return this.active;
    }
    
    public void setActive(final boolean active) {
        this.active = active;
    }
}
