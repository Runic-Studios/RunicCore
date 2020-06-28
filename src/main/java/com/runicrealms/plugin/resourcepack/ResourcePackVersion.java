package com.runicrealms.plugin.resourcepack;

public enum ResourcePackVersion {

    MC_1_9("https://www.dropbox.com/s/mlfr5lc8hgay1dm/1.10_RR.zip?dl=1", 48, 110), // same as 1.10
    MC_1_10("https://www.dropbox.com/s/mlfr5lc8hgay1dm/1.10_RR.zip?dl=1", 201, 210),
    MC_1_11("https://www.dropbox.com/s/c106voh829ozs3x/1.12_RR.zip?dl=1", 301, 316), // same as 1.12 // https://www.dropbox.com/s/b7hu3aqlehzd135/1.11_RR.zip?dl=1
    MC_1_12("https://www.dropbox.com/s/c106voh829ozs3x/1.12_RR.zip?dl=1", 317, 340),
    MC_1_13("https://www.dropbox.com/s/wl1kh91ck2w68lt/1.13_RR.zip?dl=1", 341, 404), // uses old font format
    MC_1_14("https://www.dropbox.com/s/20hz2r5e44fvtt8/1.14_RR.zip?dl=1", 441, 498),
    MC_1_15("https://www.dropbox.com/s/0aun58ahh2eq2v0/1.15_RR.zip?dl=1", 550, 578),
    MC_1_16("https://www.dropbox.com/s/f8scwmjth80b1tr/1.16_RR.zip?dl=1", 701, 1000); // new font format (again)

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
