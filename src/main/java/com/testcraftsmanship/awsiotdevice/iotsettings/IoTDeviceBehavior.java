package com.testcraftsmanship.awsiotdevice.iotsettings;

import lombok.Getter;

@Getter
public class IoTDeviceBehavior implements DeviceSpecification, PublicationMessageSpecification, SubscriptionMessageSpecification {
    private String deviceSubscriptionTopic;
    private String publishedMessageTopic;
    private String publishedMessagePayload;
    private String subscribedMessageTopic;
    private String subscribedMessagePayload;

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
            throw new IllegalStateException("Subscription topic has not been set");
        }
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
