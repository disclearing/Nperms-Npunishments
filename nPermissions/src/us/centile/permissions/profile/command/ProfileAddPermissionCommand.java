package us.centile.permissions.profile.command;

import org.bukkit.*;
import us.centile.permissions.profile.*;
import us.centile.permissions.jedis.*;
import com.google.gson.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.util.*;
import us.centile.permissions.util.command.*;
import us.centile.permissions.util.command.Command;

public class ProfileAddPermissionCommand extends BaseCommand
{
    @Command(name = "addpermission", aliases = { "addperm" }, permission = "permissions.player.addpermission", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final CommandSender sender = command.getSender();
        final String[] args = command.getArgs();
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "/addpermission <player> <permission>");
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
        final String permission = args[1].toLowerCase();
        if (profile.getPermissions().contains(permission)) {
            sender.sendMessage(ChatColor.RED + "Player named '" + name + "' already has permission node '" + permission + "'.");
            return;
        }
        profile.getPermissions().add(permission);
        profile.setupAtatchment();
        profile.save();
        if (player == null) {
            final JsonObject object = new JsonObject();
            object.addProperty("action", JedisSubscriberAction.ADD_PLAYER_PERMISSION.name());
            final JsonObject payload = new JsonObject();
            payload.addProperty("player", sender.getName());
            payload.addProperty("uuid", uuid.toString());
            payload.addProperty("permission", permission);
            object.add("payload", payload);
            this.main.getPublisher().write(object.toString());
        }
        sender.sendMessage(ChatColor.GREEN + "Permission '" + permission + "' successfully given to player named '" + name + "'.");
    }
}
