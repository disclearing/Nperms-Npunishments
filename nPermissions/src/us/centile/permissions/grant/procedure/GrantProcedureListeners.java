package us.centile.permissions.grant.procedure;

import org.bukkit.entity.*;
import org.bukkit.*;
import us.centile.permissions.rank.*;
import org.bukkit.inventory.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import us.centile.permissions.util.*;
import mkremins.fanciful.*;
import org.bukkit.event.inventory.*;

public class GrantProcedureListeners implements Listener
{
    @EventHandler
    public void onInventoryClickEvent(final InventoryClickEvent event) {
        final Player player = (Player)event.getWhoClicked();
        final Inventory inventory = event.getInventory();
        final ItemStack itemStack = event.getCurrentItem();
        final GrantProcedure procedure = GrantProcedure.getByPlayer(player);
        if (procedure != null && itemStack != null && itemStack.getType() != Material.AIR) {
            event.setCancelled(true);
            if (procedure.getData().getStage() == GrantProcedureStage.RANK && inventory.getTitle().equals(procedure.getInventory().getTitle()) && itemStack.getItemMeta().hasDisplayName()) {
                final Rank rank = Rank.getByName(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()));
                if (rank != null) {
                    procedure.getData().setRank(rank);
                    procedure.getData().setStage(GrantProcedureStage.DURATION);
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.YELLOW + "Please enter a duration in chat (use 'permanent' for permanent ranks).");
                    player.sendMessage(" ");
                }
                player.closeInventory();
            }
        }
    }
    
    @EventHandler
    public void onAsyncPlayerChatEvent(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final GrantProcedure procedure = GrantProcedure.getByPlayer(player);
        if (procedure != null && (procedure.getData().getStage() == GrantProcedureStage.DURATION || procedure.getData().getStage() == GrantProcedureStage.REASON || procedure.getData().getStage() == GrantProcedureStage.CONFIRMATION)) {
            event.setCancelled(true);
            try {
                if (event.getMessage().equalsIgnoreCase("cancel")) {
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.RED + "Grant procedure cancelled.");
                    player.sendMessage(" ");
                    GrantProcedure.getProcedures().remove(procedure);
                    return;
                }
                if (procedure.getData().getStage() == GrantProcedureStage.DURATION) {
                    if (event.getMessage().equalsIgnoreCase("permanent") || event.getMessage().equalsIgnoreCase("perm")) {
                        procedure.getData().setDuration(2147483647L);
                    }
                    else {
                        procedure.getData().setDuration(System.currentTimeMillis() - DateUtil.parseDateDiff(event.getMessage(), false));
                    }
                    procedure.getData().setStage(GrantProcedureStage.REASON);
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.YELLOW + "Duration successfully recorded.");
                    player.sendMessage(ChatColor.YELLOW + "Please enter a reason in chat.");
                    player.sendMessage(" ");
                }
                else {
                    procedure.getData().setReason(event.getMessage());
                    procedure.getData().setStage(GrantProcedureStage.CONFIRMATION);
                    player.sendMessage(" ");
                    player.sendMessage(ChatColor.YELLOW + "Reason successfully recorded.");
                    new FancyMessage(ChatColor.YELLOW + "Would you like to proceed with this grant?: ").then(ChatColor.GREEN + "" + ChatColor.BOLD + "YES").tooltip(ChatColor.GREEN + "Confirm this grant").command("/grant confirm").then(ChatColor.RED + " " + ChatColor.BOLD + "NO").command("/grant cancel").tooltip(ChatColor.RED + "Cancel this grant").send(player);
                    player.sendMessage(" ");
                }
            }
            catch (Exception e) {
                player.sendMessage(" ");
                player.sendMessage(ChatColor.RED + "Please enter a valid duration or type 'cancel' to cancel.");
                player.sendMessage(" ");
            }
        }
    }
    
    @EventHandler
    public void onInventoryCloseEvent(final InventoryCloseEvent event) {
        final Player player = (Player)event.getPlayer();
        final Inventory inventory = event.getInventory();
        final GrantProcedure procedure = GrantProcedure.getByPlayer(player);
        if (procedure != null) {
            final GrantProcedureStage stage = procedure.getData().getStage();
            if (stage == GrantProcedureStage.RANK && inventory.getTitle().equals(procedure.getInventory().getTitle())) {
                player.sendMessage(" ");
                player.sendMessage(ChatColor.RED + "Grant procedure cancelled.");
                player.sendMessage(" ");
                GrantProcedure.getProcedures().remove(procedure);
            }
        }
    }
}
