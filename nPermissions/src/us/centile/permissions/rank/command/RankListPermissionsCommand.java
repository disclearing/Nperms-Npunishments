package us.centile.permissions.rank.command;

import org.bukkit.*;
import us.centile.permissions.rank.*;
import org.bukkit.command.*;
import java.util.*;
import us.centile.permissions.util.command.*;
import us.centile.permissions.util.command.Command;

public class RankListPermissionsCommand extends BaseCommand
{
    @Command(name = "rank.listpermissions", aliases = { "rank.listperms" }, permission = "permissions.rank.listpermissions", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final CommandSender sender = command.getSender();
        final String[] args = command.getArgs();
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "/rank listpermissions <rank>");
            return;
        }
        final Rank rank = Rank.getByName(args[0]);
        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank named '" + args[0] + "' not found.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Listing permissions of " + rank.getData().getColorPrefix() + rank.getData().getName() + ChatColor.GREEN + ":");
        sender.sendMessage(ChatColor.GREEN + "Base permissions:");
        for (final String permission : rank.getPermissions()) {
            sender.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.GRAY + permission);
        }
        for (final UUID inheritance : rank.getInheritance()) {
            final Rank other = Rank.getByUuid(inheritance);
            if (other != null) {
                int count = 0;
                for (final String permission2 : other.getPermissions()) {
                    if (!rank.getPermissions().contains(permission2)) {
                        if (count == 0) {
                            sender.sendMessage(ChatColor.GREEN + "Permissions inherited from " + other.getData().getColorPrefix() + other.getData().getName() + ChatColor.GREEN + ":");
                        }
                        ++count;
                        sender.sendMessage(ChatColor.DARK_GRAY + " * " + ChatColor.GRAY + permission2);
                    }
                }
            }
        }
    }
}
