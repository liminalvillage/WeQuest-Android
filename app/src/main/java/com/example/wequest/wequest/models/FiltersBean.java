package com.example.wequest.wequest.models;

public class FiltersBean {
    /**
     * field : tag
     * key : email
     * relation : =
     * value : mahamadhusen93@gmail.com
     */

    @com.google.gson.annotations.SerializedName("field")
    private String field;
    @com.google.gson.annotations.SerializedName("key")
    private String key;
    @com.google.gson.annotations.SerializedName("relation")
    private String relation;
    @com.google.gson.annotations.SerializedName("value")
    private String value;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
