package com.hivemq.extensions.gcp.pubsub.customizations.helloworld;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.gcp.pubsub.api.model.PubSubConnection;

/**
 * @author Florian Limpöck
 * @since 4.9.0
 */
class TestPubSubConnection implements PubSubConnection {

    @Override
    public @NotNull
    String getId() {
        return "my-connection";
    }

    @Override
    public @NotNull
    String getProjectId() {
        return "my-project";
    }
}