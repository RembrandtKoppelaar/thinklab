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
package org.integratedmodelling.sql.postgres;

import java.net.URI;
import java.util.Properties;

import org.integratedmodelling.sql.SQLPlugin;
import org.integratedmodelling.sql.SQLServer;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabStorageException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;

public class PostgreSQLServer extends SQLServer {

	public static final int DEFAULT_PORT = 5432;

	public PostgreSQLServer(URI uri, Properties properties) throws ThinklabStorageException {
		initialize(uri, properties);
	}

	public PostgreSQLServer(String username, String password, String host) throws ThinklabStorageException {

		setUser(username);
		setPassword(password);
		setHost(host);
		initialize();
	}
	
	boolean useSSL = false;
	
	@Override
	public int getDefaultPort() {
		return DEFAULT_PORT;
	}

	@Override
	public String getDriverClass() {
		return "org.postgresql.Driver";
	}

	@Override
	public String getURI() {

		String ret = 
			"jdbc:postgresql://" +
			getHost() + 
			"/" +
			getDatabase() + 
			"?user=" + 
			getUser() + 
			"&password=" +
			getPassword();
		
			/* 
			 * must do it this way: the JDBC driver has a bug that requires the server
			 * to allow SSL even if we say ssl=false.
			 */
			if (useSSL) 
				ret += "&ssl=true";
			
			return ret;
	}

	@Override
	protected void startServer(Properties properties)
			throws ThinklabStorageException {
		// do nothing
	}

	@Override
	protected void stopServer() throws ThinklabStorageException {
		// do nothing
	}

	@Override
	public void createDatabase() throws ThinklabStorageException {
		// TODO add character set and anything needed to fit loaded schemata
		this.execute("CREATE DATABASE " + getDatabase() + ";");
	}

	@Override
	public void dropDatabase() throws ThinklabStorageException {
		this.execute("CLOSE DATABASE " + getDatabase() + ";");
		this.execute("DROP DATABASE " + getDatabase() + ";");		
	}
	
	public static boolean haveDatabase(String database, String user, String password, String host, int port) throws ThinklabStorageException {
		
		boolean ret = true;
		
		PostgreSQLServer pg = new PostgreSQLServer(user, password, host);
		pg.setDatabase(database);
		pg.initialize();
		
		try {
			pg.execute("SELECT 1;");
		} catch (ThinklabStorageException e) {
			ret = false;
		}

		return ret;
	}

	/**
	 * Use localhost, defaults, complain if not there
	 * 
	 * @param database
	 * @return
	 * @throws ThinklabStorageException
	 */
	public static boolean haveDatabase(String database) throws ThinklabException {

		String user = SQLPlugin.get().getDefaultUser();
		String pswd = SQLPlugin.get().getDefaultPassword();
		String host = SQLPlugin.get().getDefaultHost();
		
		if (user == null || pswd == null || host == null)
			throw new ThinklabValidationException("please set default database user and password in sql plugin properties");
		
		return haveDatabase(database, user, pswd, host, DEFAULT_PORT);
	}

	public static String getDefaultURI(String database) throws ThinklabValidationException {
		

		String user = SQLPlugin.get().getDefaultUser();
		String pswd = SQLPlugin.get().getDefaultPassword();
		String host = SQLPlugin.get().getDefaultHost();
		
		if (user == null || pswd == null || host == null)
			throw new ThinklabValidationException("please set default database user and password in sql plugin properties");
		
		return 
			"postgres://" + 
			user + 
			":" + 
			pswd + 
			"@" + 
			host + 
			":" + 
			5432 + 
			"/" 
			+ database;
	}
}
