package com.testcraftsmanship.awsiotdevice.parser;

import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageParserTest {
    @Test
    public void parserShouldUpdateBooleanParam() throws PayloadMappingException {
        final boolean valueToBeUpdated = true;
        final String mask = "{'uuid':'af234fad48vb', 'data':{'running': '{state}', 'reason': 'update'}}";
        final String subscribeMessage = "{'uuid':'af234fad48vb', 'data': {'running': " + valueToBeUpdated + ", 'reason': 'update'}}";
        final String publishMessage = "{'device_state': '{state}', 'response': 'ok'}";
        final String expectedPublishMessage = "{'device_state': " + valueToBeUpdated + ", 'response': 'ok'}";

        MessageParser messageParser = new MessageParser(mask, subscribeMessage, true);
        JSONObject updatedPublishMessage = messageParser.updateJsonParamsWithValues(publishMessage);

        JSONAssert.assertEquals(expectedPublishMessage, updatedPublishMessage, true);
    }

    @Test
    public void parserShouldUpdateNumericParam() throws PayloadMappingException {
        final int valueToBeUpdated = 17;
        final String mask = "{'uuid':'af234fad48vb', 'data':{'id': '{id}', 'reason': 'update'}}";
        final String subscribeMessage = "{'uuid':'af234fad48vb', 'data': {'id': " + valueToBeUpdated + ", 'reason': 'update'}}";
        final String publishMessage = "{'device_id': '{id}', 'response': 'ok'}";
        final String expectedPublishMessage = "{'device_id': " + valueToBeUpdated + ", 'response': 'ok'}";

        MessageParser messageParser = new MessageParser(mask, subscribeMessage, true);
        JSONObject updatedPublishMessage = messageParser.updateJsonParamsWithValues(publishMessage);

        JSONAssert.assertEquals(expectedPublishMessage, updatedPublishMessage, true);
    }

    @Test
    public void parserShouldUpdateStringParam() throws PayloadMappingException {
        final String valueToBeUpdated = "e788e700-7e13-4a12-852e-cbfd830dfc2d";
        final String mask = "{'uuid':'{id}', 'data':{'id': 10, 'reason': 'update'}}";
        final String subscribeMessage = "{'uuid':'" + valueToBeUpdated + "', 'data': {'id': 10, 'reason': 'update'}}";
        final String publishMessage = "{'device_uuid': '{id}', 'response': 'ok'}";
        final String expectedPublishMessage = "{'device_uuid': " + valueToBeUpdated + ", 'response': 'ok'}";

        MessageParser messageParser = new MessageParser(mask, subscribeMessage, true);
        JSONObject updatedPublishMessage = messageParser.updateJsonParamsWithValues(publishMessage);

        JSONAssert.assertEquals(expectedPublishMessage, updatedPublishMessage, true);
    }

    @Test
    public void parserShouldUpdateParamInNonStrictMode() throws PayloadMappingException {
        final String valueToBeUpdated = "e788e700-7e13-4a12-852e-cbfd830dfc2d";
        final String mask = "{'uuid':'{id}'}";
        final String subscribeMessage = "{'uuid':'" + valueToBeUpdated + "', 'data': {'id': 10, 'reason': 'update'}}";
        final String publishMessage = "{'device_uuid': '{id}', 'response': 'ok'}";
        final String expectedPublishMessage = "{'device_uuid': " + valueToBeUpdated + ", 'response': 'ok'}";

        MessageParser messageParser = new MessageParser(mask, subscribeMessage, false);
        JSONObject updatedPublishMessage = messageParser.updateJsonParamsWithValues(publishMessage);

        JSONAssert.assertEquals(expectedPublishMessage, updatedPublishMessage, true);
    }

    @Test(expected = PayloadMappingException.class)
    public void parserShouldNotUpdateParamInStrictModeWhenMaskHasDifferentStructure() throws PayloadMappingException {
        final String valueToBeUpdated = "e788e700-7e13-4a12-852e-cbfd830dfc2d";
        final String mask = "{'uuid':'{id}'}";
        final String subscribeMessage = "{'uuid':'" + valueToBeUpdated + "', 'data': {'id': 10, 'reason': 'update'}}";

        new MessageParser(mask, subscribeMessage, true);
    }

    @Test
    public void parserShouldUpdateMultipleParams() throws PayloadMappingException {
        final String stringValueToBeUpdated = "e788e700-7e13-4a12-852e-cbfd830dfc2d";
        final int numberValueToBeUpdated = 22;
        final boolean booleanValueToBeUpdated = true;
        final String mask = "{'uuid':'{uuid}', 'data':{'id': '{id}', 'state': '{running}'}}";
        final String subscribeMessage = "{'uuid':'" + stringValueToBeUpdated + "', 'data': {'id': " + numberValueToBeUpdated + ", 'state': " + booleanValueToBeUpdated + "}}";
        final String publishMessage = "{'device_uuid': '{uuid}', 'response': '{running}', 'state': '{running}', 'name': '{id}'}";
        final String expectedPublishMessage = "{'device_uuid': '" + stringValueToBeUpdated + "', 'response': " + booleanValueToBeUpdated + ", 'state': " + booleanValueToBeUpdated + ", 'name': " + numberValueToBeUpdated + "}";

        MessageParser messageParser = new MessageParser(mask, subscribeMessage, true);
        JSONObject updatedPublishMessage = messageParser.updateJsonParamsWithValues(publishMessage);

        JSONAssert.assertEquals(expectedPublishMessage, updatedPublishMessage, true);
    }

    @Test
    public void parserShouldIdentifyParamInJson() {
        final String publishMessage = "{'device_uuid': 'e788e700-7e13-4a12-852e-cbfd830dfc2d', 'response': '{running}', 'state': 'idle', 'name': 4}";

        boolean containsParam = MessageParser.containsMaskParams(publishMessage);
        assertThat(containsParam).isTrue();
    }

    @Test
    public void parserShouldIdentifyParamInJsonWithDifferentFormat() {
        final String publishMessage = "{\"device_uuid\": \"e788e700-7e13-4a12-852e-cbfd830dfc2d\", \"response\": \"{running}\", \"state\": \"idle\", \"name\": 4}";

        boolean containsParam = MessageParser.containsMaskParams(publishMessage);
        assertThat(containsParam).isTrue();
    }

    @Test
    public void parserShouldIdentifyWhenNoParamInJson() {
        final String publishMessage = "{'device_uuid': 'e788e700-7e13-4a12-852e-cbfd830dfc2d', 'response': \"starting\", 'state': 'idle', 'name': 4}";

        boolean containsParam = MessageParser.containsMaskParams(publishMessage);
        assertThat(containsParam).isFalse();
    }

    @Test
    public void parserShouldIdentifyWhenNoParamInJsonWithDifferentFormat() {
        final String publishMessage = "{\"device_uuid\": \"e788e700-7e13-4a12-852e-cbfd830dfc2d\", \"response\": \"starting\", \"state\": \"idle\", \"name\": 4}";

        boolean containsParam = MessageParser.containsMaskParams(publishMessage);
        assertThat(containsParam).isFalse();
    }

}
