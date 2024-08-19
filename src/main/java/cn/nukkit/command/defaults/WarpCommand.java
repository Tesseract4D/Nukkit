package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.network.protocol.ChunkRadiusUpdatePacket;


public class WarpCommand extends VanillaCommand {
    public WarpCommand(String name) {
        super(name, "%nukkit.command.warp.description", "%commands.warp.usage");
        this.setPermission("nukkit.warp.gamemode");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player target = (Player) sender;
        target.teleportImmediate(sender.getServer().getLevel(Integer.parseInt(args[0])).getSafeSpawn().getLocation());
        return true;
    }
}
