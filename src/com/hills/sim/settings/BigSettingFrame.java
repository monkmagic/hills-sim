package com.hills.sim.settings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.hills.sim.exceptions.BigSettingFrameExc;

public class BigSettingFrame {
	
	/**
	 * Contains the prefix, used in HTML TR tag ID attribute and
	 * it identifies the Setting
	 */
	private final String prefix;
	
	
	/**
	 * Contains the raw frame, either from Constants or EA classes 
	 */
	private final String[][] raw_frame;
	
	
	/**
	 * Contains the parsed HTML Settings
	 */
	private final List<Setting> frame;
	
	
	/**
	 * Make BigSettingFrame
	 * @param prefix Prefix for the Class attribute of Html TR tag
	 * @param rframe Raw frame that contains the definitions
	 * @param html_settings Settings that are parsed from Html 
	 * @throws BigSettingFrameExc
	 */
	public BigSettingFrame(String prefix, 
						    String[][] rframe,
						    List<Setting> html_settings) 
			throws BigSettingFrameExc {
		
		this.prefix = prefix;
		this.raw_frame = rframe;
		String error_msg = "%s: Error making BigSettingFrame\n";
		
		try {
			this.frame = this.copyHtmlSettings(html_settings);
			
		} catch (BigSettingFrameExc e) {
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
	}
	
	public String getPrefix() {
		return(this.prefix);
	}
	
	public String[][] getRawFrame() {
		return(this.raw_frame);
	}
	
	/**
	 * Check that the parsed HTML settings for the Big Setting meets the
	 * definition within the Constants class.
	 * @param html_settings Settings that are parsed from Html
	 * @return
	 * @throws BSFrameCompareException
	 */
	private boolean htmlHasCorrectFrame(List<Setting> html_settings) 
				throws BigSettingFrameExc {
		boolean result = false;
		String error_msg = "%s: Error comparing defined frame to HTML settings\n";
		
//		Filter the HTML settings by the defined prefix
		List<Setting> html_frame = 
						html_settings
						.stream()
						.filter(x -> x.getKey().startsWith(this.prefix))
						.collect(Collectors.toList());
		
//		Convert the List of Settings into a String Array of Array
		String[][] html_f = null;
		try {
			html_f = new String[html_frame.size()][];
			
			for(int i = 0; i < html_frame.size(); i++) {
				Setting s = html_frame.get(i);
				html_f[i] = new String[] {s.getKey(), s.getHtmlClass()};
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(html_f);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
//		Compare deeply the contents of the argument
		result = Arrays.deepEquals(this.raw_frame, html_f);
		
		return(result);
	}
	
	
	/**
	 * Copy the valid settings from the Html settings into the frame
	 * @param html_settings Settings that are parsed from Html
	 * @throws BSFrameCopyException
	 */
	private List<Setting> copyHtmlSettings(List<Setting> html_settings) 
			throws BigSettingFrameExc {
		
		String error_msg = "%s: Html settings has incorrect frame\n";
		
//		Compare the raw frame with the html settings
		try {
			if(this.htmlHasCorrectFrame(html_settings) == false) {
				System.err.printf(error_msg, this.getClass().getName());
				throw new BigSettingFrameExc();
			}
			
		} catch (BigSettingFrameExc e) {
			e.printStackTrace();
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
//		Filter the html settings by the prefix
		List<Setting> frame = html_settings
						.stream()
						.filter(x -> x.getKey().startsWith(this.prefix))
						.collect(Collectors.toList());
		
		return(frame);
	}
	
	
	/**
	 * Returns the Setting object, using the key (ID attribute)
	 * of HTML TR tag
	 * @param key ID attribute of the Html TR tag
	 * @return
	 */
	public Setting getSetting(String key) {
		List<Setting> result = this.frame
						.stream()
						.filter(x -> x.getKey().equalsIgnoreCase(key))
						.collect(Collectors.toList());
		
		return(result.get(0));
	}
	
	
	/**
	 * Return Boolean value from Primitive<SVBoolean> 
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public boolean getBooleanValue(String key) 
		throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Primitive<SVBoolean> setting = 
						(Primitive<SVBoolean>) this.getSetting(key);
		
		SVBoolean b;
		try {
			b = setting.getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting boolean from Primitive\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
		return(b.getValue());
	}
	
	
	/**
	 * Return LocalDate value from Primitive<SVDate> 
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public LocalDate getDateValue(String key) 
		throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Primitive<SVDate> setting = 
						(Primitive<SVDate>) this.getSetting(key);
		
		SVDate d;
		try {
			d = setting.getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting LocalDate from Primitive\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
		return(d.getValue());
	}
	
	
	/**
	 * Return LocalDateTime value from Primitive<SVDateTime> 
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public LocalDateTime getDateTimeValue(String key) 
		throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Primitive<SVDateTime> setting = 
						(Primitive<SVDateTime>) this.getSetting(key);
		
		SVDateTime dt;
		try {
			dt = setting.getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting DateTime from Primitive\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
		return(dt.getValue());
	}
	
	
	/**
	 * Return float value from Primitive<SVFloat> 
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public float getFloatValue(String key) 
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Primitive<SVFloat> setting = 
						(Primitive<SVFloat>) this.getSetting(key);
		
		SVFloat f;
		try {
			f = setting.getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting float from Primitive\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
		return(f.getValue());
	}
	
	
	/**
	 * Return int value from Primitive<SVInteger> 
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public int getIntegerValue(String key) 
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Primitive<SVInteger> setting = 
						(Primitive<SVInteger>) this.getSetting(key);
		
		SVInteger i;
		try {
			i = setting.getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting int from Primitive\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
		return(i.getValue());
	}
	
	
	/**
	 * Return String value from Primitive<SVString> 
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public String getStringValue(String key) 
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Primitive<SVString> setting = 
						(Primitive<SVString>) this.getSetting(key);
		
		SVString s;
		try {
			s = setting.getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting String from Primitive\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
		return(s.getValue());
	}
	
	
	/**
	 * Return Time value from Primitive<SVTime> 
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public LocalTime getTimeValue(String key) 
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Primitive<SVTime> setting = 
						(Primitive<SVTime>) this.getSetting(key);
		
		SVTime t;
		try {
			t = setting.getValue();
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting LocalTime from Primitive\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}
		
		return(t.getValue());
	}
	
	
	/**
	 * Return LocalDate[] from Bounded<SVDate>
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public LocalDate[] getDateBounds(String key)
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Bounded<SVDate> setting = 
						(Bounded<SVDate>) this.getSetting(key);
		
		LocalDate[] result;
		
		try {
			result = new LocalDate[] {setting.getLower().getValue(),
									   setting.getUpper().getValue(),};
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting LocalDate[] from Bounded\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}

		return(result);
	}
	
	
	/**
	 * Return LocalDateTime[] from Bounded<SVDateTime>
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public LocalDateTime[] getDateTimeBounds(String key)
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Bounded<SVDateTime> setting = 
						(Bounded<SVDateTime>) this.getSetting(key);
		
		LocalDateTime[] result;
		try {
			result = new LocalDateTime[] {setting.getLower().getValue(),
									   	  setting.getUpper().getValue(),};
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting LocalDateTime[] from Bounded\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}

		return(result);
	}
	
	
	/**
	 * Return float[] from Bounded<SVDateTime>
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public float[] getFloatBounds(String key)
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Bounded<SVFloat> setting = 
						(Bounded<SVFloat>) this.getSetting(key);
		
		float[] result;
		try {
			result = new float[] {setting.getLower().getValue(),
								   setting.getUpper().getValue(),};
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting float[] from Bounded\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}

		return(result);
	}
	
	
	/**
	 * Return int[] from Bounded<SVInteger>
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public int[] getIntBounds(String key)
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Bounded<SVInteger> setting = 
						(Bounded<SVInteger>) this.getSetting(key);
		
		int[] result;
		try {
			result = new int[] {setting.getLower().getValue(),
								 setting.getUpper().getValue(),};
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting int[] from Bounded\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}

		return(result);
	}
	
	
	/**
	 * Return LocalTime[] from Bounded<SVTime>
	 * @param key ID attribute of the Html TR tag
	 * @return
	 * @throws BSFrameValueException
	 */
	public LocalTime[] getTimeBounds(String key)
			throws BigSettingFrameExc {
		
		@SuppressWarnings("unchecked")
		Bounded<SVTime> setting = 
						(Bounded<SVTime>) this.getSetting(key);
		
		LocalTime[] result;
		try {
			result = new LocalTime[] {setting.getLower().getValue(),
									  setting.getUpper().getValue(),};
			
		} catch (Exception e) {
			String error_msg = "%s: Error getting LocalTime[] from Bounded\n";
			e.printStackTrace();
			System.err.println(setting);
			System.err.printf(error_msg, this.getClass().getName());
			throw new BigSettingFrameExc();
		}

		return(result);
	}
}
