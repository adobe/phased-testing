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

import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ScenarioStepDependencyFactory {

    /**
     * From a class, this method returns the methods, and what they produce / consume
     *
     * @param in_class The scenario containing the steps and dependencies
     * @return
     * @throws FileNotFoundException
     */
    public static ScenarioStepDependencies listMethodCalls(Class in_class) {
        File file = ClassPathParser.fetchClassFile(in_class);

        ScenarioStepDependencies lr_dependencies = new ScenarioStepDependencies(in_class.getTypeName());
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
                lr_dependencies.getStepDependencies().put(lt_currentMethod, new StepDependencies(lt_currentMethod));
                lr_dependencies.getStepDependencies().get(lt_currentMethod).setStepLine(n.getBegin().get().line);
                super.visit(n, arg);

            }
        }.visit(StaticJavaParser.parse(in), null);
        //System.out.println(); // empty line

        return lr_dependencies;
    }
}
