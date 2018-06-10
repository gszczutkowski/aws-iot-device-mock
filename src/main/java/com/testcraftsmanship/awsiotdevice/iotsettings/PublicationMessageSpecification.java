package com.testcraftsmanship.awsiotdevice.iotsettings;

public interface PublicationMessageSpecification {
    PublicationMessageSpecification publishTo(String topic);

    PublicationMessageSpecification publishMessageBody(String body);
}
