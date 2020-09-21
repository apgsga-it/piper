package com.apgsga.microservice.patch.api;

public class Stage {

    private String name;
    private String toState;
    private String code;
    private String implcls;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToState() {
        return toState;
    }

    public void setToState(String toState) {
        this.toState = toState;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImplcls() {
        return implcls;
    }

    public void setImplcls(String implcls) {
        this.implcls = implcls;
    }
}
