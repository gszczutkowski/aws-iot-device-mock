package com.testcraftsmanship.awsiotdevice;

import com.amazonaws.regions.Regions;
import com.testcraftsmanship.awsiotdevice.device.IoTDevice;
import com.testcraftsmanship.awsiotdevice.iotsettings.DeviceRunnable;
import com.testcraftsmanship.awsiotdevice.iotsettings.IoTDeviceBehavior;

import static com.testcraftsmanship.awsiotdevice.aws.AwsSsmClient.getSsmParameterValue;

public class IoTDeviceSimulator extends IoTDeviceBehavior implements DeviceRunnable {
    private IoTDevice iotDevice;
    private final String mqttClientEndpoint;
    private final String keyStoreSsmParamValue;
    private final String keyPasswordSsmParamValue;

    public IoTDeviceSimulator(Regions awsSsmRegion, String clientEndpoint, String keyStoreSsmParam, String keyPasswordSsmParam) {
        this.mqttClientEndpoint = clientEndpoint;
        this.keyStoreSsmParamValue = getSsmParameterValue(awsSsmRegion, keyStoreSsmParam);
        this.keyPasswordSsmParamValue = getSsmParameterValue(awsSsmRegion, keyPasswordSsmParam);
    }

    public void start() {
        iotDevice = new IoTDevice(mqttClientEndpoint, keyStoreSsmParamValue, keyPasswordSsmParamValue);
        iotDevice.publishMessageTo(getPublishedMessagePayload(), getPublishedMessageTopic());
        iotDevice.subscribeTriggerTopicCondition(getSubscribedMessageTopic());
        iotDevice.subscribeTriggerMessageCondition(getSubscribedMessagePayload());
        iotDevice.subscribeTo(getDeviceSubscriptionTopic());
        iotDevice.startSimulation();
    }

    public void stop() {
        iotDevice.stopSimulation();
    }

    public void publish() {
        iotDevice.publishMessage();
    }

}
