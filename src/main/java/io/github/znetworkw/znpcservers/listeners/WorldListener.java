package io.github.znetworkw.znpcservers.listeners;

import io.github.znetworkw.znpcservers.ServersNPC;
import io.github.znetworkw.znpcservers.npc.NPC;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {
    /**
     * Creates and register the necessary events for worlds.
     *
     * @param serversNPC The plugin instance.
     */
    public WorldListener(ServersNPC serversNPC) {
        serversNPC.getServer().getPluginManager().registerEvents(this, serversNPC);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();

        for (NPC npc : NPC.all()) {
            World cachedWorld = npc.getLocation().getWorld();

            // update cached packets with old world uid
            if (cachedWorld != null && worldName.equals(cachedWorld.getName())) {
                npc.changeType(npc.getNpcPojo().getNpcType());
            }
        }
    }
}
