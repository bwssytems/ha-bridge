package com.bwssystems.HABridge.api.hue;

/**
 * Created by Florian Foerderreuther on 07/23/17
 */
public class GroupState {
    private boolean all_on;
    private boolean any_on;
    
    public boolean isAllOn() {
        return all_on;
    }

    public boolean isAnyOn() {
        return any_on;
    }

    public void setState(boolean all_on, boolean any_on) {
        this.all_on = all_on;
        this.any_on = any_on;
    }

    public GroupState(boolean all_on, boolean any_on) {
        this.all_on = all_on;
        this.any_on = any_on;   
    }

    @Override
    public String toString() {
        return "GroupState{" +
                "all_on=" + all_on +
                ", any_on=" + any_on +
                '}';
    }
}
