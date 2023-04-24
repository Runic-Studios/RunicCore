package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.codec.CharacterClassCodec;
import com.runicrealms.plugin.codec.PerkCodec;
import com.runicrealms.plugin.codec.PluginCodecProvider;
import com.runicrealms.plugin.codec.SkillTreePositionCodec;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to set up a custom mongo config that registers all data converters
 * from child plugins. This gives us granular control over how each plugin stores/retrieves its
 * data from mongo
 *
 * @author Skyfallin
 */
@Configuration
public class CoreMongoConfiguration extends AbstractMongoClientConfiguration {

    @NotNull
    @Override
    public String getDatabaseName() {
        String databaseString = RunicCore.getInstance().getConfig().getString("database");
        assert databaseString != null;
        return databaseString;
    }

    @NotNull
    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(RunicCore.getConverterAPI().getConverters());
    }

    @NotNull
    @Override
    @Bean
    public MongoClient mongoClient() {
        // Connect to MongoDB database (Atlas)
        String connectionString = RunicCore.getInstance().getConfig().getString("connection-string");
        assert connectionString != null;
        ConnectionString connString = new ConnectionString(connectionString);

        // Load RunicCore codecs, default codecs
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries
                (
                        CodecRegistries.fromCodecs
                                (
                                        new CharacterClassCodec(),
                                        new PerkCodec(),
                                        new SkillTreePositionCodec()
                                ),
                        MongoClientSettings.getDefaultCodecRegistry()
                );

        // Load custom plugin codecs
        List<PluginCodecProvider> codecProviders = RunicCore.getCodecAPI().getCodecProviders();
        for (PluginCodecProvider codecProvider : codecProviders) {
            CodecRegistry providerCodecRegistry = CodecRegistries.fromProviders((CodecProvider) codecProvider);
            codecRegistry = CodecRegistries.fromRegistries(codecRegistry, providerCodecRegistry);
        }

        // Configure connection pool settings
        ConnectionPoolSettings connectionPoolSettings = ConnectionPoolSettings.builder()
                .maxConnectionIdleTime(5, TimeUnit.MINUTES)
                .minSize(20) // Minimum pool size of 20 connections
                .build();


        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS))
                .applyConnectionString(connString)
                .codecRegistry(codecRegistry)
                .retryWrites(true)
                .readConcern(ReadConcern.MAJORITY)
                .writeConcern(WriteConcern.MAJORITY)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyToConnectionPoolSettings(builder -> builder.applySettings(connectionPoolSettings)) // Apply connection pool settings
                .build();

        return MongoClients.create(settings);
    }

    /**
     * Here we create a custom MongoTemplate and annotate with @Bean to ensure Spring
     * manages its lifecycle.
     *
     * @param mongoConverter our custom converter with data from child plugins
     * @return a custom mongo template that reads/writes to mongo using our custom converters
     */
    @Bean(name = "pluginDataMongoTemplate")
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MongoConverter mongoConverter) {
        return new MongoTemplate(mongoDatabaseFactory, mongoConverter);
    }

}