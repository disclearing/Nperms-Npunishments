package us.centile.permissions.rank.command;

import org.bukkit.*;
import us.centile.permissions.rank.*;
import us.centile.permissions.jedis.*;
import com.google.gson.*;
import us.centile.permissions.util.command.*;

public class RankSuffixCommand extends BaseCommand
{
    @Command(name = "rank.suffix", aliases = { "rank.setsuffix", "setsuffix" }, permission = "permissions.rank.suffix", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final String[] args = command.getArgs();
        if (args.length <= 1) {
            command.getSender().sendMessage(ChatColor.RED + "/rank setsuffix <rank> <suffix>");
            return;
        }
        final Rank rank = Rank.getByName(args[0]);
        if (rank == null) {
            command.getSender().sendMessage(ChatColor.RED + "Rank named '" + args[0] + "' not found.");
            return;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; ++i) {
            sb.append(command.getArgs()[i]).append(" ");
        }
        final String suffix = sb.toString().trim().replace("\"", "");
        final JsonObject object = new JsonObject();
        object.addProperty("action", JedisSubscriberAction.SET_RANK_SUFFIX.name());
        final JsonObject payload = new JsonObject();
        payload.addProperty("rank", rank.getData().getName());
        payload.addProperty("player", command.getSender().getName());
        payload.addProperty("suffix", suffix);
        object.add("payload", payload);
        this.main.getPublisher().write(object.toString());
    }
}
