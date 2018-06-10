package com.testcraftsmanship.awsiotdevice.iotsettings;

public interface SubscriptionMessageSpecification {
    SubscriptionMessageSpecification messageTopic(String topic);

    SubscriptionMessageSpecification subscribeMessageBody(String body);

    SubscriptionMessageSpecification triggered();

    PublicationMessageSpecification then();
}
