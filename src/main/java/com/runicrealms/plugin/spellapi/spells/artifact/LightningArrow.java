//package com.runicrealms.plugin.spellapi.spells.artifact;
//
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.plugin.item.artifact.event.RunicItemArtifactTriggerEvent;
//import com.runicrealms.plugin.common.CharacterClass;
//import com.runicrealms.plugin.spellapi.spelltypes.ArtifactSpell;
//import com.runicrealms.plugin.spellapi.spelltypes.Spell;
//import com.runicrealms.plugin.utilities.DamageUtil;
//import org.bukkit.Location;
//import org.bukkit.Particle;
//import org.bukkit.Sound;
//import org.bukkit.entity.Arrow;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.util.Vector;
//
//import java.util.HashSet;
//import java.util.Set;
//
//public class LightningArrow extends Spell implements ArtifactSpell {
//
//    private static final int RADIUS = 3;
//    private static final double DAMAGE_PERCENT = 0.75;
//    private static final double CHANCE = 1.0;
//    private static final String ARTIFACT_ID = "runeforged-piercer";
//    private final Set<Arrow> lightningArrows;
//
//    public LightningArrow() {
//        super("Lightning Arrow", CharacterClass.ARCHER);
//        this.setIsPassive(true);
//        lightningArrows = new HashSet<>();
//    }
//
//    private void fireLightningArrow(Player player, int damage) {
//        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
//        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 2f);
//        Vector vec = player.getEyeLocation().getDirection().normalize().multiply(2);
//        startTask(player, vec, damage);
//    }
//
//    @Override
//    public String getArtifactId() {
//        return ARTIFACT_ID;
//    }
//
//    @Override
//    public double getChance() {
//        return CHANCE;
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST) // first
//    public void onArtifactUse(RunicItemArtifactTriggerEvent event) {
//        if (!event.getRunicItemArtifact().getTemplateId().equals(getArtifactId())) return;
//        int damage = (int) ((event.getRunicItemArtifact().getWeaponDamage().getRandomValue() * DAMAGE_PERCENT) + RunicCore.getStatAPI().getPlayerStrength(event.getPlayer().getUniqueId()));
//        fireLightningArrow(event.getPlayer(), damage);
//    }
//
//    @EventHandler
//    public void onSearingArrowHit(EntityDamageByEntityEvent event) {
//        if (!(event.getDamager() instanceof Arrow arrow)) return;
//        if (!(arrow.getShooter() instanceof Player)) return;
//        if (!lightningArrows.contains(arrow)) return;
//        event.setCancelled(true);
//    }
//
//    private void startTask(Player player, Vector vector, int damage) {
//        Arrow powerShot = player.launchProjectile(Arrow.class);
//        powerShot.setVelocity(vector);
//        powerShot.setShooter(player);
//        lightningArrows.add(powerShot);
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                Location arrowLoc = powerShot.getLocation();
//                powerShot.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc,
//                        10, 0, 0, 0, 0);
//                if (powerShot.isDead() || powerShot.isOnGround()) {
//                    this.cancel();
//                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
//                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 2f);
//                    powerShot.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc, 25, 0.5f, 0.5f, 0.5f, 0);
//                    for (Entity entity : player.getWorld().getNearbyEntities(arrowLoc, RADIUS, RADIUS, RADIUS)) {
//                        if (!(entity instanceof LivingEntity)) continue;
//                        if (!isValidEnemy(player, entity)) continue;
//                        DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player);
//                    }
//                }
//            }
//        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
//    }
//}
//
