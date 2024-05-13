package com.adobe.campaign.tests.integro.phased.events;

import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class EventInjectorListener implements IAnnotationTransformer, IMethodInterceptor, IDataProviderListener {
    private static final Logger log = LogManager.getLogger();


    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> list, ITestContext iTestContext) {
        list.stream().forEach(l -> log.info("Method {}, Class {}, Parent {}", l.getMethod().getMethodName(),
                l.getMethod().getTestClass().getClass().getTypeName(), l.getMethod().getTestClass().getClass().getSuperclass().getTypeName()));

        //list.removeAll();
        return list.stream().filter(l -> l.getMethod().getRealClass().equals(PhasedParent.class)).collect(
                Collectors.toList());
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        IAnnotationTransformer.super.transform(annotation, testClass, testConstructor, testMethod);
    }

    @Override
    public void beforeDataProviderExecution(IDataProviderMethod dataProviderMethod, ITestNGMethod method,
            ITestContext iTestContext) {

        IDataProviderListener.super.beforeDataProviderExecution(dataProviderMethod, method, iTestContext);
    }
}
