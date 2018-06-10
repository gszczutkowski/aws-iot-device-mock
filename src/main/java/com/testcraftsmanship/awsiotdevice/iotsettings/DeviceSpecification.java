package com.testcraftsmanship.awsiotdevice.iotsettings;

public interface DeviceSpecification {
    DeviceSpecification subscribeTo(String topic);

    SubscriptionMessageSpecification when();

}
