package org.integratedmodelling.corescience.units;

import java.io.PrintStream;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Dimension;
import javax.measure.unit.ProductUnit;

import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.utils.MiscUtilities;
import org.integratedmodelling.utils.Pair;

public class Unit {

	javax.measure.unit.Unit<?> _unit; 
	
	public Unit(javax.measure.unit.Unit<?> unit) {
		_unit = unit;
	}
	
	public Unit(String s) throws ThinklabValidationException {
		
		Pair<Double, String> pd = MiscUtilities.splitNumberFromString(s);
		
		// TEMPORARY - REMOVE
		if (pd == null)
			pd = new Pair<Double,String>(null, s);
		
		double factor = 1.0;
		if (pd.getFirst() != null) {
			factor = pd.getFirst();
		}
		
		try {
			_unit = javax.measure.unit.Unit.valueOf(pd.getSecond());
		} catch (Exception e) {
			throw new ThinklabValidationException(e);
		}
		
		if (factor != 1.0) {
			_unit = _unit.times(factor);
		}
	}
	
	/**
	 * Convert the given value from the passed unit to the unit we
	 * represent.
	 * 
	 * @param value
	 * @param unit
	 * @return
	 */
	public double convert(double value, Unit unit) {
		UnitConverter converter = unit.getUnit().getConverterTo(_unit);
		return converter.convert(value);
	}
	
	public javax.measure.unit.Unit<?> getUnit() {
		return _unit;
	}
	
	public boolean isRate() {
		
		boolean ret = false;
		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if (su.getDimension().equals(Dimension.TIME) && power == -1) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	public Unit getTimeExtentUnit() {
		
		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if (su.getDimension().equals(Dimension.TIME) && power == -1) {
					return new Unit(su);
				}
			}
		}
		return null;
	}
	public boolean isLengthDensity() {
		boolean ret = false;
		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if (su.getDimension().equals(Dimension.LENGTH) && power == -1) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	
	public Unit getLengthExtentUnit() {

		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if (su.getDimension().equals(Dimension.LENGTH) && power == -1) {
					return new Unit(su);
				}
			}
		}
		return null;
	}
	
	public boolean isArealDensity() {
		boolean ret = false;
		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if ((su.getDimension().equals(Dimension.LENGTH.pow(2)) && power == -1) ||
					(su.getDimension().equals(Dimension.LENGTH) && power == -2)) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * If the unit represents an areal density, return the area term with 
	 * inverted exponents - e.g. if we are something per square meter, return
	 * square meters. If not an areal density, return null.
	 * 
	 * @return
	 */
	public Unit getArealExtentUnit() {

		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if ((su.getDimension().equals(Dimension.LENGTH.pow(2)) && power == -1) ||
					(su.getDimension().equals(Dimension.LENGTH) && power == -2)) {
					return new Unit(su);
				}
			}
		}
		return null;
	}
	
	public boolean isVolumeDensity() {
		boolean ret = false;
		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if (su.getDimension().equals(Dimension.LENGTH.pow(3)) && power == -1||
						(su.getDimension().equals(Dimension.LENGTH) && power == -3)) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	
	public Unit getVolumeExtentUnit() {
		
		if (_unit instanceof ProductUnit<?>) {
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				javax.measure.unit.Unit<?> su = pu.getUnit(i);
				int power = pu.getUnitPow(i);
				if (su.getDimension().equals(Dimension.LENGTH.pow(3)) && power == -1||
						(su.getDimension().equals(Dimension.LENGTH) && power == -3)) {
					return new Unit(su);
				}
			}
		}
		return null;
	}
	
	
	@Override
	public String toString() {
		return _unit.toString();
	}
	
	public boolean isUnitless() {

		boolean ret = false;
		
		if (_unit instanceof ProductUnit<?>) {
			
			// assume no unitless unit without a distribution
			ret = true;
			ProductUnit<?> pu = (ProductUnit<?>)_unit;
			for (int i = 0; i < pu.getUnitCount(); i++) {
				int power = pu.getUnitPow(i);
				if (power > 0) {
					ret = false;
					break;
				}
			}
		}
		return ret;
		
	}

	public void dump(PrintStream out) {

		out.println("unit " + _unit);
		out.println("is" + (isUnitless() ? " " : " not ") +  "unitless");
		out.println("is" + (isRate() ? " " : " not ") +  "a rate");
		out.println("is" + (isLengthDensity() ? " " : " not ") +  "a lenght density");
		out.println("is" + (isArealDensity() ? " " : " not ") +  "an areal density");
		out.println("is" + (isVolumeDensity() ? " " : " not ") +  "a volumetric density");
	}
}
