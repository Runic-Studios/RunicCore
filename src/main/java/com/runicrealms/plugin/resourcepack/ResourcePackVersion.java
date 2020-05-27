package com.runicrealms.plugin.resourcepack;

public enum ResourcePackVersion {

    MC_1_9("", 48, 110), // todo: add
    MC_1_10("", 201, 210), // todo: add
    MC_1_11("", 301, 316), // todo: add
    MC_1_12("", 317, 340), // todo: add
    MC_1_13("", 341, 404), // todo: add
    MC_1_14("", 441, 498), // todo: add
    MC_1_15("", 550, 578); // todo: update
    //MC_1_16("", 701, 1000);

    private String link;
    private Integer minVersion;
    private Integer maxVersion;

    ResourcePackVersion(String link, Integer minVersion, Integer maxVersion) {
        this.link = link;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public String getLink() {
        return this.link;
    }

    public Integer getMinVersion() {
        return this.minVersion;
    }

    public Integer getMaxVersion() {
        return this.maxVersion;
    }

    public static ResourcePackVersion getFromVersionNumber(Integer version) {
        for (ResourcePackVersion pack : ResourcePackVersion.values()) {
            if (version >= pack.getMinVersion() && version <= pack.getMaxVersion()) {
                return pack;
            }
        }
        return null;
    }

}
