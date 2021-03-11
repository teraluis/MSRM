package models;


import io.ebean.config.IdGenerator;

import java.util.UUID;

/**
 * A customer Id Generator that can be assigned by @GeneratedValue(generator="shortUid")
 */
public class ModUuidGenerator implements IdGenerator {

    @Override
    public Object nextValue() {
        return UUID.randomUUID();
    }

    @Override
    public String getName() {
        return "shortUid";
    }
}

