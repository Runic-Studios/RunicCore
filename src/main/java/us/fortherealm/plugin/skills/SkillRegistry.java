package us.fortherealm.plugin.skills;

import us.fortherealm.plugin.skills.skilltypes.rogue.offensive.Backstab;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Fireball;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Frostbolt;
import us.fortherealm.plugin.skills.skilltypes.warrior.defensive.Deliverance;

public enum SkillRegistry {

    // Add Skills here!!
    FIREBALL(Fireball.class),
    FROSTBOLT(Frostbolt.class),
    DELIVERANCE(Deliverance.class),
    BACKSTAB(Backstab .class);
//    SPEED(Speed.class);

    private static int nextUniqueId = 0;

    static {
        for (SkillRegistry value : values())
            value.setUniqueId(nextUniqueId++);
    }

    private int uniqueId;
    private Class<? extends Skill> skillClass;

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
