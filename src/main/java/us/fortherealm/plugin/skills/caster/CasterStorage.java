package us.fortherealm.plugin.skills.caster;

import java.util.HashMap;

public class CasterStorage<T> {
	
	private HashMap<T, Caster> childCasters = new HashMap<>();
	
	private Caster motherCaster;
	
	public CasterStorage(Caster motherCaster) {
		this.motherCaster = motherCaster;
	}
	
	public Caster getMotherCaster() {
		return motherCaster;
	}
	
	public HashMap<T, Caster> getChildCasters() {
		return childCasters;
	}
	
	public void addLinkedCaster(T field) {
		childCasters.put(field, new Caster(motherCaster));
	}
	
	public void delLinkedCaster(T field) {
		childCasters.remove(field);
	}
	
}
