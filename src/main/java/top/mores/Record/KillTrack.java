package top.mores.Record;

import org.bukkit.ChatColor;
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

    public void initItemLore(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColorUtil.color(configInformation.getStatTrackRegTip()));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColorUtil.color(configInformation.getReRegStatTrackTip()));
            return;
        }

        if (!meta.hasDisplayName()) {
            player.sendMessage(ChatColorUtil.color(configInformation.getNotRegItemTip()));
            return;
        }

        //模板检查
        String loreTemplateRaw=getLoreTemplateRaw();
        if (loreTemplateRaw == null || loreTemplateRaw.isBlank() || !loreTemplateRaw.contains("%kill_stat%")) {
            player.sendMessage(ChatColorUtil.color(configInformation.getErrorTempleTip()));
            plugin.getLogger().warning("Config error: lore_text is blank or missing %kill_stat%.");
            return;
        }

        //Vault检查
        if (!vaultHandle.isReady()) {
            player.sendMessage(ChatColorUtil.color(configInformation.getErrorVaultReadyTip()));
            return;
        }

        //已注册检查
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(KILL_KEY, PersistentDataType.INTEGER)) {
            player.sendMessage(ChatColorUtil.color(configInformation.getItemRegedTip()));
            return;
        }

        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        if (lore == null) lore = new ArrayList<>();
        setOrAppendKillLoreLine(lore, 0,loreTemplateRaw);

        String newName = meta.getDisplayName();
        if (newName == null) newName = "";
        if (!newName.endsWith(configInformation.getItemStatTrackName())) {
            newName = newName + configInformation.getItemStatTrackName();
        }

        //余额不足提示
        if (!vaultHandle.hasEnough(player)) {
            player.sendMessage(ChatColorUtil.color(configInformation.getVaultNotEnoughTip()));
            return;
        }

        //扣费
        if (!vaultHandle.removePlayerVault(player)) {
            player.sendMessage(ChatColorUtil.color(configInformation.getVaultNotEnoughTip()));
            return;
        }

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

    private void setOrAppendKillLoreLine(List<String> lore, int kill, String loreTemplateRaw) {
        String newLine = buildLoreColored(kill, loreTemplateRaw);
        String label = extractLabel(loreTemplateRaw);
        if (label.isBlank()) {
            lore.add(newLine);
            return;
        }
        int foundIndex = -1;
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line == null) continue;
            if (line.equalsIgnoreCase("lore")) {
                foundIndex = i;
                lore.set(i, newLine);
                break;
            }
            if (isKillLoreLine(line, label)) {
                foundIndex = i;
                lore.set(i, newLine);
                break;
            }
        }
        if (foundIndex == -1) {
            lore.add(newLine);
            foundIndex = lore.size() - 1;
        }
        for (int i = lore.size() - 1; i >= 0; i--) {
            if (i == foundIndex) continue;
            String line = lore.get(i);
            if (line != null && isKillLoreLine(line, label)) {
                lore.remove(i);
            }
        }
    }

    private String extractLabel(String loreTemplateRaw) {
        String raw = ChatColor.stripColor(ChatColorUtil.color(loreTemplateRaw));
        int idx = raw.indexOf("%kill_stat%");
        String label = (idx >= 0 ? raw.substring(0, idx) : raw);

        return normalize(label);
    }

    private boolean isKillLoreLine(String loreLine, String normalizedLabel) {
        String line = ChatColor.stripColor(loreLine);
        line = normalize(line);
        return line.startsWith(normalizedLabel);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.replace('：', ':')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String buildLoreColored(int kill, String loreTemplateRaw) {
        return ChatColorUtil.color(loreTemplateRaw.replace("%kill_stat%", String.valueOf(kill)));
    }

    private String getLoreTemplateRaw() {
        return configInformation.getLoreMessage();
    }
}
