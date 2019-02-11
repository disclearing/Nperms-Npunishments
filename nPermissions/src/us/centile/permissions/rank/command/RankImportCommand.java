package us.centile.permissions.rank.command;

import org.bukkit.*;
import mkremins.fanciful.*;
import us.centile.permissions.rank.*;
import org.bukkit.scheduler.*;
import us.centile.permissions.jedis.*;
import com.google.gson.*;
import org.bukkit.plugin.*;
import us.centile.permissions.util.file.*;
import java.util.*;
import us.centile.permissions.util.command.*;

public class RankImportCommand extends BaseCommand
{
    @Command(name = "rank.import", aliases = { "importranks" }, permission = "permissions.rank.import", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final String[] args = command.getArgs();
        if (args.length == 0) {
            new FancyMessage(ChatColor.YELLOW + "Please confirm this action: ").then(ChatColor.GREEN + "" + ChatColor.BOLD + "CONFIRM").tooltip(ChatColor.GREEN + "Click to confirm").command("/rank import confirm").send(command.getSender());
        }
        else if (args[0].equalsIgnoreCase("confirm")) {
            Rank.getRanks().clear();
            this.main.getPermissionsDatabase().getProfiles().drop();
            this.main.getPermissionsDatabase().getRanks().drop();
            final ConfigFile config = this.main.getRanksFile();
            for (final String key : config.getConfiguration().getKeys(false)) {
                final String name = config.getString(key + ".NAME");
                final String prefix = config.getString(key + ".PREFIX", "&f", false);
                final String suffix = config.getString(key + ".SUFFIX", "&f", false);
                final boolean defaultRank = config.getBoolean(key + ".DEFAULT");
                final List<String> permissions = config.getStringListOrDefault(key + ".PERMISSIONS", new ArrayList<String>());
                final RankData data = new RankData(name);
                data.setPrefix(prefix);
                data.setSuffix(suffix);
                data.setDefaultRank(defaultRank);
                new Rank(UUID.randomUUID(), new ArrayList<UUID>(), permissions, data);
            }
            for (final String key : config.getConfiguration().getKeys(false)) {
                final Rank rank = Rank.getByName(config.getString(key + ".NAME"));
                if (rank != null) {
                    for (final String name2 : config.getStringListOrDefault(key + ".INHERITANCE", new ArrayList<String>())) {
                        final Rank other = Rank.getByName(config.getString(name2 + ".NAME"));
                        if (other != null) {
                            rank.getInheritance().add(other.getUuid());
                        }
                    }
                }
            }
            this.main.getRankHandler().save();
            command.getSender().sendMessage(ChatColor.GREEN + "Processing request..");
            new BukkitRunnable() {
                public void run() {
                    final JsonObject object = new JsonObject();
                    object.addProperty("action", JedisSubscriberAction.IMPORT_RANKS.name());
                    final JsonObject payload = new JsonObject();
                    payload.addProperty("player", command.getSender().getName());
                    object.add("payload", payload);
                    RankImportCommand.this.main.getPublisher().write(object.toString());
                }
            }.runTaskLater((Plugin)this.main, 40L);
        }
    }
}
