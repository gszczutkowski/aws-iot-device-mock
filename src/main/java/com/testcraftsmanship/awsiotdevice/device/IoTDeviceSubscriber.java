package com.testcraftsmanship.awsiotdevice.device;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.testcraftsmanship.awsiotdevice.aws.AwsException;
import com.testcraftsmanship.awsiotdevice.utils.StringGenerator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTDeviceSubscriber extends AWSIotTopic {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoTDeviceSubscriber.class);
    private AWSIotMqttClient iotPublisher;
    private IoTDeviceData iotDeviceData;

    public IoTDeviceSubscriber(IoTDeviceData deviceData, String clientEndpoint,
                               String keyStoreSsmParamValue, String keyPasswordSsmParamValue) {
        super(deviceData.getDeviceSubscriptionTopic());
        this.iotDeviceData = deviceData;
        String awsClientId = StringGenerator.generateAwsClientId();
        this.iotPublisher = new AWSIotMqttClient(clientEndpoint, awsClientId,
                keyStoreSsmParamValue,
                keyPasswordSsmParamValue);
        LOGGER.info("Created IoTDeviceSubscriber with client id: {}", awsClientId);
    }

    public void connectPublisher() throws AWSIotException {
        if (!iotPublisher.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
            iotPublisher.connect();
        }
    }

    public void disconnectPublisher() throws AWSIotException {
        if (iotPublisher.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
            iotPublisher.disconnect();
        }
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        try {
            if (publicationConditionsMet(message)) {
                iotPublisher.publish(iotDeviceData.getPublicationTopic(), iotDeviceData.getPublicationMessage());
                LOGGER.info("Message published to topic {}", iotDeviceData.getPublicationTopic());
            }
        } catch (AWSIotException e) {
            throw new AwsException("Exception while publishing message from IoTGateway", e);
        }
    }

    private boolean publicationConditionsMet(AWSIotMessage message) {
        return noPublicationRequirements()
                || messageWithExpectedTopicAndMessage(message)
                || messageWithExpectedMessageOnly(message)
                || messageWithExpectedTopicOnly(message);
    }

    private boolean messageFromExpectedTopic(AWSIotMessage message) {
        return (iotDeviceData.getSubscribeTopicCondition() != null
                && iotDeviceData.getSubscribeTopicCondition().equals(message.getTopic()));
    }

    private boolean messageWithExpectedPayload(AWSIotMessage message) {
        String formattedActualPayload = new JSONObject(message.getStringPayload()).toString();
        String formattedExpectedPayload = new JSONObject(iotDeviceData.getSubscribeMessageCondition()).toString();

        return (iotDeviceData.getSubscribeMessageCondition() != null
                && formattedExpectedPayload.equals(formattedActualPayload));
    }

    private boolean noPublicationRequirements() {
        return (iotDeviceData.getSubscribeTopicCondition() == null
                && iotDeviceData.getSubscribeMessageCondition() == null);
    }

    private boolean messageWithExpectedTopicAndMessage(AWSIotMessage message) {
        return messageFromExpectedTopic(message) && messageWithExpectedPayload(message);
    }

    private boolean messageWithExpectedTopicOnly(AWSIotMessage message) {
        return messageFromExpectedTopic(message) && iotDeviceData.getSubscribeMessageCondition() == null;
    }

    private boolean messageWithExpectedMessageOnly(AWSIotMessage message) {
        return messageWithExpectedPayload(message) && iotDeviceData.getSubscribeTopicCondition() == null;
    }
}

