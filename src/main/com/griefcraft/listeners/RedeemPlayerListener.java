package com.griefcraft.listeners;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

import com.griefcraft.BukkitPlugin;
import com.griefcraft.RedeemCode;
import com.griefcraft.PlayerState;
import com.griefcraft.PlayerState.Step;
import com.griefcraft.util.Colors;

public class RedeemPlayerListener extends PlayerListener {

	/**
	 * The bukkit plugin
	 */
	private BukkitPlugin plugin;
	
	public RedeemPlayerListener(BukkitPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * This method is called when a player state is completed
	 * 
	 * @param playerState
	 */
	private void finalize(PlayerState playerState) {
		if(!playerState.isComplete()) {
			return;
		}
		Player player = playerState.getPlayer();
		plugin.removePlayerState(player.getName());
		
		// create the code
		RedeemCode redeemCode = new RedeemCode();
		redeemCode.setName(playerState.getName());
		redeemCode.setMaxUses(playerState.getUses());
		redeemCode.setExpiry(playerState.getExpiry());
		redeemCode.setIConomyCoins(playerState.getiConomyCoins());
		redeemCode.setPlayersUsed("");
		redeemCode.setCreated(System.currentTimeMillis() / 1000L);
		
		String rewards = "";
		
		for(Map.Entry<Integer, Integer> entry : playerState.getItems().entrySet()) {
			rewards += entry.getKey() + ":" + entry.getValue() + ",";
		}
		
		if(rewards.endsWith(",")) {
			rewards = rewards.substring(0, rewards.length() - 1);
		}
		
		redeemCode.setItemRewards(rewards);
		
		// save it to the database
		plugin.getDatabase().save(redeemCode);
		
		player.sendMessage(Colors.Green + "Code successfully created.");
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		PlayerState playerState = plugin.getPlayerState(player.getName());
		
		if(playerState == null) {
			return;
		}
		
		if(playerState.isComplete()) {
			plugin.removePlayerState(player.getName());
			return;
		}
		
		String message = event.getMessage().trim();
		
		switch(playerState.getStep()) {
		case CODE_NAME:
			String codeName = message.trim();
			RedeemCode code = plugin.getDatabase().find(RedeemCode.class).where().ieq("code_name", codeName).findUnique();
			
			if(code != null) {
				player.sendMessage(Colors.Red + "That code already exists!");
				break;
			}
			
			playerState.setName(codeName);
			playerState.setStep(Step.USES);
			player.sendMessage("Code name: " + Colors.Gray + message);
			player.sendMessage(" ");
			player.sendMessage(Colors.Green + "How many total uses should be allowed? It is assumed strictly 1 use per player (e.g entering 10 means usable by 10 players.)");
			break;
			
		case USES:
			try {
				int uses = Integer.parseInt(message.trim());
				
				playerState.setUses(uses);
				playerState.setStep(Step.ITEMS);
				player.sendMessage("Code use limit: " + Colors.Gray + uses);
				player.sendMessage(" ");
				player.sendMessage(Colors.Green + "What items should be given for the code? Use Names or Ids followed by a space and then amount (e.g \"stone 128\")");
				player.sendMessage("When done, type " + Colors.Blue + "next");
			} catch(Exception e) {
				player.sendMessage(Colors.Red + "Please try again.");
			}
			
			break;
			
		case ITEMS:
			if(message.equalsIgnoreCase("next")) {
				playerState.setStep(Step.ICONOMY_COINS);
				player.sendMessage("Giving " + Colors.Green + playerState.getItems().size() + Colors.White + " different items");
				player.sendMessage(" ");
				player.sendMessage(Colors.Green + "Would you like iConomy coins to be awarded? Type the amount, otherwise " + Colors.Blue + "next");
				break;
			}
			
			String[] items = message.split(" ");
			
			try {
				Map<Integer, Integer> codeItems = playerState.getItems();
				
				for(int index=0; index<items.length; index += 2) {
					if((index + 2) > items.length) {
						break;
					}
					
					String block = items[index];
					int amount = Integer.parseInt(items[index + 1]);
					Material material = Material.matchMaterial(block);
					
					if(material == null) {
						player.sendMessage(Colors.Red + "Invalid block: " + block);
						continue;
					}
					
					// if they previously entered that id, add it to the current amount
					if(codeItems.containsKey(material.getId())) {
						amount += codeItems.get(material.getId());
					}
					
					// add the item
					codeItems.put(material.getId(), amount);
					
					// tell them
					player.sendMessage("Acknowledged " + Colors.Green + amount + Colors.White + " " + material);
				}
			} catch(Exception e) {
				player.sendMessage(Colors.Red + "Please try again.");
			}
			break;
			
		case ICONOMY_COINS:
			double iconomyCoins = 0;
			
			if(message.equals("next")) {
				playerState.setStep(Step.EXPIRY);
				playerState.setIconomyCoins(0);
				
				player.sendMessage(" ");
				player.sendMessage("Final question! If the code should expiry, when should it? Enter a simple time, e.g \"10 minutes\" or \"30 seconds\" or \"2 weeks\"");
				player.sendMessage("To skip this, type " + Colors.Blue + "next");
				break;
			}
			
			try {
				iconomyCoins = Double.parseDouble(message.trim());
				playerState.setIconomyCoins(iconomyCoins);
				playerState.setStep(Step.EXPIRY);
			} catch(NumberFormatException e) {
				player.sendMessage(Colors.Red + "Please try again.");
				break;
			}
			
			// we're done, process everything
			if(iconomyCoins != 0) {
				player.sendMessage("Players will be given " + Colors.Green + iconomyCoins + Colors.White + " iConomy coins.");
			}
			
			player.sendMessage(" ");
			player.sendMessage("Final question! If the code should expire, when should it? Enter a simple time, e.g \"10 minutes\" or \"30 seconds\" or \"2 weeks\"");
			player.sendMessage("To skip this, type " + Colors.Blue + "next");
			break;
			
		case EXPIRY:
			// we use now() as the base time (Seconds since epoch!)
			long expiry = System.currentTimeMillis() / 1000L;
			
			// extract the time
			// we sort of expect them to be exact in their methods. Oh well! (#### unit)
			message = message.toLowerCase().trim();
			
			if(message.equals("next")) {
				playerState.setStep(Step.COMPLETE);
				finalize(playerState);
				break;
			}
			
			try {
				int index = message.indexOf(" ");
				
				if(index == -1) {
					player.sendMessage(Colors.Red + "Invalid formatting.");
					break;
				}
				
				int time = Integer.parseInt(message.substring(0, index));
				
				if(message.contains("second")) {
					expiry += time;
				} else if(message.contains("minute")) {
					expiry += time * 60L;
				} else if(message.contains("hour")) {
					expiry += time * 3600L; // 60 * 60
				} else if(message.contains("day")) {
					expiry += time * 86400L; // 60 * 60 * 24
				} else if(message.contains("week")) {
					expiry += time * 604800L; // 60 * 60 * 24 * 7
				} else if(message.contains("month")) {
					expiry += time * 18144000L; // 60 * 60 * 24 * 7 * 31
				} else if(message.contains("year")) {
					expiry += time * 6622560000L; // 60 * 60 * 24 * 7 * 365
				}
				
				playerState.setExpiry(expiry);
				playerState.setStep(Step.COMPLETE);
				player.sendMessage("Code will expire at: " + Colors.Green + plugin.dateToString(expiry));
				finalize(playerState);
			} catch(Exception e) {
				e.printStackTrace();
				player.sendMessage(Colors.Red + "Please try again.");
			}
			
			break;
		}
		
		// update the state and cancel the event
		plugin.bindPlayerState(player.getName(), playerState);
		event.setCancelled(true);
	}
	
}
