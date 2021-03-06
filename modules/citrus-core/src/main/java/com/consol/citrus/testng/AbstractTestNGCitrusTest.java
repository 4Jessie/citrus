/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.testng;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCase;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.springframework.util.*;
import org.testng.*;
import org.testng.annotations.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Abstract base test implementation for testng test cases. Providing test listener support and
 * loading basic application context files for Citrus.
 *
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Listeners( { CitrusMethodInterceptor.class } )
public abstract class AbstractTestNGCitrusTest extends AbstractTestNGSpringContextTests {
    /** Logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Parameter values provided from external logic */
    private Object[][] citrusDataProviderParameters;

    /** Collection of test loaders for annotated methods */
    private Map<String, List<TestLoader>> testLoaders = new HashMap<String, List<TestLoader>>();

    /** Citrus instance */
    protected Citrus citrus;

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();

        if (method != null && method.getAnnotation(CitrusXmlTest.class) != null) {
            List<TestLoader> methodTestLoaders = testLoaders.get(method.getName());

            if (!CollectionUtils.isEmpty(methodTestLoaders)) {
                try {
                    if (citrus == null) {
                        citrus = Citrus.newInstance(applicationContext);
                    }

                    TestContext ctx = prepareTestContext(citrus.createTestContext());
                    TestLoader testLoader = methodTestLoaders.get(testResult.getMethod().getCurrentInvocationCount() % methodTestLoaders.size());
                    TestCase testCase = testLoader.load();
                    if (citrusDataProviderParameters != null) {
                        handleTestParameters(testResult.getMethod(), testCase,
                                citrusDataProviderParameters[testResult.getMethod().getCurrentInvocationCount() % citrusDataProviderParameters.length]);
                    }

                    citrus.run(testCase, ctx);
                } catch (RuntimeException e) {
                    testResult.setThrowable(e);
                    testResult.setStatus(ITestResult.FAILURE);
                } catch (Exception e) {
                    testResult.setThrowable(e);
                    testResult.setStatus(ITestResult.FAILURE);
                }
            }

            super.run(new FakeExecutionCallBack(callBack.getParameters()), testResult);
        } else {
            super.run(callBack, testResult);
        }
    }

    /**
     * Creates test loaders from @CitrusXmlTest annotated test methods and saves those to local member.
     * Test loaders get executed later when actual method is called by TestNG. This way user can annotate
     * multiple methods in one single class each executing several Citrus XML tests.
     */
    @BeforeClass(alwaysRun = true)
    public void createTestLoaders() {
        for (Method method : ReflectionUtils.getAllDeclaredMethods(this.getClass())) {
            if (method.getAnnotation(CitrusXmlTest.class) != null) {
                CitrusXmlTest citrusTestAnnotation = method.getAnnotation(CitrusXmlTest.class);

                String[] testNames = new String[] {};
                if (citrusTestAnnotation.name().length > 0) {
                    testNames = citrusTestAnnotation.name();
                } else if (citrusTestAnnotation.packageScan().length == 0) {
                    // only use default method name as test in case no package scan is set
                    testNames = new String[] { method.getName() };
                }

                String testPackage;
                if (StringUtils.hasText(citrusTestAnnotation.packageName())) {
                    testPackage = citrusTestAnnotation.packageName();
                } else {
                    testPackage = method.getDeclaringClass().getPackage().getName();
                }

                List<TestLoader> methodTestLoaders = new ArrayList<TestLoader>();
                for (String testName : testNames) {
                    methodTestLoaders.add(createTestLoader(testName, testPackage));
                }

                String[] testPackages = citrusTestAnnotation.packageScan();
                for (String packageName : testPackages) {
                    try {
                        Resource[] fileResources = new PathMatchingResourcePatternResolver().getResources(packageName.replace('.', '/') + "/**/*Test.xml");

                        for (Resource fileResource : fileResources) {
                            String filePath = fileResource.getFile().getParentFile().getCanonicalPath();
                            filePath = filePath.substring(filePath.indexOf(packageName.replace('.', '/')));

                            methodTestLoaders.add(createTestLoader(fileResource.getFilename().substring(0, fileResource.getFilename().length() - ".xml".length()), filePath));
                        }
                    } catch (IOException e) {
                        throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageName + "'", e);
                    }
                }

                testLoaders.put(method.getName(), methodTestLoaders);
            }
        }
    }

    /**
     * Creates new test loader which has TestNG test annotations set for test execution. Only
     * suitable for tests that get created at runtime through factory method. Subclasses
     * may overwrite this in order to provide custom test loader with custom test annotations set.
     * @param beanName
     * @param packageName
     * @return
     */
    protected TestLoader createTestLoader(String beanName, String packageName) {
        return new XmlTestLoader(beanName, packageName, applicationContext);
    }

    /**
     * Runs tasks before test suite.
     * @param testContext the test context.
     * @throws Exception on error.
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        springTestContextPrepareTestInstance();
        Assert.notNull(applicationContext);

        citrus = Citrus.newInstance(applicationContext);
        citrus.beforeSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
    }

    /**
     * Executes the test case.
     */
    protected void executeTest() {
        executeTest(null);
    }

    /**
     * Executes the test case.
     * @param testContext the test context.
     */
    protected void executeTest(ITestContext testContext) {
        if (citrus == null) {
            citrus = Citrus.newInstance(applicationContext);
        }

        TestContext ctx = prepareTestContext(citrus.createTestContext());
        TestCase testCase = getTestCase();
        if (citrusDataProviderParameters != null) {
            handleTestParameters(Reporter.getCurrentTestResult().getMethod(), testCase,
                    citrusDataProviderParameters[Reporter.getCurrentTestResult().getMethod().getCurrentInvocationCount()]);
        }

        citrus.run(testCase, ctx);
    }

    /**
     * Methods adds optional TestNG parameters as variables to the test case.
     *
     * @param method the testng method currently executed
     * @param testCase the constructed Citrus test.
     */
    private void handleTestParameters(ITestNGMethod method, TestCase testCase, Object[] parameterValues) {
        String[] parameterNames = getParameterNames(method);

        if (parameterValues.length != parameterNames.length) {
            throw new CitrusRuntimeException("Parameter mismatch: " + parameterNames.length +
                    " parameter names defined with " + parameterValues.length + " parameter values available");
        }


        testCase.setParameters(parameterNames, parameterValues);
    }

    /**
     * Read parameter names form method annotation.
     * @param method
     * @return
     */
    protected String[] getParameterNames(ITestNGMethod method) {
        String[] parameterNames;
        CitrusParameters citrusParameters = method.getConstructorOrMethod().getMethod().getAnnotation(CitrusParameters.class);
        Parameters testNgParameters = method.getConstructorOrMethod().getMethod().getAnnotation(Parameters.class);
        if (citrusParameters != null) {
            parameterNames = citrusParameters.value();
        } else if (testNgParameters != null) {
            parameterNames = testNgParameters.value();
        } else {
            throw new CitrusRuntimeException("Missing parameters annotation, " +
                    "please provide parameter names with proper annotation when using data provider!");
        }

        return parameterNames;
    }

    /**
     * Prepares the test context.
     *
     * Provides a hook for test context modifications before the test gets executed.
     *
     * @param testContext the test context.
     * @return the (prepared) test context.
     */
    protected TestContext prepareTestContext(final TestContext testContext) {
        return testContext;
    }

    /**
     * Constructs the test case to execute.
     * @return
     */
    protected TestCase getTestCase() {
        return new XmlTestLoader(this.getClass().getSimpleName(), this.getClass().getPackage().getName(), applicationContext).load();
    }

    /**
     * Runs tasks after test suite.
     * @param testContext the test context.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        citrus.afterSuite(testContext.getSuite().getName());
    }
    
    /**
     * Default data provider automatically adding parameters 
     * as variables to test case.
     * @return
     */
    @DataProvider(name = "citrusDataProvider")
    protected Object[][] provideTestParameters() {
      citrusDataProviderParameters = getParameterValues();
      return citrusDataProviderParameters;
    }
    
    /**
     * Hook for subclasses to provide individual test parameters.
     * @return
     */
    protected Object[][] getParameterValues() {
        return new Object[0][];
    }

    /**
     * Class faking test execution as callback. Used in run hookable method when test case
     * was executed before and callback is needed for super class run method invocation.
     */
    protected static final class FakeExecutionCallBack implements IHookCallBack {
        private Object[] parameters;

        public FakeExecutionCallBack(Object[] parameters) {
            this.parameters = Arrays.copyOf(parameters, parameters.length);
        }

        @Override
        public void runTestMethod(ITestResult testResult) {
            // do nothing as test case was already executed
        }

        @Override
        public Object[] getParameters() {
            return Arrays.copyOf(parameters, parameters.length);
        }

    }
}
