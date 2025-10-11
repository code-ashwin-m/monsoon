package org.monsoon.example.helper;

import org.monsoon.framework.db.interfaces.IdGenerator;

import java.util.UUID;

public class UUIDGenerator implements IdGenerator {
    @Override
    public Object generate() {
        return UUID.randomUUID().toString();
    }
}
