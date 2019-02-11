package us.centile.permissions.profile;

import us.centile.permissions.*;
import us.centile.permissions.grant.*;
import org.bukkit.entity.*;
import org.bukkit.permissions.*;
import org.bukkit.plugin.*;
import us.centile.permissions.rank.*;
import org.bukkit.scheduler.*;
import org.bson.*;
import org.bukkit.*;
import com.google.gson.*;
import com.mongodb.client.model.*;
import java.io.*;
import org.json.simple.parser.*;
import java.util.*;

public class Profile
{
    private static Set<Profile> profiles;
    private static nPermissions main;
    private final UUID uuid;
    private final List<String> permissions;
    private final List<Grant> grants;
    private final Player player;
    private String name;
    private boolean loaded;
    private PermissionAttachment attachment;
    
    public Profile(final UUID uuid, final List<String> permissions, final List<Grant> grants) {
        this.uuid = uuid;
        this.permissions = permissions;
        this.grants = grants;
        this.player = Bukkit.getPlayer(uuid);
        if (this.player != null) {
            this.name = this.player.getName();
            this.attachment = this.player.addAttachment((Plugin)Profile.main);
        }
        else {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer != null) {
                this.name = offlinePlayer.getName();
            }
            this.attachment = null;
        }
        Profile.profiles.add(this);
    }
    
    public Grant getActiveGrant() {
        Grant toReturn = null;
        for (final Grant grant : this.grants) {
            if (grant.isActive() && !grant.getRank().getData().isDefaultRank()) {
                toReturn = grant;
            }
        }
        if (toReturn == null) {
            toReturn = new Grant(null, Rank.getDefaultRank(), System.currentTimeMillis(), 2147483647L, "Default Rank", true);
        }
        return toReturn;
    }
    
    public void asyncLoad() {
        new BukkitRunnable() {
            public void run() {
                Profile.this.load();
            }
        }.runTaskAsynchronously((Plugin)Profile.main);
    }
    
    public Profile load() {
        final Document document = (Document)Profile.main.getPermissionsDatabase().getProfiles().find(Filters.eq("uuid", this.uuid.toString())).first();
        if (document != null) {
            for (final JsonElement element : new JsonParser().parse(document.getString("grants")).getAsJsonArray()) {
                final JsonObject keyGrant = element.getAsJsonObject();
                UUID issuer = null;
                if (keyGrant.get("issuer") != null) {
                    issuer = UUID.fromString(keyGrant.get("issuer").getAsString());
                }
                final long dateAdded = keyGrant.get("dateAdded").getAsLong();
                final long duration = keyGrant.get("duration").getAsLong();
                final String reason = keyGrant.get("reason").getAsString();
                final boolean active = keyGrant.get("active").getAsBoolean();
                Rank rank;
                try {
                    rank = Rank.getByUuid(UUID.fromString(keyGrant.get("rank").getAsString()));
                }
                catch (Exception ex) {
                    rank = Rank.getByName(keyGrant.get("rank").getAsString());
                    if (rank == null) {
                        throw new IllegalArgumentException("Invalid rank parameter");
                    }
                }
                if (rank != null) {
                    this.grants.add(new Grant(issuer, rank, dateAdded, duration, reason, active));
                }
            }
            if (document.containsKey("recentName")) {
                this.name = document.getString("recentName");
            }
            final List<String> permissionsList = new ArrayList<String>();
            for (final String id : document.get("permissions").toString().replace("[", "").replace("]", "").replace(" ", "").split(",")) {
                if (!id.isEmpty()) {
                    permissionsList.add(id);
                }
            }
            this.permissions.addAll(permissionsList);
        }
        boolean hasDefaultRank = false;
        final Iterator<Grant> iterator = this.grants.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getRank().getData().isDefaultRank()) {
                hasDefaultRank = true;
                break;
            }
        }
        if (!hasDefaultRank) {
            this.grants.add(new Grant(null, Rank.getDefaultRank(), System.currentTimeMillis(), 2147483647L, "Default Rank", true));
        }
        this.loaded = true;
        this.setupAtatchment();
        return this;
    }
    
    public void setupAtatchment() {
        if (this.attachment != null) {
            final Player player = Bukkit.getPlayer(this.uuid);
            if (player != null) {
                final Grant grant = this.getActiveGrant();
                if (!player.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', grant.getRank().getData().getPrefix() + player.getName() + grant.getRank().getData().getSuffix()))) {
                    player.setDisplayName(ChatColor.translateAlternateColorCodes('&', grant.getRank().getData().getPrefix() + player.getName() + grant.getRank().getData().getSuffix()));
                }
            }
            for (final String permission : this.attachment.getPermissions().keySet()) {
                this.attachment.unsetPermission(permission);
            }
            for (final Grant grant2 : this.grants) {
                if (grant2 == null) {
                    continue;
                }
                if (grant2.isExpired()) {
                    continue;
                }
                for (final String permission2 : grant2.getRank().getPermissions()) {
                    this.attachment.setPermission(permission2.replace("-", ""), !permission2.startsWith("-"));
                }
                for (final UUID uuid : grant2.getRank().getInheritance()) {
                    final Rank rank = Rank.getByUuid(uuid);
                    if (rank != null) {
                        for (final String permission3 : rank.getPermissions()) {
                            this.attachment.setPermission(permission3.replace("-", ""), !permission3.startsWith("-"));
                        }
                    }
                }
            }
            for (final String permission4 : this.permissions) {
                this.attachment.setPermission(permission4.replace("-", ""), !permission4.startsWith("-"));
            }
            if (player != null) {
                player.recalculatePermissions();
            }
        }
        else {
            final Player player = Bukkit.getPlayer(this.uuid);
            if (player != null) {
                this.attachment = player.addAttachment((Plugin)Profile.main);
                this.load();
            }
        }
    }
    
    public Profile remove() {
        Profile.profiles.remove(this);
        return this;
    }
    
    public Profile save() {
        final Document profileDocument = new Document();
        final JsonArray grantsDocument = new JsonArray();
        profileDocument.put("uuid", (Object)this.uuid.toString());
        if (this.name != null) {
            profileDocument.put("recentName", (Object)this.name);
            profileDocument.put("recentNameLowercase", (Object)this.name.toLowerCase());
        }
        for (final Grant grant : this.grants) {
            final JsonObject grantDocument = new JsonObject();
            if (grant.getRank() == null) {
                continue;
            }
            if (grant.getRank().getData().isDefaultRank()) {
                continue;
            }
            if (grant.getIssuer() != null) {
                grantDocument.addProperty("issuer", grant.getIssuer().toString());
            }
            grantDocument.addProperty("dateAdded", grant.getDateAdded());
            grantDocument.addProperty("duration", grant.getDuration());
            grantDocument.addProperty("reason", grant.getReason());
            grantDocument.addProperty("active", Boolean.valueOf(grant.isActive() && !grant.isExpired()));
            grantDocument.addProperty("rank", grant.getRank().getUuid().toString());
            grantDocument.addProperty("rankName", grant.getRank().getData().getName());
            grantsDocument.add(grantDocument);
        }
        profileDocument.put("grants", (Object)grantsDocument.toString());
        profileDocument.put("permissions", (Object)this.permissions);
        Profile.main.getPermissionsDatabase().getProfiles().replaceOne(Filters.eq("uuid", this.uuid.toString()), profileDocument, new UpdateOptions().upsert(true));
        return this;
    }
    
    public static Profile getByUuid(final UUID uuid) {
        for (final Profile profile : Profile.profiles) {
            if (profile.getUuid().equals(uuid)) {
                return profile;
            }
        }
        return getExternalByUuid(uuid);
    }
    
    private static Profile getExternalByUuid(final UUID uuid) {
        final Profile profile = new Profile(uuid, new ArrayList<String>(), new ArrayList<Grant>()).load();
        profile.remove();
        return profile;
    }
    
    public static Profile getExternalByName(final String name) {
        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            final Profile profile = getByUuid(player.getUniqueId());
            if (profile != null && profile.getName() != null && profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        UUID uuid;
        String realName;
        try {
            final Map.Entry<UUID, String> data = Profile.main.getRankHandler().getExternalUuid(name);
            uuid = data.getKey();
            realName = data.getValue();
        }
        catch (IOException | ParseException ex2) {
            return null;
        }
        final Profile profile2 = new Profile(uuid, new ArrayList<String>(), new ArrayList<Grant>()).load();
        if (profile2.getName() == null || !profile2.getName().equals(realName)) {
            profile2.setName(realName);
        }
        profile2.remove();
        return profile2;
    }
    
    public static Set<Profile> getProfiles() {
        return Profile.profiles;
    }
    
    public UUID getUuid() {
        return this.uuid;
    }
    
    public List<String> getPermissions() {
        return this.permissions;
    }
    
    public List<Grant> getGrants() {
        return this.grants;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public boolean isLoaded() {
        return this.loaded;
    }
    
    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }
    
    public PermissionAttachment getAttachment() {
        return this.attachment;
    }
    
    static {
        Profile.profiles = new HashSet<Profile>();
        Profile.main = nPermissions.getInstance();
    }
}
