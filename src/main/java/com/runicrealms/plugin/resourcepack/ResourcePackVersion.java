package com.runicrealms.plugin.resourcepack;

public enum ResourcePackVersion {

    MC_1_14("https://www.dropbox.com/s/9ymuk315d59gif1/RR%20Official%20Pack.zip?dl=1", 550, 578),
    MC_1_15("https://www.dropbox.com/s/9ymuk315d59gif1/RR%20Official%20Pack.zip?dl=1", 441, 498);

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
