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
import top.mores.Utils.ConfigInformation;
import top.mores.Utils.NMS;

public class Message {

    private final NMS nmsUtil;
    ConfigInformation configInformation = new ConfigInformation();

    public Message(NMS nmsUtil) {
        this.nmsUtil = nmsUtil;
    }

    /**
     * @param item   击杀者手上的物品
     * @param player 被击杀者
     * @param killer 击杀者
     **/
    public void SendNormalMessages(ItemStack item, Player player, Player killer) {
        if (item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        String messageTemplate = configInformation.getKillMessage();
        
        // 解析颜色代码
        messageTemplate = ChatColor.translateAlternateColorCodes('&', messageTemplate);

        String playerName = player.getName();
        String killerName = killer.getName();

        String itemNBT = nmsUtil.getItemNBT(item);
        TextComponent itemInfo = buildItemInfoComponent(item, meta, itemNBT);
        String message = messageTemplate
                .replace("%killer%", killerName)
                .replace("%player%", playerName);
        ComponentBuilder finalMessageBuilder = new ComponentBuilder();
        String[] parts = message.split("%weapon%");
        finalMessageBuilder.append(parts[0]);
        finalMessageBuilder.append(itemInfo);
        if (parts.length > 1) {
            finalMessageBuilder.append(parts[1]);
        }
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(finalMessageBuilder.create());
        BaseComponent[] finalMessage = builder.create();

        boolean onlyWorld = configInformation.getONLY_SAME_WORLD();
        Player[] targetPlayers = onlyWorld ? killer.getWorld().getPlayers().toArray(new Player[0]) : Bukkit.getOnlinePlayers().toArray(new Player[0]);

        for (Player targetPlayer : targetPlayers) {
            targetPlayer.spigot().sendMessage(finalMessage);
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

    public void sendActionbar(Player player, Player killer) {
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
        actionbar.setBold(Boolean.TRUE);
        actionbar.setItalic(Boolean.TRUE);
        actionbar.setUnderlined(Boolean.TRUE);
        actionbar.setColor(color);
        return actionbar;
    }
}