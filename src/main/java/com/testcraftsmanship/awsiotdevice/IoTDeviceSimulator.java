package com.testcraftsmanship.awsiotdevice;

import com.amazonaws.regions.Regions;
import com.testcraftsmanship.awsiotdevice.device.IoTDevice;
import com.testcraftsmanship.awsiotdevice.device.IoTDeviceState;
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

    /**
     * Runs the IoT device simulator. After that method is executed device is listening and able to perform publishing
     * on desired topic.
     */
    public void start() {
        if (iotDeviceIsNotRunning()) {
            iotDevice = new IoTDevice(mqttClientEndpoint, keyStoreSsmParamValue, keyPasswordSsmParamValue);
            iotDevice.publishMessageTo(getPublishedMessagePayload(), getPublishedMessageTopic());
            iotDevice.subscribeTriggerTopicCondition(getSubscribedMessageTopic());
            iotDevice.subscribeTriggerMessageCondition(getSubscribedMessagePayload());
            iotDevice.subscribeTo(getDeviceSubscriptionTopic());
            iotDevice.startSimulation();
        }
    }

    /**
     * Stops the IoT device simulator.
     */
    public void stop() {
        if (iotDevice.getState() == IoTDeviceState.RUNNING) {
            iotDevice.stopSimulation();
        }
    }

    /**
     * Perform publication of defined message to defined MQTT topic
     */
    public void publish() {
        iotDevice.publishMessage();
    }

    /**
     * Method returns true when expected message reaches the expected topic
     *
     * @return information whether expected message reaches correct topic
     */
    public boolean doesExpectedMessageReachedSubscribedTopic() {
        return iotDevice.isExpectedMessagePublished();
    }

    private boolean iotDeviceIsNotRunning() {
        return iotDevice == null || iotDevice.getState() != IoTDeviceState.RUNNING;
    }
}
