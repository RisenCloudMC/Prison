package dev.drawethree.xprison.ascensions.commands;

import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import me.lucko.helper.Commands;

public class AscensionTopCommand {

    private final XPrisonAscensions plugin;

    public AscensionTopCommand(XPrisonAscensions plugin) {

        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.plugin.getAscensionManager().sendAscensionTop(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), "ascensiontop");
    }
}
