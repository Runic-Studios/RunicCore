package com.runicrealms.plugin.item.lootchests;

/**
 * Wrapper for a template id, min, and max stack size for loot chest contents. Can't be an ItemStack or it triggers
 * dupe manager
 */
public class ChestItem {

    private final boolean scriptItem;
    private final String templateID;
    private final int min;
    private final int max;

    /**
     * @param templateID of the runic item
     * @param min        stack size
     * @param max        stack size
     */
    public ChestItem(String templateID, int min, int max) {
        this.scriptItem = false;
        this.templateID = templateID;
        this.min = min;
        this.max = max;
    }

    /**
     * This is for script items. Handled in the API 'generateItemStack' method
     */
    public ChestItem() {
        this.scriptItem = true;
        this.templateID = "";
        this.min = 1;
        this.max = 1;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public String getTemplateID() {
        return templateID;
    }

    public boolean isScriptItem() {
        return scriptItem;
    }
}
