package us.centile.permissions.rank;

import us.centile.permissions.*;
import java.util.*;

public class Rank
{
    private static List<Rank> ranks;
    private static nPermissions main;
    private final UUID uuid;
    private final List<UUID> inheritance;
    private final List<String> permissions;
    private final RankData data;
    
    public Rank(final UUID uuid, final List<UUID> inheritance, final List<String> permissions, final RankData data) {
        this.uuid = uuid;
        this.inheritance = inheritance;
        this.permissions = permissions;
        this.data = data;
        Rank.ranks.add(this);
    }
    
    public static Rank getDefaultRank() {
        for (final Rank rank : Rank.ranks) {
            if (rank.getData().isDefaultRank()) {
                return rank;
            }
        }
        return null;
    }
    
    public static Rank getByName(final String name) {
        for (final Rank rank : Rank.ranks) {
            if (rank.getData().getName().replace(" ", "").equalsIgnoreCase(name.replace(" ", ""))) {
                return rank;
            }
        }
        return null;
    }
    
    public static Rank getByUuid(final UUID uuid) {
        for (final Rank rank : Rank.ranks) {
            if (rank.getUuid().equals(uuid)) {
                return rank;
            }
        }
        return null;
    }
    
    public static List<Rank> getRanks() {
        return Rank.ranks;
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public List<UUID> getInheritance() {
        return this.inheritance;
    }
    
    public List<String> getPermissions() {
        return this.permissions;
    }
    
    public RankData getData() {
        return this.data;
    }
    
    static {
        Rank.ranks = new ArrayList<Rank>();
        Rank.main = nPermissions.getInstance();
    }
}
