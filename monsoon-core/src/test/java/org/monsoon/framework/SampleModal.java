package org.monsoon.framework;

public class SampleModal {
    private String value = "Hello from SampleModal";

    public SampleModal(String value){
        if (value != null) this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SampleModal{" +
                "value='" + value + '\'' +
                '}';
    }
}
