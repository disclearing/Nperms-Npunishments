package us.centile.permissions;

import org.bukkit.plugin.java.*;
import us.centile.permissions.util.command.*;
import us.centile.permissions.util.file.*;
import us.centile.permissions.rank.*;
import redis.clients.jedis.*;
import us.centile.permissions.jedis.*;
import us.centile.permissions.util.database.*;
import us.centile.permissions.util.*;
import org.bukkit.entity.*;
import us.centile.permissions.grant.*;
import java.util.*;
import org.bukkit.*;
import us.centile.permissions.grant.procedure.*;
import org.bukkit.event.*;
import us.centile.permissions.profile.*;
import org.bukkit.plugin.*;
import us.centile.permissions.rank.command.*;
import us.centile.permissions.profile.command.*;
import us.centile.permissions.rank.command.grant.*;

public class nPermissions extends JavaPlugin
{
    private static nPermissions instance;
    private CommandFramework commandFramework;
    private ConfigFile configFile;
    private ConfigFile ranksFile;
    private RankHandler rankHandler;
    private String address;
    private int port;
    private JedisPool pool;
    private JedisPublisher publisher;
    private JedisSubscriber subscriber;
    private PermissionsDatabase permissionsDatabase;
    
    public void onEnable() {
        this.setupJedis();
        this.registerCommands();
        this.registerListeners();
        nPermissions.instance = this;
        this.commandFramework = new CommandFramework(this);
        this.configFile = new ConfigFile(this, "config");
        this.ranksFile = new ConfigFile(this, "ranks");
        this.permissionsDatabase = new PermissionsDatabase(this);
        this.rankHandler = new RankHandler(this);
        for (final Player online : PlayerUtility.getOnlinePlayers()) {
            new Profile(online.getUniqueId(), new ArrayList<String>(), new ArrayList<Grant>()).asyncLoad();
        }
    }
    
    public void onDisable() {
        this.subscriber.getJedisPubSub().unsubscribe();
        this.pool.destroy();
        for (final Profile profile : Profile.getProfiles()) {
            if (profile.getPlayer() != null) {
                profile.getPlayer().removeAttachment(profile.getAttachment());
            }
            profile.save();
        }
        this.rankHandler.save();
        this.permissionsDatabase.getClient().close();
    }
    
    private void registerListeners() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents((Listener)new GrantProcedureListeners(), (Plugin)this);
        pluginManager.registerEvents((Listener)new ProfileListeners(), (Plugin)this);
        pluginManager.registerEvents((Listener)new GrantListeners(), (Plugin)this);
    }
    
    private void registerCommands() {
        new RankCreateCommand();
        new RankDeleteCommand();
        new RankPrefixCommand();
        new RankSuffixCommand();
        new RankImportCommand();
        new RankAddPermissionCommand();
        new RankDeletePermissionCommand();
        new RankListPermissionsCommand();
        new ProfileAddPermissionCommand();
        new ProfileDeletePermissionCommand();
        new ProfileListPermissionsCommand();
        new GrantCommand();
        new GrantsCommand();
    }
    
    private void setupJedis() {
        this.address = this.configFile.getString("DATABASE.REDIS.HOST");
        this.port = this.configFile.getInt("DATABASE.REDIS.PORT");
        this.pool = new JedisPool(this.address, this.port);
        this.publisher = new JedisPublisher(this);
        this.subscriber = new JedisSubscriber(this);
    }
    
    public static nPermissions getInstance() {
        return nPermissions.instance;
    }
    
    public CommandFramework getCommandFramework() {
        return this.commandFramework;
    }
    
    public ConfigFile getConfigFile() {
        return this.configFile;
    }
    
    public ConfigFile getRanksFile() {
        return this.ranksFile;
    }
    
    public RankHandler getRankHandler() {
        return this.rankHandler;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public JedisPool getPool() {
        return this.pool;
    }
    
    public JedisPublisher getPublisher() {
        return this.publisher;
    }
    
    public JedisSubscriber getSubscriber() {
        return this.subscriber;
    }
    
    public PermissionsDatabase getPermissionsDatabase() {
        return this.permissionsDatabase;
    }
}
