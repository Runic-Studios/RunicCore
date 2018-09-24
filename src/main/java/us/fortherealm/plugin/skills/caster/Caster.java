package us.fortherealm.plugin.skills.caster;


import org.bukkit.entity.Player;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.exceptions.SkillNotAvailableException;

import java.util.ArrayList;
import java.util.List;

public class Caster {
	
	private List<Skill> skillsAvailable = new ArrayList<>();
	
	public Caster() {}
	
	public Caster(List<Skill> skillsAvailable) {
		this.skillsAvailable = skillsAvailable;
	}
	
	public void executeSkill(Player player, Skill skill) {
		boolean skillAvailable = false;
		for(Skill availableSkill : skillsAvailable) {
			if(availableSkill.equals(skill)) {
				skillAvailable = true;
				break;
			}
		}
		if(!skillAvailable) {
			try {
				throw new SkillNotAvailableException();
			} catch (SkillNotAvailableException ex) {
				ex.printErrorToConsole();
				ex.printStackTrace();
				return;
			}
		}
		
		skill.executeSkill(player);
	}
	
	public List<Skill> getAvailableSkills() {
		return skillsAvailable;
	}
	
	public void setSkillsAvailable(List<Skill> skillsAvailable) {
		this.skillsAvailable = skillsAvailable;
	}
	
	public void addSkillAvailable(Skill skill) {
		this.skillsAvailable.add(skill);
	}
	
	public void delSkillAvailable(Skill skill) {
		this.skillsAvailable.remove(skill);
	}
	
	
}
