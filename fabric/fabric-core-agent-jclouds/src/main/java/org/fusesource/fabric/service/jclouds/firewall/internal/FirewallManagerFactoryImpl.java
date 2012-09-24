/*
 * Copyright (C) FuseSource, Inc.
 *   http://fusesource.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.fusesource.fabric.service.jclouds.firewall.internal;

import java.util.HashMap;
import java.util.Map;
import org.fusesource.fabric.service.jclouds.firewall.FirewallManager;
import org.fusesource.fabric.service.jclouds.firewall.FirewallManagerFactory;
import org.fusesource.fabric.service.jclouds.firewall.FirewallNotSupportedOnProviderException;
import org.fusesource.fabric.service.jclouds.firewall.ProviderFirewallSupport;
import org.jclouds.compute.ComputeService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class FirewallManagerFactoryImpl implements FirewallManagerFactory {

    private final Map<String, ProviderFirewallSupport> support = new HashMap<String, ProviderFirewallSupport>();
    private final Map<String, FirewallManager> managers = new HashMap<String, FirewallManager>();

    private BundleContext bundleContext;
    private ServiceTracker firewallSupportModuleTracker;

    public void init() {
        firewallSupportModuleTracker = new ServiceTracker(bundleContext,ProviderFirewallSupport.class.getName(), null) {

            @Override
            public Object addingService(ServiceReference reference) {
                ProviderFirewallSupport support = (ProviderFirewallSupport) bundleContext.getService(reference);
                bind(support);
                return support;
            }


            @Override
            public void removedService(ServiceReference reference, Object service) {
                ProviderFirewallSupport support = (ProviderFirewallSupport) service;
                unbind(support);
                super.removedService(reference, service);
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
                ProviderFirewallSupport support = (ProviderFirewallSupport) service;
                bind(support);
                super.modifiedService(reference, service);
            }
        };
        firewallSupportModuleTracker.open();
    }

    public void destroy() {
        if (firewallSupportModuleTracker != null) {
            firewallSupportModuleTracker.close();
        }
    }

    /**
     * Returns a {@link org.fusesource.fabric.service.jclouds.firewall.FirewallManager} for the specified {@link org.jclouds.compute.ComputeService}.
     *
     * @param computeService
     * @return
     */
    @Override
    public synchronized FirewallManager getFirewallManager(ComputeService computeService) throws FirewallNotSupportedOnProviderException {
        FirewallManager firewallManager = null;
        String provider = computeService.getContext().unwrap().getId();

        firewallManager = managers.get(provider);

        if (firewallManager == null) {
            ProviderFirewallSupport firewallSupport = support.get(provider);
            if (firewallSupport == null) {
                throw new FirewallNotSupportedOnProviderException("Provider "+ provider+ " is currently not supported for firewall operations");
            }
            firewallManager = new FirewallManager(computeService, firewallSupport);
            managers.put(provider, firewallManager);
        }
        return firewallManager;
    }

    public void bind(ProviderFirewallSupport providerSupport) {
        if (providerSupport != null && providerSupport.getProviders() != null) {
            for (String provider : providerSupport.getProviders()) {
                support.put(provider, providerSupport);
            }
        }
    }

    public void unbind(ProviderFirewallSupport providerSupport) {
        if (providerSupport != null && providerSupport.getProviders() != null) {
            for (String provider : providerSupport.getProviders()) {
                support.remove(provider);
            }
        }
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
