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

package org.jboss.as.server.client.impl.deployment;

import org.jboss.as.server.client.api.deployment.DeploymentAction;
import org.jboss.as.server.client.api.deployment.DeploymentPlanBuilder;
import org.jboss.as.server.client.api.deployment.ReplaceDeploymentPlanBuilder;

/**
 * Subclass of {@link DeploymentPlanBuilderImpl} that exposes the additional
 * {@link ReplaceDeploymentPlanBuilder} operations.
 *
 * @author Brian Stansberry
 */
class ReplaceDeploymentPlanBuilderImpl extends DeploymentPlanBuilderImpl implements ReplaceDeploymentPlanBuilder {

    private final DeploymentAction replacementModification;

    ReplaceDeploymentPlanBuilderImpl(DeploymentPlanBuilderImpl existing, DeploymentActionImpl modification) {
        super(existing, modification);
        this.replacementModification = modification;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.deployment.client.api.server.ReplaceDeploymentPlanBuilder#andRemoveUndeployed()
     */
    @Override
    public DeploymentPlanBuilder andRemoveUndeployed() {
        DeploymentActionImpl removeMod = DeploymentActionImpl.getRemoveAction(replacementModification.getReplacedDeploymentUnitUniqueName());
        return new DeploymentPlanBuilderImpl(this, removeMod);
    }
}