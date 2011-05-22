package com.griefcraft.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefcraft.BukkitPlugin;
import com.griefcraft.PlayerState;
import com.griefcraft.RedeemCode;
import com.griefcraft.util.Colors;

public class CodesExecutor implements CommandExecutor {

	/**
	 * Codes to show per page
	 */
	public final static int PER_PAGE = 15;
	
	private BukkitPlugin plugin;

	public CodesExecutor(BukkitPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Special permissions
		boolean canCreate = plugin.getSpecialPermission(sender, "codes.admin.create") && (sender instanceof Player);
		boolean canList = plugin.getSpecialPermission(sender, "codes.admin.list");
		boolean canRemove = plugin.getSpecialPermission(sender, "codes.admin.remove");
		boolean canDebug = plugin.getSpecialPermission(sender, "codes.admin.debug");
		
		if(args.length == 0) {
			sender.sendMessage(Colors.Green + plugin.getDescription().getFullName() + " by Hidendra");
			sender.sendMessage("");
			
			if(canCreate) {
				sender.sendMessage("/codes create  " + Colors.Blue + "Start Code Creation process");
			}
			
			if(canList) {
				sender.sendMessage("/codes list [page] " + Colors.Blue + "View the created codes");
			}
			
			if(canRemove) {
				sender.sendMessage("/codes remove <code> " + Colors.Blue + "Remove a redeemable code");
			}
			
			if(canDebug) {
				sender.sendMessage("/codes debug <code> " + Colors.Blue + "Show special information on a code");
			}
			return true;
		}
		
		String subcommand = args[0].toLowerCase();
		String subargs = "";
		
		if(args.length > 1) {
			for(int index=1; index<args.length; index++) {
				subargs += args[index] + " ";
			}
		}
		subargs = subargs.trim();

		if(subcommand.equals("create") && canCreate) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(Colors.Red + "You must be logged in as a player to create a code.");
				return true;
			}
	
			Player player = (Player) sender;
			PlayerState playerState = new PlayerState(player);
			plugin.bindPlayerState(player.getName(), playerState);
	
			player.sendMessage("For the following questions, just type the answer into Chat as if you were sending a message to others.");
			player.sendMessage("Type " + Colors.Red + "cancel" + Colors.White + " at any time to cancel creation.");
			player.sendMessage("");
			player.sendMessage(Colors.Green + "What is the name of the Code?");
		}
		else if(subcommand.equals("remove") && canRemove) {
			String codeName = subargs;
			
			// see if it exists
			RedeemCode redeemCode = plugin.getDatabase().find(RedeemCode.class).where().ieq("code_name", codeName).findUnique();
			
			if(redeemCode == null) {
				sender.sendMessage(Colors.Red + "The code \"" + codeName + "\" could not be found!");
				return true;
			}
			
			plugin.getDatabase().delete(redeemCode);
			sender.sendMessage(Colors.Green + "Removed the code \"" + codeName + "\"");
		}
		else if(subcommand.equals("list") && canList) {
			int page = 1;
			
			if(args.length > 1) {
				try {
					page = Integer.parseInt(args[1]);
				} catch(NumberFormatException e) {
					sender.sendMessage(Colors.Red + "Invalid page number.");
					return true;
				}
			}
			
			// we don't want a page number of 0
			if(page == 0) {
				page = 1;
			}
			
			// get the list of redeem codes
			int offset = (page - 1) * PER_PAGE;
			List<RedeemCode> redeemCodes = plugin.getDatabase().find(RedeemCode.class).where().setMaxRows(PER_PAGE).setFirstRow(offset).findList();
			int size = redeemCodes.size();
			
			if(size == 0) {
				sender.sendMessage(Colors.Red + "No codes found.");
				return true;
			}
			
			for(RedeemCode redeemCode : redeemCodes) {
				List<String> playersUsed = redeemCode.getPlayersUsedAsList();
				boolean expired = redeemCode.isExpired();
				boolean fullyUsed = redeemCode.isFullyUsed();
				
				String line = redeemCode.getId() + ". "; // #. 
				line += Colors.Blue + redeemCode.getName() + Colors.White + " - "; // name - 
				line += Colors.Green + playersUsed.size() + Colors.Yellow + "/" + Colors.Green + redeemCode.getMaxUses(); // #/#
				
				if(fullyUsed) {
					line += Colors.Red + "  (MAXED OUT)";
				}
				
				if(expired) {
					line += Colors.Red + "  (EXPIRED)";
				}
				
				sender.sendMessage(line);
			}
		}
		else if(subcommand.equals("debug") && canDebug) {
			String codeName = subargs;
			
			// see if it exists
			RedeemCode redeemCode = plugin.getDatabase().find(RedeemCode.class).where().ieq("code_name", codeName).findUnique();
			
			if(redeemCode == null) {
				sender.sendMessage(Colors.Red + "The code \"" + codeName + "\" could not be found!");
				return true;
			}
			
			sender.sendMessage(Colors.Blue + redeemCode.getId() + ". " + redeemCode.getName());
			sender.sendMessage("\"" + redeemCode.getPlayersUsed() + "\" -> " + redeemCode.getPlayersUsedAsList());
			sender.sendMessage("Expire: " + redeemCode.getExpiry() + " : " + (redeemCode.isExpired() ? (Colors.Red + "EXPIRED") : (Colors.Green + "TICKING")));
			sender.sendMessage("Used: " + redeemCode.getPlayersUsedAsList().size() + "/" + redeemCode.getMaxUses() + " : " + (redeemCode.isFullyUsed() ? (Colors.Red + "USED") : (Colors.Green + "UNUSED")));
			sender.sendMessage("Items: " + redeemCode.getItemRewards());
			sender.sendMessage("iConomy: " + redeemCode.getIConomyCoins());
		}

		return true;
	}

}
