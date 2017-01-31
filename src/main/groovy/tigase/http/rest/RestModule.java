/*
 * Tigase HTTP API
 * Copyright (C) 2004-2014 "Tigase, Inc." <office@tigase.com>
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
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
package tigase.http.rest;

import tigase.http.AbstractModule;
import tigase.http.DeploymentInfo;
import tigase.http.HttpServer;
import tigase.http.ServletInfo;
import tigase.http.util.StaticFileServlet;
import tigase.stats.StatisticHolder;
import tigase.stats.StatisticHolderImpl;
import tigase.stats.StatisticsList;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RestModule extends AbstractModule {
	
	private static final Logger log = Logger.getLogger(RestModule.class.getCanonicalName());
	
	private static final String DEF_SCRIPTS_DIR_VAL = "scripts/rest";
	
	private static final String SCRIPTS_DIR_KEY = "rest-scripts-dir";

	private static final String NAME = "rest";
	
	private final ReloadHandlersCmd reloadHandlersCmd = new ReloadHandlersCmd(this);
	private String contextPath = null;
	
	private HttpServer httpServer = null;
	private DeploymentInfo httpDeployment = null;

	private String scriptsDir = DEF_SCRIPTS_DIR_VAL;
	private String[] vhosts = null;
	private List<RestServlet> restServlets = new ArrayList<RestServlet>();
		
	private static final ConcurrentHashMap<String,StatisticHolder> stats = new ConcurrentHashMap<String, StatisticHolder>();
	
	@Override
	public void everyHour() {
		for (StatisticHolder holder : stats.values()) {
			holder.everyHour();
		} 		
	}
	
	@Override
	public void everyMinute() {
		for (StatisticHolder holder : stats.values()) {
			holder.everyMinute();
		} 	
	}
	
	@Override
	public void everySecond() {
		for (StatisticHolder holder : stats.values()) {
			holder.everySecond();
		} 		
	}	
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getDescription() {
		return "REST support - handles HTTP REST access using scripts";
	}
	
	@Override
	public void start() {
		if (httpDeployment != null) {
			stop();
		}

		super.start();
		httpDeployment = HttpServer.deployment().setClassLoader(this.getClass().getClassLoader())
				.setContextPath(contextPath).setService(new tigase.http.ServiceImpl(this)).setDeploymentName("REST API")
				.setDeploymentDescription(getDescription());
		if (vhosts != null) {
			httpDeployment.setVHosts(vhosts);
		}
		File scriptsDirFile = new File(scriptsDir);
		File[] scriptDirFiles = scriptsDirFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() && !"static".equals(file.getName());
			}
		});

		if (scriptDirFiles != null) {
			for (File dirFile : scriptDirFiles) {
				try {
					startRestServletForDirectory(httpDeployment, dirFile);
				} catch (IOException ex) {
					log.log(Level.FINE, "Exception while scanning for scripts to load", ex);
				}
			}
		}
		
		try {
			ServletInfo servletInfo = HttpServer.servlet("RestServlet", RestExtServlet.class);
			servletInfo.addInitParam(RestServlet.REST_MODULE_KEY, uuid)
					.addInitParam(RestServlet.SCRIPTS_DIR_KEY, scriptsDirFile.getCanonicalPath())
					.addMapping("/");
			httpDeployment.addServlets(servletInfo);
		} catch (IOException ex) {
			log.log(Level.FINE, "Exception while scanning for scripts to load", ex);
		}
		
		
		ServletInfo servletInfo = HttpServer.servlet("StaticServlet", StaticFileServlet.class);
		servletInfo.addInitParam(StaticFileServlet.DIRECTORY_KEY, new File(scriptsDirFile, "static").getAbsolutePath())
				.addMapping("/static/*");
		httpDeployment.addServlets(servletInfo);		
		
		httpServer.deploy(httpDeployment);
	}

	@Override
	public void stop() {
		if (httpDeployment != null) { 
			httpServer.undeploy(httpDeployment);
			httpDeployment = null;
		}
		restServlets = new ArrayList<RestServlet>();
		super.stop();
	}

	@Override
	public Map<String, Object> getDefaults() {
		Map<String,Object> props = super.getDefaults();
		props.put(HTTP_CONTEXT_PATH_KEY, "/" + getName());
		props.put(SCRIPTS_DIR_KEY, DEF_SCRIPTS_DIR_VAL);
		return props;
	}
	
	@Override
	public void setProperties(Map<String, Object> props) {
		super.setProperties(props);
		if (props.size() == 1)
			return;
		if (props.containsKey(HTTP_CONTEXT_PATH_KEY)) {
			contextPath = (String) props.get(HTTP_CONTEXT_PATH_KEY);		
		}
		if (props.containsKey(HTTP_SERVER_KEY)) {
			httpServer = (HttpServer) props.get(HTTP_SERVER_KEY);
		}
		if (props.containsKey(SCRIPTS_DIR_KEY)) {
			scriptsDir = (String) props.get(SCRIPTS_DIR_KEY);
		}
		vhosts = (String[]) props.get(VHOSTS_KEY);
		
		commandManager.registerCmd(reloadHandlersCmd);
	}
	
	@Override
	public void getStatistics(String compName, StatisticsList list) {
		for (StatisticHolder holder : stats.values()) {
			holder.getStatistics(compName, list);
		} 
	}			
	
	public void executedIn(String path, long executionTime) {
		StatisticHolder holder = stats.get(path);
		if (holder == null) {
			StatisticHolder tmp = new StatisticHolderImpl();
			tmp.setStatisticsPrefix(getName() + ", path=" + path);
			holder = stats.putIfAbsent(path, tmp);
			if (holder == null)
				holder = tmp;
		}
		holder.statisticExecutedIn(executionTime);
	}
	
	public void statisticExecutedIn(long executionTime) {
		
	}
	
	protected void registerRestServlet(RestServlet servlet) {
		restServlets.add(servlet);
	}
	
	protected List<? extends RestServlet> getRestServlets() {
		return restServlets;
	}
	
    private void startRestServletForDirectory(DeploymentInfo httpDeployment, File scriptsDirFile) 
			throws IOException {
        File[] scriptFiles = getGroovyFiles(scriptsDirFile);

        if (scriptFiles != null) {
			ServletInfo servletInfo = HttpServer.servlet("RestServlet", RestExtServlet.class);
			servletInfo.addInitParam(RestServlet.REST_MODULE_KEY, uuid)
					.addInitParam(RestServlet.SCRIPTS_DIR_KEY, scriptsDirFile.getCanonicalPath())
					.addInitParam("mapping", "/" + scriptsDirFile.getName() + "/*")
					.addMapping("/" + scriptsDirFile.getName() + "/*");
			httpDeployment.addServlets(servletInfo);
        }
    }	

	public static File[] getGroovyFiles( File scriptsDirFile) {
		return scriptsDirFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("groovy");
            }
        });
	}
}
