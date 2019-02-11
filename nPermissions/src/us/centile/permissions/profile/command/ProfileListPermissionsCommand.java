package us.centile.permissions.profile.command;

import org.bukkit.*;
import us.centile.permissions.profile.*;
import us.centile.permissions.rank.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.util.*;
import us.centile.permissions.util.command.*;
import us.centile.permissions.util.command.Command;

public class ProfileListPermissionsCommand extends BaseCommand
{
    @Command(name = "listpermissions", aliases = { "listperms" }, permission = "permissions.player.listpermissions", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final CommandSender sender = command.getSender();
        final String[] args = command.getArgs();
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "/listpermissions <player>");
            return;
        }
        final Player player = Bukkit.getPlayer(args[0]);
        UUID uuid;
        String name;
        if (player != null) {
            uuid = player.getUniqueId();
            name = player.getName();
        }
        else {
            try {
                final Map.Entry<UUID, String> recipient = this.main.getRankHandler().getExternalUuid(args[0]);
                uuid = recipient.getKey();
                name = recipient.getValue();
            }
            catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed to find player.");
                return;
            }
        }
        final Profile profile = Profile.getByUuid(uuid);
        if (profile == null) {
            sender.sendMessage(ChatColor.RED + "Failed to find player.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Listing permissions of " + ChatColor.translateAlternateColorCodes('&', profile.getActiveGrant().getRank().getData().getPrefix() + name + profile.getActiveGrant().getRank().getData().getSuffix()) + ChatColor.GREEN + ":");
        if (!profile.getPermissions().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Base permissions:");
            for (final String permission : profile.getPermissions()) {
                sender.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.GRAY + permission);
            }
        }
        final Rank rank = profile.getActiveGrant().getRank();
        if (rank != null) {
            int count = 0;
            for (final String permission2 : rank.getPermissions()) {
                if (count == 0) {
                    sender.sendMessage(ChatColor.GREEN + "Permissions inherited from " + rank.getData().getColorPrefix() + rank.getData().getName() + ChatColor.GREEN + ":");
                }
                ++count;
                sender.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.GRAY + permission2);
                if (count > 0 && count == rank.getPermissions().size()) {
                    count = 0;
                    for (final UUID otherId : rank.getInheritance()) {
                        if (count == 0) {
                            sender.sendMessage(ChatColor.GREEN + "Also inherits permissions from the following ranks:");
                        }
                        ++count;
                        final Rank other = Rank.getByUuid(otherId);
                        if (other != null) {
                            sender.sendMessage(ChatColor.DARK_GRAY + " * " + other.getData().getColorPrefix() + other.getData().getName());
                        }
                    }
                }
            }
        }
    }
}
