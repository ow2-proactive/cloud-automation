package org.ow2.proactive.workflowcatalog.cli.rest;

public abstract class ClientBase implements WorkflowCatalogClient {
    private static final int calling_method_stack_index = 3;

    private UnsupportedOperationException newUnsupportedOperationException() {
        return new UnsupportedOperationException(String.format(
                "%s does not implements %s(...).", className(), methodName()));
    }

    private String className() {
        return this.getClass().getSimpleName();
    }

    private String methodName() {
        return Thread.currentThread().getStackTrace()[calling_method_stack_index]
                .getMethodName();
    }

}
