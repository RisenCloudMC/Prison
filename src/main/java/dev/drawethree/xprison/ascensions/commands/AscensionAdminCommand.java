package dev.drawethree.xprison.ascensions.commands;

import dev.drawethree.xprison.ascensions.XPrisonAscensions;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class AscensionAdminCommand {
    private final XPrisonAscensions plugin;

    public AscensionAdminCommand(XPrisonAscensions plugin) {

        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPermission("xprison.ascensions.admin")
                .handler(c -> {
                    if (c.args().size() == 3) {

                        Player target = c.arg(1).parseOrFail(Player.class);
                        int amount = c.arg(2).parseOrFail(Integer.class);

                        switch (c.rawArg(0).toLowerCase()) {
                            case "set":
                                this.plugin.getAscensionManager().setPlayerAscension(c.sender(), target, amount);
                                break;
                            case "add":
                                this.plugin.getAscensionManager().addPlayerAscension(c.sender(), target, amount);
                                break;
                            case "remove":
                                this.plugin.getAscensionManager().removePlayerAscension(c.sender(), target, amount);
                                break;
                            default:
                                PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
                                PlayerUtils.sendMessage(c.sender(), "&e&lASCENSION ADMIN HELP MENU ");
                                PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
                                PlayerUtils.sendMessage(c.sender(), "&e/ascensionadmin add [player] [amount]");
                                PlayerUtils.sendMessage(c.sender(), "&e/ascensionadmin remove [player] [amount]");
                                PlayerUtils.sendMessage(c.sender(), "&e/ascensionadmin set [player] [amount]");
                                PlayerUtils.sendMessage(c.sender(), "&e&m-------&f&m-------&e&m--------&f&m--------&e&m--------&f&m-------&e&m-------");
                                break;
                        }
                    }
                }).registerAndBind(this.plugin.getCore(), "ascensionadmin", "ascensiona");
    }
}
