package com.hivemq.extensions.gcp.pubsub.customizations.helloworld;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.gcp.pubsub.api.model.CustomSetting;
import com.hivemq.extensions.gcp.pubsub.api.model.CustomSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class TestCustomSettings implements CustomSettings {

    final @NotNull List<TestCustomSetting> customSettings;

    TestCustomSettings(final @NotNull List<TestCustomSetting> customSettings) {
        this.customSettings = customSettings;
    }

    @Override
    public @NotNull Optional<String> getFirst(@NotNull String name) {
        return customSettings.stream()
                .filter(c -> c.name.equals(name))
                .findFirst()
                .map(TestCustomSetting::getValue);
    }

    @Override
    public @NotNull List<@NotNull String> getAllForName(@NotNull String name) {
        return customSettings.stream()
                .filter(c -> c.name.equals(name))
                .map(TestCustomSetting::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<@NotNull CustomSetting> asList() {
        return List.copyOf(customSettings);
    }

    @Override
    public @NotNull Map<String, String> asSingleValueMap() {
        return customSettings.stream()
                .collect(Collectors.toMap(TestCustomSetting::getName, TestCustomSetting::getValue));
    }

    @Override
    public boolean isEmpty() {
        return customSettings.isEmpty();
    }
}