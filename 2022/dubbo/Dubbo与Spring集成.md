

ServiceBean
DubboAutoConfiguration
DubboComponentScanRegistrar
ServiceAnnotationBeanPostProcessor

# Service注解的类被注入到Spring的流程：  
**ServiceAnnotationBeanPostProcessor**继承自**BeanDefinitionRegistryPostProcessor**，实现了**postProcessBeanDefinitionRegistry**方法，生成Bean，最后放到**BeanDefinitionRegistry**中  
核心代码：  
```java
/**
    * Registers {@link ServiceBean} from new annotated {@link Service} {@link BeanDefinition}
    *
    * @param beanDefinitionHolder
    * @param registry
    * @param scanner
    * @see ServiceBean
    * @see BeanDefinition
    */
private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry,
                                    DubboClassPathBeanDefinitionScanner scanner) {

    Class<?> beanClass = resolveClass(beanDefinitionHolder);

    Service service = findAnnotation(beanClass, Service.class);

    Class<?> interfaceClass = resolveServiceInterfaceClass(beanClass, service);

    String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();

    AbstractBeanDefinition serviceBeanDefinition =
            buildServiceBeanDefinition(service, interfaceClass, annotatedServiceBeanName);

    // ServiceBean Bean name
    String beanName = generateServiceBeanName(service, interfaceClass, annotatedServiceBeanName);

    if (scanner.checkCandidate(beanName, serviceBeanDefinition)) { // check duplicated candidate bean
        registry.registerBeanDefinition(beanName, serviceBeanDefinition);
    }
}
```
