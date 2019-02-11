package us.centile.permissions.profile;

import us.centile.permissions.grant.*;
import java.util.*;
import org.bukkit.event.player.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

public class ProfileListeners implements Listener {
	@EventHandler
	public void onPlayerJoinEvent(final PlayerJoinEvent event) {
		final Profile profile = new Profile(event.getPlayer().getUniqueId(), new ArrayList<String>(),
				new ArrayList<Grant>());
		profile.asyncLoad();
		if (event.getPlayer().hasPermission("test.test")) {
			event.getPlayer().sendMessage("lol");

		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerLoginEvent event) {

		final Profile profile = new Profile(event.getPlayer().getUniqueId(), new ArrayList<String>(),
				new ArrayList<Grant>());

		if (!profile.isLoaded()) {
			profile.load();
		}

		profile.setupAtatchment();
	}

	@EventHandler
	public void onPlayerQuitEvent(final PlayerQuitEvent event) {
		final Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
		if (profile != null) {
			Profile.getProfiles().remove(profile);
			profile.save();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandProcess(final PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		if (event.getMessage().toLowerCase().startsWith("//calc")
				|| event.getMessage().toLowerCase().startsWith("//eval")
				|| event.getMessage().toLowerCase().startsWith("//solve")) {
			player.sendMessage(ChatColor.RED + "You don't have enough permissions.");
			event.setCancelled(true);
		}
	}

}
