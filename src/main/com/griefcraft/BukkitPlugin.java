package com.griefcraft;

import com.griefcraft.commands.CodesExecutor;
import com.griefcraft.commands.RedeemCodeExecutor;
import com.griefcraft.listeners.RedeemPlayerListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BukkitPlugin extends JavaPlugin {

	private static String VERSION = "1.10";
	private Logger logger = Logger.getLogger("Redeem");
	
	/**
	 * The player listener
	 */
	private RedeemPlayerListener playerListener;
	
	/**
	 * Code creation states
	 */
	private Map<String, PlayerState> playerStates;
	
	/**
	 * True if we have iConomy
	 */
	private boolean hasiConomy = false;
	
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
	}
	
	/**
	 * @return
	 */
	public boolean hasiConomy() {
		return hasiConomy;
	}
	
	/**
	 * Get the permission node for a player -- returns true if permissions is NOT available and player is OP
	 * 
	 * @param sender
	 * @param node
	 * @return
	 */
	public boolean getSpecialPermission(CommandSender sender, String node) {
		return sender.hasPermission(node);
	}
	
	/**
	 * Get the permission node for a player -- returns true if permissions is NOT available
	 * 
	 * @param player
	 * @param node
	 * @return
	 */
	public boolean getPermission(Player player, String node) {
		return player.hasPermission(node);
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
        getServer().getPluginManager().registerEvents(playerListener, this);
	}

}
