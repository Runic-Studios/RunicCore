package com.runicrealms.plugin.resourcepack;

public enum ResourcePackVersion {

    MC_1_9("https://www.dropbox.com/s/e5zpvwwgn8grq1u/1.10_RR.zip?dl=0", 48, 110), // same as 1.10
    MC_1_10("https://www.dropbox.com/s/e5zpvwwgn8grq1u/1.10_RR.zip?dl=0", 201, 210),
    MC_1_11("https://www.dropbox.com/s/ho9bvonbpggisbo/1.12_RR.zip?dl=0", 301, 316), // same as 1.12 // https://www.dropbox.com/s/b7hu3aqlehzd135/1.11_RR.zip?dl=1
    MC_1_12("https://www.dropbox.com/s/ho9bvonbpggisbo/1.12_RR.zip?dl=1", 317, 340),
    MC_1_13("https://www.dropbox.com/s/2pyn23w2505ee8l/1.13_RR.zip?dl=1", 341, 404), // uses old font format
    MC_1_14("https://www.dropbox.com/s/9xqs0zzl8neduiy/1.14_RR.zip?dl=1", 441, 498),
    MC_1_15("https://www.dropbox.com/s/xzw4p4cxokh54lt/1.15_RR.zip?dl=1", 550, 578),
    MC_1_16("https://www.dropbox.com/s/0zgaccv077dze8d/1.16_RR.zip?dl=1", 701, 1000);

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
            if (version >= pack.getMinVersion() && version <= pack.getMaxVersion()) {
                return pack;
            }
        }
        return null;
    }

}
