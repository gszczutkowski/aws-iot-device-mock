package com.testcraftsmanship.awsiotdevice.mqttintegration;

import com.amazonaws.regions.Regions;
import com.testcraftsmanship.awsiotdevice.IoTDeviceSimulator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.testcraftsmanship.awsiotdevice.aws.AwsSsmClient.getSsmParameterValue;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example usage in the tests
 */
public class MqttIntegrationTest {
    private static final String IOT_ENDPOINT_SSM_PARAM = "testIotEndpoint"; //This parameter in AWS parameter store keeps IoT endpoint
    private static final String AWS_ACCESS_KEY_ID_SSM_PARAM = "testAccessKeyId"; //This parameter in AWS parameter store keeps AWS access key id
    private static final String AWS_SECRET_ACCESS_KEY_SSM_PARAM = "testSecretAccessKey"; //This parameter in AWS parameter store keeps AWS secret access key
    private IoTDeviceSimulator deviceSimulator;

    @Before
    public void setUp() {
        deviceSimulator = ioTDeviceSimulator();
    }

    @Test
    public void expectedMessageHasBeenSendByTheApplication() {
        deviceSimulator
                .when()
                .messageTopic("tc/flat/settings/report")
                .subscribeMessageBody("{'id': 2, 'state': 'running'}");

        deviceSimulator.start();
        testedApplicationSendMessageToMqtt();
        assertThat(deviceSimulator.doesExpectedMessageReachedSubscribedTopic()).isTrue();
    }

    @After
    public void tearDown() {
        deviceSimulator.stop();
    }

    private IoTDeviceSimulator ioTDeviceSimulator() {
        return new IoTDeviceSimulator(
                getSsmParameterValue(Regions.EU_WEST_1, IOT_ENDPOINT_SSM_PARAM),
                Regions.EU_WEST_1,
                AWS_ACCESS_KEY_ID_SSM_PARAM,
                AWS_SECRET_ACCESS_KEY_SSM_PARAM);
    }

    /**
     * In real example this method can be replaced with e.g. selenium web driver actions on tested application which
     * triggers sending expected message to the mqtt queue.
     */
    private void testedApplicationSendMessageToMqtt() {
        IoTDeviceSimulator ioTDeviceSimulator2 = ioTDeviceSimulator();
        ioTDeviceSimulator2
                .then()
                .publishTo("tc/flat/settings/report")
                .publishMessageBody("{'id': 2, 'state': 'running'}");

        ioTDeviceSimulator2.publishOnce();
    }
}
