/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.ee.component.interceptor;

import java.util.List;
import org.jboss.as.ee.component.injection.ResourceInjection;
import org.jboss.invocation.InterceptorFactoryContext;
import org.jboss.invocation.InterceptorInstanceFactory;

/**
 * Interceptor instance factory that applies injections to the interceptor instance once the interceptors is created.
 *
 * @author John Bailey
 */
public class InjectingInterceptorInstanceFactory implements InterceptorInstanceFactory{

    private final InterceptorInstanceFactory delegate;
    private final List<ResourceInjection> interceptorInjections;

    public InjectingInterceptorInstanceFactory(final InterceptorInstanceFactory delegate, final List<ResourceInjection> interceptorInjections) {
        this.delegate = delegate;
        this.interceptorInjections = interceptorInjections;
    }

    public Object createInstance(final InterceptorFactoryContext context) {
        final Object instance = delegate.createInstance(context);

        for(ResourceInjection injection : interceptorInjections) {
            injection.inject(instance);
        }

        return instance;
    }
}
