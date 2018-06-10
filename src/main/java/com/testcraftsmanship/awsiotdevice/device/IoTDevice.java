package com.testcraftsmanship.awsiotdevice.device;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.testcraftsmanship.awsiotdevice.aws.AwsException;
import com.testcraftsmanship.awsiotdevice.utils.StringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoTDevice.class);
    private AWSIotMqttClient iotActionsTrigger;
    private IoTDeviceSubscriber iotSubscriptionHandler;
    private IoTDeviceData iotDeviceData;
    private final String mqttClientEndpoint;
    private final String keyStoreSsmParamValue;
    private final String keyPasswordSsmParamValue;

    public IoTDevice(String clientEndpoint, String keyStoreSsmParamValue, String keyPasswordSsmParamValue) {
        String awsClientId = StringGenerator.generateAwsClientId();
        iotDeviceData = new IoTDeviceData();
        this.mqttClientEndpoint = clientEndpoint;
        this.keyStoreSsmParamValue = keyStoreSsmParamValue;
        this.keyPasswordSsmParamValue = keyPasswordSsmParamValue;
        this.iotActionsTrigger = new AWSIotMqttClient(clientEndpoint, StringGenerator.generateAwsClientId(),
                keyStoreSsmParamValue,
                keyPasswordSsmParamValue);
        LOGGER.info("Created IoTDeviceSubscriber with client id: {}", awsClientId);
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

    public void startSimulation() {
        try {
            if (!iotActionsTrigger.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
                iotActionsTrigger.connect();
            }
            if (isDeviceRespondingOnTopic()) {
                iotSubscriptionHandler = new IoTDeviceSubscriber(iotDeviceData,
                        mqttClientEndpoint, keyStoreSsmParamValue, keyPasswordSsmParamValue);

                iotActionsTrigger.subscribe(iotSubscriptionHandler);
                iotSubscriptionHandler.connectPublisher();
                LOGGER.info("Start IoT Device simulation in Subscribe-Publish mode");
            } else if (isDevicePublishingOnly()) {
                LOGGER.info("Start IoT Device simulation in Publish mode");
            }
        } catch (AWSIotException e) {
            throw new AwsException("Exception while stopping IoT Device simulator", e);
        }
    }

    public void stopSimulation() {
        try {
            if (iotSubscriptionHandler != null) {
                iotSubscriptionHandler.disconnectPublisher();
            }
            if (iotActionsTrigger.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
                iotActionsTrigger.disconnect();
            }
            LOGGER.info("IoT Device simulation stopped");
        } catch (AWSIotException e) {
            throw new AwsException("Exception while stopping IoT Device simulator", e);
        }
    }

    public void publishMessage() {
        try {
            iotActionsTrigger.publish(iotDeviceData.getPublicationTopic(), iotDeviceData.getPublicationMessage());
            LOGGER.info("Triggered publishing to topic: {}", iotDeviceData.getPublicationTopic());
        } catch (AWSIotException e) {
            throw new AwsException("Unable to publish message to topic: " + iotDeviceData.getPublicationTopic(), e);
        }
    }

    private boolean isDeviceSubscribedOnTopic() {
        return iotDeviceData.getDeviceSubscriptionTopic() != null;
    }

    private boolean isDevicePublishingOnTopic() {
        return iotDeviceData.getPublicationTopic() != null
                && iotDeviceData.getPublicationMessage() != null;
    }

    private boolean isDeviceRespondingOnTopic() {
        return isDeviceSubscribedOnTopic() && isDevicePublishingOnTopic();
    }

    private boolean isDevicePublishingOnly() {
        return !isDeviceSubscribedOnTopic() && isDevicePublishingOnTopic();
    }

    private boolean isDeviceSubscribedOnly() {
        return isDeviceSubscribedOnTopic() && !isDevicePublishingOnTopic();
    }
}
