package dev.drawethree.xprison.ascensions.commands;

import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import me.lucko.helper.Commands;

public class MaxAscensionCommand {

    private final XPrisonAscensions plugin;

    public MaxAscensionCommand(XPrisonAscensions plugin) {

        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPermission("xprison.ascensions.maxascension", this.plugin.getAscensionConfig().getMessage("no_permission"))
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {

                        if (this.plugin.getAscensionManager().isPrestiging(c.sender())) {
                            return;
                        }

                        this.plugin.getAscensionManager().buyMaxAscension(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), "maxascension", "maxp", "mp");
    }
}
