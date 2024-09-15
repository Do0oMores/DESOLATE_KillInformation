package top.mores.Record;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.Vault.VaultHandle;

import java.util.ArrayList;
import java.util.List;

public class KillTrack {

    VaultHandle vaultHandle = new VaultHandle();

    public void initItemLore(Player player) {
        ItemStack itemStack = player.getInventory().getItemInOffHand();
        if (itemStack.getType().equals(Material.AIR)) {
            player.sendMessage("请将需要注册击杀追踪的物品放到副手");
            return;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            player.sendMessage("该物品没有检测到任何标签内容，是否确定注册？请再次输入指令进行注册！");
            return;
        }
        if (!meta.hasDisplayName()) {
            player.sendMessage("该物品未在服务器内注册！");
            return;
        }
        List<String> itemLore = getStrings(meta);
        if (itemLore == null) {
            player.sendMessage("该物品已注册击杀记录！");
            return;
        }
        if (vaultHandle.removePlayerVault(player)) {
            String itemName = meta.getDisplayName();
            if (!itemName.endsWith("(StatTrack)")) {
                meta.setDisplayName(itemName + "(StatTrack)");
            }
            meta.setLore(itemLore);
            itemStack.setItemMeta(meta);
            player.getInventory().setItemInOffHand(itemStack);
            player.sendMessage(ChatColor.GREEN + "击杀记录已更新并替换手中的物品！");
        } else {
            player.sendMessage(ChatColor.RED + "没有足够的金币");
        }
    }

    private static List<String> getStrings(ItemMeta meta) {
        List<String> itemLore = meta.getLore();
        if (itemLore == null) {
            itemLore = new ArrayList<>();
            itemLore.add("已击杀：0");
        } else {
            for (String lore : itemLore) {
                if (lore.startsWith("已击杀")) {
                    return null;
                }
            }
            boolean foundLore = false;
            for (int i = 0; i < itemLore.size(); i++) {
                String lore = itemLore.get(i);
                if (lore.equals("lore")) {
                    itemLore.set(i, "已击杀：0");
                    foundLore = true;
                    break;
                }
            }
            if (!foundLore) {
                itemLore.add("已击杀：0");
            }
        }
        return itemLore;
    }

    public void addKillAmount(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().equals(Material.AIR)) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return;
        }

        List<String> itemLore = meta.getLore();
        if (itemLore == null) {
            return;
        }

        for (int i = 0; i < itemLore.size(); i++) {
            String lore = itemLore.get(i);
            if (lore.startsWith("已击杀：")) {
                try {
                    int killCount = Integer.parseInt(lore.substring(4));
                    killCount++;
                    // 更新 lore
                    itemLore.set(i, "已击杀：" + killCount);
                    meta.setLore(itemLore);
                    itemStack.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(itemStack);
                    return;
                } catch (NumberFormatException e) {
                    player.sendMessage("无法解析击杀数，请联系管理员");
                    return;
                }
            }
        }
        player.sendMessage("该物品没有注册击杀记录");
    }
}
