package com.testcraftsmanship.awsiotdevice;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.testcraftsmanship.awsiotdevice.device.IoTDevice;
import com.testcraftsmanship.awsiotdevice.device.IoTDeviceState;
import com.testcraftsmanship.awsiotdevice.iotsettings.DeviceRunnable;
import com.testcraftsmanship.awsiotdevice.iotsettings.IoTDeviceBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.testcraftsmanship.awsiotdevice.aws.AwsSsmClient.getSsmParameterValue;

public class IoTDeviceSimulator extends IoTDeviceBehavior implements DeviceRunnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoTDeviceSimulator.class);
    private IoTDevice iotDevice;
    private final String mqttClientEndpoint;
    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;

    /**
     * Create new instance of IoTDeviceSimulator which uses AWS credentials(AWS Access Key Id, AWS Secret Access Key)
     * which are saved in AWS parameter store in parameters passed as an arguments of this constructor. To extract values
     * of those parameters we are using Default AWS Credential Provider Chain.
     *
     * @param clientEndpoint of IoT service to which IoTDeviceSimulator will be connecting to
     * @param awsSsmRegion where parameters are stored
     * @param awsAccessKeyIdSsmParam name of the parameter which stores AWS Access Key Id
     * @param awsSecretAccessKeySsmParam name of the parameter which stores AWS Secret Access Key
     */
    public IoTDeviceSimulator(String clientEndpoint, Regions awsSsmRegion,
                              String awsAccessKeyIdSsmParam, String awsSecretAccessKeySsmParam) {
        this.mqttClientEndpoint = clientEndpoint;
        this.awsAccessKeyId = getSsmParameterValue(awsSsmRegion, awsAccessKeyIdSsmParam);
        this.awsSecretAccessKey = getSsmParameterValue(awsSsmRegion, awsSecretAccessKeySsmParam);
    }

    /**
     * Create new instance of IoTDeviceSimulator which uses AWS credentials(AWS Access Key Id, AWS Secret Access Key)
     * passed as an arguments of the constructor.
     *
     * @param clientEndpoint of IoT service to which IoTDeviceSimulator will be connecting to
     * @param awsAccessKeyId value of the AWS Access Key Id
     * @param awsSecretAccessKey value of the AWS Secret Access Key
     */
    public IoTDeviceSimulator(String clientEndpoint, String awsAccessKeyId, String awsSecretAccessKey) {
        this.mqttClientEndpoint = clientEndpoint;
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    /**
     * Create new instance of IoTDeviceSimulator which uses default AWS credentials typically located at ~/.aws/credentials
     * and shared by many of the AWS SDKs and by the AWS CLI. The AWS SDK for Java uses the ProfileCredentialsProvider to
     * load these credentials in this constructor.
     *
     * You can create a credentials file by using the aws configure command provided by the AWS CLI, or you can create it
     * by editing the file with a text editor. For information about the credentials file format, see AWS Credentials File Format.
     *
     * @param clientEndpoint of IoT service to which IoTDeviceSimulator will be connecting to
     */
    public IoTDeviceSimulator(String clientEndpoint) {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        this.mqttClientEndpoint = clientEndpoint;
        this.awsAccessKeyId = credentialsProvider.getCredentials().getAWSAccessKeyId();
        this.awsSecretAccessKey = credentialsProvider.getCredentials().getAWSSecretKey();
    }

    /**
     * Runs the IoT device simulator. After that method is executed device is listening and able to perform publishing
     * on desired topic.
     */
    public void start() {
        if (iotDeviceIsNotRunning()) {
            iotDevice = new IoTDevice(mqttClientEndpoint, awsAccessKeyId, awsSecretAccessKey);
            iotDevice.publishMessageTo(getPublishedMessagePayload(), getPublishedMessageTopic());
            iotDevice.subscribeTriggerTopicCondition(getSubscribedMessageTopic());
            iotDevice.subscribeTriggerMessageCondition(getSubscribedMessagePayload());
            iotDevice.subscribeTo(getDeviceSubscriptionTopic());
            iotDevice.setResponseMessageDelayInSeconds(getResponseDelayInSeconds());
            iotDevice.startSimulation();
        } else {
            LOGGER.warn("IoT Simulator is already running. Staring is redundant.");
        }
    }

    /**
     * Stops the IoT device simulator.
     */
    public void stop() {
        if (iotDeviceIsNotRunning()) {
            LOGGER.info("IoT Simulator is not running so it can't be stopped.");
        } else {
            iotDevice.stopSimulation();
        }
    }

    /**
     * Stops the IoT device simulator and clear all settings connected to subscribe/publish topic and message.
     */
    public void close() {
        if (iotDeviceIsNotRunning()) {
            LOGGER.info("IoT Simulator is not running so it can't be stopped.");
        } else {
            iotDevice.closeSimulation();
        }
    }

    /**
     * Perform publication of defined message to defined MQTT topic
     */
    public void publish() {
        iotDevice.publishMessage();
    }

    /**
     *  Runs the IoT device simulator, publishes the defined message and stops simulator.
     */
    public void publishOnce() {
        start();
        publish();
        close();
    }

    /**
     * Method returns true when expected message reaches the expected topic
     *
     * @return information whether expected message reaches correct topic
     */
    public boolean doesExpectedMessageReachedSubscribedTopic() {
        return iotDevice.isExpectedMessageOnSubscribedTopic();
    }

    private boolean iotDeviceIsNotRunning() {
        return iotDevice == null || iotDevice.getState() != IoTDeviceState.RUNNING;
    }
}
