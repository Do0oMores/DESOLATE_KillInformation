package top.mores.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.mores.KillInformation;

import java.util.*;

public class ConfigInformation {

    Configuration config = KillInformation.getInstance().getConfig();

    public boolean getONLY_SAME_WORLD() {
        return config.getBoolean("只给相同世界的玩家发送信息");
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

    public int getKillTrackValue(){
        return config.getInt("击杀记录");
    }

    public int getKillTick(){
        return config.getInt("连杀间隔");
    }

    public String getLoreMessage(){
        return config.getString("lore文本");
    }

    public List<String> getKillMessageList(){
        return config.getStringList("击杀提示信息");
    }

    public String getAddKillMessage(){
        return config.getString("击杀数增加提示");
    }

    public String getKillMessage(){
        List<String> messageList=getKillMessageList();
        Collections.shuffle(messageList);
        return messageList.get(0);
    }
}