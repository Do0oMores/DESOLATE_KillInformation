package top.mores.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlaceholderUtil {

    /**
     * 替换字符串中的占位符
     *
     * @param text       包含占位符的文本
     * @param killer     击杀者
     * @param victim     被击杀者
     * @param weapon     武器
     * @param killStreak 连杀次数（可选）
     * @return 替换后的文本
     */
    public static String replacePlaceholders(String text, Player killer, Player victim, ItemStack weapon, Integer killStreak) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String weaponName = getWeaponName(weapon);
        String result = text
                .replace("%killer%", killer != null ? killer.getName() : "Unknown")
                .replace("%killer_uuid%", killer != null ? killer.getUniqueId().toString() : "Unknown")
                .replace("%player%", victim != null ? victim.getName() : "Unknown")
                .replace("%player_uuid%", victim != null ? victim.getUniqueId().toString() : "Unknown")
                .replace("%weapon%", weaponName);

        if (killStreak != null) {
            result = result.replace("%kill_streak%", String.valueOf(killStreak));
        }

        return result;
    }

    /**
     * 获取武器名称
     *
     * @param weapon 武器
     * @return 武器名称
     */
    private static String getWeaponName(ItemStack weapon) {
        if (weapon == null || weapon.getItemMeta() == null) {
            return "Unknown Weapon";
        }

        ItemMeta meta = weapon.getItemMeta();
        if (meta.hasDisplayName()) {
            return meta.getDisplayName();
        }

        return weapon.getType().name();
    }
}