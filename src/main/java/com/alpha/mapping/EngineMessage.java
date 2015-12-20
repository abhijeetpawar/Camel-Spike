package com.alpha.mapping;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class EngineMessage implements Serializable {
    private Map<String, Object> message;

    public EngineMessage() {
        this.message = new LinkedHashMap<>();
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public Object get(String key) {
        Stack<String> stack = predicateStack(key);
        return get(stack, message);
    }

    public void put(String key, Object value) {
        Stack<String> stack = predicateStack(key);
        put(stack, value, message);
    }

    private Object get(Stack<String> stack, Map<String, Object> content) {
        String key = stack.pop();

        Object value = content.get(key);
        if (value == null)
            throw new RuntimeException("No such key found : " + key);

        if (stack.isEmpty()) {
            return value;
        } else {
            if (value instanceof Map) {
                return get(stack, (Map<String, Object>) value);
            } else {
                throw new RuntimeException("Key Path does not exist");
            }
        }
    }

    private void put(Stack<String> stack, Object value, Map<String, Object> content) {
        String key = stack.pop();

        if(stack.isEmpty()) {
            content.put(key, value);
        } else {
            if(!content.containsKey(key)) {
                content.put(key, new LinkedHashMap<>());
            }
            put(stack, value, (Map<String, Object>) content.get(key));
        }
    }

    private Stack<String> predicateStack(String key) {
        String[] elements = key.split("\\.");
        final Stack<String> stack = new Stack<>();
        for (int i = elements.length - 1; i >= 0; i--) {
            stack.push(elements[i]);
        }
        return stack;
    }

    public static EngineMessage from(Map<String, Object> map) {
        EngineMessage result = new EngineMessage();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
