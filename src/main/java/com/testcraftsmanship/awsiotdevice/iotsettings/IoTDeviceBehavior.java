package com.testcraftsmanship.awsiotdevice.iotsettings;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class IoTDeviceBehavior implements DeviceSpecification, PublicationMessageSpecification, SubscriptionMessageSpecification {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoTDeviceBehavior.class);
    private String deviceSubscriptionTopic;
    private String publishedMessageTopic;
    private String publishedMessagePayload;
    private String subscribedMessageTopic;
    private String subscribedMessagePayload;
    private int responseDelayInSeconds;

    public DeviceSpecification given() {
        return this;
    }

    public SubscriptionMessageSpecification when() {
        return this;
    }

    public PublicationMessageSpecification then() {
        return this;
    }

    public String getDeviceSubscriptionTopic() {
        if (deviceSubscriptionTopic != null) {
            return deviceSubscriptionTopic;
        } else if (subscribedMessageTopic != null) {
            return subscribedMessageTopic;
        } else {
            LOGGER.info("There is no subscription topic so device is run in publishing only mode.");
            return null;
        }
    }

    @Override
    public DeviceSpecification withResponseDelay(int seconds) {
        this.responseDelayInSeconds = seconds;
        return this;
    }

    @Override
    public DeviceSpecification subscribeTo(String topic) {
        this.deviceSubscriptionTopic = topic;
        return this;
    }

    @Override
    public PublicationMessageSpecification publishTo(String topic) {
        this.publishedMessageTopic = topic;
        return this;
    }

    @Override
    public PublicationMessageSpecification publishMessageBody(String body) {
        this.publishedMessagePayload = body;
        return this;
    }

    @Override
    public PublicationMessageSpecification inform() {
        return null;
    }

    @Override
    public SubscriptionMessageSpecification messageTopic(String topic) {
        this.subscribedMessageTopic = topic;
        return this;
    }

    @Override
    public SubscriptionMessageSpecification subscribeMessageBody(String body) {
        this.subscribedMessagePayload = body;
        return this;
    }

    @Override
    public SubscriptionMessageSpecification triggered() {
        return this;
    }
}
