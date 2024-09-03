package top.mores.PluginCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mores.KillInformation;
import top.mores.Record.KillTrack;
import top.mores.Utils.ConfigInformation;

import java.util.List;

public class InformationCommand implements CommandExecutor {
    ConfigInformation configInformation = new ConfigInformation();
    KillTrack killTrack = new KillTrack();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (strings.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "未知的命令");
            return true;
        }
        String subCommand = strings[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!commandSender.hasPermission("op")) {
                    commandSender.sendMessage("你没有权限执行该命令");
                    return true;
                }
                KillInformation.getInstance().reloadConfig();
                KillInformation.getInstance().reloadDataFile();
                commandSender.sendMessage(ChatColor.GREEN + "所有配置文件已重新加载");
                break;

            case "cx":
                if (commandSender instanceof Player player) {
                    List<String> messages = configInformation.BuildMessage(player);
                    messages.forEach(player::sendMessage);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "该命令只能由玩家执行");
                }
                break;

            case "killtrack":
                if (commandSender instanceof Player player) {
                    killTrack.initItemLore(player);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "该命令只能由玩家执行");
                }
                break;

            default:
                commandSender.sendMessage(ChatColor.RED + "未知的命令");
                break;
        }
        return true;
    }
}
