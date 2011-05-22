package com.griefcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "sx_codes")
public class RedeemCode {
	
	private Integer id;
	private String name;
	private Integer maxUses;
	private String playersUsed;
	private String itemRewards;
	private Double iConomyCoins;
	private Long expiry;
	private Long created;
	
	@Transient
	private Object lock = new Object();
	
	@Id
	@Column(name = "code_id")
	public Integer getId() {
		return id;
	}
	
	@Column(name = "code_name")
	@Basic(optional = false)
	@NotEmpty
	public String getName() {
		return name;
	}

	@Column(name = "max_uses")
	@Basic(optional = false)
	@NotNull
	public Integer getMaxUses() {
		return maxUses;
	}

	@Column(name = "code_players")
	@Basic(optional = false)
	@NotNull
	public String getPlayersUsed() {
		return playersUsed;
	}
	
	@Column(name = "code_rewards")
	@NotNull
	public String getItemRewards() {
		return itemRewards;
	}
	
	@Column(name = "code_iconomy")
	@NotNull
	public Double getIConomyCoins() {
		return iConomyCoins;
	}
	
	@Column(name = "code_expiry")
	@Basic(optional = false)
	@NotNull
	public Long getExpiry() {
		return expiry;
	}

	@Column(name = "code_created")
	@Basic(optional = false)
	@NotNull
	public Long getCreated() {
		return created;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMaxUses(Integer maxUses) {
		this.maxUses = maxUses;
	}

	public void setPlayersUsed(String playersUsed) {
		synchronized(lock) {
			this.playersUsed = playersUsed;
		}
	}

	public void setItemRewards(String itemRewards) {
		this.itemRewards = itemRewards;
	}

	public void setIConomyCoins(Double iConomyCoins) {
		this.iConomyCoins = iConomyCoins;
	}

	public void setExpiry(Long expiry) {
		this.expiry = expiry;
	}

	public void setCreated(Long created) {
		this.created = created;
	}
	
	/**
	 * @return
	 */
	public List<String> getPlayersUsedAsList() {
		if(playersUsed.isEmpty()) {
			return new ArrayList<String>();
		}
		
		return Arrays.asList(playersUsed.split(","));
	}
	
	/**
	 * Add a player to the used list
	 * 
	 * @param player
	 */
	public void addPlayerToUsed(String player) {
		synchronized(lock) {
			String tempPlayers = playersUsed;
			if(!tempPlayers.isEmpty()) {
				tempPlayers += ",";
			}
			
			tempPlayers += player;
			setPlayersUsed(tempPlayers);
		}
	}
	
	/**
	 * @return true if the redeem code has expired
	 */
	public boolean isExpired() {
		if(expiry <= 0L) {
			return false;
		}
		
		long currentEpoch = System.currentTimeMillis() / 1000L;
		
		return currentEpoch > expiry;
	}
	
	/**
	 * @return true if the redeem code has been fully used
	 */
	public boolean isFullyUsed() {
		List<String> players = getPlayersUsedAsList();

		return players.size() >= maxUses;
	}
	
	/**
	 * Check if a player has used the redeem code
	 * 
	 * @param player
	 * @return
	 */
	public boolean hasUsed(String player) {
		List<String> players = getPlayersUsedAsList();
		
		return players.contains(player);
	}
	
}
