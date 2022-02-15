package com.runicrealms.plugin.resourcepack;

/**
 * Programmatically determine player's client version / resource pack version using protocols
 * Exclusive on the bottom, inclusive on the top. E.g., (bottom, top]
 */
public enum ResourcePackVersion {

    MC_1_9("https://www.dropbox.com/s/0okigj17fpnfbta/1.10_RR.zip?dl=1", 47, 110), // same as 1.10
    MC_1_10("https://www.dropbox.com/s/0okigj17fpnfbta/1.10_RR.zip?dl=1", 110, 210),
    MC_1_11("https://www.dropbox.com/s/n57fryz953693g5/1.12_RR.zip?dl=1", 210, 316), // same as 1.12
    MC_1_12("https://www.dropbox.com/s/n57fryz953693g5/1.12_RR.zip?dl=1", 316, 340),
    MC_1_13("https://www.dropbox.com/s/k33evk3fvnq56a6/1.14_RR.zip?dl=1", 340, 404), // same as 1.14
    MC_1_14("https://www.dropbox.com/s/k33evk3fvnq56a6/1.14_RR.zip?dl=1", 404, 498),
    MC_1_15("https://www.dropbox.com/s/k33evk3fvnq56a6/1.14_RR.zip?dl=1", 498, 578),
    MC_1_16("https://www.dropbox.com/s/k33evk3fvnq56a6/1.14_RR.zip?dl=1", 578, 754),
    MC_1_17("https://www.dropbox.com/s/k33evk3fvnq56a6/1.14_RR.zip?dl=1", 754, 756),
    MC_1_18("https://www.dropbox.com/s/k33evk3fvnq56a6/1.14_RR.zip?dl=1", 756, 1000);

    private final String link;
    private final Integer minVersion;
    private final Integer maxVersion;

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
            if (version > pack.getMinVersion() && version <= pack.getMaxVersion()) {
                return pack;
            }
        }
        return null;
    }

}
