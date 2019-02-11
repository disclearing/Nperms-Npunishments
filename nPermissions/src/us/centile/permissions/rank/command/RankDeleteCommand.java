package us.centile.permissions.rank.command;

import org.bukkit.*;
import us.centile.permissions.rank.*;
import us.centile.permissions.jedis.*;
import com.google.gson.*;
import us.centile.permissions.util.command.*;

public class RankDeleteCommand extends BaseCommand
{
    @Command(name = "rank.delete", aliases = { "deleterank" }, permission = "permissions.rank.delete", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final String[] args = command.getArgs();
        if (args.length == 0) {
            command.getSender().sendMessage(ChatColor.RED + "/rank delete <rank>");
            return;
        }
        final Rank rank = Rank.getByName(args[0]);
        if (rank == null) {
            command.getSender().sendMessage(ChatColor.RED + "Rank named '" + args[0] + "' not found.");
            return;
        }
        final JsonObject object = new JsonObject();
        object.addProperty("action", JedisSubscriberAction.DELETE_RANK.name());
        final JsonObject payload = new JsonObject();
        payload.addProperty("rank", rank.getData().getName());
        payload.addProperty("player", command.getSender().getName());
        object.add("payload", payload);
        this.main.getPublisher().write(object.toString());
    }
}
