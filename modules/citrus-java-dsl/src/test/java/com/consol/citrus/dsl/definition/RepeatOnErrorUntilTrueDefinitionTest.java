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

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RepeatOnErrorUntilTrueDefinitionTest extends AbstractTestNGUnitTest {
    @Test
    public void testRepeatOnErrorUntilTrueBuilder() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                repeatOnError(echo("${var}"), sleep(3000), echo("${var}"))
                    .autoSleep(2000)
                    .until("i gt 5");

                repeatOnError(echo("${var}"))
                    .autoSleep(200)
                    .index("k")
                    .startsWith(2)
                    .until("k gt= 5");
            }
        };
        
        builder.execute();
        
        assertEquals(builder.testCase().getActions().size(), 2);
        assertEquals(builder.testCase().getActions().get(0).getClass(), RepeatOnErrorUntilTrue.class);
        assertEquals(builder.testCase().getActions().get(0).getName(), "repeat-on-error");
        
        RepeatOnErrorUntilTrue container = (RepeatOnErrorUntilTrue)builder.testCase().getActions().get(0);
        assertEquals(container.getActions().size(), 3);
        assertEquals(container.getAutoSleep(), Long.valueOf(2000L));
        assertEquals(container.getCondition(), "i gt 5");
        assertEquals(container.getIndex(), 1);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);

        container = (RepeatOnErrorUntilTrue)builder.testCase().getActions().get(1);
        assertEquals(container.getActions().size(), 1);
        assertEquals(container.getAutoSleep(), Long.valueOf(200L));
        assertEquals(container.getCondition(), "k gt= 5");
        assertEquals(container.getIndex(), 2);
        assertEquals(container.getIndexName(), "k");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }
}
