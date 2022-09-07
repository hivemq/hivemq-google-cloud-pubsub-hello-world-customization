/*
 * Copyright 2022-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.extensions.google.cloud.pubsub.customizations.helloworld;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.google.cloud.pubsub.api.model.CustomSetting;
import com.hivemq.extensions.google.cloud.pubsub.api.model.CustomSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Florian Limpöck
 * @since 4.9.0
 */
class TestCustomSettings implements CustomSettings {

    final @NotNull List<TestCustomSetting> customSettings;

    TestCustomSettings(final @NotNull List<TestCustomSetting> customSettings) {
        this.customSettings = customSettings;
    }

    @Override
    public @NotNull Optional<String> getFirst(final @NotNull String name) {
        return customSettings.stream()
                .filter(c -> c.name.equals(name))
                .findFirst()
                .map(TestCustomSetting::getValue);
    }

    @Override
    public @NotNull List<@NotNull String> getAllForName(final @NotNull String name) {
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