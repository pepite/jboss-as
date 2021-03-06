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

package org.jboss.as.server.deployment.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

/**
 * @author John E. Bailey
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ModuleDependency implements Serializable {

    private static final long serialVersionUID = 2749276798703740853L;

    private final ModuleLoader moduleLoader;
    private final ModuleIdentifier identifier;
    private final boolean export;
    private final boolean optional;
    private final List<FilterSpecification> importFilters = new ArrayList<FilterSpecification>();
    private final List<FilterSpecification> exportFilters = new ArrayList<FilterSpecification>();
    private final boolean importServices;

    /**
     * Construct a new instance.
     *
     * @param moduleLoader the module loader of the dependency (if {@code null}, then use the default server module loader)
     * @param identifier the module identifier
     * @param optional {@code true} if this is an optional dependency
     * @param export {@code true} if resources should be exported by default
     * @param importServices
     */
    public ModuleDependency(final ModuleLoader moduleLoader, final ModuleIdentifier identifier, final boolean optional, final boolean export, final boolean importServices) {
        this.identifier = identifier;
        this.optional = optional;
        this.export = export;
        this.moduleLoader = moduleLoader;
        this.importServices = importServices;
    }

    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }

    public ModuleIdentifier getIdentifier() {
        return identifier;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isExport() {
        return export;
    }

    public List<FilterSpecification> getImportFilters() {
        return importFilters;
    }

    public List<FilterSpecification> getExportFilters() {
        return exportFilters;
    }

    public boolean isImportServices() {
        return importServices;
    }
}
