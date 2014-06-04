package de.smava.css.intersector;

public enum IntersectDataType {
    INTERSECTED("intersected"),
    DIFFERENCE_A("differenceA"),
    DIFFERENCE_B("differenceB");

    private final String value;

    private IntersectDataType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
