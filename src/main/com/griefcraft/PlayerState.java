package com.griefcraft;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class PlayerState {
	
	// Next step
	public enum Step {
		CODE_NAME,
		
		USES,
		
		ITEMS,
		
		ICONOMY_COINS,
		
		EXPIRY,
		
		COMPLETE
	}
	
	/**
	 * The current step
	 */
	private Step step = Step.CODE_NAME;

	/**
	 * The player it is assigned to
	 */
	private Player player;
	
	/**
	 * The name of the redeem code
	 */
	private String name;
	
	/**
	 * The amount of uses
	 */
	private int uses = 0;
	
	/**
	 * The map of items to give
	 * Id : Amount
	 */
	private Map<Integer, Integer> items = new HashMap<Integer, Integer>();
	
	/**
	 * The amount of iconomy counts
	 */
	private double iconomyCoins = 0;
	
	/**
	 * The time the code expires (seconds since epoch)
	 */
	private long expiry = 0L;
	
	/**
	 * @return the step
	 */
	public Step getStep() {
		return step;
	}
	
	public void setStep(Step step) {
		this.step = step;
	}
	
	public PlayerState(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return name;
	}
	
	public int getUses() {
		return uses;
	}
	
	public void setUses(int uses) {
		this.uses = uses;
	}
	
	public long getExpiry() {
		return expiry;
	}
	
	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}

	public Map<Integer, Integer> getItems() {
		return items;
	}

	public double getiConomyCoins() {
		return iconomyCoins;
	}

	public boolean isComplete() {
		return step == Step.COMPLETE;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIconomyCoins(double iconomyCoins) {
		this.iconomyCoins = iconomyCoins;
	}
	
}
