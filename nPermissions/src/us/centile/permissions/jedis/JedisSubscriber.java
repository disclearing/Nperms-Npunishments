package us.centile.permissions.jedis;

import redis.clients.jedis.*;
import us.centile.permissions.*;
import us.centile.permissions.profile.*;
import org.bukkit.*;
import us.centile.permissions.grant.*;
import us.centile.permissions.util.*;
import org.bukkit.entity.*;
import us.centile.permissions.rank.*;
import com.google.gson.*;
import java.util.*;

public class JedisSubscriber
{
    private JedisPubSub jedisPubSub;
    private Jedis jedis;
    private nPermissions main;
    
    public JedisSubscriber(final nPermissions main) {
        this.main = main;
        this.jedis = new Jedis(main.getAddress(), main.getPort());
        if (main.getConfigFile().getBoolean("DATABASE.REDIS.AUTHENTICATION.ENABLED")) {
            this.jedis.auth(main.getConfigFile().getString("DATABASE.REDIS.AUTHENTICATION.PASSWORD"));
        }
        this.subscribe();
    }
    
    public void subscribe() {
        this.jedisPubSub = this.get();
        new Thread() {
            @Override
            public void run() {
                JedisSubscriber.this.jedis.subscribe(JedisSubscriber.this.jedisPubSub, "permissions");
            }
        }.start();
    }
    
