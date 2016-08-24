/*
 * Tigase HTTP API
 * Copyright (C) 2004-2016 "Tigase, Inc." <office@tigase.com>
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
package tigase.http.modules.ui;

import tigase.http.HttpMessageReceiver;
import tigase.kernel.beans.Bean;
import tigase.kernel.beans.BeanSelector;
import tigase.kernel.core.Kernel;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by andrzej on 05.08.2016.
 */
@Bean(name = "ui", parent = HttpMessageReceiver.class, selectors = { UIModule.UIModuleSelector.class })
public class UIModule extends WebModule {

	public UIModule() {
		File warFile = getWarFile();
		this.warPath = warFile.getAbsolutePath();
	}

	@Override
	public String getDescription() {
		return "Web UI XMPP client and management utility";
	}

	public static File getWarFile() {
		File[] files = new File("jars").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".war") && name.startsWith("tigase-web-ui");
			}
		});
		return (files != null && files.length > 0) ? files[0] : null;
	}

	public static class UIModuleSelector implements BeanSelector {

		@Override
		public boolean shouldRegister(Kernel kernel) {
			return getWarFile() != null;
		}
	}
}