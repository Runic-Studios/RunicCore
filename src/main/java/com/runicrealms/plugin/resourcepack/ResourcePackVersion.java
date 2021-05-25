package com.runicrealms.plugin.resourcepack;

public enum ResourcePackVersion {

    MC_1_9("https://www.dropbox.com/s/9cphjrwqbg8nwk9/1.10_RR.zip?dl=1", 48, 110), // same as 1.10
    MC_1_10("https://www.dropbox.com/s/9cphjrwqbg8nwk9/1.10_RR.zip?dl=1", 201, 210),
    MC_1_11("https://www.dropbox.com/s/s7mhy66xvbimqsq/1.12_RR.zip?dl=1", 301, 316), // same as 1.12 // https://www.dropbox.com/s/b7hu3aqlehzd135/1.11_RR.zip?dl=1
    MC_1_12("https://www.dropbox.com/s/s7mhy66xvbimqsq/1.12_RR.zip?dl=1", 317, 340),
    MC_1_13("https://www.dropbox.com/s/ysj3iv3mu97rw7d/1.13_RR.zip?dl=1", 341, 404), // uses old font format
    MC_1_14("https://www.dropbox.com/s/muya93ndk100cxc/1.14_RR.zip?dl=1", 441, 498),
    MC_1_15("https://www.dropbox.com/s/ed53od8ds88mwmd/1.15_RR.zip?dl=1", 550, 578),
    MC_1_16("https://www.dropbox.com/s/gt3qumzvzrnswja/1.16_RR.zip?dl=1", 701, 1000);

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
