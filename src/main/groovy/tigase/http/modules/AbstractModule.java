/*
 * AbstractModule.java
 *
 * Tigase HTTP API
 * Copyright (C) 2004-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.http.modules;

import tigase.db.AuthRepository;
import tigase.db.UserRepository;
import tigase.http.modules.rest.ApiKeyRepository;
import tigase.kernel.beans.Initializable;
import tigase.kernel.beans.Inject;
import tigase.kernel.beans.UnregisterAware;
import tigase.kernel.beans.config.ConfigurationChangedAware;
import tigase.xmpp.jid.BareJID;

public abstract class AbstractModule
		extends AbstractBareModule
		implements Module, Initializable, ConfigurationChangedAware, UnregisterAware {

	@Inject
	private ApiKeyRepository apiKeyRepository;
	@Inject
	private AuthRepository authRepository;
	@Inject
	private UserRepository userRepository;

	public static <T extends Module> T getModuleByUUID(String uuid) {
		return (T) AbstractBareModule.getModuleByUUID(uuid);
	}

	public void setApiKeyRepository(ApiKeyRepository apiKeyRepository) {
		if (getComponentName() != null) {
			apiKeyRepository.setRepoUser(BareJID.bareJIDInstanceNS(getName(), getComponentName()));
			apiKeyRepository.setRepo(userRepository);
		}
		this.apiKeyRepository = apiKeyRepository;
	}

	@Override
	public boolean isRequestAllowed(String key, String domain, String path) {
		return apiKeyRepository.isAllowed(key, domain, path);
	}

	@Override
	public UserRepository getUserRepository() {
		return userRepository;
	}

	@Override
	public AuthRepository getAuthRepository() {
		return authRepository;
	}

	@Override
	public void stop() {
		super.stop();
		apiKeyRepository.setAutoloadTimer(0);
	}

}
