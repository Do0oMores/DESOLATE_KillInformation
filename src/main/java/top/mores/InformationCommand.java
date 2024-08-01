package top.mores;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InformationCommand implements CommandExecutor {
    ConfigInformation configInformation=new ConfigInformation();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (strings.length == 1 && strings[0].equalsIgnoreCase("reload")) {
            if (!commandSender.hasPermission("op")) {
                commandSender.sendMessage("你没有权限执行该命令");
                return true;
            }
            KillInformation.getInstance().reloadConfig();
            KillInformation.getInstance().reloadDataFile();
            commandSender.sendMessage(ChatColor.GREEN + "所有配置文件已重新加载");
        } else if (strings.length == 1 && strings[0].equalsIgnoreCase("cx")) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                List<String> messages = configInformation.BuildMessage(player);
                for (String message : messages) {
                    player.sendMessage(message);
                }
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "未知的命令");
            return true;
        }
        return true;
    }
}
