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
package org.integratedmodelling.currency;

import java.net.URL;

import org.integratedmodelling.currency.cpi.CpiConversionFactory;
import org.integratedmodelling.currency.units.CurrencyUnit;
import org.integratedmodelling.sql.SQLPlugin;
import org.integratedmodelling.sql.SQLServer;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.plugin.ThinklabPlugin;

public class CurrencyPlugin extends ThinklabPlugin  {

	CpiConversionFactory convFactory = null;
	URL cpiDataURL = null;
	URL exchangeDataURL = null;
	
	static final public String ID = "org.integratedmodelling.thinklab.currency";
	public static final String MONETARY_VALUE_OBSERVATION = "currency:MonetaryValuation";
	public static final String MONETARY_VALUE_OBSERVABLE = "economics:EconomicValue";
	
	public static CurrencyUnit USD = new CurrencyUnit("USD");
	public static CurrencyUnit EUR = new CurrencyUnit("EUR");
	public static CurrencyUnit GBP = new CurrencyUnit("GBP");
	
	public CpiConversionFactory getConverter() {
		return convFactory;
	}
	
	public static CurrencyPlugin get() {
		return (CurrencyPlugin) getPlugin(ID);
	}

	public void load(KnowledgeManager km) throws ThinklabException {
		
		requirePlugin("org.integratedmodelling.thinklab.sql");
		requirePlugin("org.integratedmodelling.thinklab.corescience");
		requirePlugin("org.integratedmodelling.thinklab.time");
		
		cpiDataURL = this.getResourceURL("cpidata.txt");
		exchangeDataURL = this.getResourceURL("exchrates.txt");

		String db = getProperties().getProperty("currency.database", "hsqlmem://sa@localhost/currency");
		
		logger().info("currency database is " + db);

		/**
		 * Create a database to hold the currency data using the given
		 * database from property, defaulting to HSQLDB in-memory. Any db-relevant properties
		 * can be put in currency.properties.
		 */
		SQLServer sqls = SQLPlugin.get().createSQLServer(db, getProperties());

		convFactory = new CpiConversionFactory();
		convFactory.initialize(
				sqls,
				exchangeDataURL, 
				cpiDataURL);
	}

	@Override
	protected void unload() throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

}
