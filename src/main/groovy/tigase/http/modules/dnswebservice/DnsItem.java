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
package tigase.http.modules.dnswebservice;

public class DnsItem {

	private final String domain;

	private final DnsEntry[] c2s;
	private final DnsEntry[] bosh;
	private final DnsEntry[] websocket;
	
	public DnsItem(String domain, DnsEntry[] c2s, DnsEntry[] bosh, DnsEntry[] websocket) {
		this.domain = domain;
		this.c2s = c2s;
		this.bosh = bosh;
		this.websocket = websocket;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public DnsEntry[] getC2S() {
		return c2s;
	}
	
	public DnsEntry[] getBosh() {
		return bosh;
	}
	
	public DnsEntry[] getWebSocket() {
		return websocket;
	}
	
}