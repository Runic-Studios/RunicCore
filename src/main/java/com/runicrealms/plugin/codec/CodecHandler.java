package com.runicrealms.plugin.codec;

import com.runicrealms.plugin.api.CodecAPI;

import java.util.ArrayList;
import java.util.List;

public class CodecHandler implements CodecAPI {

    private final List<PluginCodecProvider> codecProviders;

    public CodecHandler() {
        this.codecProviders = new ArrayList<>();
    }

    @Override
    public void addCodecProvider(PluginCodecProvider pluginCodecProvider) {
        this.codecProviders.add(pluginCodecProvider);
    }

    @Override
    public List<PluginCodecProvider> getCodecProviders() {
        return codecProviders;
    }

}