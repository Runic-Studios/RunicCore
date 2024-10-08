package com.runicrealms.plugin.converter;

import com.runicrealms.plugin.rdb.api.ConverterAPI;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

public class ConverterHandler implements ConverterAPI {
    private final List<Converter<?, ?>> converters;

    public ConverterHandler() {
        converters = new ArrayList<>();
        // Add RunicCore converters
        addDataConverter(new LocationReadConverter());
        addDataConverter(new LocationWriteConverter());
    }

    @Override
    public void addDataConverter(Converter<?, ?> converter) {
        this.converters.add(converter);
    }

    @Override
    public List<Converter<?, ?>> getConverters() {
        return converters;
    }
}
