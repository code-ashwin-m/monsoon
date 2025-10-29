package org.monsoon.framework.core.context;

import org.monsoon.framework.core.AutoConfiguration;
import org.monsoon.framework.core.BeanDefinition;
import org.monsoon.framework.core.annotations.*;
import org.monsoon.framework.core.interfaces.BeanPostProcessor;
import org.monsoon.framework.core.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class is used to create an application context from the given configuration class.
 * It registers the bean definitions and the singletons in the application context.
 * It also registers the bean post processors, which are used to process the beans before they are added to the application context.
 */
public class ApplicationContextHelper {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextHelper.class);
    private final Class<?> mainClass;
    protected Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    private Map<String, Object> singletonBeans = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    /**
     * This constructor is used to create an application context from the given configuration class.
     * It registers the bean definitions and the singletons in the application context.
     * It also registers the bean post processors, which are used to process the beans before they are added to the application context.
     */
    public ApplicationContextHelper(Class<?> mainClass) throws Exception {
        this.mainClass = mainClass;

        Package basePackage = mainClass.getPackage();
        String basePackageName = basePackage.getName();

        logger.debug("Base package name is {}", basePackageName);
        List<Class<?>> classes = new ArrayList<>();
        classes.addAll(ClassUtils.scanForClasses(basePackageName));

        if (ClassUtils.isAnnotationPresent(mainClass, ComponentScan.class)){
            logger.debug("ComponentScan annotation is present");
            scanComponents(classes);
            scanConfigurations(classes);
        }else{
            logger.debug("ComponentScan annotation is not present, checking for Configuration annotation");
            if (ClassUtils.isAnnotationPresent(mainClass, Configuration.class)){
                Object configuration = createInstance(mainClass);
                registerConfiguration(configuration, mainClass);
                logger.debug("Configuration registered from class: {}", mainClass.getSimpleName());
            }
        }

        if (ClassUtils.isAnnotationPresent(mainClass, EnableAutoConfiguration.class)){
            logger.debug("EnableAutoConfiguration annotation is present");
            scanAutoConfiguration();
        }

        registerBeanPostProcessor(classes);
    }

    /**
     * This method is used to scan for components in the given list of classes.
     * It iterates over the classes and checks if the class is annotated with @Component.
     * If it is, it registers the component in the application context.
     * @param classes The list of classes to scan for components.
     */
    private void scanComponents(List<Class<?>> classes) {
        logger.debug("Component scanning started");
        int counter = 0;
        for (Class<?> clazz : classes){
            if (ClassUtils.isAnnotationPresent(clazz, Component.class)){
                registerComponent(clazz);
                counter++;
            }
        }
        logger.debug("Component scanning completed. Total components {}", counter);
    }

    /**
     * This method is used to scan for configurations in the given list of classes.
     * It iterates over the classes and checks if the class is annotated with @Configuration.
     * If it is, it registers the configuration in the application context.
     * @param classes The list of classes to scan for configurations.
     */
    private void scanConfigurations(List<Class<?>> classes) {
        logger.debug("Configuration scanning started");
        int counter = 0;
        for (Class<?> clazz : classes){
            if (ClassUtils.isAnnotationPresent(clazz, Configuration.class)){
                try {
                    Object configuration = createInstance(clazz);
                    registerConfiguration(configuration, clazz);
                    counter++;
                } catch (Exception e) {
                    logger.error("Failed to load class {}", clazz.getName(), e);
                }
            }
        }
        logger.debug("Configuration scanning completed. Total configurations {}", counter);
    }

    /**
     * This method is used to scan for auto configurations.
     * It iterates over the auto configurations and checks if the class is annotated with @Configuration.
     * If it is, it registers the configuration in the application context.
     */
    private void scanAutoConfiguration() {
        logger.debug("Auto configuration scanning started");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> autoConfigurations = AutoConfiguration.loadClassNamesFromImports();
        int counter = 0;

        List<Class<?>> classList = new ArrayList<>();
        for (String className: autoConfigurations){
            try {
                if (!ClassUtils.isPresent(className)){
                    logger.debug("Skipping auto-configuration: {} (class not found)", className);
                    continue;
                }

                Class<?> clazz = Class.forName(className, false, classLoader);
                classList.add(clazz);
            } catch (Exception e) {
                logger.error("Failed to load class {}", className);
            }
        }

        List<Class<?>> classListSorted = AutoConfiguration.sort(classList);

        for(Class<?> clazz : classListSorted){
            if (clazz.isInterface()) continue;

            ConditionalOnClass conditional = clazz.getAnnotation(ConditionalOnClass.class);
            if (conditional != null){
                List<String> classNames = new ArrayList<>();
                classNames.addAll(Arrays.asList(conditional.name()));

                for (Class<?> clz: conditional.value()){
                    classNames.add(clz.getName());
                }

                boolean allPresent = true;
                for (String required : classNames) {
                    if (!ClassUtils.isPresent(required)) {
                        logger.debug("Skipping {} (missing dependency: {})", clazz.getName(), required);
                        allPresent = false;
                        break;
                    }
                }
                if (!allPresent) {
                    continue;
                }
            }

            ConditionalOnMissingBean missingBean = clazz.getAnnotation(ConditionalOnMissingBean.class);
            if (missingBean != null){
                List<String> classNames = new ArrayList<>();
                classNames.addAll(Arrays.asList(missingBean.name()));

                for (Class<?> clz: missingBean.value()){
                    classNames.add(clz.getName());
                }

                boolean anyBeanPresent = false;
                for (String required : classNames) {
                    Class<?> clz = ClassUtils.forName(required);
                    if (clz != null && containsBeanOfType(clz)) {
                        logger.debug("Skipping {} (another bean of same type is already registered)",
                                clazz.getName(), required);
                        anyBeanPresent = true;
                        break;
                    }
                }
                if (anyBeanPresent) {
                    continue;
                }
            }

            try {
                Object configuration  = createInstance(clazz);
                registerConfiguration(configuration, clazz);
                logger.debug("Auto configuration registered from class: {}", clazz.getName());
                counter++;
            } catch (Exception e) {
                logger.error("Failed to create bean for {}", clazz.getName());
            }

        }

        logger.debug("Auto configuration scanning completed. Total configurations {}", counter);
    }

    private boolean containsBeanOfType(Class<?> type) {
        for (BeanDefinition def : beanDefinitions.values()) {
            if (type.isAssignableFrom(def.getBeanClass())) {
                return true; // Found a bean of the requested type (or subtype)
            }
        }
        return false;
    }

    /**
     * This method is used to register a component in the application context.
     * It creates a bean definition for the component and adds it to the application context.
     * @param clazz The class of the component to register.
     */
    private void registerComponent(Class<?> clazz) {
        Component component = ClassUtils.findAnnotation(clazz, Component.class);
        String beanName = component.name();
        if (beanName.equals("")) beanName = Introspector.decapitalize(clazz.getSimpleName());
        Boolean singleton = component.singleton();
        BeanDefinition def = new BeanDefinition(clazz, beanName, singleton);
        beanDefinitions.put(beanName, def);
    }


    /**
     * This method is used to register a configuration class in the application context.
     * It iterates over all the methods of the configuration class and checks if the method is annotated with @Bean.
     * If it is, it invokes the method and registers the returned bean in the application context.
     * @param configuration The configuration class to register.
     * @param clazz The class of the configuration class.
     */
    private void registerConfiguration(Object configuration, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()){
            if (method.isAnnotationPresent(Bean.class)){
                try {
                    method.setAccessible(true);
                    String beanName = method.getAnnotation(Bean.class).name();
                    if (beanName.equals("")) beanName = Introspector.decapitalize(method.getName());
                    BeanDefinition def = new BeanDefinition(method.getReturnType(), beanName, true);
                    beanDefinitions.put(beanName, def);
                    Object bean = method.invoke(configuration);
                    singletonBeans.put(beanName, bean);
                } catch (Exception e) {
                    logger.error("Failed to invoke method {}", method.getName(), e);
                }
            }
        }
    }

    /**
     * This method creates a bean instance from the given class.
     * It looks up the bean definition in the application context and creates a bean instance.
     * If the bean definition is not found, it returns null.
     * @param clazz The class of the bean instance to create.
     * @return The bean instance, or null if the bean definition is not found.
     * @throws Exception If there is an error while creating the bean instance.
     */
    public Object createBean(Class<?> clazz) throws Exception {
        Component component = ClassUtils.findAnnotation(clazz, Component.class);
        if (component == null) return null;
        String beanName = component.name();
        if (beanName.equals("")) beanName = Introspector.decapitalize(clazz.getSimpleName());
        return createBean(beanName);
    }

    /**
     * This method creates a bean instance from the given bean name.
     * It looks up the bean definition in the application context and creates a bean instance.
     * If the bean definition is not found, it returns null.
     * @param beanName The name of the bean to create.
     * @return The bean instance, or null if the bean definition is not found.
     * @throws Exception If there is an error while creating the bean instance.
     */
    public Object createBean(String beanName) throws Exception {
        BeanDefinition def = beanDefinitions.get(beanName);

        if (def == null){
            logger.error("Bean not found: {}", beanName);
            throw new Exception("Bean not found: " + beanName);
        }

        if (def.isSingleton()){
            if (singletonBeans.containsKey(beanName)) return singletonBeans.get(beanName);
            Object instance = createInstance(def.getBeanClass());
            singletonBeans.put(beanName, instance);
            return instance;
        }

        return createInstance(def.getBeanClass());
    }

    /**
     * This method creates a bean instance from the given class.
     * It looks up the bean definition in the application context and creates a bean instance.
     * If the bean definition is not found, it returns null.
     * @param beanClass The class of the bean to create.
     * @return The bean instance, or null if the bean definition is not found.
     * @throws Exception If there is an error while creating the bean instance.
     */
    private Object createInstance(Class<?> beanClass) throws Exception {
        Object instance = null;

        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();

        if (constructors.length == 0){
            instance = applyProcessor(beanClass);
            if (instance == null) {
                logger.error("No constructors found for class: {}", beanClass.getSimpleName());
                throw new IllegalStateException("No constructors found for class: " + beanClass.getSimpleName());
            }
        }else {
            Constructor<?> targetConstructor = Arrays.stream(constructors)
                    .filter(c -> c.isAnnotationPresent(Autowired.class))
                    .findFirst()
                    .orElseGet(() -> constructors[0]);

            Class<?>[] paramTypes = targetConstructor.getParameterTypes();

            if (paramTypes.length > 0){
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = createBean(paramTypes[i]); // recursion
                }

                targetConstructor.setAccessible(true);
                instance = targetConstructor.newInstance(params);
            } else {
                targetConstructor.setAccessible(true);
                instance = targetConstructor.newInstance();
            }
        }
        injectDependencies(instance);
        return instance;
    }

    /**
     * This method injects dependencies into the given instance.
     * It iterates over all the fields of the instance and checks if the field is annotated with @Autowired.
     * If it is, it creates a bean instance and injects it into the field.
     * @param instance The instance to inject dependencies into.
     * @throws Exception If there is an error while injecting dependencies.
     */
    private void injectDependencies(Object instance) throws Exception {
        if (instance == null) return;
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)){
                Object dependency = createBean(field.getType());
                if ( dependency == null) {
                    logger.error("Dependency not found: {}", field.getType().getName());
                    throw new RuntimeException("Dependency not found: " + field.getType().getName());
                }
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }

    /**
     * This method registers a bean post processor in the application context.
     * It iterates over all the classes in the given list and checks if the class implements the BeanPostProcessor interface.
     * If it does, it creates a bean instance and adds it to the list of bean post processors.
     * @param classes The list of classes to register as bean post processors.
     * @throws Exception If there is an error while registering the bean post processors.
     */
    private void registerBeanPostProcessor(List<Class<?>> classes) throws Exception {
        for (Class<?> clazz: classes){
            for (Class<?> inter : clazz.getInterfaces()){
                if (inter.equals(BeanPostProcessor.class)){
                    beanPostProcessors.add((BeanPostProcessor) createInstance(clazz));
                }
            }
        }
    }

    /**
     * This method applies a bean post processor to the given class.
     * It iterates over all the bean post processors in the application context and applies them to the class.
     * If a bean post processor returns a non-null value, it returns that value.
     * @param clazz The class to apply the bean post processor to.
     * @return The bean post processor result, or null if no bean post processor returns a non-null value.
     */
    private Object applyProcessor(Class<?> clazz) {
        Object instance;
        for (BeanPostProcessor processor: beanPostProcessors){
            instance = processor.postProcess(clazz);
            if ( instance != null ) return instance;
        }
        return null;
    }
}
