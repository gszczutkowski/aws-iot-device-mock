package com.testcraftsmanship.awsiotdevice.device;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.testcraftsmanship.awsiotdevice.aws.AwsException;
import com.testcraftsmanship.awsiotdevice.parser.MessageParser;
import com.testcraftsmanship.awsiotdevice.utils.StringOperations;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoTDevice.class);
    private AWSIotMqttClient iotActionsTrigger;
    private IoTDeviceListener ioTDeviceListener;
    private IoTDeviceData iotDeviceData;
    private final String mqttClientEndpoint;
    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    @Getter
    private IoTDeviceState state;

    public IoTDevice(String clientEndpoint, String awsAccessKeyId, String awsSecretAccessKey) {
        String awsClientId = StringOperations.generateAwsClientId();
        iotDeviceData = new IoTDeviceData();
        this.mqttClientEndpoint = clientEndpoint;
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.iotActionsTrigger = new AWSIotMqttClient(clientEndpoint, awsClientId,
                awsAccessKeyId,
                awsSecretAccessKey);
        LOGGER.info("Created IoTDeviceListener with client id: {}", awsClientId);
    }

    public void publishMessageTo(String message, String topic) {
        iotDeviceData.setPublicationMessage(message);
        iotDeviceData.setPublicationTopic(topic);
    }

    public void subscribeTo(String topic) {
        iotDeviceData.setDeviceSubscriptionTopic(topic);
    }

    public void subscribeTriggerMessageCondition(String messageCondition) {
        iotDeviceData.setSubscribeMessageCondition(messageCondition);
    }

    public void subscribeTriggerTopicCondition(String topicCondition) {
        iotDeviceData.setSubscribeTopicCondition(topicCondition);
    }

    public void setResponseMessageDelayInSeconds(int delayInSeconds) {
        iotDeviceData.setResponseMessageDelayInSeconds(delayInSeconds);
    }

    public synchronized void startSimulation() {
        if (iotActionsTrigger.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
            LOGGER.info("IoT Device with id {} is already running.", iotActionsTrigger.getClientId());
            return;
        }
        try {
            iotActionsTrigger.connect();
            if (isDeviceRespondingOnMessage()) {
                ioTDeviceListener = new IoTDeviceListener(iotDeviceData,
                        mqttClientEndpoint, awsAccessKeyId, awsSecretAccessKey);
                iotActionsTrigger.subscribe(ioTDeviceListener);
                ioTDeviceListener.connectPublisher();
                state = IoTDeviceState.RUNNING;
                LOGGER.info("Start IoT Device simulation in Subscribe-Publish mode");
            } else if (isDevicePublishingOnly()) {
                state = IoTDeviceState.RUNNING;
                LOGGER.info("Start IoT Device simulation in Publish mode");
            } else if (isDeviceSubscribedOnly()) {
                ioTDeviceListener = new IoTDeviceListener(iotDeviceData,
                        mqttClientEndpoint, awsAccessKeyId, awsSecretAccessKey);
                iotActionsTrigger.subscribe(ioTDeviceListener);
                state = IoTDeviceState.RUNNING;
                LOGGER.info("Start IoT Device simulation in Subscribing mode");
            }
        } catch (AWSIotException e) {
            throw new AwsException("Exception while stopping IoT Device simulator", e);
        }
    }

    public synchronized void stopSimulation() {
        try {
            if (ioTDeviceListener != null) {
                ioTDeviceListener.disconnectPublisher();
            }
            if (iotActionsTrigger.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
                iotActionsTrigger.disconnect();
            }
            state = IoTDeviceState.STOPPED;
            LOGGER.info("IoT Device simulation stopped");
        } catch (AWSIotException e) {
            throw new AwsException("Exception while stopping IoT Device simulator", e);
        }
    }

    public void closeSimulation() {
        stopSimulation();
        this.iotDeviceData = new IoTDeviceData();
    }

    public void publishMessage() {
        if (!canPublishOnDemand()) {
            throw new IllegalStateException(
                    "Device has not defined publication message/topic or publication message is parametrized.");
        }
        try {
            iotActionsTrigger.publish(iotDeviceData.getPublicationTopic(), iotDeviceData.getPublicationMessage());
            LOGGER.info("Publishing message {} on topic: {}",
                    iotDeviceData.getPublicationMessage(), iotDeviceData.getPublicationTopic());
        } catch (AWSIotException e) {
            throw new AwsException("Unable to publish message to topic: " + iotDeviceData.getPublicationTopic(), e);
        }
    }

    public boolean isExpectedMessageOnSubscribedTopic() {
        if (isDeviceSubscribedOnTopic()) {
            return ioTDeviceListener.expectedMessageHasBeenPublished();
        } else {
            throw new IllegalStateException("Device is not subscribed to any topic.");
        }
    }

    private boolean canPublishOnDemand() {
        return (isDeviceRespondingOnMessage() || isDevicePublishingOnly())
                && !MessageParser.containsMaskParams(iotDeviceData.getPublicationMessage());
    }

    private boolean isDeviceRespondingOnMessage() {
        return isDeviceSubscribedOnTopic() && isDevicePublishingOnTopic();
    }

    private boolean isDevicePublishingOnly() {
        return !isDeviceSubscribedOnTopic() && isDevicePublishingOnTopic();
    }

    private boolean isDeviceSubscribedOnly() {
        return isDeviceSubscribedOnTopic() && !isDevicePublishingOnTopic();
    }

    private boolean isDeviceSubscribedOnTopic() {
        return iotDeviceData.getDeviceSubscriptionTopic() != null;
    }

    private boolean isDevicePublishingOnTopic() {
        return iotDeviceData.getPublicationTopic() != null
                && iotDeviceData.getPublicationMessage() != null;
    }
}
