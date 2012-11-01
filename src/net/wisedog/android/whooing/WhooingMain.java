package net.wisedog.android.whooing;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.wisedog.android.whooing.network.ThreadHandshake;
import net.wisedog.android.whooing.network.ThreadRestAPI;
import net.wisedog.android.whooing.views.MountainGraph;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * */
public class WhooingMain extends Activity {
	private ProgressDialog dialog;
	private Activity mActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.whooing_main1);
		mActivity = this;
		//For Debug
		Define.REAL_TOKEN = "13165741351c21b2088c12706c1acd1d63cf7b49";
		Define.PIN = "992505";
		Define.TOKEN_SECRET = "e56d804b1a703625596ed3a1fd0f4c529fc2ff2c";
		Define.USER_ID = "8955";
		Define.APP_SECTION = "s10550";
		
		
		/*SharedPreferences prefs = getSharedPreferences(Define.SHARED_PREFERENCE, MODE_PRIVATE);
		Define.REAL_TOKEN = prefs.getString(Define.KEY_SHARED_TOKEN, null);
		Define.PIN = prefs.getString(Define.KEY_SHARED_PIN, null);
		Define.TOKEN_SECRET = prefs.getString(Define.KEY_SHARED_TOKEN_SECRET, null);
		Define.APP_SECTION = prefs.getString(Define.KEY_SHARED_SECTION_ID, null);
		Define.USER_ID = prefs.getString(Define.KEY_SHARED_USER_ID, null);*/
    	if(Define.PIN == null || Define.REAL_TOKEN == null){
    		ThreadHandshake thread = new ThreadHandshake(mHandler, this, false);
    		thread.start();
    		dialog = ProgressDialog.show(this, "", getString(R.string.authorizing), true);
    		dialog.setCancelable(true);
    	}
    	else{
    		refreshAll();
    	}
	}
	
    @Override
	protected void onResume() {
		super.onResume();
	}
    
    public void refreshAll(){
    	ThreadRestAPI thread = new ThreadRestAPI(mHandler, this, Define.API_GET_MAIN);
		thread.start();
		/*ThreadRestAPI thread = new ThreadRestAPI(mHandler, this, Define.API_GET_BUDGET);
		thread.start();
		ThreadRestAPI thread1 = new ThreadRestAPI(mHandler, this, Define.API_GET_BALANCE);
		thread1.start();*/
    }
    
    Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Define.MSG_FAIL){
				dialog.dismiss();
				Toast.makeText(mActivity, getString(R.string.msg_auth_fail), Toast.LENGTH_LONG).show();
			}
			else if(msg.what == Define.MSG_REQ_AUTH){
				Intent intent = new Intent(mActivity, WhooingAuth.class);
				intent.putExtra("first_token", (String)msg.obj);
				
				startActivityForResult(intent, Define.REQUEST_AUTH);
			}
			else if(msg.what == Define.MSG_AUTH_DONE){
				ThreadRestAPI thread = new ThreadRestAPI(mHandler, mActivity, Define.API_GET_SECTIONS);
				thread.start();
			}
			else if(msg.what == Define.MSG_API_OK){
			    //API for main page
				if(msg.arg1 == Define.API_GET_MAIN){
				    //Monthly Budget
					JSONObject obj = (JSONObject)msg.obj;
					TextView monthlyExpenseText = (TextView)findViewById(R.id.budget_monthly_expense);
					TextView labelAssets = (TextView)findViewById(R.id.label_asset);
					Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
					monthlyExpenseText.setTypeface(typeface, Typeface.BOLD);
					labelAssets.setTypeface(typeface);
					try {
						JSONObject total = obj.getJSONObject("budget").getJSONObject("aggregate")
								.getJSONObject("total");
						
						int budget = total.getInt("budget");
						int expenses = total.getInt("money");
						if(budget < expenses){
							monthlyExpenseText.setTextColor(Color.RED);
						}
						//monthlyBudgetText.setText("예산:"+budget);
						monthlyExpenseText.setText(expenses+" / " + budget);
						
					} catch (JSONException e) {
					    setErrorHandler("통신 오류! Err-MAIN1");
						e.printStackTrace();
					}
					
					//Balance
					TextView currentBalance = (TextView)findViewById(R.id.balance_num);
					TextView inoutBalance = (TextView)findViewById(R.id.inout_num);
					currentBalance.setTypeface(typeface);
					inoutBalance.setTypeface(typeface);
					try{
						JSONObject obj1 = obj.getJSONObject("mountain").getJSONObject("aggregate");
						DecimalFormat df = new DecimalFormat("#,##0");
						currentBalance.setText(df.format(obj1.getLong("capital")));
						
						inoutBalance.setText(df.format(obj1.getLong("liabilities")));						
						
					}catch(JSONException e){
					    setErrorHandler("통신 오류! Err-MAIN2");
						e.printStackTrace();
					}catch(IllegalArgumentException e){
						e.printStackTrace();
					}
					
					TextView creditCard = (TextView)findViewById(R.id.text_credit_card);
					creditCard.setTypeface(typeface);
					try {
						JSONArray array = obj.getJSONObject("bill").getJSONObject("aggregate")
								.getJSONArray("accounts");
/*						JSONObject total = obj.getJSONObject("bill").getJSONObject("aggregate")
								.getJSONObject("total");*/
						
						String fullString = "";
						for(int i = 0; i< array.length(); i++){
							JSONObject object =(JSONObject) array.get(i);
							String accountName = object.getString("account_id");
							long money = object.getLong("money");
							fullString = fullString + accountName + " : " + money+ "\n";
						}
						creditCard.setText(fullString);
						
					} catch (JSONException e) {
					    setErrorHandler("통신 오류! Err-MAIN3");
						e.printStackTrace();
					}
					
					//showGraph();
				}
				else if(msg.arg1 == Define.API_GET_SECTIONS){
					JSONObject result = (JSONObject)msg.obj;					
					try {
						JSONArray array = result.getJSONArray("results");					
						JSONObject obj = (JSONObject) array.get(0);
						String section = obj.getString("section_id");
						if(section != null){
							Define.APP_SECTION = section;
							Log.d("whooing", "APP SECTION:"+ Define.APP_SECTION);
							SharedPreferences prefs = mActivity.getSharedPreferences(Define.SHARED_PREFERENCE,
									Activity.MODE_PRIVATE);
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString(Define.KEY_SHARED_SECTION_ID, section);
							editor.commit();
							dialog.dismiss();
							Toast.makeText(mActivity, getString(R.string.msg_auth_success),
									Toast.LENGTH_LONG).show();
						}
						else{
							throw new JSONException("Error in getting section id");
						}
					} catch (JSONException e) {
					    setErrorHandler("통신 오류! Err-SCT1");
						e.printStackTrace();
					}
				}
				else if(msg.arg1 == Define.API_GET_BUDGET){
					TextView monthlyBudgetText = (TextView)findViewById(R.id.budget_monthly);
					TextView monthlyExpenseText = (TextView)findViewById(R.id.budget_monthly_expense);
					JSONObject obj = (JSONObject)msg.obj;
					try {
						int budget = obj.getInt("budget");
						int expenses = obj.getInt("money");
						if(budget < expenses){
							monthlyExpenseText.setTextColor(Color.RED);
						}
						monthlyBudgetText.setText("예산:"+budget);
						monthlyExpenseText.setText("지출 : "+expenses);
						
					} catch (JSONException e) {
					    setErrorHandler("통신 오류! Err-BDG1");
					}
					
				}
				else if(msg.arg1 == Define.API_GET_BALANCE){
					TextView currentBalance = (TextView)findViewById(R.id.balance_num);
					TextView inoutBalance = (TextView)findViewById(R.id.inout_num);
					JSONObject obj = (JSONObject)msg.obj;
					try{
						JSONObject obj1 = obj.getJSONObject("assets");
						DecimalFormat df = new DecimalFormat("#,##0");
						currentBalance.setText(df.format(obj1.getLong("total")));
						
						JSONObject obj2 = obj.getJSONObject("liabilities");
						inoutBalance.setText(df.format(obj2.getLong("total")));						
						
					}catch(JSONException e){
					    setErrorHandler("통신 오류! Err-BNC1");
						e.printStackTrace();
					}catch(IllegalArgumentException e){
					    setErrorHandler("통신 오류! Err-BNC2");
						e.printStackTrace();
					}
				}
			}
		}

			
	};


	@Override
	public void onBackPressed() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.exit));
		alert.setMessage(getString(R.string.is_exit));
		alert.setCancelable(true);
		alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
				setResult(Define.RESPONSE_EXIT);
				finish();
			}
		});
		alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK){
			if(requestCode == Define.REQUEST_AUTH){
			    if(data == null){
			        setErrorHandler("인증오류! Err No.1");
			        return;
			    }
			    String secondtoken = data.getStringExtra("token");
			    String pin = data.getStringExtra("pin");
			    if(secondtoken == null || pin == null){
			        setErrorHandler("인증오류! Err No.2");
			        return;
			    }
			    Define.PIN = pin;
				SharedPreferences prefs = getSharedPreferences(Define.SHARED_PREFERENCE,
						MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Define.KEY_SHARED_PIN, pin);
				editor.commit();
				
				ThreadHandshake thread = new ThreadHandshake(mHandler, this, true, secondtoken);
	    		thread.start();
			}			
		}
		else if(requestCode == RESULT_CANCELED){
			if(requestCode == Define.REQUEST_AUTH){
				dialog.dismiss();
				Toast.makeText(mActivity, getString(R.string.msg_auth_fail), Toast.LENGTH_LONG).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void setErrorHandler(String errorMsg){
	    if(dialog != null){
	        dialog.dismiss();
	    }
	    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
	}
	
}
