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
package org.integratedmodelling.time.literals;

import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;

import javax.measure.quantity.Duration;

import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabNoKMException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.annotations.LiteralImplementation;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.literals.IValue;
import org.integratedmodelling.thinklab.literals.ParsedLiteralValue;
import org.integratedmodelling.time.TimePlugin;
import org.integratedmodelling.utils.MiscUtilities;
import org.integratedmodelling.utils.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.jscience.physics.amount.Amount;

/**
 * A parsed literal representing a time duration. Linked to the DurationValue OWL class to be used as an
 * extended literal for it. Maximum resolution is milliseconds. Can be initialized
 * by a string expressing number with units; unit must express time and the syntax is the one loaded in the 
 * JScience framework by the CoreScience plugin.
 *   
 * @author Ferdinando Villa
 *
 */
@LiteralImplementation(concept="time:DurationValue")
public class DurationValue extends ParsedLiteralValue {

    long value = 0l;
    String literal = null;
    int precision = TemporalPrecision.MILLISECOND;
    int origQuantity = 1;
    String origUnit = "";
    
    public DurationValue() throws ThinklabException {
        super();
        concept = TimePlugin.Duration();
    }

    @Override
    public void parseLiteral(String s) throws ThinklabValidationException {
    	
    	/*
    	 * insert a space to break strings like 1s or 2year
    	 */
    	int brk = -1;
    	if (!s.contains(" ")) {
    		for (int i = 0; i < s.length(); i++) {
    			if (!Character.isDigit(s.charAt(i))) {
    				brk = i;
    				origUnit = s.substring(i);
    				break;
    			}
    		}
    	} else {
    		origUnit = s.substring(s.indexOf(" ") + 1);
    	}
    	
    	if (brk > 0) {
    		s =
    			s.substring(0,brk) + 
    			" " +
    			s.substring(brk);
    	}
    	
		if (Character.isDigit(s.charAt(0)))
			this.origQuantity = MiscUtilities.readIntegerFromString(s);

        try  {
        	precision = TemporalPrecision.getPrecisionFromUnit(s);
        	literal = s;
        	/* oh do I like this */
        	Amount<Duration> duration = Amount.valueOf(s).to(MILLI(SECOND));
        	value = duration.getExactValue();
        	concept = TimePlugin.Duration();
        } catch (Exception e) {
            throw new ThinklabValidationException(e);
        }
    }

    public DurationValue(IConcept c) throws ThinklabException {
        super(c);
        value = 0l;
    }
    
    public DurationValue(String s) throws ThinklabValidationException, ThinklabNoKMException {
        parseLiteral(s);
    }
    
    public boolean isNumber() {
        return false;
    }

    public boolean isText() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }
    
    public boolean isClass() {
        return false;
    }
 
    public boolean isObject() {
        return false;
    }
    
    public boolean isLiteral() {
        return true;
    } 

    public String toString() {
        return 
        	literal == null ?
        		(value + " ms"):
        		literal;
    }
    
    public IValue clone() {
        DurationValue ret = null;
        try {
            ret = new DurationValue(concept);
            ret.value = value;
        } catch (ThinklabException e) {
        }
        return ret;
    }

	public long getMilliseconds() {
		return value;
	}
    
	@Override
	public Object demote() {
		return value;
	}

	/**
	 * Localize a duration to an extent starting at the current moment
	 * using the same resolution that was implied in the generating 
	 * text. For example, if the duration was one year, localize to the 
	 * current year (jan 1st to dec 31st). Return the start and end points
	 * of the extent.
	 * 
	 * @return
	 */
	public Pair<TimeValue, TimeValue> localize() {
		
		DateTime date = new DateTime();
		TimeValue start = null, end = null;
		long val = value;
		
		switch (precision) {
		
			case TemporalPrecision.MILLISECOND:
				start = new TimeValue(date);
				end = new TimeValue(date.plus(val));
				break;
			case TemporalPrecision.SECOND:
				val = value/DateTimeConstants.MILLIS_PER_SECOND;
				start = 
					new TimeValue(
						new DateTime(
							date.getYear(),
							date.getMonthOfYear(),
							date.getDayOfMonth(),
							date.getHourOfDay(),
							date.getMinuteOfHour(),
							date.getSecondOfMinute(),
							0));
				end = new TimeValue(start.getTimeData().plusSeconds((int)val));
				break;
			case TemporalPrecision.MINUTE:
				val = value/DateTimeConstants.MILLIS_PER_MINUTE;
				start = 
					new TimeValue(
						new DateTime(
							date.getYear(),
							date.getMonthOfYear(),
							date.getDayOfMonth(),
							date.getHourOfDay(),
							date.getMinuteOfHour(),
							0,
							0));
				end = new TimeValue(start.getTimeData().plusMinutes((int)val));
				break;
			case TemporalPrecision.HOUR:
				val = value/DateTimeConstants.MILLIS_PER_HOUR;
				start = 
					new TimeValue(
						new DateTime(
							date.getYear(),
							date.getMonthOfYear(),
							date.getDayOfMonth(),
							date.getHourOfDay(),
							0,
							0,
							0));
				end = new TimeValue(start.getTimeData().plusHours((int)val));
				break;
			case TemporalPrecision.DAY:
				val = value/DateTimeConstants.MILLIS_PER_DAY;
				start = 
					new TimeValue(
						new DateTime(
							date.getYear(),
							date.getMonthOfYear(),
							date.getDayOfMonth(),
							0,
							0,
							0,
							0));
				end = new TimeValue(start.getTimeData().plusDays((int)val));
				break;
			case TemporalPrecision.MONTH:
				start = 
					new TimeValue(
						new DateTime(
							date.getYear(),
							date.getMonthOfYear(),
							1,
							0,
							0,
							0,
							0));
				end = new TimeValue(start.getTimeData().plusMonths(origQuantity));
				break;
			case TemporalPrecision.YEAR:
				start = 
					new TimeValue(
						new DateTime(
							date.getYear(),
							1,
							1,
							0,
							0,
							0,
							0));
				end = new TimeValue(start.getTimeData().plusYears(origQuantity));
				break;		
		}
		
		return new Pair<TimeValue, TimeValue>(start, end);
	}
	
	public int getOriginalQuantity() {
		return origQuantity;
	}
	
	public String getOriginalUnit() {
		return origUnit;
	}
}
