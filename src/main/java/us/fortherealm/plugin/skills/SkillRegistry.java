package us.fortherealm.plugin.skills;

import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.skilltypes.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.offensive.Fireball;

public enum SkillRegistry {

    FIREBALL(Fireball.class, 1),
    DELIVERANCE(Deliverance.class, 2);


    private int uniqueId;
    private Class<? extends Skill> skillClass;

    SkillRegistry(Class<? extends Skill> skillClass, int uniqueId) {
        this.uniqueId = uniqueId;
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
}
