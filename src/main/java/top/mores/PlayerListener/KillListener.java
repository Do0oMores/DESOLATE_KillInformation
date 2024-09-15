package top.mores.PlayerListener;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import top.mores.Message;
import top.mores.Record.KillRecord;
import top.mores.Record.KillTrack;

import java.util.HashMap;
import java.util.UUID;

public class KillListener implements Listener {
    private final Message message;
    private final KillRecord killRecord;
    private final HashMap<UUID, KillStreak> killStreaks = new HashMap<>();
    KillTrack killTrack = new KillTrack();

    public KillListener(Message message, KillRecord killRecord) {
        this.message = message;
        this.killRecord = killRecord;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUniqueId();
        // 重置被击杀玩家的连杀记录
        killStreaks.remove(playerUUID);
        if (player.getKiller() != null) {
            Player killer = player.getKiller();
            killTrack.addKillAmount(killer);
            UUID killerUUID = killer.getUniqueId();
            long currentTime = System.currentTimeMillis();

            KillStreak streak = killStreaks.getOrDefault(killerUUID, new KillStreak(0, currentTime));
            long STREAK_TIMEOUT = 30000;
            if (currentTime - streak.getLastKillTime() <= STREAK_TIMEOUT) {
                streak.incrementKills();

            } else {
                streak.resetKills();
            }

            streak.setLastKillTime(currentTime);
            killStreaks.put(killerUUID, streak);
            //获取物品
            ItemStack itemStack = killer.getInventory().getItemInMainHand();
            killRecord.updatePlayerData(killer, itemStack);
            message.SendNormalMessages(itemStack, player, killer);
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
            message.sendActionbar(player, killer);

            if (streak.getKillCount() >= 2) {
                // 发送连杀提示给同世界的所有玩家
                String killStreakMessage = ChatColor.GRAY + "【 " + ChatColor.DARK_RED + "连杀提示" + ChatColor.GRAY + " 】" +
                        ChatColor.GOLD + killer.getName() +
                        ChatColor.GREEN + "已经连续造成  " +
                        ChatColor.DARK_RED + streak.getKillCount() +
                        ChatColor.GREEN + "  次杀戮！";
                for (Player worldPlayer : killer.getWorld().getPlayers()) {
                    worldPlayer.sendMessage(killStreakMessage);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        killRecord.initPlayerData(player);
    }

    private static class KillStreak {
        private int killCount;
        private long lastKillTime;

        public KillStreak(int killCount, long lastKillTime) {
            this.killCount = killCount;
            this.lastKillTime = lastKillTime;
        }

        public int getKillCount() {
            return killCount;
        }

        public void incrementKills() {
            killCount++;
        }

        public void resetKills() {
            killCount = 1;
        }

        public long getLastKillTime() {
            return lastKillTime;
        }

        public void setLastKillTime(long lastKillTime) {
            this.lastKillTime = lastKillTime;
        }
    }
}