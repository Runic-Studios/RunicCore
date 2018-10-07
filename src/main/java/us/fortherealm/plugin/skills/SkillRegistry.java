package us.fortherealm.plugin.skills;

//import us.fortherealm.plugin.skills.skilltypes.defensive.Speed;
//import us.fortherealm.plugin.skills.skilltypes.rogue.offensive.backstab.Backstab;
//import us.fortherealm.plugin.skills.skilltypes.warrior.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.runic.offensive.Fireball;

public enum SkillRegistry {

    // Add Skills here!!
    FIREBALL(Fireball.class, 1);
//    DELIVERANCE(Deliverance.class);
//    BACKSTAB(Backstab.class),
//    SPEED(Speed.class);

    private int uniqueId;
    private Class<? extends Skill> skillClass;

    SkillRegistry(Class<? extends Skill> skillClass, int uniqueId) {
        this.skillClass = skillClass;
        this.uniqueId = uniqueId;
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

}
