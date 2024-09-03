package top.mores;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.mores.PlayerListener.KillListener;
import top.mores.PluginCommand.InformationCommand;
import top.mores.Record.KillRecord;
import top.mores.Utils.NMS;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class KillInformation extends JavaPlugin {

    public static FileConfiguration config;
    static int mcVersion;
    private File configFile;
    private static KillInformation instance;
    private File dataFile;
    private FileConfiguration dataConfig;

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

        //加载data.yml
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Failed to create data.yml!");
            }
        }
        reloadDataFile();
        //获取版本
        mcVersion = Integer.parseInt(getServer().getBukkitVersion().replace('-', '.').split("\\.")[1]);
        //初始化配置文件
        config = getConfig();
        KillListener killListener;
        try {
            NMS nmsUtil = new NMS(this);
            KillRecord killRecord=new KillRecord();
            Message message = new Message(nmsUtil);
            killListener = new KillListener(message,killRecord);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //注册监听器
        getServer().getPluginManager().registerEvents(killListener, this);

        //注册命令
        Objects.requireNonNull(getCommand("kf")).setExecutor(new InformationCommand());
        getLogger().info("KillMessage Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public static KillInformation getInstance() {
        return instance;
    }

    // 保存 data.yml
    public void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("保存数据文件出错！");
        }
    }

    // 重载 data.yml
    public void reloadDataFile() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // 获取 data.yml
    public FileConfiguration getDataConfig() {
        return dataConfig;
    }
}
