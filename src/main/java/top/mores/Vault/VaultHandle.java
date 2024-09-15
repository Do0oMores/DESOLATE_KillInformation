package top.mores.Vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import top.mores.Utils.ConfigInformation;

public class VaultHandle {
    ConfigInformation config = new ConfigInformation();

    private static Economy economy;

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public boolean removePlayerVault(Player player) {
        if (economy == null) return false;
        EconomyResponse resp = economy.withdrawPlayer(player, config.getKillTrackValue());
        return resp.transactionSuccess();
    }
}
