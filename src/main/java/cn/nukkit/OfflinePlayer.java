package cn.nukkit;

import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

/**
 * 描述一个不在线的玩家的类。<br>
 * Describes an offline player.
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see cn.nukkit.Player
 * @since Nukkit 1.0 | Nukkit API 1.0.0
 */
public class OfflinePlayer implements IPlayer {
    private String name;
    private Server server;
    private CompoundTag namedTag;

    /**
     * 初始化这个{@code OfflinePlayer}对象。<br>
     * Initializes the object {@code OfflinePlayer}.
     *
     * @param server 这个玩家所在服务器的{@code Server}对象。<br>
     *               The server this player is in, as a {@code Server} object.
     * @param name   这个玩家所的名字。<br>
     *               Name of this player.
     * @since Nukkit 1.0 | Nukkit API 1.0.0
     */
    public OfflinePlayer(Server server, String name) {
        this.server = server;
        this.name = name;

        if (new File(this.server.getDataPath() + "players/" + name.toLowerCase() + ".dat").exists()) {
            this.namedTag = this.server.getOfflinePlayerData(this.name);
        } else {
            this.namedTag = null;
        }
    }

    @Override
    public boolean isOnline() {
        return this.getPlayer() != null;
    }

    @Override
    public String getName() {
        return name;
    }

    public Server getServer() {
        return server;
    }

    @Override
    public boolean isOp() {
        return ServerInfo.operators.contains(getName());
    }

    @Override
    public void setOp(boolean value) {
        if (value == this.isOp())
            return;
        if (value) ServerInfo.operators.add(getName());
        else ServerInfo.operators.remove(getName());
    }

    @Override
    public boolean isBanned() {
        return ServerInfo.banByName.contains(this.getName());
    }

    @Override
    public void setBanned(boolean value) {
        if (value) {
            ServerInfo.banByName.add(this.getName());
        } else
            ServerInfo.banByName.remove(this.getName());
    }

    @Override
    public boolean isWhitelisted() {
        return ServerInfo.whitelist.contains(getName());
    }

    @Override
    public void setWhitelisted(boolean value) {
        if (value) ServerInfo.whitelist.add(getName());
        else ServerInfo.whitelist.remove(getName());
    }

    @Override
    public Player getPlayer() {
        return this.server.getPlayerExact(this.getName());
    }

    @Override
    public Long getFirstPlayed() {
        return this.namedTag != null ? this.namedTag.getLong("firstPlayed") : null;
    }

    @Override
    public Long getLastPlayed() {
        return this.namedTag != null ? this.namedTag.getLong("lastPlayed") : null;
    }

    @Override
    public boolean hasPlayedBefore() {
        return this.namedTag != null;
    }

    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        this.server.getPlayerMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    public List<MetadataValue> getMetadata(String metadataKey) {
        return this.server.getPlayerMetadata().getMetadata(this, metadataKey);
    }

    public boolean hasMetadata(String metadataKey) {
        return this.server.getPlayerMetadata().hasMetadata(this, metadataKey);
    }

    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        this.server.getPlayerMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

}
