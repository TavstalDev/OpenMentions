package io.github.tavstaldev.openMentions.tasks;

import io.github.tavstaldev.openMentions.managers.PlayerCacheManager;
import org.bukkit.scheduler.BukkitRunnable;

public class CacheCleanTask extends BukkitRunnable {
    @Override
    public void run() {
        if (PlayerCacheManager.isMarkedForRemovalEmpty())
            return;

        for (var playerId : PlayerCacheManager.getMarkedForRemovalSet()) {
            if (PlayerCacheManager.isOnCooldown(playerId))
                continue;

            PlayerCacheManager.removeCooldown(playerId);
            PlayerCacheManager.unmarkForRemoval(playerId);
        }
    }
}
