package us.centile.permissions.grant.procedure;

import us.centile.permissions.rank.*;

public class GrantProcedureData
{
    private GrantProcedureStage stage;
    private Rank rank;
    private long created;
    private long duration;
    private String reason;
    
    public GrantProcedureData() {
        this.stage = GrantProcedureStage.RANK;
    }
    
    public GrantProcedureStage getStage() {
        return this.stage;
    }
    
    public void setStage(final GrantProcedureStage stage) {
        this.stage = stage;
    }
    
    public Rank getRank() {
        return this.rank;
    }
    
    public void setRank(final Rank rank) {
        this.rank = rank;
    }
    
    public long getCreated() {
        return this.created;
    }
    
    public long getDuration() {
        return this.duration;
    }
    
    public void setCreated(final long created) {
        this.created = created;
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
}
