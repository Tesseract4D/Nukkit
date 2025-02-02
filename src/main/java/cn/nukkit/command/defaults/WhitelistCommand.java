package cn.nukkit.command.defaults;

import cn.nukkit.ServerProperties;
import cn.nukkit.ServerInfo;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.TranslationContainer;
import cn.nukkit.utils.TextFormat;

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
public class WhitelistCommand extends VanillaCommand {

    public WhitelistCommand(String name) {
        super(name, "%nukkit.command.whitelist.description", "%commands.whitelist.usage");
        this.setPermission(
                "nukkit.command.whitelist.reload;" +
                        "nukkit.command.whitelist.enable;" +
                        "nukkit.command.whitelist.disable;" +
                        "nukkit.command.whitelist.list;" +
                        "nukkit.command.whitelist.add;" +
                        "nukkit.command.whitelist.remove"
        );
    }


    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            sender.sendMessage(new TranslationContainer("commands.generic.usage", this.usageMessage));
            return true;
        }

        if (args.length == 1) {
            if (this.badPerm(sender, args[0].toLowerCase())) {
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "reload":
                    sender.getServer().loadInfo();
                    Command.broadcastCommandMessage(sender, new TranslationContainer("commands.whitelist.reloaded"));

                    return true;
                case "on":
                    ServerProperties.white_list = true;
                    Command.broadcastCommandMessage(sender, new TranslationContainer("commands.whitelist.enabled"));

                    return true;
                case "off":
                    ServerProperties.white_list = false;
                    Command.broadcastCommandMessage(sender, new TranslationContainer("commands.whitelist.disabled"));

                    return true;
                case "list":
                    String result = "";
                    int count = 0;
                    for (String player : ServerInfo.whitelist) {
                        result += player + ", ";
                        ++count;
                    }
                    sender.sendMessage(new TranslationContainer("commands.whitelist.list", new String[]{String.valueOf(count), String.valueOf(count)}));
                    sender.sendMessage(result.length() > 0 ? result.substring(0, result.length() - 2) : "");

                    return true;

                case "add":
                    sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.whitelist.add.usage"));
                    return true;

                case "remove":
                    sender.sendMessage(new TranslationContainer("commands.generic.usage", "%commands.whitelist.remove.usage"));
                    return true;
            }
        } else if (args.length == 2) {
            if (this.badPerm(sender, args[0].toLowerCase())) {
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "add":
                    sender.getServer().getOfflinePlayer(args[1]).setWhitelisted(true);
                    Command.broadcastCommandMessage(sender, new TranslationContainer("commands.whitelist.add.success", args[1]));

                    return true;
                case "remove":
                    sender.getServer().getOfflinePlayer(args[1]).setWhitelisted(false);
                    Command.broadcastCommandMessage(sender, new TranslationContainer("commands.whitelist.remove.success", args[1]));

                    return true;
            }
        }

        return true;
    }

    private boolean badPerm(CommandSender sender, String perm) {
        if (!sender.hasPermission("nukkit.command.whitelist" + perm)) {
            sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.permission"));

            return true;
        }

        return false;
    }
}
