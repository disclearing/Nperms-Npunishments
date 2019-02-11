package us.centile.permissions.util.command;

import us.centile.permissions.*;
import us.centile.permissions.util.file.*;

public abstract class BaseCommand
{
    public nPermissions main;
    public ConfigFile configFile;
    
    public BaseCommand() {
        this.main = nPermissions.getInstance();
        this.configFile = this.main.getConfigFile();
        this.main.getCommandFramework().registerCommands(this);
    }
    
    public abstract void onCommand(final CommandArgs p0);
}
