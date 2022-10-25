/*
 * MIT License
 *
 * © Copyright 2020 Adobe. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.adobe.campaign.tests.integro.phased.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
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

    private static final List<Class> CONFIG_CLASSES = Arrays.asList(BeforeClass.class, BeforeMethod.class, BeforeSuite.class, BeforeGroups.class,
            BeforeTest.class, AfterClass.class,AfterMethod.class,AfterSuite.class, AfterGroups.class, AfterTest.class);

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
            throw new PhasedTestConfigurationException("The class "+in_class.getTypeName()+" could not be found in the given directory "+ PhasedTestManager.PHASED_TEST_SOURCE_LOCATION+ "you can configure this by setting the execution property PHASED.TESTS.CODE.ROOT", e);
        }

        new VoidVisitorAdapter<Object>() {
            String lt_currentMethod = "not set";

            @Override
            public void visit(MethodCallExpr n, Object arg) {
                super.visit(n, arg);
                //System.out.println(" [L " + n.getName() + "] " + n.getArgument(0) +" - "+ n);
                if (n.getName().asString().equals("produce")) {
                    lr_dependencies.putProduce(lt_currentMethod, n.getArgument(0).toString().replaceAll("\"", ""),
                            n.getBegin().get().line);
                }
                if (n.getName().asString().equals("consume")) {
                    lr_dependencies.putConsume(lt_currentMethod, n.getArgument(0).toString().replaceAll("\"", ""),
                            n.getBegin().get().line);
                }
            }

            @Override
            public void visit(MethodDeclaration n, Object arg) {
                lt_currentMethod = n.getName().asString();

                Method lt_meMethod = Arrays.stream(in_class.getMethods()).filter(f -> f.getName().equals(lt_currentMethod)).findFirst().get();

                lr_dependencies.getStepDependencies().put(lt_currentMethod, new StepDependencies(lt_currentMethod));
                lr_dependencies.getStep(lt_currentMethod).setStepLine(n.getBegin().get().line);
                if (Arrays.stream(lt_meMethod.getDeclaredAnnotations()).anyMatch( a -> CONFIG_CLASSES.contains(a.annotationType()))) {
                    lr_dependencies.getStep(lt_currentMethod).setConfigMethod(true);
                }

                super.visit(n, arg);

            }
        }.visit(StaticJavaParser.parse(fis), null);

        return lr_dependencies;
    }
}
