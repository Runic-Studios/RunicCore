package com.runicrealms.plugin.converter;

import org.springframework.core.convert.converter.Converter;

import java.util.List;

public interface ConverterAPI {

    /**
     * Registers a custom data converter from another plugin.
     * This gives us granular control over the way data from each plugin is stored / retrieved
     * in MongoDB
     *
     * @param converter some custom read/write converter
     */
    void addDataConverter(Converter<?, ?> converter);

    /**
     * Returns a list of custom converters for reading/writing in Spring Data MongoDB
     *
     * @return a list of converters that override read-write logic
     */
    List<Converter<?, ?>> getConverters();
}
