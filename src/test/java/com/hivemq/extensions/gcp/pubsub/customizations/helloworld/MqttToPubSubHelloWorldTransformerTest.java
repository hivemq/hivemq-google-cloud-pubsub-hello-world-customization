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

import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.general.UserProperties;
import com.hivemq.extension.sdk.api.packets.publish.PublishPacket;
import com.hivemq.extensions.gcp.pubsub.api.builders.OutboundPubSubMessageBuilder;
import com.hivemq.extensions.gcp.pubsub.api.model.CustomSetting;
import com.hivemq.extensions.gcp.pubsub.api.model.CustomSettings;
import com.hivemq.extensions.gcp.pubsub.api.model.OutboundPubSubMessage;
import com.hivemq.extensions.gcp.pubsub.api.model.PubSubConnection;
import com.hivemq.extensions.gcp.pubsub.api.transformers.MqttToPubSubInitInput;
import com.hivemq.extensions.gcp.pubsub.api.transformers.MqttToPubSubInput;
import com.hivemq.extensions.gcp.pubsub.api.transformers.MqttToPubSubOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * @since 4.9.0
 * @author Florian Limpöck
 */
class MqttToPubSubHelloWorldTransformerTest {

    private @NotNull MqttToPubSubHelloWorldTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new MqttToPubSubHelloWorldTransformer();
    }

    @Test
    void initTransformer_customSettingsSet() {
        final MqttToPubSubInitInput initInput = mock(MqttToPubSubInitInput.class);
        when(initInput.getPubSubConnection()).thenReturn(new TestPubSubConnection());
        when(initInput.getCustomSettings()).thenReturn(new TestCustomSettings(
                List.of(
                        new TestCustomSetting("settings-1", "value-1"),
                        new TestCustomSetting("settings-2", "value-2")
                )
        ));

        transformer.init(initInput);

        assertNotNull(transformer.customSettings);
    }

    @Test
    void initTransformer_initFailed_noException() {
        final MqttToPubSubInitInput initInput = mock(MqttToPubSubInitInput.class);
        when(initInput.getPubSubConnection()).thenReturn(new TestPubSubConnection());
        when(initInput.getCustomSettings()).thenThrow(new RuntimeException("TEST_FAILED"));

        assertDoesNotThrow(() -> transformer.init(initInput));
    }

    @Test
    void transformMessage_noCustomSettings_emptyResult() {
        final MqttToPubSubInput input = mock(MqttToPubSubInput.class);
        final MqttToPubSubOutput output = mock(MqttToPubSubOutput.class);
        final PublishPacket publishPacket = mock(PublishPacket.class);
        when(publishPacket.getTopic()).thenReturn("my/mqtt/topic");
        when(input.getPublishPacket()).thenReturn(publishPacket);
        final AtomicReference<List<OutboundPubSubMessage>> reference = new AtomicReference<>();
        doAnswer((Answer<Void>) invocation -> {
            List<OutboundPubSubMessage> outboundPubSubMessages = invocation.getArgument(0);
            reference.set(outboundPubSubMessages);
            return null;
        }).when(output).setOutboundPubSubMessages(anyList());

        transformer.transformMqttToPubSub(input, output);

        assertEquals(0, reference.get().size());

    }

    @Test
    void transformMessage_failed_noException() {
        final MqttToPubSubInput input = mock(MqttToPubSubInput.class);
        final MqttToPubSubOutput output = mock(MqttToPubSubOutput.class);
        final PublishPacket publishPacket = mock(PublishPacket.class);
        when(publishPacket.getTopic()).thenReturn("my/mqtt/topic");
        when(input.getPublishPacket()).thenThrow(new RuntimeException("TEST_EXCEPTION"));

        assertDoesNotThrow(() -> transformer.transformMqttToPubSub(input, output));

    }

    @Test
    void transformMessage_withCustomSettingsNoTopic_emptyResult() {

        //we init first to get customsettings
        final MqttToPubSubInitInput initInput = mock(MqttToPubSubInitInput.class);
        when(initInput.getPubSubConnection()).thenReturn(new TestPubSubConnection());
        when(initInput.getCustomSettings()).thenReturn(new TestCustomSettings(
                List.of(
                        //we dont set pubsub-topic setting
                        new TestCustomSetting("settings-1", "value-1"),
                        new TestCustomSetting("settings-2", "value-2")
                )
        ));

        transformer.init(initInput);

        final MqttToPubSubInput input = mock(MqttToPubSubInput.class);
        final MqttToPubSubOutput output = mock(MqttToPubSubOutput.class);
        final PublishPacket publishPacket = mock(PublishPacket.class);
        when(publishPacket.getTopic()).thenReturn("my/mqtt/topic");
        when(input.getPublishPacket()).thenReturn(publishPacket);
        final AtomicReference<List<OutboundPubSubMessage>> reference = new AtomicReference<>();
        doAnswer((Answer<Void>) invocation -> {
            List<OutboundPubSubMessage> outboundPubSubMessages = invocation.getArgument(0);
            reference.set(outboundPubSubMessages);
            return null;
        }).when(output).setOutboundPubSubMessages(anyList());

        transformer.transformMqttToPubSub(input, output);

        assertEquals(0, reference.get().size());

    }

    @Test
    void transformMessage_withCustomSettingsMultipleTopics_multiResult() {

        //we init first to get customsettings
        final MqttToPubSubInitInput initInput = mock(MqttToPubSubInitInput.class);
        when(initInput.getPubSubConnection()).thenReturn(new TestPubSubConnection());
        when(initInput.getCustomSettings()).thenReturn(new TestCustomSettings(
                List.of(
                        //we set pubsub-topic setting
                        new TestCustomSetting("pubsub-topic", "topic-1"),
                        new TestCustomSetting("pubsub-topic", "topic-2")
                )
        ));

        transformer.init(initInput);

        final MqttToPubSubInput input = mock(MqttToPubSubInput.class);
        final MqttToPubSubOutput output = mock(MqttToPubSubOutput.class);
        final PublishPacket publishPacket = mock(PublishPacket.class);
        final OutboundPubSubMessageBuilder messageBuilder = mock(OutboundPubSubMessageBuilder.class);
        final UserProperties userProperties = mock(UserProperties.class);
        when(publishPacket.getTopic()).thenReturn("my/mqtt/topic");
        when(publishPacket.getQos()).thenReturn(Qos.AT_LEAST_ONCE);
        when(publishPacket.getUserProperties()).thenReturn(userProperties);
        when(userProperties.asList()).thenReturn(List.of());
        when(input.getPublishPacket()).thenReturn(publishPacket);
        final AtomicReference<List<OutboundPubSubMessage>> reference = new AtomicReference<>();
        doAnswer((Answer<Void>) invocation -> {
            List<OutboundPubSubMessage> outboundPubSubMessages = invocation.getArgument(0);
            reference.set(outboundPubSubMessages);
            return null;
        }).when(output).setOutboundPubSubMessages(anyList());
        when(output.newOutboundPubSubMessageBuilder()).thenReturn(messageBuilder);
        when(messageBuilder.build()).thenReturn(new TestOutboundPubSubMessage());

        transformer.transformMqttToPubSub(input, output);

        assertEquals(2, reference.get().size());

    }

    @Test
    void transformMessage_exceptionForOneMessage_oneResultLess() {

        //we init first to get customsettings
        final MqttToPubSubInitInput initInput = mock(MqttToPubSubInitInput.class);
        when(initInput.getPubSubConnection()).thenReturn(new TestPubSubConnection());
        when(initInput.getCustomSettings()).thenReturn(new TestCustomSettings(
                List.of(
                        //we set pubsub-topic setting
                        new TestCustomSetting("pubsub-topic", "topic-1"),
                        new TestCustomSetting("pubsub-topic", "topic-2")
                )
        ));

        transformer.init(initInput);

        final MqttToPubSubInput input = mock(MqttToPubSubInput.class);
        final MqttToPubSubOutput output = mock(MqttToPubSubOutput.class);
        final PublishPacket publishPacket = mock(PublishPacket.class);
        final OutboundPubSubMessageBuilder messageBuilder = mock(OutboundPubSubMessageBuilder.class);
        final UserProperties userProperties = mock(UserProperties.class);
        when(publishPacket.getTopic()).thenReturn("my/mqtt/topic");
        when(publishPacket.getQos())
                .thenReturn(Qos.AT_LEAST_ONCE)
                .thenThrow(new RuntimeException("TEST_EXCEPTION"));
        when(publishPacket.getUserProperties()).thenReturn(userProperties);
        when(userProperties.asList()).thenReturn(List.of());
        when(input.getPublishPacket()).thenReturn(publishPacket);
        final AtomicReference<List<OutboundPubSubMessage>> reference = new AtomicReference<>();
        doAnswer((Answer<Void>) invocation -> {
            List<OutboundPubSubMessage> outboundPubSubMessages = invocation.getArgument(0);
            reference.set(outboundPubSubMessages);
            return null;
        }).when(output).setOutboundPubSubMessages(anyList());
        when(output.newOutboundPubSubMessageBuilder()).thenReturn(messageBuilder);
        when(messageBuilder.build()).thenReturn(new TestOutboundPubSubMessage());

        transformer.transformMqttToPubSub(input, output);

        assertEquals(1, reference.get().size());

    }

    private static class TestOutboundPubSubMessage implements OutboundPubSubMessage {

        @Override
        public @NotNull String getTopicName() {
            return null;
        }

        @Override
        public @NotNull Map<String, String> getAttributes() {
            return null;
        }

        @Override
        public @NotNull Optional<@Immutable ByteBuffer> getData() {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<byte[]> getDataAsByteArray() {
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<String> getOrderingKey() {
            return Optional.empty();
        }
    }
}