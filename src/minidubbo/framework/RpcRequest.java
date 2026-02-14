package minidubbo.framework;

import java.io.Serializable;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = -4071906532505979436L;
    private final String className;
    private final String methodName;
    private final Object[] params;

    public RpcRequest(String className, String methodName, Object[] params) {
        this.className = className;
        this.methodName = methodName;
        this.params = params;
    }
    // Getter & Setter ...
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public Object[] getParams() { return params; }
}
