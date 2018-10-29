package us.fortherealm.plugin.cooldown;

import org.bukkit.entity.Player;

public class RealmCooldown {

    /*

    Realm Cooldown should be used by any class that contains a cooldown.
    It sorts, messages, and updates all cool downs. Feel free to extend
    it to insert your own functionality, but first make sure that you're
    not reinventing the wheel.

     */

    private long lastCast;
    private double cooldown;

    public RealmCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

//    private void displayCooldown(Player player) {
//        synchronized (castersOnCooldown) {
//            castersOnCooldown.put(this, player);
//        }
//
//        if(isTaskRunning)
//            return;
//
//        isTaskRunning = true;
//
//        new BukkitRunnable() {
//
//            @Override
//            public void run() {
//                synchronized(castersOnCooldown) {
//                    Set<CasterItemStack> oldCastersOnCooldown = new HashSet<>(); // Avoids threading issues
//                    for(CasterItemStack casterItem : castersOnCooldown.keySet()) {
//                        if(!casterItem.isOnCooldown()) {
//                            oldCastersOnCooldown.add(casterItem);
//                            continue;
//                        }
//
//                        castersOnCooldown.get(casterItem).spigot().sendMessage(
//                                ChatMessageType.ACTION_BAR,
//                                new net.md_5.bungee.api.chat.TextComponent(
//                                        casterItem.getName() + " is on cooldown for " +
//                                                casterItem.getCurrentCooldown()/1000 + " more seconds"
//                                )
//                        );
//                    }
//
//                    for(CasterItemStack casterItem : oldCastersOnCooldown) {
//                        castersOnCooldown.remove(casterItem);
//                    }
//                    oldCastersOnCooldown.clear();
//
//                    if(castersOnCooldown.isEmpty()) {
//                        isTaskRunning = false;
//                        this.cancel();
//                        return;
//                    }
//                }
//
//            }
//        }.runTaskTimer(Main.getInstance(), 0, 10);
//    }

    public void displayCooldown(Player player) {
        player.sendMessage("On cooldown for " + getCurrentCooldown() + " more seconds");
    }

    public boolean isOnCooldown() {
        return getCurrentCooldown() != 0;
    }

    public double getCooldown() {
        return cooldown;
    }

    public double getCurrentCooldown() {
        return Math.max(0, cooldown*1000 - (System.currentTimeMillis() - lastCast));
    }

    public double getTotalCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public long getLastCast() {
        return lastCast;
    }

    public void setLastCast(long lastCast) {
        this.lastCast = lastCast;
    }

}
