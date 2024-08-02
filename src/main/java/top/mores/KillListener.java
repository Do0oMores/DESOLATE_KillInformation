package top.mores;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class KillListener implements Listener {
    private final Message message;
    private final KillRecord killRecord;

    public KillListener(Message message, KillRecord killRecord) {
        this.message = message;
        this.killRecord = killRecord;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getKiller() != null) {
            Player killer = player.getKiller();
            //获取物品
            ItemStack itemStack = killer.getInventory().getItemInMainHand();
            killRecord.updatePlayerData(killer, itemStack);
            message.SendNormalMessages(itemStack, player, killer);
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
            message.sendActionbar(player, killer);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        killRecord.initPlayerData(player);
    }
}
