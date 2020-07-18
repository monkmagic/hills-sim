package com.hills.sim.settings;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.hills.sim.Constants.SettingE;
import com.hills.sim.Constants.SettingValueE;
import com.hills.sim.exceptions.BoundedExc;
import com.hills.sim.exceptions.HtmlParserExc;
import com.hills.sim.exceptions.RangeExc;
import com.hills.sim.exceptions.SettingExc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * SEHtmlParser will parse and create the Settings
 * @author Mark L
 *
 */
public class HtmlParser {

	private String filename;
	
	/**
	 * Make SEHtmlParser, given a filename for the Html Settings
	 * @param filename Filename of the settings file
	 */
	public HtmlParser(String filename) {
		this.filename = filename;
	}
	
	/**
	 * Return a list of Setting from a pathname to the Html settings file
	 * @return
	 * @throws HtmlParserExc
	 */
	public List<Setting> makeSettingsFromHtml() 
							throws HtmlParserExc {
		
		List<Setting> result = new ArrayList<>();
		Elements elems = null;
		
//		Throws exceptions when:
//			1. Invalid pathname is given
//			2. Invalid content in file
//			3. Invalid syntax for the setting file is found
		try {
			File file = new File(this.filename);
			Document html_doc = Jsoup.parse(file, "UTF-8");
			elems = html_doc.select("TR.SETTING");
			
			for(Element e: elems)
				result.add(this.makeSetting(e));
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.err.printf("%s: Invalid filename path for html file\n", 
								this.getClass().getName());
			throw new HtmlParserExc();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.printf("%s: Unable to parse content from html file\n", 
								this.getClass().getName());
			throw new HtmlParserExc();	
			
		} catch (HtmlParserExc e) {
			e.printStackTrace();
			System.err.printf("%s: Incorrect setting syntax is used\n", 
								this.getClass().getName());
			throw new HtmlParserExc();				
		}
		
		return(result);
	}

