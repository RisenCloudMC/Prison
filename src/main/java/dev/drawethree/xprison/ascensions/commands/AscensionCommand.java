package dev.drawethree.xprison.ascensions.commands;

import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import me.lucko.helper.Commands;

public class AscensionCommand {

    private final XPrisonAscensions plugin;

    public AscensionCommand(XPrisonAscensions plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPlayer()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.plugin.getAscensionManager().buyNextAscension(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), "ascension");
    }
}
