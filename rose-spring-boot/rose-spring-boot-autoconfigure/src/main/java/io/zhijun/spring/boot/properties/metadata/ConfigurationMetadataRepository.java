package io.zhijun.spring.boot.properties.metadata;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemHint;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata.ItemType;

import java.util.*;

public class ConfigurationMetadataRepository implements CommandLineRunner {

    private final ConfigurationMetadataReader configurationMetadataReader;

    private Map<String, ItemMetadata> namedGroups;

    private Map<String, ItemMetadata> namedProperties;

    private Map<String, List<ItemHint>> namedHints;

    public ConfigurationMetadataRepository(ConfigurationMetadataReader configurationMetadataReader) {
        this.configurationMetadataReader = configurationMetadataReader;
    }

    public Set<String> getPropertyGroups() {
        return this.namedGroups.keySet();
    }

    public Set<String> getPropertyNames() {
        return this.namedProperties.keySet();
    }

    public Collection<ItemMetadata> getGroups() {
        return this.namedGroups.values();
    }

    public Collection<ItemMetadata> getProperties() {
        return this.namedProperties.values();
    }

    public ItemMetadata getGroup(String name) {
        return this.namedGroups.get(name);
    }

    public ItemMetadata getProperty(String name) {
        return this.namedProperties.get(name);
    }

    public List<ItemHint> getHints(String name) {
        return this.namedHints.getOrDefault(name, Collections.emptyList());
    }

    public ConfigurationMetadataReader getConfigurationMetadataReader() {
        return configurationMetadataReader;
    }

    @Override
    public void run(String... args) throws Exception {
        ConfigurationMetadata configurationMetadata = this.configurationMetadataReader.read();
        init(configurationMetadata);
    }

    private void init(ConfigurationMetadata configurationMetadata) {
        List<ItemMetadata> items = configurationMetadata.getItems();
        initNamedGroupItems(items);
        initNamedPropertyItems(items);
        initNamedHints(configurationMetadata.getHints());
    }

    private void initNamedGroupItems(List<ItemMetadata> items) {
        this.namedGroups = createNamedItems(items, ItemType.GROUP);
    }

    private void initNamedPropertyItems(List<ItemMetadata> items) {
        this.namedProperties = createNamedItems(items, ItemType.PROPERTY);
    }

    private void initNamedHints(List<ItemHint> items) {
        Map<String, List<ItemHint>> namedHints = new LinkedHashMap<>();
        for (ItemHint itemHint : items) {
            namedHints.computeIfAbsent(itemHint.getName(), i -> new LinkedList<>()).add(itemHint);
        }
        this.namedHints = namedHints;
    }

    private static Map<String, ItemMetadata> createNamedItems(List<ItemMetadata> items, ItemType itemType) {
        Map<String, ItemMetadata> namedItems = new LinkedHashMap<>();
        for (ItemMetadata item : items) {
            if (item.isOfItemType(itemType)) {
                namedItems.put(item.getName(), item);
            }
        }
        return namedItems;
    }
}
