package top.mores;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Message {
    private final NMS nmsUtil;

    public Message(NMS nmsUtil) {
        this.nmsUtil = nmsUtil;
    }

    /**
     * @param item   击杀者手上的物品
     * @param player 被击杀者
     * @param killer 击杀者
     */
    public void SendNormalMessages(ItemStack item, Player player, Player killer) {
        // 判断空手
        if (item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        String itemNBT = nmsUtil.getItemNBT(item);
        ComponentBuilder builder = new ComponentBuilder();

        // 构建消息文本
        String playerName = player.getName();
        String killerName = killer.getName();

        TextComponent killerText = new TextComponent(killerName);
        killerText.setColor(ChatColor.GOLD);
        killerText.setBold(true);
        killerText.setItalic(true);
        builder.append(killerText);

        TextComponent forText = new TextComponent("使用");
        forText.setColor(ChatColor.WHITE);
        builder.append(forText);

        // 构建物品信息
        TextComponent itemInfo = buildItemInfoComponent(item, meta, itemNBT);
        builder.append(itemInfo);

        TextComponent andText = new TextComponent("击杀了");
        andText.setColor(ChatColor.WHITE);
        builder.append(andText);

        TextComponent playerText = new TextComponent(playerName);
        playerText.setColor(ChatColor.DARK_RED);
        playerText.setItalic(true);
        playerText.setStrikethrough(true);
        builder.append(playerText);

        BaseComponent[] message = builder.create();

        // 判断世界
        boolean onlyWorld = KillInformation.config.getBoolean("只给相同世界的玩家发送信息");
        Player[] targetPlayers = onlyWorld ? killer.getWorld().getPlayers().toArray(new Player[0]) : Bukkit.getOnlinePlayers().toArray(new Player[0]);
        for (Player targetPlayer : targetPlayers) {
            targetPlayer.spigot().sendMessage(message);
        }
    }

    private TextComponent buildItemInfoComponent(ItemStack item, ItemMeta meta, String itemNBT) {
        TextComponent itemInfo = new TextComponent("[");

        // 是否有显示名称
        if (meta.hasDisplayName()) {
            for (BaseComponent component : TextComponent.fromLegacyText(meta.getDisplayName())) {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemNBT)}));
                itemInfo.addExtra(component);
            }
        } else {
            // 获取翻译键
            boolean tag = true;
            if (tag) {
                itemInfo.setColor(itemNBT.contains("Enchantments:[") ? ChatColor.AQUA : ChatColor.WHITE);
            } else {
                itemInfo.setColor(item.getType().isRecord() || itemNBT.contains("ench:[") ? ChatColor.AQUA : ChatColor.WHITE);
            }

            String key = KillInformation.mcVersion == 12 ? nmsUtil.getTranslateKey(item) : getTranslateKey(item.getType().getKey().toString(), item.getType().isBlock());
            TranslatableComponent keyTranslate = new TranslatableComponent(key);
            keyTranslate.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemNBT)}));
            itemInfo.addExtra(keyTranslate);
        }

        itemInfo.addExtra("]");
        return itemInfo;
    }


    private static String getTranslateKey(String id, boolean isBlock) {
        return (isBlock ? "block." : "item.") + id.replace(':', '.');
    }

    protected void sendActionbar(Player player, Player killer) {
        String playerName = player.getName();
        if (checkPlayerTeam(killer) == null && checkPlayerTeam(player) == null) {
            killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, BuildActionbar(playerName, ChatColor.LIGHT_PURPLE));
        } else {
            String teamName = checkPlayerTeam(player);
            assert teamName != null;
            if (teamName.equals("red")) {
                killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, BuildActionbar(playerName, ChatColor.DARK_RED));
            } else if (teamName.equals("blue")) {
                killer.spigot().sendMessage(ChatMessageType.ACTION_BAR, BuildActionbar(playerName, ChatColor.DARK_BLUE));
            }
        }
    }

    private String checkPlayerTeam(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getEntryTeam(player.getName());
        if (team != null) {
            return team.getDisplayName();
        } else {
            return null;
        }
    }

    private TextComponent BuildActionbar(String playerName, ChatColor color) {
        TextComponent actionbar = new TextComponent(playerName);
        actionbar.setBold(true);
        actionbar.setItalic(true);
        actionbar.setUnderlined(true);
        actionbar.setColor(color);
        return actionbar;
    }
}
