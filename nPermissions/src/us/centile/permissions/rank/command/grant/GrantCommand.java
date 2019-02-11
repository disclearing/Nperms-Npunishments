package us.centile.permissions.rank.command.grant;

import org.bukkit.*;
import us.centile.permissions.rank.*;
import us.centile.permissions.util.*;
import us.centile.permissions.profile.*;
import us.centile.permissions.grant.*;
import us.centile.permissions.jedis.*;
import com.google.gson.*;
import us.centile.permissions.grant.procedure.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.util.*;
import us.centile.permissions.util.command.*;
import us.centile.permissions.util.command.Command;

public class GrantCommand extends BaseCommand
{
    @Command(name = "grant", permission = "permissions.rank.grant", inGameOnly = false)
    @Override
    public void onCommand(final CommandArgs command) {
        final String[] args = command.getArgs();
        if (command.getSender() instanceof ConsoleCommandSender) {
            final CommandSender sender = command.getSender();
            if (args.length < 4) {
                command.getSender().sendMessage(ChatColor.RED + "/grant <player> <rank> <duration> <reason>");
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
            final Rank rank = Rank.getByName(args[1]);
            if (rank == null) {
                sender.sendMessage(ChatColor.RED + "Failed to find rank.");
                return;
            }
            long duration;
            if (args[2].equalsIgnoreCase("perm") || args[2].equalsIgnoreCase("permanent")) {
                duration = 2147483647L;
            }
            else {
                try {
                    duration = System.currentTimeMillis() - DateUtil.parseDateDiff(args[2], false);
                }
                catch (Exception e2) {
                    sender.sendMessage(ChatColor.RED + "Invalid duration.");
                    return;
                }
            }
            final StringBuilder sb = new StringBuilder();
            for (int i = 3; i < args.length; ++i) {
                sb.append(command.getArgs()[i]).append(" ");
            }
            final String reason = sb.toString().trim();
            final Profile profile = Profile.getByUuid(uuid);
            if (profile != null) {
                if (profile.getActiveGrant().getRank() == rank) {
                    sender.sendMessage(ChatColor.RED + "User has that grant already.");
                    return;
                }
                if (profile.getName() == null || !profile.getName().equals(name)) {
                    profile.setName(name);
                    profile.save();
                }
                for (final Grant grant : profile.getGrants()) {
                    if (!grant.getRank().getData().isDefaultRank() && !grant.isExpired()) {
                        grant.setActive(false);
                    }
                }
                final Grant newGrant = new Grant(null, rank, System.currentTimeMillis(), duration, reason, true);
                profile.getGrants().add(newGrant);
                profile.setupAtatchment();
                profile.save();
                if (player == null) {
                    Profile.getProfiles().remove(profile);
                    final JsonObject object = new JsonObject();
                    object.addProperty("action", JedisSubscriberAction.ADD_GRANT.name());
                    final JsonObject payload = new JsonObject();
                    payload.addProperty("uuid", profile.getUuid().toString());
                    final JsonObject grant2 = new JsonObject();
                    grant2.addProperty("rank", rank.getUuid().toString());
                    grant2.addProperty("datedAdded", System.currentTimeMillis());
                    grant2.addProperty("duration", duration);
                    grant2.addProperty("reason", reason);
                    payload.add("grant", grant2);
                    object.add("payload", payload);
                    this.main.getPublisher().write(object.toString());
                }
                else {
                    player.sendMessage(ChatColor.GREEN + "Your rank has been set to " + newGrant.getRank().getData().getColorPrefix() + newGrant.getRank().getData().getName() + ChatColor.GREEN + ".");
                }
            }
            sender.sendMessage(ChatColor.GREEN + "Grant successfully created.");
        }
        else {
            final Player sender2 = command.getPlayer();
            if (args.length == 0) {
                command.getSender().sendMessage(ChatColor.RED + "/grant <player>");
                return;
            }
            final GrantProcedure procedure = GrantProcedure.getByPlayer(sender2);
            if (procedure != null) {
                if (args[0].equalsIgnoreCase("cancel")) {
                    sender2.sendMessage(" ");
                    sender2.sendMessage(ChatColor.RED + "Grant procedure cancelled.");
                    sender2.sendMessage(" ");
                    GrantProcedure.getProcedures().remove(procedure);
                }
                else if (args[0].equalsIgnoreCase("confirm")) {
                    GrantProcedure.getProcedures().remove(procedure);
                    procedure.getData().setCreated(System.currentTimeMillis());
                    sender2.sendMessage(" ");
                    sender2.sendMessage(ChatColor.YELLOW + "Grant successfully created.");
                    sender2.sendMessage(" ");
                    final Profile profile2 = Profile.getByUuid(procedure.getRecipient().getUuid());
                    if (profile2 != null) {
                        final Player player2 = Bukkit.getPlayer(profile2.getUuid());
                        if (profile2.getName() == null || !profile2.getName().equals(procedure.getRecipient().getName())) {
                            profile2.setName(procedure.getRecipient().getName());
                        }
                        for (final Grant grant3 : profile2.getGrants()) {
                            if (!grant3.getRank().getData().isDefaultRank() && !grant3.isExpired()) {
                                grant3.setActive(false);
                            }
                        }
                        final Grant newGrant2 = new Grant(sender2.getUniqueId(), procedure.getData().getRank(), procedure.getData().getCreated(), procedure.getData().getDuration(), procedure.getData().getReason(), true);
                        profile2.getGrants().add(newGrant2);
                        profile2.setupAtatchment();
                        profile2.save();
                        if (player2 == null) {
                            Profile.getProfiles().remove(profile2);
                            final JsonObject object2 = new JsonObject();
                            object2.addProperty("action", JedisSubscriberAction.ADD_GRANT.name());
                            final JsonObject payload2 = new JsonObject();
                            payload2.addProperty("uuid", profile2.getUuid().toString());
                            final JsonObject grant4 = new JsonObject();
                            grant4.addProperty("issuer", sender2.getUniqueId().toString());
                            grant4.addProperty("rank", procedure.getData().getRank().getUuid().toString());
                            grant4.addProperty("datedAdded", procedure.getData().getCreated());
                            grant4.addProperty("duration", procedure.getData().getDuration());
                            grant4.addProperty("reason", procedure.getData().getReason());
                            payload2.add("grant", grant4);
                            object2.add("payload", payload2);
                            this.main.getPublisher().write(object2.toString());
                        }
                        else {
                            player2.sendMessage(ChatColor.GREEN + "Your rank has been set to " + newGrant2.getRank().getData().getColorPrefix() + newGrant2.getRank().getData().getName() + ChatColor.GREEN + ".");
                        }
                    }
                }
                else {
                    sender2.sendMessage(" ");
                    sender2.sendMessage(ChatColor.RED + "You're already in a grant procedure.");
                    sender2.sendMessage(ChatColor.RED + "Please enter a valid duration or type 'cancel' to cancel.");
                    sender2.sendMessage(" ");
                }
                return;
            }
            if (args[0].equalsIgnoreCase("confirm") || args[0].equalsIgnoreCase("cancel")) {
                return;
            }
            final Player player3 = Bukkit.getPlayer(args[0]);
            UUID uuid2;
            String name2;
            if (player3 != null) {
                uuid2 = player3.getUniqueId();
                name2 = player3.getName();
            }
            else {
                try {
                    final Map.Entry<UUID, String> recipient2 = this.main.getRankHandler().getExternalUuid(args[0]);
                    uuid2 = recipient2.getKey();
                    name2 = recipient2.getValue();
                }
                catch (Exception e3) {
                    command.getSender().sendMessage(ChatColor.RED + "Failed to find player.");
                    return;
                }
            }
            final Profile profile3 = Profile.getByUuid(uuid2);
            if (profile3.getName() == null || !profile3.getName().equals(name2)) {
                profile3.setName(name2);
                profile3.save();
            }
            sender2.openInventory(new GrantProcedure(new GrantRecipient(uuid2, name2), sender2.getUniqueId(), new GrantProcedureData()).getInventory());
        }
    }
}
