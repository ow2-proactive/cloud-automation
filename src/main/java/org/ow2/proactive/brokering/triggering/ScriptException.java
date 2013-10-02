package org.ow2.proactive.brokering.triggering;

public class ScriptException extends Exception {
    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ScriptException(Throwable throwable) {
        super(throwable);
    }
}

