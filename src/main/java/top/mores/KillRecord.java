package top.mores;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KillRecord {
    FileConfiguration dataFile = KillInformation.getInstance().getDataConfig();

    //初始化玩家数据
    public void initPlayerData(Player player) {
        if (!dataFile.contains(player.getName())) {
            dataFile.createSection(player.getName());
            KillInformation.getInstance().saveDataFile();
        }
    }

    //更新玩家数据
    public void updatePlayerData(Player player, ItemStack itemStack) {
        initPlayerData(player);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        if (itemMeta.hasDisplayName()) {
            String itemDisplayName = itemMeta.getDisplayName();
            //移除§.
            itemDisplayName = itemDisplayName.replaceAll("§.", "");
            String playerName = player.getName();
            if (!dataFile.contains(playerName + "." + itemDisplayName)) {
                dataFile.set(playerName + "." + itemDisplayName, 1);
                KillInformation.getInstance().saveDataFile();
            } else {
                int itemKill = dataFile.getInt(playerName + "." + itemDisplayName) + 1;
                dataFile.set(playerName + "." + itemDisplayName, itemKill);
                player.sendMessage(ChatColor.GOLD + "[" +
                        ChatColor.GRAY + itemDisplayName +
                        ChatColor.GOLD + "]" +
                        ChatColor.DARK_GREEN + "已击杀：" +
                        ChatColor.DARK_AQUA + itemKill);
                KillInformation.getInstance().saveDataFile();
            }
        } else {
            player.sendMessage(ChatColor.DARK_RED + "该物品未在服务器注册无法保存击杀信息");
        }
    }
}
