/*
 * Tigase HTTP API component - Tigase HTTP API component
 * Copyright (C) 2013 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.http.modules.dnswebservice;

import tigase.http.DeploymentInfo;
import tigase.http.HttpMessageReceiver;
import tigase.http.ServletInfo;
import tigase.http.modules.AbstractModule;
import tigase.http.modules.wellknown.WellKnownServletsProvider;
import tigase.kernel.beans.Bean;
import tigase.kernel.beans.selector.ConfigType;
import tigase.kernel.beans.selector.ConfigTypeEnum;

import java.util.List;

@Bean(name = "dns-webservice", parent = HttpMessageReceiver.class, active = true, exportable = true)
@ConfigType({ConfigTypeEnum.DefaultMode, ConfigTypeEnum.SessionManagerMode, ConfigTypeEnum.ConnectionManagersMode,
			 ConfigTypeEnum.ComponentMode})
public class DnsWebServiceModule
		extends AbstractModule implements WellKnownServletsProvider {

	private final List<ServletInfo> wellKnownServletInfos;
	private DeploymentInfo deployment = null;

	public  DnsWebServiceModule() {
		super();
		wellKnownServletInfos = List.of(new ServletInfo("HostMeta", DnsHostMetaServlet.class).addMapping("/host-meta").addInitParam("format", "xml"),new ServletInfo("HostMeta", DnsHostMetaServlet.class).addMapping("/host-meta.json").addInitParam("format", "json"));
	}

	@Override
	public String getDescription() {
		return "WebService for DNS resolution";
	}

	@Override
	public void start() {
		if (deployment != null) {
			stop();
		}

		super.start();

		deployment = httpServer.deployment()
				.setClassLoader(this.getClass().getClassLoader())
				.setContextPath(contextPath)
				.setDeploymentName("DnsWebService")
				.setDeploymentDescription(getDescription())
				.addServlets(httpServer.servlet("HostMeta", DnsHostMetaServlet.class).addMapping("/.well-known/host-meta").addMapping("/.well-known/host-meta.json"), httpServer.servlet("JsonServlet", JsonServlet.class).addMapping("/*"));
		if (vhosts != null) {
			deployment.setVHosts(vhosts);
		}

		httpServer.deploy(deployment);
	}

	@Override
	public void stop() {
		if (deployment != null) {
			httpServer.undeploy(deployment);
			deployment = null;
		}
		super.stop();
	}

	@Override
	public List<ServletInfo> getServletInfos() {
		return wellKnownServletInfos;
	}
}
