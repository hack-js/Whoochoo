package net.wisedog.android.whooing;

public class Define {
	public static String APP_ID = "125";
	public static String APP_KEY = "1c5224ad2961704a6076c0bda127003933828a16";
	public static String TOKEN = null;
	public static String PIN = null;
	public static String USER_ID = null;
	public static String TOKEN_SECRET = null;
	public static String APP_SECTION = null;
	
	// General Key and Shared Preference Key
	public static String KEY_AUTHPAGE = "AUTHPAGE";
	public static String SHARED_PREFERENCE = "SHARED_AUTH";
	public static String KEY_SHARED_PIN = "SHARED_PIN";
	public static String KEY_SHARED_TOKEN = "SHARED_TOKEN";
	public static String KEY_SHARED_TOKEN_SECRET = "SHARED_TOKEN_SECRET";
	public static String KEY_SHARED_USER_ID = "SHARED_USER_ID";
	public static String KEY_SHARED_SECTION_ID = "SHARED_SECTION_ID";
	
	// Response/Request Key
	public static final int RESPONSE_EXIT = 10;
	public static final int REQUEST_AUTH = 50;
	
	//Message
	public static final int MSG_REQ_AUTH = 100;
	public static final int MSG_FAIL = 101;
	public static final int MSG_AUTH_DONE = 102;
	
	public static final int MSG_API_OK = 1000;
	public static final int MSG_API_FAIL = 1003;
	public static final int MSG_API_FAIL_WITH_404 = 404;
		
	public static final int API_GET_SECTIONS = 2010;
	public static final int API_GET_MAIN = 2020;
	public static final int API_GET_BUDGET = 2030;
	public static final int API_GET_BALANCE = 2040;
	
	
	
	
}