    private JedisPubSub get() {
        return new JedisPubSub() {
            @Override
            public void onMessage(final String channel, final String message) {
                if (channel.equalsIgnoreCase("permissions")) {
                    final JsonObject object = new JsonParser().parse(message).getAsJsonObject();
                    final JedisSubscriberAction action = JedisSubscriberAction.valueOf(object.get("action").getAsString());
                    final JsonObject payload = object.get("payload").getAsJsonObject();
                    if (action == JedisSubscriberAction.DELETE_PLAYER_PERMISSION) {
                        final Player player = Bukkit.getPlayer(UUID.fromString(payload.get("uuid").getAsString()));
                        if (player != null) {
                            final Profile profile = Profile.getByUuid(player.getUniqueId());
                            if (profile != null) {
                                final String permission = payload.get("permission").getAsString();
                                profile.getPermissions().remove(permission);
                                profile.setupAtatchment();
                            }
                        }
                        return;
                    }
                    if (action == JedisSubscriberAction.ADD_PLAYER_PERMISSION) {
                        final Player player = Bukkit.getPlayer(UUID.fromString(payload.get("uuid").getAsString()));
                        if (player != null) {
                            final Profile profile = Profile.getByUuid(player.getUniqueId());
                            if (profile != null) {
                                final String permission = payload.get("permission").getAsString();
                                profile.getPermissions().add(permission);
                                profile.setupAtatchment();
                            }
                        }
                        return;
                    }
                    if (action == JedisSubscriberAction.ADD_RANK_PERMISSION) {
                        Rank rank;
                        try {
                            rank = Rank.getByUuid(UUID.fromString(payload.get("rank").getAsString()));
                        }
                        catch (Exception ex) {
                            rank = Rank.getByName(payload.get("rank").getAsString());
                            if (rank == null) {
                                throw new IllegalArgumentException("Invalid rank parameter");
                            }
                        }
                        if (rank != null) {
                            final String permission2 = payload.get("permission").getAsString();
                            rank.getPermissions().add(permission2);
                            final Player player2 = Bukkit.getPlayer(payload.get("player").getAsString());
                            if (player2 != null) {
                                player2.sendMessage(ChatColor.GREEN + "Permission '" + permission2 + "' successfully added to rank named '" + rank.getData().getName() + "'.");
                            }
                            for (final Profile profile2 : Profile.getProfiles()) {
                                if (profile2.getActiveGrant().getRank().getUuid().equals(rank.getUuid())) {
                                    profile2.setupAtatchment();
                                }
                            }
                        }
                        return;
                    }
                    if (action == JedisSubscriberAction.DELETE_RANK_PERMISSION) {
                        Rank rank;
                        try {
                            rank = Rank.getByUuid(UUID.fromString(payload.get("rank").getAsString()));
                        }
                        catch (Exception ex) {
                            rank = Rank.getByName(payload.get("rank").getAsString());
                            if (rank == null) {
                                throw new IllegalArgumentException("Invalid rank parameter");
                            }
                        }
                        if (rank != null) {
                            final String permission2 = payload.get("permission").getAsString();
                            rank.getPermissions().remove(permission2);
                            final Player player2 = Bukkit.getPlayer(payload.get("player").getAsString());
                            if (player2 != null) {
                                player2.sendMessage(ChatColor.GREEN + "Permission '" + permission2 + "' successfully removed from rank named '" + rank.getData().getName() + "'.");
                            }
                            for (final Profile profile2 : Profile.getProfiles()) {
                                if (profile2.getActiveGrant().getRank().getUuid().equals(rank.getUuid())) {
                                    profile2.setupAtatchment();
                                }
                            }
                        }
                        return;
                    }
                    if (action == JedisSubscriberAction.DELETE_GRANT) {
                        final UUID uuid = UUID.fromString(payload.get("uuid").getAsString());
                        final Player player3 = Bukkit.getPlayer(uuid);
                        if (player3 != null) {
                            final Profile profile3 = Profile.getByUuid(player3.getUniqueId());
                            if (!profile3.getActiveGrant().getRank().getData().isDefaultRank()) {
                                profile3.getActiveGrant().setActive(false);
                                final Rank rank2 = Rank.getDefaultRank();
                                if (rank2 != null) {
                                    player3.sendMessage(ChatColor.GREEN + "Your rank has been set to " + rank2.getData().getColorPrefix() + rank2.getData().getName() + ChatColor.GREEN + ".");
                                }
                            }
                        }
                        return;
                    }
                    if (action == JedisSubscriberAction.ADD_GRANT) {
                        final JsonObject grant = payload.get("grant").getAsJsonObject();
                        final UUID uuid2 = UUID.fromString(payload.get("uuid").getAsString());
                        final Player player2 = Bukkit.getPlayer(uuid2);
                        if (player2 != null) {
                            final Profile profile4 = Profile.getByUuid(player2.getUniqueId());
                            Rank rank3;
                            try {
                                rank3 = Rank.getByUuid(UUID.fromString(grant.get("rank").getAsString()));
                            }
                            catch (Exception ex2) {
                                rank3 = Rank.getByName(grant.get("rank").getAsString());
                                if (rank3 == null) {
                                    throw new IllegalArgumentException("Invalid rank parameter");
                                }
                            }
                            if (rank3 != null) {
                                final UUID issuer = grant.has("issuer") ? UUID.fromString(grant.get("issuer").getAsString()) : null;
                                for (final Grant other : profile4.getGrants()) {
                                    if (!other.getRank().getData().isDefaultRank() && !other.isExpired()) {
                                        other.setActive(false);
                                    }
                                }
                                final Grant newGrant = new Grant(issuer, rank3, grant.get("datedAdded").getAsLong(), grant.get("duration").getAsLong(), grant.get("reason").getAsString(), true);
                                profile4.getGrants().add(newGrant);
                                player2.sendMessage(ChatColor.GREEN + "Your rank has been set to " + newGrant.getRank().getData().getColorPrefix() + newGrant.getRank().getData().getName() + ChatColor.GREEN + ".");
                            }
                        }
                        return;
                    }
                    if (action == JedisSubscriberAction.IMPORT_RANKS) {
                        Rank.getRanks().clear();
                        final Iterator<Profile> profileIterator = Profile.getProfiles().iterator();
                        while (profileIterator.hasNext()) {
                            final Profile profile = profileIterator.next();
                            final Player player2 = profile.getPlayer();
                            if (player2 != null && profile.getAttachment() != null) {
                                player2.removeAttachment(profile.getAttachment());
                            }
                            profileIterator.remove();
                        }
                        for (final Player online : PlayerUtility.getOnlinePlayers()) {
                            new Profile(online.getUniqueId(), new ArrayList<String>(), new ArrayList<Grant>());
                        }
                        final Player player3 = Bukkit.getPlayer(payload.get("player").getAsString());
                        if (player3 != null) {
                            player3.sendMessage(ChatColor.GREEN + "Ranks successfully imported!");
                        }
                        JedisSubscriber.this.main.getRankHandler().load();
                        return;
                    }
                    if (action == JedisSubscriberAction.ADD_RANK) {
                        final String name = payload.get("name").getAsString();
                        final Rank rank4 = new Rank(UUID.randomUUID(), new ArrayList<UUID>(), new ArrayList<String>(), new RankData(name));
                        final Player player2 = Bukkit.getPlayer(payload.get("player").getAsString());
                        if (player2 != null) {
                            player2.sendMessage(ChatColor.GREEN + "Rank named '" + rank4.getData().getName() + "' successfully created.");
                        }
                        return;
                    }
                    if (action == JedisSubscriberAction.DELETE_RANK) {
                        final Rank rank = Rank.getByName(payload.get("rank").getAsString());
                        if (rank != null) {
                            final Player player3 = Bukkit.getPlayer(payload.get("player").getAsString());
                            if (player3 != null) {
                                player3.sendMessage(ChatColor.GREEN + "Rank named '" + rank.getData().getName() + "' successfully deleted.");
                            }
                            Rank.getRanks().remove(rank);
                        }
                    }
                    if (action == JedisSubscriberAction.SET_RANK_PREFIX) {
                        final Rank rank = Rank.getByName(payload.get("rank").getAsString());
                        if (rank != null) {
                            final Player player3 = Bukkit.getPlayer(payload.get("player").getAsString());
                            rank.getData().setPrefix(payload.get("prefix").getAsString());
                            if (player3 != null) {
                                player3.sendMessage(ChatColor.GREEN + "Rank named '" + rank.getData().getName() + "' prefix successfully changed.");
                            }
                        }
                    }
                    if (action == JedisSubscriberAction.SET_RANK_SUFFIX) {
                        final Rank rank = Rank.getByName(payload.get("rank").getAsString());
                        if (rank != null) {
                            final Player player3 = Bukkit.getPlayer(payload.get("player").getAsString());
                            rank.getData().setSuffix(payload.get("suffix").getAsString());
                            if (player3 != null) {
                                player3.sendMessage(ChatColor.GREEN + "Rank named '" + rank.getData().getName() + "' suffix successfully changed.");
                            }
                        }
                    }
                }
            }
        };
    }
    
    public JedisPubSub getJedisPubSub() {
        return this.jedisPubSub;
    }
    
    public Jedis getJedis() {
        return this.jedis;
    }
}
