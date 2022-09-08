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

/**
 * @author Florian Limpöck
 * @since 4.9.0
 */
class TestCustomSetting implements CustomSetting {

    final @NotNull String name;
    final @NotNull String value;

    TestCustomSetting(final @NotNull String name, final @NotNull String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getValue() {
        return value;
    }
}