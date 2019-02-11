package us.centile.permissions.grant.procedure;

import java.util.*;

public class GrantRecipient
{
    private final UUID uuid;
    private final String name;
    
    public GrantRecipient(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public String getName() {
        return this.name;
    }
}
