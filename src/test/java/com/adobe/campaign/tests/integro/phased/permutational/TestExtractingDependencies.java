package com.adobe.campaign.tests.integro.phased.permutational;

import com.adobe.campaign.tests.integro.phased.data.permutational.SimpleProducerConsumer;
import com.adobe.campaign.tests.integro.phased.internal.ScenarioStepDependencies;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestExtractingDependencies {

    @Test
    public void testFetchProduceConsume()
            throws NoSuchMethodException, SecurityException, IOException {

        Class<SimpleProducerConsumer> l_testClass = SimpleProducerConsumer.class;

        ScenarioStepDependencies dependencies = listMethodCalls(l_testClass);

        assertThat("Our object should have a map od StpDependencies", dependencies.getStepDependencies(),
                instanceOf(Map.class));
        assertThat("Our object should be linked to the analyzed class", dependencies.getScenarioName(),
                equalTo(SimpleProducerConsumer.class.getTypeName()));
        assertThat("We should now have two steps defined here", dependencies.getStepDependencies().keySet().size(),
                equalTo(2));

        assertThat("We should have fetched the correct methods", dependencies.getStepDependencies().keySet(),
                containsInAnyOrder("bbbbb", "aaaa"));

        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getConsumeSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getProduceSet(),
                hasItems("bbbbkey"));
        assertThat("we should set the correct consume", dependencies.getStep("bbbbb").getStepPointer(), equalTo(7));

        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getProduceSet(), hasSize(0));
        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getConsumeSet(),
                hasItems("bbbbkey"));
        assertThat("we should set the correct consume", dependencies.getStep("aaaa").getStepPointer(), equalTo(11));
    }

    /**
     * From a class, this method returns the methods, and what they produce / consume
     *
     * @param in_class The scenario containing the steps and dependencies
     * @return
     * @throws FileNotFoundException
     */
    public static ScenarioStepDependencies listMethodCalls(Class in_class) throws FileNotFoundException {
        File file = ClassPathParser.fetchClassFile(in_class.getName());

        ScenarioStepDependencies lr_dependencies = new ScenarioStepDependencies(in_class.getTypeName());
        FileInputStream in = new FileInputStream(file);

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
                super.visit(n, arg);

            }
        }.visit(StaticJavaParser.parse(in), null);
        //System.out.println(); // empty line

        return lr_dependencies;
    }

}


