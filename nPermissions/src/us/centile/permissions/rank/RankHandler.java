package us.centile.permissions.rank;

import us.centile.permissions.*;
import java.text.*;
import org.bukkit.scheduler.*;
import us.centile.permissions.profile.*;
import us.centile.permissions.grant.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import us.centile.permissions.util.*;
import com.mongodb.*;
import org.bson.*;
import com.mongodb.client.model.*;
import java.util.*;
import org.json.simple.*;
import java.net.*;
import java.io.*;
import org.json.simple.parser.*;
import org.json.simple.parser.ParseException;

public class RankHandler
{
    private final nPermissions main;
    private static final SimpleDateFormat DATE_FORMAT;
    
    public RankHandler(final nPermissions main) {
        this.main = main;
        this.load();
        new BukkitRunnable() {
            public void run() {
                for (final Profile profile : Profile.getProfiles()) {
                    for (final Grant grant : profile.getGrants()) {
                        if (grant.isExpired() && grant.isActive()) {
                            grant.setActive(false);
                            profile.setupAtatchment();
                            final Player player = Bukkit.getPlayer(profile.getUuid());
                            if (player == null) {
                                continue;
                            }
                            player.sendMessage(ChatColor.GREEN + "Your rank has been set to " + profile.getActiveGrant().getRank().getData().getColorPrefix() + profile.getActiveGrant().getRank().getData().getName() + ChatColor.GREEN + ".");
                        }
                    }
                }
            }
        }.runTaskTimer((Plugin)main, 20L, 20L);
    }
    
    public Inventory getGrantsInventory(final Profile profile, final String name, final int page) {
        int total = (int)Math.ceil(profile.getGrants().size() / 9.0);
        if (total == 0) {
            total = 1;
        }
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, 18, ChatColor.RED + "Grants - " + page + "/" + total);
        inventory.setItem(0, new ItemBuilder(Material.CARPET).durability(7).name(ChatColor.RED + "Previous Page").build());
        inventory.setItem(8, new ItemBuilder(Material.CARPET).durability(7).name(ChatColor.RED + "Next Page").build());
        inventory.setItem(4, new ItemBuilder(Material.PAPER).name(ChatColor.RED + "Page " + page + "/" + ((total == 0) ? 1 : total)).lore(Arrays.asList(ChatColor.YELLOW + "Player: " + ChatColor.RED + name)).build());
        final ArrayList<Grant> toLoop = new ArrayList<Grant>(profile.getGrants());
        Collections.reverse(toLoop);
        final Iterator<Grant> iterator = toLoop.iterator();
        while (iterator.hasNext()) {
            final Grant grant = iterator.next();
            if (grant.getRank().getData().isDefaultRank()) {
                iterator.remove();
            }
        }
        for (final Grant grant2 : toLoop) {
            if (toLoop.indexOf(grant2) >= page * 9 - 9 && toLoop.indexOf(grant2) < page * 9) {
                String end = "";
                if (grant2.getDuration() != 2147483647L) {
                    if (grant2.isExpired()) {
                        end = "Expired";
                    }
                    else {
                        final Calendar from = Calendar.getInstance();
                        final Calendar to = Calendar.getInstance();
                        from.setTime(new Date(System.currentTimeMillis()));
                        to.setTime(new Date(grant2.getDateAdded() + grant2.getDuration()));
                        end = DateUtil.formatDateDiff(from, to);
                    }
                }
                String issuerName;
                if (grant2.getIssuer() == null) {
                    issuerName = "Console";
                }
                else {
                    issuerName = Profile.getByUuid(grant2.getIssuer()).getName();
                }
                inventory.setItem(9 + toLoop.indexOf(grant2) % 9, new ItemBuilder(Material.WOOL).durability((grant2.isActive() && !grant2.isExpired()) ? 5 : 14).name(ChatColor.YELLOW + RankHandler.DATE_FORMAT.format(new Date(grant2.getDateAdded()))).lore(Arrays.asList("&7&m------------------------------", "&eBy: &c" + issuerName, "&eReason: &c" + grant2.getReason(), "&eRank: &c" + grant2.getRank().getData().getName(), "&7&m------------------------------", (grant2.getDuration() == 2147483647L) ? "&eThis is a permanent grant." : ("&eExpires in: &c" + end), "&7&m------------------------------")).build());
            }
        }
        return inventory;
    }
    
    public void load() {
        final Block<Document> printDocumentBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                final RankData rankData = new RankData(document.getString("name"));
                rankData.setPrefix(document.getString("prefix"));
                rankData.setSuffix(document.getString("suffix"));
                rankData.setDefaultRank(document.getBoolean("default"));
                final Object inheritance = document.get("inheritance");
                final Object permissions = document.get("permissions");
                final List<UUID> inheritanceList = new ArrayList<UUID>();
                for (final String id : inheritance.toString().replace("[", "").replace("]", "").replace(" ", "").split(",")) {
                    if (!id.isEmpty()) {
                        inheritanceList.add(UUID.fromString(id));
                    }
                }
                final List<String> permissionsList = new ArrayList<String>();
                for (final String id2 : permissions.toString().replace("[", "").replace("]", "").replace(" ", "").split(",")) {
                    if (!id2.isEmpty()) {
                        permissionsList.add(id2);
                    }
                }
                new Rank(UUID.fromString(document.getString("uuid")), inheritanceList, permissionsList, rankData);
            }
        };
        this.main.getPermissionsDatabase().getRanks().find().forEach(printDocumentBlock);
    }
    
    public void save() {
        for (final Rank rank : Rank.getRanks()) {
            final Document document = new Document();
            document.put("uuid", (Object)rank.getUuid().toString());
            final List<String> inheritance = new ArrayList<String>();
            for (final UUID uuid : rank.getInheritance()) {
                inheritance.add(uuid.toString());
            }
            document.put("inheritance", (Object)inheritance);
            document.put("permissions", (Object)rank.getPermissions());
            document.put("name", (Object)rank.getData().getName());
            document.put("prefix", (Object)rank.getData().getPrefix());
            document.put("suffix", (Object)rank.getData().getSuffix());
            document.put("default", (Object)rank.getData().isDefaultRank());
            this.main.getPermissionsDatabase().getRanks().replaceOne(Filters.eq("uuid", rank.getUuid().toString()), document, new UpdateOptions().upsert(true));
        }
    }
    
    public Map.Entry<UUID, String> getExternalUuid(String name) throws IOException, ParseException {
        final Document document = (Document)nPermissions.getInstance().getPermissionsDatabase().getProfiles().find(Filters.eq("recentName", name)).first();
        if (document != null && document.containsKey("recentName")) {
            return new AbstractMap.SimpleEntry<UUID, String>(UUID.fromString(document.getString("uuid")), document.getString("recentName"));
        }
        final URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        final URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        final JSONParser parser = new JSONParser();
        final JSONObject obj = (JSONObject)parser.parse(reader.readLine());
        final UUID uuid = UUID.fromString(String.valueOf(obj.get((Object)"id")).replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        name = String.valueOf(obj.get((Object)"name"));
        reader.close();
        return new AbstractMap.SimpleEntry<UUID, String>(uuid, name);
    }
    
    static {
        DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
    }
}
