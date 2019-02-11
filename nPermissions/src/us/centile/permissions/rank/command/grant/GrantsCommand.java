package us.centile.permissions.rank.command.grant;

import org.bukkit.*;
import us.centile.permissions.profile.*;
import org.bukkit.entity.*;
import java.util.*;
import us.centile.permissions.util.command.*;

public class GrantsCommand extends BaseCommand
{
    @Command(name = "rankhistory", aliases = { "rankhistory.view" }, permission = "permissions.rank.view")
    @Override
    public void onCommand(final CommandArgs command) {
        final String[] args = command.getArgs();
        final Player sender = command.getPlayer();
        if (args.length == 0) {
            command.getSender().sendMessage(ChatColor.RED + "/rankhistory <player>");
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
                command.getSender().sendMessage(ChatColor.RED + "Failed to find player.");
                return;
            }
        }
        final Profile profile = Profile.getByUuid(uuid);
        if (profile != null) {
            if (profile.getName() == null || !profile.getName().equals(name)) {
                profile.setName(name);
                profile.save();
            }
            sender.sendMessage(ChatColor.YELLOW + "Displaying the grants of " + name + ".");
            sender.openInventory(this.main.getRankHandler().getGrantsInventory(profile, name, 1));
        }
    }
}