	/**
	 * Factory method to create the Primitive, Bounded and Range settings,
	 * with the setting values.
	 * It works by extracting the html attributes, values and produce "Setting-SettingValue" type.
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private Setting makeSetting(Element tr) throws HtmlParserExc {
		Setting result = null;
		
		String key, html_class;
		SettingE setting_type;
		SettingValueE setting_value_type;
		String[] value = null;

//		Extract the ID attribute and the Class name from HTML TR tag
		try {
			key = this.getKeyFromElement(tr);
			html_class = this.getHtmlClassFromElement(tr);
			
		} catch (HtmlParserExc e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf("%s: Error parsing HTML TR attribute(s) ID and Class\n", 
								this.getClass().getName());
			throw new HtmlParserExc();
		}

//		Extract the "Setting" and "SettingValue" from the Class name "Setting-SettingValue"		
		try {
			setting_type = this.getSettingTypeFromElement(tr);
			setting_value_type = this.getSettingValueFromElement(tr);
			
		} catch (HtmlParserExc e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf("%s: Error parsing HTML TR Class attribute for "+
							  "\"Setting-SettingValue\"\n", 
							  this.getClass().getName());
			throw new HtmlParserExc();
		}
		
//		Extract the value(s) from Html TD tags, within the TR tag
		try {
			switch(setting_type) {
				case PRIMITIVE:
					value = new String[] {this.getPValueFromElement(tr)};
					break;
					
				case BOUNDED:
					value = this.getBValuesFromElement(tr);
					break;
					
				case RANGE:
					value = this.getRValuesFromElement(tr);
					break;
				
				default: 
					System.err.println(tr.toString());
					System.err.printf("%s: Error parsing HTML TD value(s)\n", 
							this.getClass().getName());
					throw new HtmlParserExc();
			}
			
		} catch (HtmlParserExc e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf("%s: Error parsing HTML TD value(s)\n", 
								this.getClass().getName());
			throw new HtmlParserExc();
		}

//		Make the Setting
		try {
			switch(setting_type) {
				case PRIMITIVE:
					result = this.makePrimitive(setting_value_type, value, key, html_class);
					break;
					
				case BOUNDED:
					result = this.makeBounded(setting_value_type, value, key, html_class);
					break;
					
				case RANGE:
					result = this.makeRange(setting_value_type, value, key, html_class);
					break;
					
				default: 
					System.err.printf("%s: Error making setting\n", this.getClass().getName());
					throw new HtmlParserExc();
			}
			
		} catch (SettingExc e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf("%s: Error making setting\n", this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		return(result);
	}
	
	/**
	 * Method to make the Range setting. Uses a helper method to check that start, end, and step.
	 * @param setting_value_type Basic setting value types (i.e Integer, Boolean etc)
	 * @param value Basic setting value (i.e Integer, Boolean etc)
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @return 
	 * @throws SettingExc
	 */
	private Setting makeRange(SettingValueE setting_value_type, 
								   String[] value, String key, String html_class) 
					throws SettingExc {
		Setting result;
		String error_msg = "%s: Error making Range setting\n";
		
		try {
			SVInteger step;
			switch (setting_value_type) {
				case DATE:
					SVDate d_start = new SVDate(value[0]);
					SVDate d_end = new SVDate(value[1]);
					step = new SVInteger(value[2]);
					result = makeRangeHelper(d_start, d_end, step, key, html_class);
					break;
					
				case DATETIME:
					SVDateTime dt_start = new SVDateTime(value[0]);
					SVDateTime dt_end = new SVDateTime(value[1]);
					step = new SVInteger(value[2]);
					result = makeRangeHelper(dt_start, dt_end, step, key, html_class);
					break;	
					
				case FLOAT:
					SVFloat f_start = new SVFloat(value[0]);
					SVFloat f_end = new SVFloat(value[1]);
					step = new SVInteger(value[2]);
					result = makeRangeHelper(f_start, f_end, step, key, html_class);
					break;	
					
				case INTEGER:
					SVInteger i_start = new SVInteger(value[0]);
					SVInteger i_end = new SVInteger(value[1]);
					step = new SVInteger(value[2]);
					result = makeRangeHelper(i_start, i_end, step, key, html_class);	
					break;		
					
				case TIME:
					SVTime t_start = new SVTime(value[0]);
					SVTime t_end = new SVTime(value[1]);
					step = new SVInteger(value[2]);
					result = makeRangeHelper(t_start, t_end, step, key, html_class);	
					break;	
					
				default: 
					System.err.printf(error_msg, this.getClass().getName());
					throw new SettingExc();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SettingExc();
		}
		
		return(result);
		
	}
	
	/**
	 * Method to make the Range setting. Uses a helper method to check that start < end.
	 * @param start Generic type T, start
	 * @param end Generic type T, end
	 * @param step SVInteger step
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @return
	 * @throws BoundedExc
	 */
	private <T extends SVBuildable & Comparable<T>> Range<T> makeRangeHelper
				(T start, T end, SVInteger step, String key, String html_class) 
				throws RangeExc {

		String error_msg = "%s: Start value is equal to or greater than end value\n";
		String error_msg2 = "%s: Error initialising Range object\n";
		
		if(start.compareTo(end) >= 0) {
			System.err.printf(error_msg, this.getClass().getName());
			throw new RangeExc();
		}
		
		Range<T> result;
		try {
			result = new Range<>(start, end, step, key, html_class);
			
		} catch (SettingExc e) {
			e.printStackTrace();
			System.err.printf(error_msg2, this.getClass().getName());
			throw new RangeExc();
		}
		
		return(result);
	}
	
	/**
	 * Method to make the Bounded setting. Uses a helper method to check that lower < upper.
	 * @param setting_value_type Basic setting value types (i.e Integer, Boolean etc)
	 * @param value Basic setting value (i.e Integer, Boolean etc)
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @return
	 * @throws SettingExc
	 */
	private Setting makeBounded(SettingValueE setting_value_type, 
								   String[] value, String key, String html_class) 
					throws SettingExc {
		Setting result;
		String error_msg = "%s: Error making Bounded setting\n";
		
		try {
			switch (setting_value_type) {
				case DATE:
					SVDate d_lower = new SVDate(value[0]);
					SVDate d_upper = new SVDate(value[1]);
					result = makeBoundedHelper(d_lower, d_upper, key, html_class);
					break;
					
				case DATETIME:
					SVDateTime dt_lower = new SVDateTime(value[0]);
					SVDateTime dt_upper = new SVDateTime(value[1]);
					result = makeBoundedHelper(dt_lower, dt_upper, key, html_class);
					break;	
					
				case FLOAT:
					SVFloat f_lower = new SVFloat(value[0]);
					SVFloat f_upper = new SVFloat(value[1]);
					result = makeBoundedHelper(f_lower, f_upper, key, html_class);
					break;	
					
				case INTEGER:
					SVInteger i_lower = new SVInteger(value[0]);
					SVInteger i_upper = new SVInteger(value[1]);
					result = makeBoundedHelper(i_lower, i_upper, key, html_class);	
					break;		
					
				case TIME:
					SVTime t_lower = new SVTime(value[0]);
					SVTime t_upper = new SVTime(value[1]);
					result = makeBoundedHelper(t_lower, t_upper, key, html_class);	
					break;		
					
				default: 
					System.err.printf(error_msg, this.getClass().getName());
					throw new SettingExc();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SettingExc();
		}
		
		return(result);
		
	}
	
	/**
	 * Helper method to check that lower < upper
	 * @param lower Generic type T, lower bound
	 * @param upper Generic type T, upper bound
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @return
	 */
	private <T extends SVBuildable & Comparable<T>> Bounded<T> makeBoundedHelper
				(T lower, T upper, String key, String html_class) 
				throws BoundedExc {
		
		String error_msg = "%s: Lower value is equal to or greater than upper value\n";
		String error_msg2 = "%s: Error initialising Bounded setting object\n";
		
		if(lower.compareTo(upper) >= 0) {
			System.err.printf(error_msg, this.getClass().getName());
			throw new BoundedExc();
		}
		
		Bounded<T> result;
		try {
			result = new Bounded<>(lower, upper, key, html_class);
			
		} catch (SettingExc e) {
			e.printStackTrace();
			System.err.printf(error_msg2, this.getClass().getName());
			throw new BoundedExc();
		}
		
		return(result);
	}
	
	/**
	 * Method to make the Primitive<T> type. No further checks needed
	 * @param setting_value_type Basic setting value types (i.e Integer, Boolean etc)
	 * @param value Basic setting value (i.e Integer, Boolean etc)
	 * @param key ID attribute of the Html TR tag
	 * @param html_class Class attribute of the Html TR tag
	 * @return
	 */
	private Setting makePrimitive(SettingValueE setting_value_type, 
								   String[] value, String key, String html_class) 
					throws SettingExc {
		Setting result;
		String error_msg = "%s: Error making Primitive setting\n";
		
		try {
			switch (setting_value_type) {
				case BOOLEAN:
					SVBoolean b = new SVBoolean(value[0]);
					result = new Primitive<>(b, key, html_class);
					break;
					
				case DATE:
					SVDate d = new SVDate(value[0]);
					result = new Primitive<>(d, key, html_class);
					break;
					
				case DATETIME:
					SVDateTime dt = new SVDateTime(value[0]);
					result = new Primitive<>(dt, key, html_class);
					break;	
					
				case FLOAT:
					SVFloat f = new SVFloat(value[0]);
					result = new Primitive<>(f, key, html_class);
					break;	
					
				case INTEGER:
					SVInteger i = new SVInteger(value[0]);
					result = new Primitive<>(i, key, html_class);
					break;	
					
				case STRING:
					SVString s = new SVString(value[0]);
					result = new Primitive<>(s, key, html_class);
					break;	
					
				case TIME:
					SVTime t = new SVTime(value[0]);
					result = new Primitive<>(t, key, html_class);
					break;	
					
				default: 
					System.err.printf(error_msg, this.getClass().getName());
					throw new SettingExc();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new SettingExc();
		}
			
		return(result);
	}
	
	/**
	 * Get the type of setting from Element
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private SettingE getSettingTypeFromElement(Element tr) 
							throws HtmlParserExc {
		
		List<String> html_classes;
		String setting_type;
		String error_msg = "%s: Html TR class attribute has wrong/missing format for setting\n";
		
		try {
			html_classes = new ArrayList<String>(tr.classNames());
			setting_type = html_classes.get(1);
			setting_type = setting_type.split("-")[0];
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		if(setting_type.equalsIgnoreCase("PRIMITIVE"))
			return SettingE.PRIMITIVE;
		
		else if(setting_type.equalsIgnoreCase("BOUNDED"))
			return SettingE.BOUNDED;
		
		else if(setting_type.equalsIgnoreCase("RANGE"))
			return SettingE.RANGE;
		
		else {
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
	}
	
	/**
	 * Get the type of setting value from Element
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private SettingValueE getSettingValueFromElement(Element tr) 
								throws HtmlParserExc {
		
		List<String> html_classes;
		String setting_type;
		String error_msg = "%s: Html TR class attribute has wrong/missing format for setting value\n";
		
		try {
			html_classes = new ArrayList<String>(tr.classNames());
			setting_type = html_classes.get(1);
			setting_type = setting_type.split("-")[1];
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		if(setting_type.equalsIgnoreCase("BOOLEAN"))
			return SettingValueE.BOOLEAN;
		
		else if(setting_type.equalsIgnoreCase("DATE"))
			return SettingValueE.DATE;
		
		else if(setting_type.equalsIgnoreCase("DATETIME"))
			return SettingValueE.DATETIME;
		
		else if(setting_type.equalsIgnoreCase("FLOAT"))
			return SettingValueE.FLOAT;
		
		else if(setting_type.equalsIgnoreCase("INTEGER"))
			return SettingValueE.INTEGER;
		
		else if(setting_type.equalsIgnoreCase("STRING"))
			return SettingValueE.STRING;
		
		else if(setting_type.equalsIgnoreCase("TIME"))
			return SettingValueE.TIME;	
		
		else {
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
	}
	
	/**
	 * Get Key (Html ID attribute) from Html element
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private String getKeyFromElement(Element tr) 
						throws HtmlParserExc {
		String result = "";
		String error_msg = "%s: Html TR tag has a blank or no ID attribute\n";
		
		try {
			result = tr.id();
			
			if(result == "") {
				System.err.println(tr.toString());
				System.err.printf(error_msg, this.getClass().getName());
				throw new HtmlParserExc();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		return(result);
	}
	
	/**
	 * Get Html Class (exclude Setting class) from Html elemnt
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private String getHtmlClassFromElement(Element tr) 
						throws HtmlParserExc {
		String result = "";
		String error_msg = "%s: Missing/Wrong class attribute of type \"Setting-SettingValue\"" +
							"from Html TR tag \n";
		
		try {
			List<String> html_classes = new ArrayList<String>(tr.classNames());
			result = html_classes.get(1);
			
			if(result == "") {
				System.err.println(tr.toString());
				System.err.printf(error_msg, this.getClass().getName());
				throw new HtmlParserExc();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		return(result);
	}
	
	/**
	 * Extract the String value of the Primitive setting
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private String getPValueFromElement(Element tr) 
						throws HtmlParserExc {
		String result;
		String error_msg = "%s: Html TD tag has missing/wrong setting value for primitive setting type\n";
		
		try {
			Element value = tr.select("TD.VALUE").get(0);
			result = value.ownText();
			
			if(result == "") {
				System.err.println(tr.toString());
				System.err.printf(error_msg, this.getClass().getName());
				throw new HtmlParserExc();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		return(result);
	}
	
	/**
	 * Extract the String values of the Bounded setting
	 * Returns ["Value-Lower", "Value-Higher"]
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private String[] getBValuesFromElement(Element tr) 
						throws HtmlParserExc {
		String[] result;
		String error_msg = "%s: Html TD tag has missing/wrong setting value for bounded setting type\n";
		
		try {
			Element value_lower = tr.select("TD.VALUE-LOWER").get(0);
			Element value_higher = tr.select("TD.VALUE-UPPER").get(0);
			
			result = new String[] {value_lower.ownText(),
								   value_higher.ownText()};
			
			if(result[0] == "" || result[1] == "") {
				System.err.println(tr.toString());
				System.err.printf(error_msg, this.getClass().getName());
				throw new HtmlParserExc();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		return(result);
	}
	
	/**
	 * Extract the String values of the Range setting
	 * @param tr Parsed Html Element tr
	 * @return
	 */
	private String[] getRValuesFromElement(Element tr) 
						throws HtmlParserExc {
		String[] result;
		String error_msg = "%s: Html TD tag has missing/wrong setting value for range setting type\n";
		
		try {
			Element value_start = tr.select("TD.VALUE-START").get(0);
			Element value_end = tr.select("TD.VALUE-END").get(0);
			Element value_step = tr.select("TD.VALUE-STEP").get(0);
			
			result = new String[]  {value_start.ownText(),
									value_end.ownText(),
									value_step.ownText()};
			
			if(result[0] == "" || result[1] == "" || result[2] == "") {
				System.err.println(tr.toString());
				System.err.printf(error_msg, this.getClass().getName());
				throw new HtmlParserExc();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(tr.toString());
			System.err.printf(error_msg, this.getClass().getName());
			throw new HtmlParserExc();
		}
		
		return(result);	
	}
}
