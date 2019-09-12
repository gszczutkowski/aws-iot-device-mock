package com.testcraftsmanship.awsiotdevice.device;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.testcraftsmanship.awsiotdevice.aws.AwsException;
import com.testcraftsmanship.awsiotdevice.parser.MessageParser;
import com.testcraftsmanship.awsiotdevice.parser.PayloadMappingException;
import com.testcraftsmanship.awsiotdevice.utils.StringOperations;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.testcraftsmanship.awsiotdevice.utils.StringOperations.minimize;

public class IoTDeviceListener extends AWSIotTopic {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoTDeviceListener.class);
    private AWSIotMqttClient iotPublisher;
    private IoTDeviceData iotDeviceData;
    private boolean expectedInformationReceived = false;

    IoTDeviceListener(IoTDeviceData deviceData, String clientEndpoint,
                      String keyStoreSsmParamValue, String keyPasswordSsmParamValue) {
        super(deviceData.getDeviceSubscriptionTopic());
        this.iotDeviceData = deviceData;
        String awsClientId = StringOperations.generateAwsClientId();
        this.iotPublisher = new AWSIotMqttClient(clientEndpoint, awsClientId,
                keyStoreSsmParamValue,
                keyPasswordSsmParamValue);
        LOGGER.info("Created IoTDeviceListener with client id: {}", awsClientId);
    }

    @Override
    public synchronized void onMessage(AWSIotMessage message) {
        if (MessageParser.containsMaskParams(iotDeviceData.getSubscribeMessageCondition()) && publicationConditionsMet()) {
            try {
                MessageParser messageParser = new MessageParser(iotDeviceData.getSubscribeMessageCondition(),
                        message.getStringPayload(), true);
                String updatedPublicationMessage = messageParser
                        .updateJsonParamsWithValues(iotDeviceData.getPublicationMessage()).toString();
                waitForPublishingResponse();
                publishIotDeviceData(iotDeviceData.getPublicationTopic(), updatedPublicationMessage);
            } catch (PayloadMappingException e) {
                LOGGER.warn(e.getMessage());
            }
        } else if (subscriptionConditionsMet(message) && publicationConditionsMet()) {
            LOGGER.info("Subscribed on topic {}, has received the message {}",
                    message.getTopic(), minimize(message.getStringPayload()));
            waitForPublishingResponse();
            publishIotDeviceData();
        } else if (subscriptionConditionsMet(message) && !publicationConditionsMet()) {
            LOGGER.info("Subscribed on topic {}, has received the message {}",
                    message.getTopic(), minimize(message.getStringPayload()));
        } else {
            LOGGER.warn("Not handled message {} received on topic {}.",
                    minimize(message.getStringPayload()), message.getTopic());
            return;
        }
        expectedInformationReceived = true;
    }

    /**
     * Method return true if message defined in subscribeMessageBody has been reported on messageTopic. Method reports true
     * only once per received message.
     *
     * @return true if message has been received, false if not
     */
    synchronized boolean expectedMessageHasBeenPublished() {
        if (expectedInformationReceived) {
            expectedInformationReceived = false;
            return true;
        }
        return false;
    }

    void connectPublisher() throws AWSIotException {
        if (!iotPublisher.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
            iotPublisher.connect();
        }
    }

    void disconnectPublisher() throws AWSIotException {
        if (iotPublisher.getConnectionStatus().equals(AWSIotConnectionStatus.CONNECTED)) {
            iotPublisher.disconnect();
        }
    }

    private void waitForPublishingResponse() {
        try {
            Thread.sleep(iotDeviceData.getResponseMessageDelayInSeconds());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean publicationConditionsMet() {
        return iotDeviceData.getPublicationTopic() != null && iotDeviceData.getPublicationMessage() != null;
    }

    private boolean subscriptionConditionsMet(AWSIotMessage message) {
        return expectReceiveMessageOnTopic(message)
                || expectReceiveMessageOnly(message)
                || expectReceiveOnTopicOnly(message);
    }

    private boolean expectReceiveMessageOnTopic(AWSIotMessage message) {
        return messageFromExpectedTopic(message) && messageWithExpectedPayload(message);
    }

    private boolean expectReceiveOnTopicOnly(AWSIotMessage message) {
        return messageFromExpectedTopic(message) && iotDeviceData.getSubscribeMessageCondition() == null;
    }

    private boolean expectReceiveMessageOnly(AWSIotMessage message) {
        return messageWithExpectedPayload(message) && iotDeviceData.getSubscribeTopicCondition() == null;
    }


    private boolean messageFromExpectedTopic(AWSIotMessage message) {
        return iotDeviceData.getSubscribeTopicCondition() != null
                && iotDeviceData.getSubscribeTopicCondition().equals(message.getTopic());
    }

    private boolean messageWithExpectedPayload(AWSIotMessage message) {
        if (iotDeviceData.getSubscribeMessageCondition() == null) {
            return false;
        }
        String formattedActualPayload = new JSONObject(message.getStringPayload()).toString();
        String formattedExpectedPayload = new JSONObject(iotDeviceData.getSubscribeMessageCondition()).toString();
        if (MessageParser.containsMaskParams(formattedExpectedPayload)) {
            try {
                MessageParser messageParser = new MessageParser(formattedExpectedPayload, formattedActualPayload, true);
                String expectedPayloadWithUpdatedParams = messageParser
                        .updateJsonParamsWithValues(formattedExpectedPayload).toString();
                return formattedActualPayload.equals(expectedPayloadWithUpdatedParams);
            } catch (PayloadMappingException e) {
                LOGGER.info(e.getMessage());
                return false;
            }
        }

        return formattedExpectedPayload.equals(formattedActualPayload);
    }

    private void publishIotDeviceData(String topic, String payload) {
        try {
            iotPublisher.publish(topic, payload);
            LOGGER.info("Message {} published to topic {}",
                    minimize(payload), topic);
        } catch (AWSIotException e) {
            throw new AwsException("Exception while publishing message from IoTGateway", e);
        }
    }

    private void publishIotDeviceData() {
        publishIotDeviceData(iotDeviceData.getPublicationTopic(), iotDeviceData.getPublicationMessage());
    }
}

