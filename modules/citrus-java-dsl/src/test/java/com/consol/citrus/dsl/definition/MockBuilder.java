/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.CitrusTestBuilder;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import org.springframework.context.ApplicationContext;

/**
 * Test instance for {@link TestNGCitrusTestBuilder} used in unit tests in order to provide
 * unit testing access to builder functionality.
 * 
 * @author Christoph Deppisch
 */
public class MockBuilder extends CitrusTestBuilder {

    /**
     * Constructor using an application context.
     * @param applicationContext
     */
    public MockBuilder(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void execute() {
        configure();
    }

    @Override
    public void execute(TestContext context) {
        configure();
    }

    /**
     * Provide public access to test case for unit test assertions.
     * @return
     */
    public TestCase testCase() {
        return getTestCase();
    }
}
