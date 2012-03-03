package com.griefcraft.commands;

import com.griefcraft.BukkitPlugin;
import com.griefcraft.RedeemCode;
import com.griefcraft.util.Colors;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedeemCodeExecutor implements CommandExecutor {

	private BukkitPlugin plugin;
    private Economy economy = null;
	
	public RedeemCodeExecutor(BukkitPlugin plugin) {
		this.plugin = plugin;

        RegisteredServiceProvider<Economy> serviceProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (serviceProvider != null) {
            economy = serviceProvider.getProvider();
        }
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if(!plugin.getPermission(player, "codes.redeem")) {
			player.sendMessage(Colors.Red + "You do not have permission to do that!");
			return true;
		}
		
		if(args.length == 0) {
			player.sendMessage(Colors.Red + "You need to specify a code name!");
			return false;
		}
		
		// get the code name
		String codeName = "";
		
		for(String arg : args) {
			codeName += arg + " ";
		}
		
		codeName = codeName.trim();
		
		// look for it
		RedeemCode redeemCode = plugin.getDatabase().find(RedeemCode.class).where().ieq("code_name", codeName).findUnique();
		
		if(redeemCode == null) {
			player.sendMessage(Colors.Red + "Code not found.");
			return false;
		}
		
		// check the uses
		if(redeemCode.isFullyUsed()) {
			player.sendMessage(Colors.Red + "That redeem code has been fully used up!");
			return true;
		}
		
		// check the expiry date
		if(redeemCode.isExpired()) {
			player.sendMessage(Colors.Red + "That redeem code has expired!");
			return true;
		}
		
		// check if the player is in the used db
		if(redeemCode.hasUsed(player.getName())) {
			player.sendMessage(Colors.Red + "You have already redeemed that code!");
			return true;
		}
		
		// Looks good, give it to them
		String rewards = redeemCode.getItemRewards();
		
		if(!rewards.isEmpty() && rewards.contains(":")) {
			List<ItemStack> itemRewards = new ArrayList<ItemStack>();
			List<ItemStack> added = new ArrayList<ItemStack>();
			
			for(String item : rewards.split(",")) {
				String[] split = item.split(":");
				int itemId = Integer.parseInt(split[0]);
				int amount = Integer.parseInt(split[1]);
				
				itemRewards.add(new ItemStack(itemId, amount));
			}
			
			for(ItemStack reward : itemRewards) {
				Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward);
				
				if(leftover.size() > 0) {
					// rewind
					for(ItemStack rem : added) {
						player.getInventory().remove(rem);
					}
					
					player.sendMessage(Colors.Red + "Not enough space available in your inventory!");
					return true;
				} else {
					added.add(reward);
				}
			}
		}
		
		// now see about iConomy coins
		double economyCoins = redeemCode.getIConomyCoins();
		
		if(economyCoins != 0 && plugin.hasiConomy()) {
            economy.depositPlayer(player.getName(), economyCoins);
            player.sendMessage("Cha-ching! Received " + Colors.Green + economy.format(economyCoins));
		}
		
		// They're redeemed! Let's add them to the used list & save
		player.sendMessage(Colors.Green + "Redeemed code successfully! Enjoy.");
		plugin.removePlayerState(player.getName());
		redeemCode.addPlayerToUsed(player.getName());
		plugin.getDatabase().save(redeemCode);
		return true;
	}
	
}
