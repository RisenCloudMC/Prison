package dev.drawethree.xprison.ascensions.listener;

import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import me.lucko.helper.Events;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AscensionListener {
    private final XPrisonAscensions plugin;

    public AscensionListener(XPrisonAscensions plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.subscribePlayerJoinEvent();
        this.subscribePlayerQuitEvent();
    }

    private void subscribePlayerQuitEvent() {
        Events.subscribe(PlayerQuitEvent.class)
                .handler(e -> this.plugin.getAscensionManager().savePlayerData(e.getPlayer(), true, true)).bindWith(plugin.getCore());
    }

    private void subscribePlayerJoinEvent() {
        Events.subscribe(PlayerJoinEvent.class)
                .handler(e -> this.plugin.getAscensionManager().loadPlayerAscension(e.getPlayer())).bindWith(plugin.getCore());
    }


}
