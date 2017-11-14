package com.bwssystems.HABridge.plugins.fibaro;

public enum ModeType {
    OFF(0, "Off"),
    HEAT(1, "Heat"),
    COOL(2, "Cool"),
    AUTO(3, "Auto"),
    AUX_HEAT(4, "Aux Heat"),
    RESUME(5, "Resume"),
    FAN_ONLY(6, "Fan Only"),
    FURNANCE(7, "Furnace"),
    DRY_AIR(8, "Dry Air"),
    MOIST_AIR(9, "Moist Air"),
    AUTO_CHANGEOVER(10, "Auto Changeover"),
    HEAT_ECON(11, "Heat Econ"),
    COOL_ECON(12, "Cool Econ"),
    AWAY(13, "Away"),
    MANUAL(31, "Manual");

    private int key;
    private String label;

    private ModeType(int key, String label) {
        this.key = key;
        this.label = label;
    }

    public int getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }
}
