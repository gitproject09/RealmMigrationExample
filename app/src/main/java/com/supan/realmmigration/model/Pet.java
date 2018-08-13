package com.supan.realmmigration.model;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Pet extends RealmObject {
    @Required
    private String name;
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
