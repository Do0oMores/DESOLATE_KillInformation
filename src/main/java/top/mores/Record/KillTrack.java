package top.mores.Record;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import top.mores.KillInformation;
import top.mores.Utils.ChatColorUtil;
import top.mores.Utils.ConfigInformation;
import top.mores.Vault.VaultHandle;

import java.util.ArrayList;
import java.util.List;

public class KillTrack {

    private final KillInformation plugin;
    private final VaultHandle vaultHandle;
    private final ConfigInformation configInformation;

    private final NamespacedKey KILL_KEY;

    public KillTrack(KillInformation plugin) {
        this(plugin, new VaultHandle(), new ConfigInformation());
    }

    public KillTrack(KillInformation plugin, VaultHandle vaultHandle, ConfigInformation configInformation) {
        this.plugin = plugin;
        this.vaultHandle = vaultHandle;
        this.configInformation = configInformation;

        this.KILL_KEY = new NamespacedKey(plugin, "kill_stat");
    }

    /** 注册：写 NBT + 写/替换 Lore 展示行（不可重复注册） */
    public void initItemLore(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // 1) 主手检查
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColorUtil.color(configInformation.getStatTrackRegTip()));
            return;
        }

        // 2) meta 检查
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColorUtil.color(configInformation.getReRegStatTrackTip()));
            return;
        }

        // 3)必须有 displayName
        if (!meta.hasDisplayName()) {
            player.sendMessage(ChatColorUtil.color(configInformation.getNotRegItemTip()));
            return;
        }

        // 4) 模板检查
        String loreTemplateRaw=getLoreTemplateRaw();
        if (loreTemplateRaw == null || loreTemplateRaw.isBlank() || !loreTemplateRaw.contains("%kill_stat%")) {
            player.sendMessage(ChatColorUtil.color(configInformation.getErrorTempleTip()));
            plugin.getLogger().warning("Config error: lore_text is blank or missing %kill_stat%.");
            return;
        }

        // 5) Vault 就绪检查
        if (!vaultHandle.isReady()) {
            player.sendMessage(ChatColorUtil.color(configInformation.getErrorVaultReadyTip()));
            return;
        }

        // 6) 已注册检查
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(KILL_KEY, PersistentDataType.INTEGER)) {
            player.sendMessage(ChatColorUtil.color(configInformation.getItemRegedTip()));
            return;
        }

        // 7) 扣费前先“预构造”要写入的内容，确保不会因为 lore/null 出错
        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        if (lore == null) lore = new ArrayList<>();
        setOrAppendKillLoreLine(lore, 0,loreTemplateRaw);

        String newName = meta.getDisplayName();
        if (newName == null) newName = "";
        if (!newName.endsWith(configInformation.getItemStatTrackName())) {
            newName = newName + configInformation.getItemStatTrackName();
        }

        // 8) 余额不足提示
        if (!vaultHandle.hasEnough(player)) {
            player.sendMessage(ChatColorUtil.color(configInformation.getVaultNotEnoughTip()));
            return;
        }

        // 9) 扣费
        if (!vaultHandle.removePlayerVault(player)) {
            //可能是余额不足 / 经济插件拒绝交易
            player.sendMessage(ChatColorUtil.color(configInformation.getVaultNotEnoughTip()));
            return;
        }

        // 10) 落盘：写 NBT + 写 Lore/Name（尽量不失败）
        try {
            pdc.set(KILL_KEY, PersistentDataType.INTEGER, 0);
            meta.setDisplayName(newName);
            meta.setLore(lore);

            item.setItemMeta(meta);
            player.getInventory().setItemInMainHand(item);

            player.sendMessage(ChatColorUtil.color(configInformation.getStatTrackSuccessTip()));
        } catch (Exception ex) {
            plugin.getLogger().severe("KillTrack register failed AFTER withdraw. Player=" + player.getName());
            ex.printStackTrace();
            player.sendMessage(ChatColorUtil.color("&c注册失败：可能已扣费，请联系管理员补偿。"));
        }
    }

    /** 击杀 +1：只读写 NBT，然后刷新 Lore 展示行 */
    public void addKillAmount(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String loreTemplateRaw=getLoreTemplateRaw();
        if (loreTemplateRaw == null || loreTemplateRaw.isBlank() || !loreTemplateRaw.contains("%kill_stat%")) {
            return;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer kill = pdc.get(KILL_KEY, PersistentDataType.INTEGER);
        if (kill == null) return; // 未注册追踪，忽略

        int newKill = kill + 1;

        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        if (lore == null) lore = new ArrayList<>();
        setOrAppendKillLoreLine(lore, newKill,loreTemplateRaw);

        try {
            pdc.set(KILL_KEY, PersistentDataType.INTEGER, newKill);
            meta.setLore(lore);
            item.setItemMeta(meta);
            player.getInventory().setItemInMainHand(item);
        } catch (Exception ex) {
            plugin.getLogger().warning("KillTrack addKillAmount failed: " + ex.getMessage());
        }
    }

    /** 替换/追加 kill lore 行；并清理重复统计行 */
    private void setOrAppendKillLoreLine(List<String> lore, int kill,String loreTemplateRaw) {
        String newLine = buildLoreColored(kill,loreTemplateRaw);
        String prefix = getTemplatePrefixColored(loreTemplateRaw);

        int foundIndex = -1;

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line == null) continue;

            if (line.equalsIgnoreCase("lore") || line.startsWith(prefix)) {
                foundIndex = i;
                lore.set(i, newLine);
                break;
            }
        }

        if (foundIndex == -1) {
            lore.add(newLine);
            foundIndex = lore.size() - 1;
        }

        // 清理其它重复行
        for (int i = lore.size() - 1; i >= 0; i--) {
            if (i == foundIndex) continue;
            String line = lore.get(i);
            if (line != null && line.startsWith(prefix)) {
                lore.remove(i);
            }
        }
    }

    private String buildLoreColored(int kill, String loreTemplateRaw) {
        return ChatColorUtil.color(loreTemplateRaw.replace("%kill_stat%", String.valueOf(kill)));
    }

    private String getTemplatePrefixColored(String loreTemplateRaw) {
        return ChatColorUtil.color(loreTemplateRaw.replace("%kill_stat%", ""));
    }

    private String getLoreTemplateRaw() {
        return configInformation.getLoreMessage();
    }
}
