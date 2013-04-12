package org.ow2.proactive.brokering;

import javax.ws.rs.core.Response;

public class Result {
    private int code; // 0=no error
    private String reason;

    public Result() {
        this.code = 0;
        this.reason = "OK";
    }

    public Result(int code, String reason) {
        this.code = code;
        this.reason = reason;
        Response.status(code);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", reason='" + reason + '\'' +
                '}';
    }
}
