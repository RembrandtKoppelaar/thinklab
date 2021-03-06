/**
 * Copyright 2011 The ARIES Consortium (http://www.ariesonline.org) and
 * www.integratedmodelling.org. 

   This file is part of Thinklab.

   Thinklab is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published
   by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Thinklab is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Thinklab.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.integratedmodelling.thinklab.authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.integratedmodelling.thinklab.Thinklab;
import org.integratedmodelling.thinklab.exception.ThinklabAuthenticationException;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabPluginException;
import org.integratedmodelling.thinklab.exception.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.interfaces.IThinklabAuthenticationProvider;
import org.integratedmodelling.thinklab.interfaces.applications.ISession;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;

/**
 * The singleton authentication manager is an authentication provider that
 * selects the actual provider using properties and proxies requests to it. If
 * no authentication provider is requested in the thinklab properties, the
 * simple auth provider (based on a user.xml file) is created.
 * 
 * @author Ferdinando Villa
 */
public class AuthenticationManager implements IThinklabAuthenticationProvider {

	public static final String USERID_PROPERTY = "authentication.user.id";
	public static final String USER_PROPERTIES = "authentication.user.properties";
	public static final String USE_ENCRYPTION_PROPERTY = "authentication.encryption";

	static AuthenticationManager _this = null;

	IThinklabAuthenticationProvider authManager = null;

	public static AuthenticationManager get() {

		if (_this == null)
			try {
				_this = new AuthenticationManager();
			} catch (ThinklabException e) {
				throw new ThinklabRuntimeException(e);
			}
		return _this;
	}

	public AuthenticationManager() throws ThinklabException {

		/*
		 * create the auth manager specified in plugin properties, if any. If
		 * nothing is specified, use the simple file-based authenticator.
		 */
		String authClass = Thinklab.get().getProperties()
				.getProperty("authentication.manager.class");

		if (authClass != null && !authClass.equals("")) {

			try {
				Class<?> cl = Class.forName(authClass);
				if (cl != null) {
					authManager = (IThinklabAuthenticationProvider) cl
							.newInstance();
				}
			} catch (Exception e) {
				throw new ThinklabAuthenticationException(e);
			}
		} else {
			authManager = new SimpleAuthenticationProvider();
		}

		authManager.initialize(Thinklab.get().getProperties());
	}

	public boolean haveAuthentication() {
		return authManager != null;
	}

	public boolean authenticateUser(String username, String password,
			Properties userProperties) throws ThinklabException {

		boolean ret = false;

		if (authManager != null) {
			try {
				ret = authManager.authenticateUser(username, password,
						userProperties);
			} catch (ThinklabException e) {
				/* do nothing, just return false. */
			}
		}

		return ret;
	}

	public Properties getUserProperties(String username)
			throws ThinklabException {

		return authManager == null ? new Properties() : authManager
				.getUserProperties(username);
	}

	public String getUserProperty(String user, String property,
			String defaultValue) throws ThinklabException {

		return authManager == null ? null : authManager.getUserProperty(user,
				property, defaultValue);
	}

	public void saveUserProperties(String user) throws ThinklabException {

		if (authManager == null) {
			throw new ThinklabPluginException(
					"no authentication scheme selected: can't save user properties");
		}

		authManager.saveUserProperties(user);

	}

	public void setUserProperty(String user, String property, String value)
			throws ThinklabException {

		if (authManager == null) {
			throw new ThinklabPluginException(
					"no authentication scheme selected: can't save user properties");
		}

		authManager.setUserProperty(user, property, value);
	}

	public void createUser(String user, String password)
			throws ThinklabException {

		if (authManager == null) {
			throw new ThinklabPluginException(
					"no authentication scheme selected: can't add user");
		}
		authManager.createUser(user, password);
	}

	public boolean haveUser(String user) {

		if (authManager == null) {
			return false;
		}

		return authManager.haveUser(user);
	}

	public void setUserPassword(String user, String password)
			throws ThinklabException {

		if (authManager == null) {
			throw new ThinklabPluginException(
					"no authentication scheme selected: can't set user password");
		}

		authManager.setUserPassword(user, password);
	}

	@Override
	public void deleteUser(String user) throws ThinklabException {

		if (authManager == null) {
			throw new ThinklabPluginException(
					"no authentication scheme selected: can't set user password");
		}
		authManager.deleteUser(user);
	}

	@Override
	public Collection<String> listUsers() throws ThinklabException {
		if (authManager == null) {
			return new ArrayList<String>();
		}
		return authManager.listUsers();
	}

	@Override
	public void initialize(Properties properties) throws ThinklabException {
	}

	@Override
	public IInstance getUserInstance(String user, ISession session) throws ThinklabException {
		return authManager == null ? null : authManager.getUserInstance(user, session);
	}

}
