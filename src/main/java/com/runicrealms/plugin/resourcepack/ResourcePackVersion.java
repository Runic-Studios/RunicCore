package com.runicrealms.plugin.resourcepack;

/**
 * Programmatically determine player's client version / resource pack version using protocols
 * Exclusive on the bottom, inclusive on the top. E.g., (bottom, top]
 */
public enum ResourcePackVersion {
    // Every version is the same now actually
    MC_1_16("https://www.dropbox.com/s/xtjoq09us61nrp3/RR%20Resourcepack%206.8.23.2.zip?dl=1", 578, 754),
    MC_1_17("https://www.dropbox.com/s/xtjoq09us61nrp3/RR%20Resourcepack%206.8.23.2.zip?dl=1", 754, 756),
    MC_1_18("https://www.dropbox.com/s/xtjoq09us61nrp3/RR%20Resourcepack%206.8.23.2.zip?dl=1", 756, 9999);

    private final String link;
    private final Integer minVersion;
    private final Integer maxVersion;

    ResourcePackVersion(String link, Integer minVersion, Integer maxVersion) {
        this.link = link;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public static ResourcePackVersion getFromVersionNumber(Integer version) {
        for (ResourcePackVersion pack : ResourcePackVersion.values()) {
            if (version > pack.getMinVersion() && version <= pack.getMaxVersion()) {
                return pack;
            }
        }
        return null;
    }

    public String getLink() {
        return this.link;
    }

    public Integer getMaxVersion() {
        return this.maxVersion;
    }

    public Integer getMinVersion() {
        return this.minVersion;
    }

}
