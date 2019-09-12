# AWS-IoT-Device-Mock

AWS-IoT-Device-Mock is a java library which can mock AWS IoT devices connected to MQTT in your tests. You can easily define behaviors of this device:

  - publish defined message to MQTT topic when triggered
  - publish defined message when other defined message reach other MQTT topic

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

The next example ilustrate how to force simulator to pass some information from message it gets as a subscriber to publishing message. Note that values written in the curly brackets in the body of when section are treated as an values to get. Values of those fields are written to field with value between curly brackets. The values with the curly brackets in the body of then section will be filled in with value from subscribed message with the same tag name. So in the example below when we get message {'id':2, 'state':0} then we will publish {'id': 2, 'fan': 0, 'light': 10, 'door':1}.

```java
deviceSimulator
        .when()
            .messageTopic("tc/flatkrk100/settings/set")
            .subscribeMessageBody("{'id': 2, 'state': '{fan_state}'}")
        .then()
            .publishTo("tc/flatkrk100/settings/report")
            .publishMessageBody("{'id': 2, 'fan': '{fan_state}', 'light': 10, 'door':1}");

deviceSimulator.start();

    // here should be test of application in which message with any state value  (e.g. {'id': 2, 'state': 1}) should be send to 
    // tc/flatkrk100/settings/set topic and the answer of the IoT device should be resend to 
    // tc/flatkrk100/settings/report topic with message {'id': 2, 'fan': 1, 'light': 10, 'door':1} so the value from state argument is
    // rewritten to fan argument.

deviceSimulator.stop();
```

With device simulator we can also verify whether expected message reached expected topic. In this example we can alos use curly brackets when values of some arguments are not important but just number and names of the arguments.


```java
deviceSimulator
        .when()
            .messageTopic("tc/flatkrk100/settings/set")
            .subscribeMessageBody("{'id': 2, 'state': 1}")

deviceSimulator.start();

    // here should be test of application in which message  {'id': 2, 'state': 1}) should be send to 
    // tc/flatkrk100/settings/set topic. 
    
assertTrue(deviceSimulator.doesExpectedMessageReachedSubscribedTopic());
deviceSimulator.stop();
```

```java
deviceSimulator
        .when()
            .messageTopic("tc/flatkrk100/settings/set")
            .subscribeMessageBody("{'id': 2, 'state': '{fan_state}'}")

deviceSimulator.start();

    // here should be test of application in which message with any state value should be send to 
    // tc/flatkrk100/settings/set topic. So e.g. messages {'id': 2, 'state': 1} or {'id': 2, 'state': 0} are valid. but
    // {'id': 1, 'state': 1} is not.
    
assertTrue(deviceSimulator.doesExpectedMessageReachedSubscribedTopic());
deviceSimulator.stop();
```

When we want our response message to be send with given delay then we have to add given() section like shown below:
```java
deviceSimulator
        .given()
            .responseMessageDelay(2)
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
    // two seconds after receiving the message

deviceSimulator.stop();
```

