package us.centile.permissions.rank.command;

import org.bukkit.*;
import us.centile.permissions.rank.*;
import us.centile.permissions.jedis.*;
import com.google.gson.*;
import org.bukkit.command.*;
import us.centile.permissions.util.command.*;
import us.centile.permissions.util.command.Command;

public class RankDeletePermissionCommand extends BaseCommand
{
    @Command(name = "rank.deletepermission", aliases = { "rank.delperm", "rank.deleteperm", "rank.delpermission" }, permission = "permissions.rank.deletepermission", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final CommandSender sender = command.getSender();
        final String[] args = command.getArgs();
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "/rank deletepermission <rank> <permission>");
            return;
        }
        final Rank rank = Rank.getByName(args[0]);
        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank named '" + args[0] + "' not found.");
            return;
        }
        final String permission = args[1].toLowerCase();
        if (!rank.getPermissions().contains(permission)) {
            sender.sendMessage(ChatColor.RED + "Rank named '" + rank.getData().getName() + "' doesn't have permission node '" + permission + "'.");
            return;
        }
        final JsonObject object = new JsonObject();
        object.addProperty("action", JedisSubscriberAction.DELETE_RANK_PERMISSION.name());
        final JsonObject payload = new JsonObject();
        payload.addProperty("player", sender.getName());
        payload.addProperty("rank", rank.getUuid().toString());
        payload.addProperty("permission", permission);
        object.add("payload", payload);
        this.main.getPublisher().write(object.toString());
    }
}
