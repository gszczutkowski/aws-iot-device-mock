# AWS-IoT-Device-Mock

AWS-IoT-Device-Mock is a java library which can mock AWS IoT devices connected to MQTT in your tests. You can easily define behaviors of this device:

  - publish defined message to mqtt topic when trigered
  - publish defined message when other defined message reach other mqtt topic

### How to use it

To create IoTDeviceSimulator four arguments have to be passed to the construction: you AWS region, endpoint of your AWS IoT Client, SSM parameter which stores Access Key and SSM parameter which stores Secret Key 
```java
IoTDeviceSimulator deviceSimulator = new IoTDeviceSimulator(
             new Regions("your-aws-region"),
             "your-aws-iot-client-endpoint",
             "ssm-param-for-access-key",
             "ssm-param-for-secret-key");
```

Below is the example use of the IoT Device Mock which will publish {'id': 2, 'fan': 1, 'light': 10, 'door':1} to topic 
tc/flatkrk100/settings/report when message {'id': 2, 'fan': 1} will be published to topic tc/flatkrk100/settings/set.  
```java
deviceSimulator
        .when()
            .messageTopic("tc/flatkrk100/settings/set")
            .subscribeMessageBody("{'id': 2, 'fan': 1}")
        .then()
            .publishTo("tc/flatkrk100/settings/report")
            .publishMessageBody("{'id': 2, 'fan': 1, 'light': 10, 'door':1}");

deviceSimulator.start();

    // here should be test of application in which message {'id': 2, 'fan': 1} should be send to 
    // tc/flatkrk100/settings/set topic and the answer of the IoT device should be resend to 
    // tc/flatkrk100/settings/report topic with message {'id': 2, 'fan': 1, 'light': 10, 'door':1}

deviceSimulator.stop();
```