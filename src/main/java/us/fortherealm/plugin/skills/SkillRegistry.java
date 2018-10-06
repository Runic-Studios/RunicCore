package us.fortherealm.plugin.skills;

import us.fortherealm.plugin.skills.skilltypes.defensive.Speed;
import us.fortherealm.plugin.skills.skilltypes.rogue.offensive.backstab.Backstab;
import us.fortherealm.plugin.skills.skilltypes.warrior.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.fireball.Fireball;

public enum SkillRegistry {

    // Add Skills here!!
    FIREBALL(Fireball.class),
    DELIVERANCE(Deliverance.class),
    BACKSTAB(Backstab.class),
    SPEED(Speed.class);

    private static int nextUniqueId = 0;

    private int uniqueId;
    private Class<? extends Skill> skillClass;

    static {
        for(SkillRegistry value : values())
            value.setUniqueId(nextUniqueId++);
    }

    SkillRegistry(Class<? extends Skill> skillClass) {
        this.skillClass = skillClass;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public Skill getSkill() {
        try {
            return skillClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setUniqueId(int ID) {
        this.uniqueId = ID;
    }

}
