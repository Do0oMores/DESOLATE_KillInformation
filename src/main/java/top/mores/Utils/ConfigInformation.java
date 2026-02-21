package top.mores.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mores.KillInformation;

import java.util.*;

public class ConfigInformation {

    private FileConfiguration getConfig() {
        return KillInformation.getInstance().getConfigFile();
    }

    public boolean getONLY_SAME_WORLD() {
        return getConfig().getBoolean("send_to_same_world_only");
    }

    public List<String> getPlayerItemKillData(Player player) {
        FileConfiguration dataFile = KillInformation.getInstance().getDataConfig();
        String playerName = player.getName();
        List<String> playerData = new ArrayList<>();
        // 检查玩家的数据条目是否存在
        if (dataFile.contains(playerName)) {
            // 获取玩家键下的所有键值
            Set<String> keys = Objects.requireNonNull(dataFile.getConfigurationSection(playerName)).getKeys(false);
            //只返回前五条数据
            int count = 0;
            for (String key : keys) {
                if (count >= 5) break;
                Object value = dataFile.get(playerName + "." + key);
                playerData.add(key + ": " + value);
                count++;
            }
        } else {
            playerData.add(String.format("没有关于 %s 的数据", playerName));
        }
        return playerData;
    }

    public List<String> BuildMessage(Player player) {
        List<String> ListMessage = new ArrayList<>();
        String playerName = player.getName();
        ListMessage.add(ChatColor.DARK_PURPLE + "玩家ID: " + ChatColor.GOLD + playerName);
        //获取玩家数据
        List<String> playerData = getPlayerItemKillData(player);
        // 将玩家数据添加到消息列表中
        for (String data : playerData) {
            ListMessage.add(ChatColor.GRAY + data);
        }
        return ListMessage;
    }

    public int getKillTrackValue() {
        return getConfig().getInt("kill_track_cost");
    }

    public int getKillTick() {
        return getConfig().getInt("kill_streak_interval");
    }

    public String getLoreMessage() {
        return getConfig().getString("lore_text", "已击杀：%kill_stat%");
    }

    public List<String> getKillMessageList() {
        return getConfig().getStringList("kill_messages");
    }

    public String getAddKillMessage() {
        return getConfig().getString("kill_count_increase_message");
    }

    public String getKillMessage() {
        List<String> messageList = getKillMessageList();
        Collections.shuffle(messageList);
        return messageList.get(0);
    }

    public String getKillStreakMessage() {
        String message = getConfig().getString("kill_streak_message");
        if (message == null || message.isEmpty()) {
            // 默认连杀提示消息
            return "&7[&4连杀提示&7] &6%killer% &a已经连续造成 &4%kill_streak% &a次杀戮!";
        }
        return message;
    }

    public List<String> getKillCommands() {
        return getConfig().getStringList("kill_commands");
    }

    // 新增方法：获取格式化后的击杀命令列表
    public List<String> getFormattedKillCommands(Player killer, Player victim, ItemStack weapon) {
        List<String> commands = getKillCommands();
        List<String> formattedCommands = new ArrayList<>();

        for (String command : commands) {
            // 为击杀命令添加占位符替换
            String formattedCommand = PlaceholderUtil.replacePlaceholders(command, killer, victim, weapon, null);
            formattedCommands.add(formattedCommand);
        }

        return formattedCommands;
    }

    public List<String> getKillStreakCommands(int killStreak) {
        // 获取最接近的配置项
        int closestStreak = 0;
        for (String key : Objects.requireNonNull(getConfig().getConfigurationSection("kill_streak_commands")).getKeys(false)) {
            int streak = Integer.parseInt(key);
            if (streak <= killStreak && streak > closestStreak) {
                closestStreak = streak;
            }
        }

        if (closestStreak > 0) {
            return getConfig().getStringList("kill_streak_commands." + closestStreak);
        }

        return new ArrayList<>();
    }

    // 获取格式化后的连杀命令列表
    public List<String> getFormattedKillStreakCommands(Player killer, Player victim, ItemStack weapon, int killStreak) {
        List<String> commands = getKillStreakCommands(killStreak);
        List<String> formattedCommands = new ArrayList<>();

        for (String command : commands) {
            // 为连杀命令添加占位符替换
            String formattedCommand = PlaceholderUtil.replacePlaceholders(command, killer, victim, weapon, killStreak);
            formattedCommands.add(formattedCommand);
        }

        return formattedCommands;
    }

    public boolean getEnableKillTips() {
        return getConfig().getBoolean("enable_kill_tips");
    }

    public String getItemStatTrackName() {
        return getConfig().getString("item_statTrack_name");
    }

    public String getStatTrackRegTip() {
        return getConfig().getString("stattrack_reg_tip");
    }

    public String getReRegStatTrackTip() {
        return getConfig().getString("re_reg_stattrack_tip");
    }

    public String getNotRegItemTip() {
        return getConfig().getString("not_reg_item_tip");
    }

    public String getItemRegedTip() {
        return getConfig().getString("item_reged_tip");
    }

    public String getVaultNotEnoughTip() {
        return getConfig().getString("vault_insufficient_tip");
    }

    public String getStatTrackSuccessTip() {
        return getConfig().getString("stattrack_success_tip");
    }
}