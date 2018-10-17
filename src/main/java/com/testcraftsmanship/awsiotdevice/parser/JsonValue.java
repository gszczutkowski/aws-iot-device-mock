package com.testcraftsmanship.awsiotdevice.parser;

import lombok.Getter;

@Getter
public class JsonValue {
    private JsonValueType type;
    private String value;

    public JsonValue(JsonValueType type, String value) {
        this.type = type;
        this.value = value;
    }
}
