package com.griefcraft;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.commands.CodesExecutor;
import com.griefcraft.commands.RedeemCodeExecutor;
import com.griefcraft.listeners.RedeemPlayerListener;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class BukkitPlugin extends JavaPlugin {

	private static String VERSION = "1.00-rc1";
	private Logger logger = Logger.getLogger("Redeem");
	
	/**
	 * The player listener
	 */
	private PlayerListener playerListener;
	
	/**
	 * Code creation states
	 */
	private Map<String, PlayerState> playerStates;
	
	/**
	 * True if we have iConomy
	 */
	private boolean hasiConomy = false;
	
	/**
	 * Permissions handler if available
	 */
	private PermissionHandler permissions;
	
	public BukkitPlugin() {
		playerListener = new RedeemPlayerListener(this);
		playerStates = new HashMap<String, PlayerState>();
	}
	
	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(RedeemCode.class);
		return classes;
	}
	
	/**
	 * Bind a player state to a player
	 * 
	 * @param player
	 * @param playerState
	 */
	public void bindPlayerState(String player, PlayerState playerState) {
		playerStates.put(player, playerState);
	}
	
	/**
	 * Remove a player state
	 * 
	 * @param player
	 * @return
	 */
	public PlayerState removePlayerState(String player) {
		return playerStates.remove(player);
	}
	
	/**
	 * Get the player state for a player
	 * 
	 * @param player
	 * @return
	 */
	public PlayerState getPlayerState(String player) {
		return playerStates.get(player);
	}
	
	@Override
	public void onDisable() {
		logger.info("RedeemCodes disabled!");
	}

	@Override
	public void onEnable() {
		registerCommands();
		registerEvents();
		setupDatabase();
		logger.info("RedeemCodes enabled: " + VERSION);
		
		// check for iConomy
		hasiConomy = getServer().getPluginManager().getPlugin("iConomy") != null;
		
		// check for permissions
		Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
		 
		if(permissionsPlugin != null) {
			permissions = ((Permissions) permissionsPlugin).getHandler();
		}
	}
	
	/**
	 * @return
	 */
	public boolean hasiConomy() {
		return hasiConomy;
	}
	
	/**
	 * @return
	 */
	public boolean hasPermissions() {
		return permissions != null;
	}
	
	/**
	 * Get the permission node for a player -- returns true if permissions is NOT available and player is OP
	 * 
	 * @param player
	 * @param node
	 * @return
	 */
	public boolean getSpecialPermission(CommandSender sender, String node) {
		if(!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player) sender;
		
		if(!hasPermissions()) {
			return player.isOp();
		}
		
		return permissions.has(player, node);
	}
	
	/**
	 * Get the permission node for a player -- returns true if permissions is NOT available
	 * 
	 * @param player
	 * @param node
	 * @return
	 */
	public boolean getPermission(Player player, String node) {
		if(!hasPermissions()) {
			return true;
		}
		
		return permissions.has(player, node);
	}
	
	/**
	 * Convert a time in seconds since epoch to a nice readable date + time
	 * 
	 * @param time
	 * @return
	 */
	public String dateToString(long time) {
		Date date = new Date(time * 1000L);
		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
	}
	
	/**
	 * Setup the database if it needs to be
	 */
	private void setupDatabase() {
		try {
			getDatabase().find(RedeemCode.class).findRowCount();
		} catch(PersistenceException e) {
			installDDL();
		}
	}
	
	/**
	 * Register the plugin commands
	 */
	private void registerCommands() {
		getCommand("codes").setExecutor(new CodesExecutor(this));
		getCommand("redeem").setExecutor(new RedeemCodeExecutor(this));
	}
	
	/**
	 * Register the plugin events
	 */
	private void registerEvents() {
		registerEvent(playerListener, Type.PLAYER_CHAT);
		registerEvent(playerListener, Type.PLAYER_QUIT);
	}

	/**
	 * Register a hook with default priority
	 * 
	 * TODO: Change priority back to NORMAL when real permissions are in
	 * 
	 * @param hook
	 *            the hook to register
	 */
	private void registerEvent(Listener listener, Type eventType) {
		registerEvent(listener, eventType, Priority.Normal);
	}

	/**
	 * Register a hook
	 * 
	 * @param hook
	 *            the hook to register
	 * @priority the priority to use
	 */
	private void registerEvent(Listener listener, Type eventType, Priority priority) {
		logger.info("-> " + eventType.toString());

		getServer().getPluginManager().registerEvent(eventType, listener, priority, this);
	}

}
