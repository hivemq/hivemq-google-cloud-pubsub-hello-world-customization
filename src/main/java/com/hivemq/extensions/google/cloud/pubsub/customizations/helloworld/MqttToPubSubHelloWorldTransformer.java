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
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extensions.google.cloud.pubsub.api.builders.OutboundPubSubMessageBuilder;
import com.hivemq.extensions.google.cloud.pubsub.api.model.CustomSettings;
import com.hivemq.extensions.google.cloud.pubsub.api.model.OutboundPubSubMessage;
import com.hivemq.extensions.google.cloud.pubsub.api.model.PubSubConnection;
import com.hivemq.extensions.google.cloud.pubsub.api.transformers.MqttToPubSubInitInput;
import com.hivemq.extensions.google.cloud.pubsub.api.transformers.MqttToPubSubInput;
import com.hivemq.extensions.google.cloud.pubsub.api.transformers.MqttToPubSubOutput;
import com.hivemq.extensions.google.cloud.pubsub.api.transformers.MqttToPubSubTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This example {@link MqttToPubSubTransformer} accepts an MQTT PUBLISH and tries to create a new Pub/Sub message from it.
 * <p>
 * The example performs the following computational steps:
 * <ol>
 *     <li> Read the Pub/Sub topics from the `destination` custom setting. </li>
 *     <li> Create a new Pub/Sub message for each Pub/Sub topic that contains the following information: </li>
 *         <ul>
 *             <li> The payload as data. </li>
 *             <li> The MQTT topic as a Pub/Sub attribute. </li>
 *             <li> The retained flag as a Pub/Sub attribute. </li>
 *             <li> The quality of service as a Pub/Sub attribute. </li>
 *             <li> All present user properties as Pub/Sub attributes. </li>
 *         </ul>
 *      <li> Provide the messages to the extension for publishing. </li>
 * </ol>
 * <p>
 * An example `google-cloud-pubsub-configuration.xml` file that enables this transformer is provided in `{@code src/main/resources}`.
 *
 * @author Florian Limpöck
 * @since 4.9.0
 */
public class MqttToPubSubHelloWorldTransformer implements MqttToPubSubTransformer {

    private static final @NotNull Logger LOG = LoggerFactory.getLogger(MqttToPubSubHelloWorldTransformer.class);
    @Nullable CustomSettings customSettings;

    @Override
    public void init(final @NotNull MqttToPubSubInitInput input) {
        try {
            final PubSubConnection pubSubConnection = input.getPubSubConnection();
            this.customSettings = input.getCustomSettings();
            LOG.info(
                    "MQTT-To-PubSub-Hello-World-Transformer for pubsub connection '{}' and project '{}' initialized.",
                    pubSubConnection.getId(),
                    pubSubConnection.getProjectId());
        } catch (final Exception e) {
            LOG.error("MQTT to Google Cloud PubSub transformer initialisation failed: ", e);
        }
    }

    @Override
    public void transformMqttToPubSub(
            final @NotNull MqttToPubSubInput mqttToPubSubInput,
            final @NotNull MqttToPubSubOutput mqttToPubSubOutput) {
        try {
            final PublishPacket publishPacket = mqttToPubSubInput.getPublishPacket();
            final String mqttTopic = publishPacket.getTopic();

            final List<String> pubSubTopics;
            if (customSettings != null) {
                pubSubTopics = customSettings.getAllForName("destination");
            } else {
                pubSubTopics = Collections.emptyList();
            }

            final List<OutboundPubSubMessage> outboundPubSubMessages = new ArrayList<>();
            for (final String pubSubTopic : pubSubTopics) {
                try {
                    final OutboundPubSubMessageBuilder builder = mqttToPubSubOutput.newOutboundPubSubMessageBuilder();
                    builder.topicName(pubSubTopic);
                    publishPacket.getUserProperties().asList()
                            .forEach(userProperty -> builder.attribute(userProperty.getName(), userProperty.getValue()));
                    //attributes with the same name from user properties will be overwritten here
                    builder.attribute("mqtt-topic", mqttTopic);
                    builder.attribute("retained", String.valueOf(publishPacket.getRetain()));
                    builder.attribute("qos", String.valueOf(publishPacket.getQos().getQosNumber()));
                    publishPacket.getPayload().ifPresent(builder::data);
                    outboundPubSubMessages.add(builder.build());
                } catch (final Exception e) {
                    LOG.error("Could not create a Google Cloud PubSub message from MQTT message with topic '{}' because", mqttTopic, e);
                }
            }
            mqttToPubSubOutput.setOutboundPubSubMessages(outboundPubSubMessages);
        } catch (final Exception e) {
            LOG.error("MQTT to Google Cloud PubSub transformation failed: ", e);
        }
    }
}
