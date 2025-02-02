package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.ServerInfo;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.TranslationContainer;

import java.util.Objects;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class BanCommand extends VanillaCommand {

    public BanCommand(String name) {
        super(name, "%nukkit.command.ban.player.description", "%commands.ban.usage");
        this.setPermission("nukkit.command.ban.player");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(new TranslationContainer("commands.generic.usage", this.usageMessage));

            return false;
        }

        String name = args[0];
        String reason = "";
        for (int i = 1; i < args.length; i++) {
            reason += args[i] + " ";
        }

        if (reason.length() > 0) {
            reason = reason.substring(0, reason.length() - 1);
        }

        ServerInfo.banByName.add(name);

        Player player = sender.getServer().getPlayerExact(name);
        if (player != null) {
            player.kick(!Objects.equals(reason, "") ? "Banned by admin. Reason: " + reason : "Banned by admin");
        }

        Command.broadcastCommandMessage(sender, new TranslationContainer("%commands.ban.success", player != null ? player.getName() : name));

        return true;
    }
}
