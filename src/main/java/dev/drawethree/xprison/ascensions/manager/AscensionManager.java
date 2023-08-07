package dev.drawethree.xprison.ascensions.manager;

import dev.drawethree.xprison.api.enums.LostCause;
import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import dev.drawethree.xprison.ascensions.api.events.PlayerAscensionEvent;
import dev.drawethree.xprison.ascensions.config.AscensionConfig;
import dev.drawethree.xprison.ascensions.model.Ascension;
import dev.drawethree.xprison.ranks.XPrisonRanks;
import dev.drawethree.xprison.ranks.manager.RanksManager;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AscensionManager {

    private final XPrisonAscensions plugin;

    private final Map<UUID, Long> onlinePlayersAscension;
    private final List<UUID> prestigingPlayers;

    public AscensionManager(XPrisonAscensions plugin) {
        this.plugin = plugin;
        this.onlinePlayersAscension = new ConcurrentHashMap<>();
        this.prestigingPlayers = new ArrayList<>(10);
    }


    private void saveAllDataSync() {
        for (UUID uuid : this.onlinePlayersAscension.keySet()) {
            this.plugin.getAscensionService().setAscension(Players.getOfflineNullable(uuid), onlinePlayersAscension.get(uuid));
        }
        this.plugin.getCore().getLogger().info("Saved online players ascensions.");
    }

    private void loadAllData() {
        for (Player p : Players.all()) {
            loadPlayerAscension(p);
        }
    }

	public void savePlayerData(Player player, boolean removeFromCache, boolean async) {
		if (async) {
			Schedulers.async().run(() -> savePlayerDataLogic(player, removeFromCache));
		} else {
			savePlayerDataLogic(player, removeFromCache);
		}
	}

	private void savePlayerDataLogic(Player player, boolean removeFromCache) {
        this.plugin.getAscensionService().setAscension(player, this.getPlayerAscension(player).getId());
		if (removeFromCache) {
			this.onlinePlayersAscension.remove(player.getUniqueId());
		}
        this.plugin.getCore().debug("Saved " + player.getName() + "'s ascension to database.", this.plugin);
	}


    public void loadPlayerAscension(Player player) {
        Schedulers.async().run(() -> {
            this.plugin.getAscensionService().createAscension(player);
            long ascension = this.plugin.getAscensionService().getPlayerAscension(player);
            this.onlinePlayersAscension.put(player.getUniqueId(), ascension);
            this.plugin.getCore().debug("Loaded " + player.getName() + "'s ascension.", this.plugin);
        });
    }

    private double calculateNextAscensionCost(Ascension origin) {

        if (!this.getConfig().isIncreaseCostEnabled()) {
            return origin.getCost();
        }

        return origin.getId() == 0 ? (this.getConfig().getUnlimitedAscensionCost()) : (origin.getCost() * this.getConfig().getIncreaseCostBy());
    }

    private AscensionConfig getConfig() {
        return this.plugin.getAscensionConfig();
    }


    public Ascension getNextAscension(Ascension ascension) {
        if (this.getConfig().isUnlimitedAscensions()) {
            return new Ascension(ascension.getId() + 1, this.calculateNextAscensionCost(ascension), this.getConfig().getUnlimitedAscensionPrefix().replace("%ascension%", String.format("%,d", ascension.getId() + 1)), this.getConfig().getUnlimitedAscensionsRewards().getOrDefault(ascension.getId() + 1, null));
        }
        return this.getConfig().getAscensionById().getOrDefault(ascension.getId() + 1, null);
    }

    public Ascension getAscensionById(long id) {
        if (this.getConfig().isUnlimitedAscensions()) {
            return new Ascension(id, id * this.getConfig().getIncreaseCostBy(), this.getConfig().getUnlimitedAscensionPrefix().replace("%ascension%", String.format("%,d", id)), this.getConfig().getUnlimitedAscensionsRewards().getOrDefault(id, null));
        }
        return this.getConfig().getAscensionById().getOrDefault(id, null);
    }

    public synchronized Ascension getPlayerAscension(Player p) {
        if (this.getConfig().isUnlimitedAscensions()) {
            long ascension = this.onlinePlayersAscension.getOrDefault(p.getUniqueId(), 0L);
            return new Ascension(ascension, this.calculateAscensionCost(ascension), this.getConfig().getUnlimitedAscensionPrefix().replace("%ascension%", String.format("%,d", ascension)), null);
        } else {
            return this.getConfig().getAscensionById().getOrDefault(this.onlinePlayersAscension.get(p.getUniqueId()), this.getConfig().getAscensionById().get(0L));
        }
    }

    private double calculateAscensionCost(long ascension) {

        if (!this.getConfig().isIncreaseCostEnabled()) {
            return this.getConfig().getUnlimitedAscensionCost();
        }

        double origin = this.getConfig().getUnlimitedAscensionCost();

        for (long i = 0; i < ascension; i++) {
            if (i == 0) {
                continue;
            }
            origin = origin * this.getConfig().getIncreaseCostBy();
        }

        return origin;
    }

    public boolean isMaxAscension(Player p) {
        if (this.getConfig().isUnlimitedAscensions()) {
            return this.getPlayerAscension(p).getId() >= this.getConfig().getUnlimitedAscensionMax();
        }
        return this.getPlayerAscension(p).getId() == this.getConfig().getMaxAscension().getId();
    }

    private boolean completeTransaction(Player p, double cost) {
		if (this.getConfig().isUseTokensCurrency()) {
            this.plugin.getCore().getTokens().getApi().removeTokens(p, (long) cost, LostCause.RANKUP);
            return true;
        } else {
            return this.plugin.getCore().getEconomy().withdrawPlayer(p, cost).transactionSuccess();
        }
    }

    private boolean isTransactionAllowed(Player p, double cost) {
        if (this.getConfig().isUseTokensCurrency()) {
            return this.plugin.getCore().getTokens().getApi().hasEnough(p, (long) cost);
        } else {
            return this.plugin.getCore().getEconomy().has(p, cost);
        }

    }

    public boolean buyNextAscension(Player p) {

        if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
            PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_last_rank"));
            return false;
        }

        if (isMaxAscension(p)) {
            PlayerUtils.sendMessage(p, this.getConfig().getMessage("last_ascension"));
            return false;
        }

        Ascension currentAscension = this.getPlayerAscension(p);
        Ascension toBuy = getNextAscension(currentAscension);

        if (!this.isTransactionAllowed(p, toBuy.getCost())) {
            if (this.getConfig().isUseTokensCurrency()) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_tokens_ascension").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
            } else {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_money_ascension").replace("%cost%", String.format("%,.0f", toBuy.getCost())));
            }
            return false;
        }

        PlayerAscensionEvent event = new PlayerAscensionEvent(p, currentAscension, toBuy);

        Events.call(event);

        if (event.isCancelled()) {
            this.plugin.getCore().debug("PlayerAscensionEvent was cancelled.", this.plugin);
            return false;
        }

        doAscension(p, toBuy);

        PlayerUtils.sendMessage(p, this.getConfig().getMessage("ascension_up").replace("%Ascension%", toBuy.getPrefix()));

        return true;
    }

    public void sendAscensionTop(CommandSender sender) {

        List<String> ascensionTopFormat = this.getConfig().getAscensionTopFormat();
        Map<UUID, Long> topAscension = this.plugin.getAscensionService().getTopAscensions(this.getConfig().getTopPlayersAmount());

		for (String s : ascensionTopFormat) {
            if (s.startsWith("{FOR_EACH_PLAYER}")) {
                String rawContent = s.replace("{FOR_EACH_PLAYER} ", "");
                for (int i = 0; i < 10; i++) {
                    try {
                        UUID uuid = (UUID) topAscension.keySet().toArray()[i];
                        OfflinePlayer player = Players.getOfflineNullable(uuid);
                        String name;
                        if (player.getName() == null) {
                            name = "Unknown Player";
                        } else {
                            name = player.getName();
                        }
                        long ascension = topAscension.get(uuid);
                        PlayerUtils.sendMessage(sender, rawContent.replace("%position%", String.valueOf(i + 1)).replace("%player%", name).replace("%ascension%", String.format("%,d", ascension)));
                    } catch (Exception e) {
                        break;
                    }
                }
            } else {
                PlayerUtils.sendMessage(sender, s);
            }
        }
    }

    public void buyMaxAscension(Player p) {

        Schedulers.async().run(() -> {
            if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_last_rank"));
                return;
            }

            if (isMaxAscension(p)) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("last_ascension"));
                return;
            }

            Ascension startAscension = this.getPlayerAscension(p);

            Ascension currentAscension = startAscension;

            Ascension nextAscension = this.getNextAscension(startAscension);

            if (!this.isTransactionAllowed(p, nextAscension.getCost())) {
                if (this.getConfig().isUseTokensCurrency()) {
                    PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_tokens_ascension").replace("%cost%", String.format("%,.0f", nextAscension.getCost())));
                } else {
                    PlayerUtils.sendMessage(p, this.getConfig().getMessage("not_enough_money_ascension").replace("%cost%", String.format("%,.0f", nextAscension.getCost())));
                }
                return;
            }

            PlayerUtils.sendMessage(p, this.getConfig().getMessage("max_ascension_started"));

            this.prestigingPlayers.add(p.getUniqueId());
            while (p.isOnline() && !isMaxAscension(p) && this.isTransactionAllowed(p, nextAscension.getCost())) {

                if (areRanksEnabled() && !getRankManager().isMaxRank(p)) {
                    break;
                }

                doAscension(p, nextAscension);
                currentAscension = nextAscension;
                nextAscension = this.getNextAscension(nextAscension);
            }

            PlayerAscensionEvent event = new PlayerAscensionEvent(p, startAscension, currentAscension);

            Events.callSync(event);

            this.prestigingPlayers.remove(p.getUniqueId());

            if (startAscension.getId() < this.onlinePlayersAscension.get(p.getUniqueId())) {
                PlayerUtils.sendMessage(p, this.getConfig().getMessage("max_ascension_done").replace("%start_ascension%", String.format("%,d", startAscension.getId())).replace("%ascension%", String.format("%,d", this.onlinePlayersAscension.get(p.getUniqueId()))));
            }
        });
    }

    private void doAscension(Player p, Ascension nextAscension) {

        if (!this.completeTransaction(p, nextAscension.getCost())) {
            return;
        }

        this.onlinePlayersAscension.put(p.getUniqueId(), nextAscension.getId());

        giveAscensionRewards(nextAscension,p);

        List<String> rewardsPerAscension = this.getConfig().getUnlimitedAscensionsRewardPerAscension();
        if (rewardsPerAscension != null) {
            if (!Bukkit.isPrimaryThread()) {
                Schedulers.sync().run(() -> rewardsPerAscension.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName()))));
            } else {
                rewardsPerAscension.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", p.getName())));
            }
        }

        if (this.getConfig().isResetRankAfterAscension() && areRanksEnabled()) {
            getRankManager().resetPlayerRank(p);
        }
    }

    public void addPlayerAscension(CommandSender sender, Player target, int amount) {

        if (0 > amount) {
            return;
        }

        if (isMaxAscension(target)) {
            return;
        }

        Ascension startAscension = this.getPlayerAscension(target);

        long maxAscension = this.getConfig().isUnlimitedAscensions() ? this.getConfig().getUnlimitedAscensionMax() : this.getConfig().getMaxAscension().getId();
        if (startAscension.getId() + amount > maxAscension) {
            this.onlinePlayersAscension.put(target.getUniqueId(), maxAscension);
        } else {
            this.onlinePlayersAscension.put(target.getUniqueId(), this.onlinePlayersAscension.get(target.getUniqueId()) + amount);
        }

        Ascension currentAscension = this.getPlayerAscension(target);

        long ascensionGained = currentAscension.getId() - startAscension.getId();

        for (int i = 0; i < ascensionGained; i++) {
            Ascension toGive = this.getAscensionById(currentAscension.getId() + 1 + i);

            if (toGive == null) {
                break;
            }

            giveAscensionRewards(toGive,target);

            List<String> rewardsPerAscension = this.getConfig().getUnlimitedAscensionsRewardPerAscension();
            if (rewardsPerAscension != null) {
                if (!Bukkit.isPrimaryThread()) {
                    Schedulers.sync().run(() -> rewardsPerAscension.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", target.getName()))));
                } else {
                    rewardsPerAscension.forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", target.getName())));
                }
            }
        }

        PlayerAscensionEvent event = new PlayerAscensionEvent(target, startAscension, currentAscension);

        Events.callSync(event);

        PlayerUtils.sendMessage(sender, this.getConfig().getMessage("ascension_add").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
    }

    public void setPlayerAscension(CommandSender sender, Player target, long amount) {

        if (0 > amount) {
            return;
        }

        long maxPretige = this.getConfig().isUnlimitedAscensions() ? this.getConfig().getUnlimitedAscensionMax() : this.getConfig().getMaxAscension().getId();

        this.onlinePlayersAscension.put(target.getUniqueId(), Math.min(amount, maxPretige));

        if (sender != null) {
            PlayerUtils.sendMessage(sender, this.getConfig().getMessage("ascension_set").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
        }
    }

    public void removePlayerAscension(CommandSender sender, Player target, long amount) {

        if (0 > amount) {
            return;
        }

        Ascension currentAscension = this.getPlayerAscension(target);

        if (currentAscension.getId() - amount < 0) {
            this.onlinePlayersAscension.put(target.getUniqueId(), 0L);
        } else {
            this.onlinePlayersAscension.put(target.getUniqueId(), this.onlinePlayersAscension.get(target.getUniqueId()) - amount);
        }

        PlayerUtils.sendMessage(sender, this.getConfig().getMessage("ascension_remove").replace("%player%", target.getName()).replace("%amount%", String.format("%,d", amount)));
    }

    public int getAscensionProgress(Player player) {

        if (this.isMaxAscension(player)) {
            return 100;
        }

        Ascension current = this.getPlayerAscension(player);
        Ascension next = this.getNextAscension(current);

        double currentBalance = this.getConfig().isUseTokensCurrency() ? this.plugin.getCore().getTokens().getApi().getPlayerTokens(player) : this.plugin.getCore().getEconomy().getBalance(player);

        int progress = (int) ((currentBalance / next.getCost()) * 100);

        if (progress > 100) {
            progress = 100;
        }

        return progress;
    }

    private boolean areRanksEnabled() {
        return this.plugin.getCore().isModuleEnabled(XPrisonRanks.MODULE_NAME);
    }

    private RanksManager getRankManager() {
        if (!areRanksEnabled()) {
            throw new IllegalStateException("Ranks module is not enabled");
        }
        return this.plugin.getCore().getRanks().getRanksManager();
    }

    public boolean isPrestiging(Player sender) {
        return this.prestigingPlayers.contains(sender.getUniqueId());
    }

    public void giveAscensionRewards(Ascension ascension, Player p) {
        if (ascension.getCommandsToExecute() != null) {
            if (!Bukkit.isPrimaryThread()) {
                Schedulers.sync().run(() -> executeCommands(ascension, p));
            } else {
                executeCommands(ascension, p);
            }
        }
    }

    private void executeCommands(Ascension ascension, Player p) {
        for (String cmd : ascension.getCommandsToExecute()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()).replace("%Ascension%", ascension.getPrefix()));
        }
    }

    public void enable() {
        this.loadAllData();
    }

	public void disable() {
		this.saveAllDataSync();
	}
}
