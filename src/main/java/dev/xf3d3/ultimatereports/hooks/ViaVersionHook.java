package dev.xf3d3.ultimatereports.hooks;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import lombok.Getter;
import org.bukkit.entity.Player;

public class ViaVersionHook {
    @Getter
    private final ViaAPI viaAPI;

    public ViaVersionHook() {
        this.viaAPI = Via.getAPI();
    }

    public int getPlayerVersion(Player player) {
        return viaAPI.getPlayerVersion(player.getUniqueId());
    }
}
