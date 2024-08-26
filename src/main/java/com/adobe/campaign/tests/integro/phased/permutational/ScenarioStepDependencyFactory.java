/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.permutational;

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import com.adobe.campaign.tests.integro.phased.ConfigValueHandlerPhased;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ScenarioStepDependencyFactory {

    private static final List<Class> CONFIG_CLASSES = Arrays.asList(BeforeClass.class, BeforeMethod.class,
            BeforeSuite.class, BeforeGroups.class,
            BeforeTest.class, AfterClass.class, AfterMethod.class, AfterSuite.class, AfterGroups.class,
            AfterTest.class);

    /**
     * From a class, this method returns the methods, and what they produce / consume
     *
     * @param in_class The scenario containing the steps and dependencies
     * @return A ScenarioStepDependencies object containing all the produce and consumed data.
     */
    public static ScenarioStepDependencies listMethodCalls(Class in_class) {
        File file = ClassPathParser.fetchClassFile(in_class);

        ScenarioStepDependencies lr_dependencies = new ScenarioStepDependencies(in_class.getTypeName());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new PhasedTestConfigurationException(
                    "The class " + in_class.getTypeName() + " could not be found in the given directory "
                            + ConfigValueHandlerPhased.PHASED_TEST_SOURCE_LOCATION
                            + "you can configure this by setting the execution property PHASED.TESTS.CODE.ROOT", e);
        }

        new VoidVisitorAdapter<Object>() {
            String lt_currentMethod = "not set";

            @Override
            public void visit(MethodCallExpr n, Object arg) {
                super.visit(n, arg);
                switch (n.getName().asString()) {
                case "produce":
                    lr_dependencies.putProduce(lt_currentMethod, n.getArgument(0).toString().replace("\"", ""),
                            fetchLineNumberOfCalls(n, lr_dependencies));
                    break;
                case "produceInStep":
                    lr_dependencies.putProduce(lt_currentMethod, lt_currentMethod,
                            fetchLineNumberOfCalls(n, lr_dependencies));
                    break;
                case "consume":
                case "consumeFromStep":
                    lr_dependencies.putConsume(lt_currentMethod, n.getArgument(0).toString().replace("\"", ""),
                            fetchLineNumberOfCalls(n, lr_dependencies));
                    break;
                default:
                    break;

                }
            }

            @Override
            public void visit(MethodDeclaration n, Object arg) {
                lt_currentMethod = n.getName().asString();

                Method lt_method = Arrays.stream(in_class.getMethods())
                        .filter(f -> f.getName().equals(lt_currentMethod)).findFirst().orElseThrow(PhasedTestConfigurationException::new);

                lr_dependencies.getStepDependencies().put(lt_currentMethod, new StepDependencies(lt_currentMethod));
                lr_dependencies.getStep(lt_currentMethod).setStepLine(n.getBegin().map(s -> s.line).orElse(lr_dependencies.fetchLastStepPosition() + 1));
                if (Arrays.stream(lt_method.getDeclaredAnnotations())
                        .anyMatch(a -> CONFIG_CLASSES.contains(a.annotationType()))) {
                    lr_dependencies.getStep(lt_currentMethod).setConfigMethod(true);
                }

                super.visit(n, arg);

            }
        }.visit(StaticJavaParser.parse(fis), null);

        return lr_dependencies;
    }

    //if the code contains a line number we return it otherwise we calculate where the next line would be
    private static int fetchLineNumberOfCalls(MethodCallExpr n, ScenarioStepDependencies lr_dependencies) {

            return n.getBegin().map(s -> s.line).orElse(lr_dependencies.fetchLastStepPosition() + 1);

    }

}
