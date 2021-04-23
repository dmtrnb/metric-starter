package ru.example.autoconfigure.bean_post_processor;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import ru.example.autoconfigure.annotation.Timed;
import ru.example.autoconfigure.metric.MethodInvocationMetric;
import ru.example.autoconfigure.metric.StorageMethodInvocationMetric;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
public class TimedBeanPostProcessor implements BeanPostProcessor {

    private static final Class<? extends Annotation> ANNOTATION = Timed.class;
    private final Map<String, Class<?>> annotatedClasses = new HashMap<>();
    private final Map<String, Class<?>> classesWithAnnotatedMethods = new HashMap<>();

    private final StorageMethodInvocationMetric storage;

    @Autowired
    public TimedBeanPostProcessor(StorageMethodInvocationMetric storage) {
        this.storage = storage;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (clazz.isAnnotationPresent(ANNOTATION)) {
            annotatedClasses.put(beanName, clazz);
        } else {
            for (Method m: clazz.getMethods()) {
                if (m.isAnnotationPresent(ANNOTATION)) {
                    classesWithAnnotatedMethods.put(beanName, clazz);
                    break;
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
        if (annotatedClasses.containsKey(beanName)) {
            Class<?> clazz = annotatedClasses.get(beanName);
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                return Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, (obj, method, args) -> getObject(bean, method, args));
            } else {
                return Enhancer.create(clazz, (MethodInterceptor) (obj, method, args, methodProxy) -> getObject(bean, method, args));

            }
        } else if (classesWithAnnotatedMethods.containsKey(beanName)) {
            Class<?> clazz = classesWithAnnotatedMethods.get(beanName);
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                return Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, (obj, method, args) -> getObjectFromMethod(bean, method, args));
            } else {
                return Enhancer.create(clazz, (MethodInterceptor) (obj, method, args, methodProxy) -> getObjectFromMethod(bean, method, args));
            }
        }
        return bean;
    }

    private Object getObject(Object bean, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        long start = System.currentTimeMillis();
        Object returnValue = method.invoke(bean, args);
        long finish = System.currentTimeMillis();

        String name = bean.getClass().getSimpleName() + "." + method.getName();
        LocalDateTime startTime = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDateTime();
        MethodInvocationMetric metric = new MethodInvocationMetric(name, startTime, (int) (finish - start));
        storage.add(metric);

        return returnValue;
    }

    private Object getObjectFromMethod(Object bean, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (method.isAnnotationPresent(ANNOTATION)) {
            return getObject(bean, method, args);
        } else {
            return method.invoke(bean, args);
        }
    }
}
