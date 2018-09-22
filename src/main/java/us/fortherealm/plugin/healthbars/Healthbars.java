package us.fortherealm.plugin.healthbars;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.*;

public class Healthbars implements Listener {
    private static Map<UUID, Bar> mobs = new HashMap<>();

    private static Map<EntityType, Integer> mobHeight = new HashMap<EntityType, Integer>();

    public Healthbars() {
        Arrays.stream(EntityType.values()).filter(EntityType::isAlive).filter(e -> e != EntityType.VILLAGER && e != EntityType.PLAYER).forEach(e -> mobHeight.put(e, 1));
    }

    @EventHandler
    // TODO: mob health bars break on reload
    public void onMobSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        String name = entity.getCustomName();
        if (mobHeight.keySet().contains(entity.getType())) {
            loadBar(entity.getUniqueId(), name);
            setBar(entity, (int) (entity.getHealth()));
        }
    }

    @EventHandler
    public void onMobDamaged(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity && mobs.containsKey(event.getEntity().getUniqueId())) {//avoid health bars on citizens and unwanted entities
            LivingEntity e = (LivingEntity) event.getEntity();
            setBar(e, (int) (e.getHealth() - event.getDamage()));
        }
    }

    @EventHandler
    public void onMobHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof LivingEntity && mobs.containsKey(event.getEntity().getUniqueId())) {
            LivingEntity e = (LivingEntity) event.getEntity();
            setBar(e, (int) (e.getHealth() + event.getAmount()));
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (mobs.containsKey(event.getEntity().getUniqueId())) {
            clearSnowballs(event.getEntity());
            mobs.remove(event.getEntity().getUniqueId());
        }
    }


    private void clearSnowballs(Entity e) {
        e = e.getPassenger();
        if (e != null) {
            while (e != null && e.getType() == EntityType.SNOWBALL) {
                Entity remove = e;
                e = e.getPassenger();
                remove.remove();
            }
        }
    }

    private void loadBar(UUID id, String name) {
        addBar(id, name, BarType.PIPE);
    }

    private void setBar(LivingEntity e, int health) {
        if (mobs.containsKey(e.getUniqueId())) {
            Bar b = mobs.get(e.getUniqueId());
            String name = b.getName();
            if (health < 0)
                health = 0;

            if (b.getTypes().contains(BarType.PIPE)) {
                String s = ChatColor.GREEN + "";
                int i = 0;
                while (i < health / 2) {
                    s = s + "|";
                    i++;
                }
                if (health < e.getMaxHealth()) {
                    s += ChatColor.RED + "";
                    while (i < e.getMaxHealth() / 2) { // Amt. of bars is half of the entity's health
                        s += "|";
                        i++;
                    }
                }
                name = s;
            }
            setNameTag(e, name);
        }
    }

    private void setNameTag(LivingEntity e, String tag) {
        if (e.isCustomNameVisible()) {
            e.setCustomName(null);

            Entity sb = e.getPassenger();

            Entity top = null;

            while (sb != null && sb.getType() == EntityType.SNOWBALL) {
                top = sb;
                sb = sb.getPassenger();
            }

            if (top == null) {
                Entity ridden = e;

                for (int i = 0; i < getMobHeight(e.getType()); i++) {
                    top = e.getWorld().spawnEntity(e.getLocation(), EntityType.SNOWBALL);
                    ridden.setPassenger(top);
                    ridden = top;
                }

            }

            if (top != null) {
                top.setCustomNameVisible(true);
                top.setCustomName(tag);
            } else {
                e.setCustomName(tag);
            }

        } else
            e.setCustomName(tag);
    }

    private void addBar(UUID id, String name, BarType type) {
        if (mobs.containsKey(id))
            mobs.get(id).getTypes().add(type);
        else
            mobs.put(id, new Bar(type, name));
    }

    private int getMobHeight(EntityType et) {
        if (mobHeight.containsKey(et) && mobHeight.get(et) > 0)
            return mobHeight.get(et);
        return 1;
    }


    private class Bar {
        private String name;
        private Set<BarType> types = new HashSet<>();

        Bar(BarType type, String name) {
            this.types.add(type);
            this.name = name;
        }

        Set<BarType> getTypes() {
            return types;
        }

        String getName() {
            return name;
        }
    }

    private enum BarType {
        PIPE
    }
}