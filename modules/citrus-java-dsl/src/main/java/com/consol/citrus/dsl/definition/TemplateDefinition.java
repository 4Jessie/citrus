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

import com.consol.citrus.container.Template;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Definition defines call template action with global context and parameters.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3 
 */
public class TemplateDefinition extends AbstractActionDefinition<Template> {

	public TemplateDefinition(Template action) {
	    super(action);
    }

    /**
     * Loads template definition from Spring bean application context and sets attributes.
     * @param applicationContext
     * @return
     */
    public TemplateDefinition load(ApplicationContext applicationContext) {
        Template rootTemplate = applicationContext.getBean(getName(), Template.class);

        getAction().setGlobalContext(rootTemplate.isGlobalContext());
        getAction().setActor(rootTemplate.getActor());
        getAction().setActions(rootTemplate.getActions());
        getAction().setParameter(rootTemplate.getParameter());

        return this;
    }
	
	/**
     * Boolean flag marking the template variables should also affect
     * variables in test case.
     * @param globalContext the globalContext to set
     */
	public TemplateDefinition globalContext(boolean globalContext) {
		action.setGlobalContext(globalContext);
		return this;
	}

	/**
     * Set parameter before execution.
     * @param parameters the parameter to set
     */
	public TemplateDefinition parameters(Map<String, String> parameters) {
		action.getParameter().putAll(parameters);
		return this;
	}
	
	/**
     * Set parameter before execution.
     * @param name
     * @param value
     */
	public TemplateDefinition parameter(String name, String value) {
	    action.getParameter().put(name, value);
		return this;
	}
}
