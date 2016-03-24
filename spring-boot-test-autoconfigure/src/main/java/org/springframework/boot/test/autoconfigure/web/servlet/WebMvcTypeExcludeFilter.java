/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.autoconfigure.web.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.embedded.DelegatingFilterProxyRegistrationBean;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.test.autoconfigure.filter.AnnotationCustomizableTypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * {@link TypeExcludeFilter} for {@link WebMvcTest @WebMvcTest}.
 *
 * @author Phillip Webb
 */
class WebMvcTypeExcludeFilter extends AnnotationCustomizableTypeExcludeFilter {

	private static final Set<Class<?>> DEFAULT_INCLUDES;

	static {
		Set<Class<?>> includes = new LinkedHashSet<Class<?>>();
		includes.add(ControllerAdvice.class);
		includes.add(JsonComponent.class);
		includes.add(WebMvcConfigurer.class);
		includes.add(javax.servlet.Filter.class);
		includes.add(FilterRegistrationBean.class);
		includes.add(DelegatingFilterProxyRegistrationBean.class);
		includes.add(HandlerMethodArgumentResolver.class);
		DEFAULT_INCLUDES = Collections.unmodifiableSet(includes);
	};

	private static final Set<Class<?>> DEFAULT_INCLUDES_AND_CONTROLLER;

	static {
		Set<Class<?>> includes = new LinkedHashSet<Class<?>>(DEFAULT_INCLUDES);
		includes.add(Controller.class);
		DEFAULT_INCLUDES_AND_CONTROLLER = Collections.unmodifiableSet(includes);
	}

	private final WebMvcTest annotation;

	WebMvcTypeExcludeFilter(Class<?> testClass) {
		this.annotation = AnnotatedElementUtils.getMergedAnnotation(testClass,
				WebMvcTest.class);
	}

	@Override
	protected boolean defaultInclude(MetadataReader metadataReader,
			MetadataReaderFactory metadataReaderFactory) throws IOException {
		if (super.defaultInclude(metadataReader, metadataReaderFactory)) {
			return true;
		}
		for (Class<?> controller : this.annotation.controllers()) {
			if (isTypeOrAnnotated(metadataReader, metadataReaderFactory, controller)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean hasAnnotation() {
		return this.annotation != null;
	}

	@Override
	protected Filter[] getFilters(FilterType type) {
		switch (type) {
		case INCLUDE:
			return this.annotation.includeFilters();
		case EXCLUDE:
			return this.annotation.excludeFilters();
		}
		throw new IllegalStateException("Unsupported type " + type);
	}

	@Override
	protected boolean isUseDefaultFilters() {
		return this.annotation.useDefaultFilters();
	}

	@Override
	protected Set<Class<?>> getDefaultIncudes() {
		if (ObjectUtils.isEmpty(this.annotation.controllers())) {
			return DEFAULT_INCLUDES_AND_CONTROLLER;
		}
		return DEFAULT_INCLUDES;
	}

}