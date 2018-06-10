package com.testcraftsmanship.awsiotdevice.device;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@Getter
@Setter
public class IoTDeviceData {
    private String deviceSubscriptionTopic = null;
    private String publicationTopic = null;
    private String publicationMessage = null;
    private String subscribeTopicCondition = null;
    private String subscribeMessageCondition = null;

    public void setPublicationMessage(String message) {
        publicationMessage = new JSONObject(message).toString();
    }
}
