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
package com.hivemq.extensions.gcp.pubsub.customizations.helloworld;

import com.codahale.metrics.Counter;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.services.builder.PublishBuilder;
import com.hivemq.extensions.gcp.pubsub.api.model.CustomSettings;
import com.hivemq.extensions.gcp.pubsub.api.model.InboundPubSubMessage;
import com.hivemq.extensions.gcp.pubsub.api.model.PubSubConnection;
import com.hivemq.extensions.gcp.pubsub.api.transformers.PubSubToMqttInitInput;
import com.hivemq.extensions.gcp.pubsub.api.transformers.PubSubToMqttInput;
import com.hivemq.extensions.gcp.pubsub.api.transformers.PubSubToMqttOutput;
import com.hivemq.extensions.gcp.pubsub.api.transformers.PubSubToMqttTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This example {@link PubSubToMqttTransformer} accepts a GCP PubSub message and tries to create a new MQTT PUBLISH from it.
 * <p>
 * It performs the following computational steps:
 * <ol>
 *     <li> Create a new MQTT publish consisting of: </li>
 *         <ul>
 *             <li> The pubsub topic as MQTT topic. </li>
 *             <li> The qos from custom-settings config as MQTT qos </li>
 *             <li> The data as payload if present </li>
 *             <li> All present pubsub attributes as user properties. </li>
 *         </ul>
 *      <li> Increment a metric everytime a pubsub message has no data set. </li>
 *      <li> Provide the MQTT publish to the extension for publishing. </li>
 * </ol>
 * <p>
 * An example gcp-pubsub-configuration.xml enabling this transformer is provided in {@code src/main/resources}.
 *
 * @since 4.9.0
 * @author Florian Limpöck
 */
public class PubSubToMqttHelloWorldTransformer implements PubSubToMqttTransformer {

    private static final @NotNull Logger LOG = LoggerFactory.getLogger(PubSubToMqttHelloWorldTransformer.class);

    public static final @NotNull String MISSING_DATA_COUNTER_NAME = "com.hivemq.extensions.gcp-pubsub.customizations.pubsub-to-mqtt.transformer.missing-data.count";

    private @Nullable Counter missingValueCounter;
    @Nullable CustomSettings customSettings;

    @Override
    public void init(final @NotNull PubSubToMqttInitInput input) {
        try {
            final PubSubConnection pubSubConnection = input.getPubSubConnection();
            this.customSettings = input.getCustomSettings();
            // build any custom metrics based on your business logic and needs
            this.missingValueCounter = input.getMetricRegistry().counter(MISSING_DATA_COUNTER_NAME);
            LOG.info(
                    "PubSub-To-MQTT-Hello-World-Transformer for pubsub connection '{}' and project '{}' initialized.",
                    pubSubConnection.getId(),
                    pubSubConnection.getProjectId());
        } catch (final Exception e) {
            LOG.error("GCP PubSub to MQTT transformer initialisation failed: ", e);
        }
    }

    @Override
    public void transformPubSubToMqtt(
            final @NotNull PubSubToMqttInput pubSubToMqttInput,
            final @NotNull PubSubToMqttOutput pubSubToMqttOutput) {

        try {
            final InboundPubSubMessage pubSubMessage = pubSubToMqttInput.getInboundPubSubMessage();
            final PublishBuilder publishBuilder = pubSubToMqttOutput.newPublishBuilder()
                    .topic("mqtt/topic");

            if (customSettings != null) {
                final Optional<String> qosOptional = customSettings.getFirst("qos");
                qosOptional.ifPresent(qosAsString -> {
                    try {
                        final int qosAsInt = Integer.parseInt(qosAsString);
                        publishBuilder.qos(Qos.valueOf(qosAsInt));
                    } catch (IllegalArgumentException e) {
                        LOG.debug("Could not parse qos from custom settings. Using default qos 0. ", e);
                        publishBuilder.qos(Qos.AT_MOST_ONCE);
                    }
                });
            }

            pubSubMessage.getData()
                    .ifPresentOrElse(publishBuilder::payload, Objects.requireNonNull(missingValueCounter)::inc);
            pubSubMessage.getAttributes().forEach(publishBuilder::userProperty);
            pubSubToMqttOutput.setPublishes(List.of(publishBuilder.build()));

        } catch (Exception e) {
            LOG.error("GCP PubSub to MQTT transformation failed: ", e);
        }
    }
}
