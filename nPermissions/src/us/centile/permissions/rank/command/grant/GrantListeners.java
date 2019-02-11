package us.centile.permissions.rank.command.grant;

import us.centile.permissions.*;
import org.bukkit.event.player.*;
import us.centile.permissions.profile.*;
import org.bukkit.entity.*;
import us.centile.permissions.grant.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.*;
import us.centile.permissions.jedis.*;
import com.google.gson.*;
import us.centile.permissions.rank.*;
import org.bukkit.inventory.*;

public class GrantListeners implements Listener
{
    private static nPermissions main;
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatEvent(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final Profile profile = Profile.getByUuid(player.getUniqueId());
        event.setFormat("%1$s: %2$s");
        player.setDisplayName(player.getName());
        if (profile != null) {
            final Grant grant = profile.getActiveGrant();
            if (!player.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', grant.getRank().getData().getPrefix() + player.getName() + grant.getRank().getData().getSuffix()))) {
                player.setDisplayName(ChatColor.translateAlternateColorCodes('&', grant.getRank().getData().getPrefix() + player.getName() + grant.getRank().getData().getSuffix()));
            }
        }
    }
    
    @EventHandler
    public void onInventoryClickEvent(final InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        final ItemStack itemStack = event.getCurrentItem();
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            final String title = event.getInventory().getTitle();
            final String displayName = itemStack.getItemMeta().getDisplayName();
            if (title.contains(ChatColor.RED + "Grants") && player.hasPermission("permissions.rank.view")) {
                event.setCancelled(true);
                final int page = Integer.parseInt(title.substring(title.lastIndexOf("/") - 1, title.lastIndexOf("/")));
                final int total = Integer.parseInt(title.substring(title.lastIndexOf("/") + 1, title.lastIndexOf("/") + 2));
                final String playerName = ChatColor.stripColor(event.getInventory().getItem(4).getItemMeta().getLore().get(0).substring(event.getInventory().getItem(4).getItemMeta().getLore().get(0).indexOf(" "), event.getInventory().getItem(4).getItemMeta().getLore().get(0).length())).trim();
                final Profile profile = Profile.getExternalByName(playerName);
                if (event.getRawSlot() == 9 && profile != null && itemStack.getDurability() == 5) {
                    final Grant activeGrant = profile.getActiveGrant();
                    activeGrant.setActive(false);
                    profile.save();
                    final Player profilePlayer = Bukkit.getPlayer(profile.getUuid());
                    if (profilePlayer == null) {
                        final JsonObject object = new JsonObject();
                        object.addProperty("action", JedisSubscriberAction.DELETE_GRANT.name());
                        final JsonObject payload = new JsonObject();
                        payload.addProperty("uuid", profile.getUuid().toString());
                        object.add("payload", payload);
                        GrantListeners.main.getPublisher().write(object.toString());
                    }
                    else {
                        final Rank rank = Rank.getDefaultRank();
                        if (rank != null) {
                            profilePlayer.sendMessage(ChatColor.GREEN + "Your rank has been set to " + rank.getData().getColorPrefix() + rank.getData().getName() + ChatColor.GREEN + ".");
                            profile.setupAtatchment();
                        }
                    }
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "Grant successfully disabled.");
                    return;
                }
                if (displayName.contains("Next Page")) {
                    if (page + 1 > total) {
                        player.sendMessage(ChatColor.RED + "There are no more pages.");
                        return;
                    }
                    player.openInventory(GrantListeners.main.getRankHandler().getGrantsInventory(profile, playerName, page + 1));
                }
                else if (displayName.contains("Previous Page")) {
                    if (page == 1) {
                        player.sendMessage(ChatColor.RED + "You're on the first page.");
                        return;
                    }
                    player.openInventory(GrantListeners.main.getRankHandler().getGrantsInventory(profile, playerName, page - 1));
                }
            }
        }
    }
    
    static {
        GrantListeners.main = nPermissions.getInstance();
    }
}
