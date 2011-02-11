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

package org.jboss.as.server.deployment;

import java.util.List;
import java.util.ListIterator;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.DelegatingServiceRegistry;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * A service which executes a particular phase of deployment.
 *
 * @param <T> the public type of this deployment unit phase
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class DeploymentUnitPhaseService<T> implements Service<T> {

    private final InjectedValue<DeployerChains> deployerChainsInjector = new InjectedValue<DeployerChains>();
    private final InjectedValue<DeploymentUnit> deploymentUnitInjector = new InjectedValue<DeploymentUnit>();
    private final Phase phase;
    private final AttachmentKey<T> valueKey;

    private static final Logger log = Logger.getLogger("org.jboss.as.server.deployment");

    private DeploymentUnitPhaseService(final Phase phase, final AttachmentKey<T> valueKey) {
        this.phase = phase;
        this.valueKey = valueKey;
    }

    private static <T> DeploymentUnitPhaseService<T> create(final Phase phase, AttachmentKey<T> valueKey) {
        return new DeploymentUnitPhaseService<T>(phase, valueKey);
    }

    static DeploymentUnitPhaseService<?> create(final Phase phase) {
        return create(phase, phase.getPhaseKey());
    }

    public synchronized void start(final StartContext context) throws StartException {
        final DeployerChains chains = deployerChainsInjector.getValue();
        final DeploymentUnit deploymentUnit = deploymentUnitInjector.getValue();
        final List<DeploymentUnitProcessor> list = chains.getChain(phase);
        final ListIterator<DeploymentUnitProcessor> iterator = list.listIterator();
        final ServiceContainer container = context.getController().getServiceContainer();
        final ServiceTarget serviceTarget = context.getChildTarget().subTarget();
        final DeploymentPhaseContext processorContext = new DeploymentPhaseContextImpl(serviceTarget, new DelegatingServiceRegistry(container), deploymentUnit, phase);
        while (iterator.hasNext()) {
            final DeploymentUnitProcessor processor = iterator.next();
            try {
                processor.deploy(processorContext);
            } catch (Throwable e) {
                while (iterator.hasPrevious()) {
                    final DeploymentUnitProcessor prev = iterator.previous();
                    safeUndeploy(deploymentUnit, phase, prev);
                }
                throw new StartException(String.format("Failed to process phase %s of %s", phase, deploymentUnit), e);
            }
        }
        final Phase nextPhase = phase.next();
        if (nextPhase != null) {
            final String name = deploymentUnit.getName();
            final DeploymentUnit parent = deploymentUnit.getParent();
            final ServiceName serviceName = parent == null ? Services.deploymentUnitName(name, nextPhase) : Services.deploymentUnitName(parent.getName(), name, nextPhase);
            final DeploymentUnitPhaseService<?> phaseService = DeploymentUnitPhaseService.create(nextPhase);
            final ServiceBuilder<?> phaseServiceBuilder = serviceTarget.addService(serviceName, phaseService);
            phaseServiceBuilder.addDependency(deploymentUnit.getServiceName(), DeploymentUnit.class, phaseService.getDeploymentUnitInjector());
            phaseServiceBuilder.addDependency(Services.JBOSS_DEPLOYMENT_CHAINS, DeployerChains.class, phaseService.getDeployerChainsInjector());
            phaseServiceBuilder.addDependency(context.getController().getName());

            final List<ServiceName> nextPhaseDeps = processorContext.getAttachment(Attachments.NEXT_PHASE_DEPS);
            if(nextPhaseDeps != null) {
                phaseServiceBuilder.addDependencies(nextPhaseDeps);
            }

            phaseServiceBuilder.install();
        }
    }

    public synchronized void stop(final StopContext context) {
        context.asynchronous();
        final DeploymentUnit deploymentUnitContext = deploymentUnitInjector.getValue();
        final DeployerChains chains = deployerChainsInjector.getValue();
        final List<DeploymentUnitProcessor> list = chains.getChain(phase);
        final ListIterator<DeploymentUnitProcessor> iterator = list.listIterator(list.size());
        while (iterator.hasPrevious()) {
            final DeploymentUnitProcessor prev = iterator.previous();
            safeUndeploy(deploymentUnitContext, phase, prev);
        }
    }

    private static void safeUndeploy(final DeploymentUnit deploymentUnit, final Phase phase, final DeploymentUnitProcessor prev) {
        try {
            prev.undeploy(deploymentUnit);
        } catch (Throwable t) {
            log.errorf(t, "Deployment unit processor %s unexpectedly threw an exception during undeploy phase %s of %s", prev, phase, deploymentUnit);
        }
    }

    public synchronized T getValue() throws IllegalStateException, IllegalArgumentException {
        return deploymentUnitInjector.getValue().getAttachment(valueKey);
    }

    InjectedValue<DeployerChains> getDeployerChainsInjector() {
        return deployerChainsInjector;
    }

    Injector<DeploymentUnit> getDeploymentUnitInjector() {
        return deploymentUnitInjector;
    }
}
