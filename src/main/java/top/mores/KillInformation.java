package top.mores;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class KillInformation extends JavaPlugin {

    public static FileConfiguration config;
    static int mcVersion;
    private File configFile;
    private static KillInformation instance;

    @Override
    public void onEnable() {
        instance = this;
        //加载配置文件
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            boolean isCreateDir = configFile.getParentFile().mkdirs();
            //添加一个文件夹创建判断
            if (!isCreateDir) {
                getLogger().warning("Failed to create config.yml!");
                return;
            }
            saveResource("config.yml", false);
        }
        reloadConfig();
        //获取版本
        mcVersion = Integer.parseInt(getServer().getBukkitVersion().replace('-', '.').split("\\.")[1]);
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

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    //保存配置文件
    public void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            System.out.println("保存配置文件出错");
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public static KillInformation getInstance() {
        return instance;
    }
}
