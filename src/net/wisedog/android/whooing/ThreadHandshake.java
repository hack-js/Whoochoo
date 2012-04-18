package net.wisedog.android.whooing;

import java.io.IOException;
import java.io.InputStream;

import net.wisedog.android.whooing.utils.StringUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class ThreadHandshake extends Thread {
	private Handler mHandler;
	private Activity mActivity;
	
	public ThreadHandshake(Handler mHandler, Activity ctx) {
		super();
		this.mHandler = mHandler;
		this.mActivity = ctx;
	}

	@Override
	public void run() {
		Define.TOKEN = initHandshake();
		Define.PIN = secondHandshake(Define.TOKEN);
		/*Message msg = new Message();
		if(Define.TOKEN != null && Define.PIN != null){			
			msg.what = Define.MSG_OK;					
		}
		else{
			msg.what = Define.MSG_FAIL;	
		}
		mHandler.sendMessage(msg);	*/
		super.run();
	}	
	/**
	 * Do initial handshake to server. Getting a token in this phase
	 * @return	Returns token or null
	 * */
	@SuppressWarnings("static-access")
	public String initHandshake(){
		String token = null;
	    HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"https://whooing.com/app_auth/request_token?app_id="+ Define.APP_ID+"&app_secret="+Define.APP_KEY+"");
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				
				// Load the requested page converted to a string into a JSONObject.
                JSONObject result;
				try {
					// Get the token value
					result = new JSONObject(StringUtil.convertStreamToString(content));
					token = result.getString("token");
				} catch (JSONException e) {
					Log.e(WhooingMain.class.toString(), "Failed to get JSON token value");
					return null;
				}
			} else {
				Log.e(WhooingMain.class.toString(), "Failed to download file");
				return null;
			}
		} catch (ClientProtocolException e) {
			Log.e(WhooingMain.class.toString(), "HttpResponse Failed");
			token = null;
		} catch (IOException e) {
			Log.e(WhooingMain.class.toString(), "HttpResponse IO Failed");
			token = null;
		}
		
		SharedPreferences prefs = mActivity.getSharedPreferences(Define.SHARED_PREFERENCE,
				mActivity.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Define.KEY_SHARED_TOKEN, token);
		editor.commit();
		
		return token;
	}
	

	
	/**
	 * Getting PIN number. If shaking hand with server is successful, then 
	 * invoke an activity to try to authorize user
	 * @return Returns Pin or null
	 * */
	private String secondHandshake(String token){
		String pin = null;
		if(token == null)
			return null;
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(
				"https://whooing.com/app_auth/authorize?token="+ Define.TOKEN);
		try{
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				String contentStr = StringUtil.convertStreamToString(content);
				Message msg = new Message();		
				msg.what = Define.MSG_OK;			
				msg.obj = contentStr;

				mHandler.sendMessage(msg);
				/*Intent intent = new Intent(mActivity, WhooingAuth.class);
				intent.putExtra(Define.KEY_AUTHPAGE, contentStr);
				intent.putExtra("token", token);
				
				mActivity.startActivityForResult(intent, Define.REQUEST_AUTH);
				mActivity.startActivity(intent);*/
			}
			else {
				Log.e(WhooingMain.class.toString(), "Failed to download file");
				return null;
			}
		}
		catch(ClientProtocolException e){
			Log.e(WhooingMain.class.toString(), "HttpResponse Failed");
			pin = null;
		} 
		catch (IOException e) {
			Log.e(WhooingMain.class.toString(), "HttpResponse IO Failed");
			pin = null;
		}
		return pin;
	}
}