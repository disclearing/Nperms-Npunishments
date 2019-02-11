package us.centile.permissions.grant.procedure;

import us.centile.permissions.rank.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import us.centile.permissions.util.*;
import org.bukkit.entity.*;
import java.util.*;

public class GrantProcedure
{
    private static Set<GrantProcedure> procedures;
    private final GrantRecipient recipient;
    private final UUID issuer;
    private final GrantProcedureData data;
    
    public GrantProcedure(final GrantRecipient recipient, final UUID issuer, final GrantProcedureData data) {
        this.recipient = recipient;
        this.issuer = issuer;
        this.data = data;
        GrantProcedure.procedures.add(this);
    }
    
    public Inventory getInventory() {
        final int size = (int)Math.ceil(Rank.getRanks().size() / 9.0);
        final Inventory inventory = Bukkit.createInventory((InventoryHolder)null, (size == 0) ? 9 : (size * 9), ChatColor.YELLOW + "" + ChatColor.BOLD + "Choose a Rank");
        for (final Rank rank : Rank.getRanks()) {
            if (rank.getData().isDefaultRank()) {
                continue;
            }
            ChatColor color;
            if (rank.getData().getPrefix().isEmpty()) {
                color = ChatColor.WHITE;
            }
            else {
                char code = 'f';
                for (final String string : rank.getData().getPrefix().split("&")) {
                    if (!string.isEmpty() && ChatColor.getByChar(string.toCharArray()[0]) != null) {
                        code = string.toCharArray()[0];
                    }
                }
                color = ChatColor.getByChar(code);
            }
            inventory.addItem(new ItemStack[] { new ItemBuilder(Material.WOOL).durability(WoolUtil.convertChatColorToWoolData(color)).name(color + rank.getData().getName()).lore(Arrays.asList("&7&m------------------------------", "&9Click to grant &f" + this.getRecipient().getName() + "&9 the " + color + rank.getData().getName() + "&9 rank.", "&7&m------------------------------")).build() });
        }
        return inventory;
    }
    
    public static GrantProcedure getByPlayer(final Player player) {
        for (final GrantProcedure grantProcedure : GrantProcedure.procedures) {
            if (grantProcedure.getIssuer() != null && grantProcedure.getIssuer().equals(player.getUniqueId())) {
                return grantProcedure;
            }
        }
        return null;
    }
    
    public static Set<GrantProcedure> getProcedures() {
        return GrantProcedure.procedures;
    }
    
    public GrantRecipient getRecipient() {
        return this.recipient;
    }
    
    public UUID getIssuer() {
        return this.issuer;
    }
    
    public GrantProcedureData getData() {
        return this.data;
    }
    
    static {
        GrantProcedure.procedures = new HashSet<GrantProcedure>();
    }
}
