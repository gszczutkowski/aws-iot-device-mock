package com.testcraftsmanship.awsiotdevice.parser;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.testcraftsmanship.awsiotdevice.utils.StringOperations.minimize;

public class MessageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageParser.class);
    private static final String JSON_VALUE_PARAM_REGEXP = "^\\{(.*?)\\}$";
    private final Map<String, JsonValue> jsonParamsWithValues;

    public MessageParser(String jsonParserMask, String jsonSubscribedMessage, boolean strict)
            throws PayloadMappingException {
        jsonParamsWithValues = getParamsValuesFromMessage(
                new JSONObject(jsonSubscribedMessage), new JSONObject(jsonParserMask), strict);
    }

    public JSONObject updateJsonParamsWithValues(String jsonPublishMessage) {
        return updateJsonParamsWithValues(new JSONObject(jsonPublishMessage));
    }

    public JSONObject updateJsonParamsWithValues(JSONObject jsonPublishMessage) {
        String payload = minimize(jsonPublishMessage.toString());
        LOGGER.info("Publish json message to be updated: {}", payload);
        for (Map.Entry<String, JsonValue> paramValue : jsonParamsWithValues.entrySet()) {
            if (doesStringContainsRegexp(payload, getParamRegexp(paramValue.getKey()))) {
                payload = payload.replaceAll(getParamRegexp(paramValue.getKey()), getParamValue(paramValue.getValue()));
                LOGGER.info("Param {} has been found and updated to {}",
                        getParamRegexp(paramValue.getKey()), getParamValue(paramValue.getValue()));
            } else {
                LOGGER.info("Param {} has not been found in publish message so will not be updated",
                        getParamRegexp(paramValue.getKey()));
            }
        }
        LOGGER.info("Updated json message: {}", payload);
        return new JSONObject(payload);
    }

    public static boolean containsMaskParams(String jsonMessage) {
        String standardizedMessage = new JSONObject(jsonMessage).toString();
        return doesStringContainsRegexp(standardizedMessage, ":\"\\{.*?\\}\"");
    }

    private static boolean doesStringContainsRegexp(String text, String regexp) {
        return text.matches("^.*" + regexp + ".*$");
    }

    private static String getParamValue(JsonValue jsonValue) {
        if (jsonValue.getType().equals(JsonValueType.STRING)) {
            return "\"" + jsonValue.getValue() + "\"";
        } else {
            return jsonValue.getValue();
        }
    }

    private static String getParamRegexp(String param) {
        return "\"\\{" + param + "\\}\"";
    }

    private static JsonValueType getValueType(Object object) {
        if (object instanceof String) {
            return JsonValueType.STRING;
        } else if (object instanceof Integer) {
            return JsonValueType.NUMBER;
        } else if (object instanceof Boolean) {
            return JsonValueType.BOOLEAN;
        } else if (object instanceof JSONObject) {
            return JsonValueType.OBJECT;
        } else {
            throw new IllegalArgumentException("Can't recognise the Json value type in the passed object");
        }
    }

    private boolean isJsonObject(Object object) {
        return getValueType(object).equals(JsonValueType.OBJECT);
    }

    private boolean isString(Object object) {
        return getValueType(object).equals(JsonValueType.STRING);
    }

    private Map<String, JsonValue> getParamsValuesFromMessage(JSONObject jsonMessage, JSONObject jsonMask, boolean strict)
            throws PayloadMappingException {
        Map<String, JsonValue> attributesWithValues = new HashMap<>();
        if (strict && !jsonMask.keySet().equals(jsonMessage.keySet())) {
            throw new PayloadMappingException("Mask is not matching the parsed json. Key sets are different:"
                    + jsonMask.keySet() + " " + jsonMessage.keySet());
        }
        for (String key : jsonMask.keySet()) {
            Object messagePart = jsonMessage.get(key);
            Object maskPart = jsonMask.get(key);
            if (isJsonObject(messagePart) && isJsonObject(maskPart)) {
                JSONObject jsonMessageObject = (JSONObject) messagePart;
                JSONObject jsonMaskObject = (JSONObject) maskPart;
                attributesWithValues.putAll(getParamsValuesFromMessage(jsonMessageObject, jsonMaskObject, strict));
            } else {
                attributesWithValues.putAll(extractParamFromPart(messagePart, maskPart));
            }
        }
        return attributesWithValues;
    }

    private Map<String, JsonValue> extractParamFromPart(Object jsonMessagePart, Object jsonMaskPart)
            throws PayloadMappingException {
        Map<String, JsonValue> attributesWithValues = new HashMap<>();
        if (isString(jsonMaskPart)) {
            Pattern pattern = Pattern.compile(JSON_VALUE_PARAM_REGEXP);
            Matcher matcher = pattern.matcher(jsonMaskPart.toString());
            if (matcher.find()) {
                JsonValue jsonMessagePartValue = new JsonValue(getValueType(jsonMessagePart), jsonMessagePart.toString());
                String keyValue = matcher.group(1);
                attributesWithValues.put(keyValue, jsonMessagePartValue);
                return attributesWithValues;
            }
        }
        if (jsonMessagePart.equals(jsonMaskPart)) {
            return attributesWithValues;
        } else {
            throw new PayloadMappingException("Mask is not matching the parsed json. Value for mask "
                    + jsonMaskPart + " differs from message " + jsonMessagePart);
        }
    }
}
