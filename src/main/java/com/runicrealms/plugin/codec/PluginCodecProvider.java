package com.runicrealms.plugin.codec;

import org.bson.codecs.Codec;

public interface PluginCodecProvider {

    /**
     * Used by custom codec providers to encode / decode custom data types in mongo
     *
     * @param clazz some custom class type (RunicItem)
     * @return some custom codec (RunicItemCodec)
     */
    <T> Codec<T> getCodec(Class<T> clazz);
}
