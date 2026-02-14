package minispring.core;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class BeanDefinition {
    private Class<?> beanClass;
    private String scope;
    private String beanName;

    public BeanDefinition(Class<?> beanClass) {
        this.beanClass = beanClass;
        this.scope = "singleton";
    }

    public Class<?> getBeanClass() { return beanClass; }
    public void setBeanClass(Class<?> beanClass) { this.beanClass = beanClass; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getBeanName() { return beanName; }
    public void setBeanName(String beanName) { this.beanName = beanName; }
}
