package dev.drawethree.xprison.ascensions.config;

import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import dev.drawethree.xprison.ascensions.model.Ascension;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AscensionConfig {

    private final XPrisonAscensions plugin;
    private final FileManager.Config config;
    private Ascension maxAscension;
    private String unlimitedAscensionPrefix;
    private List<String> ascensionTopFormat;
    private List<String> unlimitedAscensionsRewardPerAscension;
    private Map<Long, Ascension> ascensionById;
    private Map<String, String> messages;
    private Map<Long, List<String>> unlimitedAscensionsRewards;
    private int topPlayersAmount;
    private long unlimitedAscensionMax;
    private double unlimitedAscensionCost;
    private double increaseCostBy;
    private boolean useTokensCurrency;
    private boolean unlimitedAscensions;
    private boolean increaseCostEnabled;
    private boolean resetRankAfterAscension;
    private int savePlayerDataInterval;

    public AscensionConfig(XPrisonAscensions plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getCore().getFileManager().getConfig("ascensions.yml").copyDefaults(true).save();
        this.ascensionById = new HashMap<>();
    }


    private void loadMessages(YamlConfiguration configuration) {
        this.messages = new HashMap<>();

        for (String key : configuration.getConfigurationSection("messages").getKeys(false)) {
            messages.put(key.toLowerCase(), TextUtils.applyColor(getConfig().get().getString("messages." + key)));
        }
    }

    public void reload() {
        YamlConfiguration configuration = getYamlConfig();
        this.loadVariables(configuration);
        this.loadAscensions(configuration);
        this.loadUnlimitedAscensionsRewards(configuration);
        this.loadMessages(configuration);
    }

    private void loadAscensions(YamlConfiguration configuration) {
        this.ascensionById.clear();

        if (this.unlimitedAscensions) {
            this.plugin.getCore().getLogger().info(String.format("Loaded %,d ascensions.", this.unlimitedAscensionMax));
        } else {
            for (String key : configuration.getConfigurationSection("Ascension").getKeys(false)) {
                long id = Long.parseLong(key);
                String prefix = TextUtils.applyColor(configuration.getString("Ascension." + key + ".Prefix"));
                long cost = configuration.getLong("Ascension." + key + ".Cost");
                List<String> commands = configuration.getStringList("Ascension." + key + ".CMD");
                Ascension p = new Ascension(id, cost, prefix, commands);
                this.ascensionById.put(id, p);
                this.maxAscension = p;
            }
            this.plugin.getCore().getLogger().info(String.format("Loaded %,d ascensions!", this.ascensionById.keySet().size()));
        }
    }

    public void load() {
        this.reload();
    }


    public String getMessage(String messageKey) {
        return this.messages.getOrDefault(messageKey.toLowerCase(), "Missing message with key: " + messageKey);
    }

    private void loadVariables(YamlConfiguration configuration) {
        this.ascensionTopFormat = configuration.getStringList("ascension-top-format");
        this.unlimitedAscensions = configuration.getBoolean("unlimited_ascensions.enabled");
        this.unlimitedAscensionCost = configuration.getDouble("unlimited_ascensions.ascension_cost");
        this.unlimitedAscensionPrefix = TextUtils.applyColor(configuration.getString("unlimited_ascensions.prefix"));
        this.unlimitedAscensionMax = configuration.getLong("unlimited_ascensions.max_ascension");
        this.increaseCostEnabled = configuration.getBoolean("unlimited_ascensions.increase_cost.enabled");
        this.increaseCostBy = configuration.getDouble("unlimited_ascensions.increase_cost.increase_cost_by");
        boolean unlimitedAscensionsRewardPerAscensionEnabled = configuration.getBoolean("unlimited_ascensions.rewards-per-ascension.enabled");
        if (unlimitedAscensionsRewardPerAscensionEnabled) {
            this.unlimitedAscensionsRewardPerAscension = configuration.getStringList("unlimited_ascensions.rewards-per-ascension.rewards");
        }
        this.topPlayersAmount = configuration.getInt("top_players_amount");
        this.savePlayerDataInterval = configuration.getInt("player_data_save_interval");
        this.resetRankAfterAscension = configuration.getBoolean("reset_rank_after_ascension");
        this.useTokensCurrency = configuration.getBoolean("use_tokens_currency");
        this.plugin.getCore().getLogger().info("Using " + (useTokensCurrency ? "Tokens" : "Money") + " currency for Ascensions.");

    }

    private void loadUnlimitedAscensionsRewards(YamlConfiguration configuration) {
        this.unlimitedAscensionsRewards = new LinkedHashMap<>();

        ConfigurationSection section = configuration.getConfigurationSection("unlimited_ascensions.rewards");

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                long id = Long.parseLong(key);

                List<String> rewards = section.getStringList(key);

                if (!rewards.isEmpty()) {
                    this.unlimitedAscensionsRewards.put(id, rewards);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private FileManager.Config getConfig() {
        return this.config;
    }

    public YamlConfiguration getYamlConfig() {
        return this.config.get();
    }

    public Ascension getMaxAscension() {
        return maxAscension;
    }

    public String getUnlimitedAscensionPrefix() {
        return unlimitedAscensionPrefix;
    }

    public List<String> getAscensionTopFormat() {
        return ascensionTopFormat;
    }

    public List<String> getUnlimitedAscensionsRewardPerAscension() {
        return unlimitedAscensionsRewardPerAscension;
    }

    public Map<Long, Ascension> getAscensionById() {
        return ascensionById;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public Map<Long, List<String>> getUnlimitedAscensionsRewards() {
        return unlimitedAscensionsRewards;
    }

    public long getUnlimitedAscensionMax() {
        return unlimitedAscensionMax;
    }

    public double getUnlimitedAscensionCost() {
        return unlimitedAscensionCost;
    }

    public double getIncreaseCostBy() {
        return increaseCostBy;
    }

    public boolean isUseTokensCurrency() {
        return useTokensCurrency;
    }

    public boolean isUnlimitedAscensions() {
        return unlimitedAscensions;
    }

    public boolean isIncreaseCostEnabled() {
        return increaseCostEnabled;
    }

    public boolean isResetRankAfterAscension() {
        return resetRankAfterAscension;
    }

    public int getTopPlayersAmount() {
        return topPlayersAmount;
    }

    public long getSavePlayerDataInterval() {
        return savePlayerDataInterval;
    }
}
