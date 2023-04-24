package com.runicrealms.plugin.api;

import com.runicrealms.plugin.codec.PluginCodecProvider;

import java.util.List;

public interface CodecAPI {

    /**
     * @param pluginCodecProvider a custom codec provider defined in child plugins
     */
    void addCodecProvider(PluginCodecProvider pluginCodecProvider);

    /**
     * @return a list of all custom codec providers
     */
    List<PluginCodecProvider> getCodecProviders();
}
