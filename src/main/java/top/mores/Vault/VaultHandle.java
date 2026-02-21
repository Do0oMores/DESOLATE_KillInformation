package top.mores.Vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import top.mores.Utils.ConfigInformation;

public class VaultHandle {
    private final ConfigInformation config = new ConfigInformation();
    private static Economy economy;

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public boolean isReady() {
        return economy != null;
    }

    public double getCost() {
        return config.getKillTrackValue();
    }

    public boolean hasEnough(Player player) {
        return isReady() && economy.has(player, getCost());
    }

    public boolean removePlayerVault(Player player) {
        if (!isReady()) return false;
        EconomyResponse resp = economy.withdrawPlayer(player, getCost());
        return resp.transactionSuccess();
    }
}