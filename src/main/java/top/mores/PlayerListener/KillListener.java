package top.mores.PlayerListener;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import top.mores.KillInformation;
import top.mores.Message;
import top.mores.Record.KillRecord;
import top.mores.Record.KillTrack;
import top.mores.Utils.ConfigInformation;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class KillListener implements Listener {
    private final Message message;
    private final KillRecord killRecord;
    private final HashMap<UUID, KillStreak> killStreaks = new HashMap<>();
    private final KillTrack killTrack;
    private final ConfigInformation configInformation;

    public KillListener(Message message,
                        KillRecord killRecord,
                        KillTrack killTrack,
                        ConfigInformation configInformation) {
        this.message = message;
        this.killRecord = killRecord;
        this.killTrack = new KillTrack();
        this.configInformation = new ConfigInformation();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        UUID victimUUID = victim.getUniqueId();

        // 重置被击杀玩家的连杀记录
        killStreaks.remove(victimUUID);

        Player killer = victim.getKiller();
        if (killer != null) {
            // 更新击杀追踪数据
            killTrack.addKillAmount(killer);

            // 更新连杀记录
            KillStreak streak = updateKillStreak(killer);

            // 获取击杀者手中的武器
            ItemStack weapon = killer.getInventory().getItemInMainHand();

            // 更新玩家数据
            killRecord.updatePlayerData(killer, weapon);

            executeKillCommands(killer, victim, weapon);

            if (configInformation.getEnableKillTips()) {
                // 发送击杀消息
                message.SendNormalMessages(weapon, victim, killer);
                // 播放音效
                killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                // 发送动作栏消息
                message.sendActionbar(victim, killer);
            }
            // 检查是否为连杀
            if (streak.getKillCount() >= 2) {
                handleKillStreak(killer, victim, weapon, streak);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        killRecord.initPlayerData(player);
    }

    /**
     * 更新连杀记录
     *
     * @param killer 击杀者
     * @return 连杀记录
     */
    private KillStreak updateKillStreak(Player killer) {
        UUID killerUUID = killer.getUniqueId();
        long currentTime = System.currentTimeMillis();

        KillStreak streak = killStreaks.getOrDefault(killerUUID, new KillStreak(0, currentTime));
        long streakTimeout = configInformation.getKillTick() * 1000L;

        if (currentTime - streak.getLastKillTime() <= streakTimeout) {
            streak.incrementKills();
        } else {
            streak.resetKills();
        }

        streak.setLastKillTime(currentTime);
        killStreaks.put(killerUUID, streak);

        return streak;
    }

    /**
     * 处理连杀事件
     *
     * @param killer 击杀者
     * @param victim 被击杀者
     * @param weapon 武器
     * @param streak 连杀记录
     */
    private void handleKillStreak(Player killer, Player victim, ItemStack weapon, KillStreak streak) {
        // 发送连杀提示给同世界的所有玩家
        sendKillStreakMessage(killer, streak.getKillCount());

        // 执行连杀命令
        executeKillStreakCommands(killer, victim, weapon, streak.getKillCount());
    }

    /**
     * 发送连杀提示消息
     *
     * @param killer     击杀者
     * @param killStreak 连杀次数
     */
    private void sendKillStreakMessage(Player killer, int killStreak) {
        String killStreakMessage = configInformation.getKillStreakMessage()
                .replace("%killer%", killer.getName())
                .replace("%kill_streak%", String.valueOf(killStreak));

        // 解析颜色代码
        killStreakMessage = ChatColor.translateAlternateColorCodes('&', killStreakMessage);

        for (Player worldPlayer : killer.getWorld().getPlayers()) {
            worldPlayer.sendMessage(killStreakMessage);
        }
    }

    /**
     * 执行击杀命令
     *
     * @param killer 击杀者
     * @param victim 被击杀者
     * @param weapon 武器
     */
    private void executeKillCommands(Player killer, Player victim, ItemStack weapon) {
        List<String> killCommands = configInformation.getFormattedKillCommands(killer, victim, weapon);
        for (String command : killCommands) {
            KillInformation.getInstance().getServer().dispatchCommand(
                    KillInformation.getInstance().getServer().getConsoleSender(),
                    command
            );
        }
    }

    /**
     * 执行连杀命令
     *
     * @param killer     击杀者
     * @param victim     被击杀者
     * @param weapon     武器
     * @param killStreak 连杀次数
     */
    private void executeKillStreakCommands(Player killer, Player victim, ItemStack weapon, int killStreak) {
        List<String> killStreakCommands = configInformation.getFormattedKillStreakCommands(killer, victim, weapon, killStreak);
        for (String command : killStreakCommands) {
            KillInformation.getInstance().getServer().dispatchCommand(
                    KillInformation.getInstance().getServer().getConsoleSender(),
                    command
            );
        }
    }
}