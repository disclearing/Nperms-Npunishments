package us.centile.permissions.rank;

import org.bukkit.*;

public class RankData
{
    private String name;
    private String prefix;
    private String suffix;
    private boolean defaultRank;
    
    public RankData(final String name, final String prefix, final String suffix, final boolean defaultRank) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.defaultRank = defaultRank;
    }
    
    public RankData(final String name) {
        this(name, "&f", "&f", false);
    }
    
    public String getColorPrefix() {
        if (this.prefix.isEmpty()) {
            return "";
        }
        char code = 'f';
        char magic = 'f';
        int count = 0;
        for (final String string : this.prefix.split("&")) {
            if (!string.isEmpty() && ChatColor.getByChar(string.toCharArray()[0]) != null) {
                if (count == 0) {
                    code = string.toCharArray()[0];
                }
                else {
                    magic = string.toCharArray()[0];
                }
                ++count;
            }
        }
        final ChatColor color = ChatColor.getByChar(code);
        final ChatColor magicColor = ChatColor.getByChar(magic);
        return (count == 1) ? color.toString() : (color.toString() + magicColor.toString());
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
    
    public String getSuffix() {
        return this.suffix;
    }
    
    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }
    
    public boolean isDefaultRank() {
        return this.defaultRank;
    }
    
    public void setDefaultRank(final boolean defaultRank) {
        this.defaultRank = defaultRank;
    }
}
