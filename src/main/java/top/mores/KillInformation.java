package top.mores;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class KillInformation extends JavaPlugin {

    static FileConfiguration config;
    static int mcVersion;

    @Override
    public void onEnable() {
        //加载配置文件
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            boolean isCreateDir = configFile.getParentFile().mkdirs();
            //添加一个文件夹创建判断
            if (!isCreateDir) {
                getLogger().warning("Failed to create config.yml!");
                return;
            }
            saveResource("config.yml", false);
        }
        //获取版本
        mcVersion=Integer.parseInt(getServer().getBukkitVersion().replace('-', '.').split("\\.")[1]);
        //初始化配置文件
        config = getConfig();
        KillListener killListener;
        try {
            NMS nmsUtil = new NMS(this);
            Message message = new Message(nmsUtil);
            killListener = new KillListener(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //注册监听器
        getServer().getPluginManager().registerEvents(killListener, this);
        getLogger().info("KillMessage Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }
}
