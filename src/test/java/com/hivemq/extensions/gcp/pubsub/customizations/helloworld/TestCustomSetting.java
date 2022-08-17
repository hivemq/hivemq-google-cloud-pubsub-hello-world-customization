package com.hivemq.extensions.gcp.pubsub.customizations.helloworld;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.gcp.pubsub.api.model.CustomSetting;

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