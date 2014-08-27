/*
 * Copyright (C) 2013 Jongha Kim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wisedog.android.whooing.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.wisedog.android.whooing.Define;
import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.WhooingApplication;
import net.wisedog.android.whooing.api.GeneralApi;
import net.wisedog.android.whooing.db.AccountsEntity;
import net.wisedog.android.whooing.engine.DataRepository;
import net.wisedog.android.whooing.engine.GeneralProcessor;
import net.wisedog.android.whooing.utils.FragmentUtil;
import net.wisedog.android.whooing.utils.WhooingAlert;
import net.wisedog.android.whooing.utils.WhooingCalendar;
import net.wisedog.android.whooing.utils.WhooingCurrency;
import net.wisedog.android.whooing.widget.WiTextView;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;


/**
 * @author Wisedog(me@wisedog.net)
 *
 */
public class BalanceFragment extends Fragment{

    public static BalanceFragment newInstance(Bundle b) {
        BalanceFragment fragment = new BalanceFragment();
        fragment.setArguments(b);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.balance_fragment, container, false);
		return view;
    }
    
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        TableLayout tl = (TableLayout) getActivity()
                .findViewById(R.id.balance_asset_table);
        if(tl.getChildCount() <= 2){
        	DataRepository repository = WhooingApplication.getInstance().getRepo();
            if(repository.getBsValue() != null){
                showBalance(repository.getBsValue());
            }
            else{
            	if(getView() != null){
            		ProgressBar progress = (ProgressBar)getView().findViewById(R.id.balance_progress);
            		if(progress != null){
            			progress.setVisibility(View.VISIBLE);
            		}
            	}
            	AsyncTask<Void, Integer, JSONObject> task = new AsyncTask<Void, Integer, JSONObject>(){

                    @Override
                    protected JSONObject doInBackground(Void... arg0) {
                        String budgetURL = "https://whooing.com/api/bs.json_array" + "?section_id="
                                + Define.APP_SECTION + "&end_date=" + WhooingCalendar.getTodayYYYYMMDD();
                        GeneralApi balanceApi = new GeneralApi();
                        JSONObject result = balanceApi.getInfo(budgetURL, Define.APP_ID, Define.REAL_TOKEN,
                                Define.APP_SECRET, Define.TOKEN_SECRET);
                        balanceApi = null;	//for gc
                    	
                        return result;
                    }

                    @Override
                    protected void onPostExecute(JSONObject result) {
                    	
                    	if(getView() != null){
                    		ProgressBar progress = (ProgressBar)getView().findViewById(R.id.balance_progress);
                    		if(progress != null){
                    			progress.setVisibility(View.VISIBLE);
                    		}
                    	}
                    	
                    	
                        if(Define.DEBUG && result != null){
                            Log.d("wisedog", "API Call - Balance : " + result.toString());
                        }
                        try {
                            int returnCode = result.getInt("code");
                            if(returnCode == Define.RESULT_INSUFFIENT_API && Define.SHOW_NO_API_INFORM == false){
                                Define.SHOW_NO_API_INFORM = true;
                                WhooingAlert.showNotEnoughApi(getActivity());
                                return;
                            }
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                            return;
                        }
                        DataRepository repository = WhooingApplication.getInstance().getRepo();
                        try {
                            repository.setRestApi(result.getInt("rest_of_api"));
                            repository.refreshRestApi(getActivity());
                        } catch (JSONException e) {
                            // Just passing..... 
                            e.printStackTrace();
                        }
						repository.setBsValue(result);
						if(isAdded() == true){
							showBalance(result);
						}
                        super.onPostExecute(result);
                    }

                };
                
                task.execute();            	
            }
        }
        super.onActivityCreated(savedInstanceState);
    }
    
	/**
	 * Show Balance
	 * @param	obj		JSON formatted balance data
	 * */
	public void showBalance(JSONObject obj) {
	    WiTextView labelTotalAssetValue = (WiTextView)getActivity().findViewById(R.id.balance_total_asset_value);
		
		TableLayout tl = (TableLayout) getActivity()
				.findViewById(R.id.balance_asset_table);
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		final int secondColumnWidth = (int) (metrics.widthPixels * 0.6);
		Resources r = getResources();
		final int valueWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 95,
                r.getDisplayMetrics());
		final int viewHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14,
                r.getDisplayMetrics());
		final int rightMargin4Dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                r.getDisplayMetrics());
		try {
			JSONObject objResult = obj.getJSONObject("results");
			JSONObject objAssets = objResult.getJSONObject("assets");
			double totalAssetValue = objAssets.getDouble("total");
            if (labelTotalAssetValue != null) {
            	double totalAssetValue1 = objAssets.getDouble("total");
                labelTotalAssetValue.setText(WhooingCurrency.getFormattedValue(totalAssetValue1, getActivity()));
                View bar = (View) getActivity().findViewById(R.id.bar_total_asset);
                int barWidth = FragmentUtil.getBarWidth(objAssets.getInt("total"), totalAssetValue,
                        secondColumnWidth, valueWidth);
                
                android.widget.LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                        barWidth, viewHeight);
                lParams.setMargins(0, 0, rightMargin4Dip, 0);
                lParams.gravity = Gravity.CENTER_VERTICAL;
                bar.setLayoutParams(lParams);
            }
			
			JSONArray objAssetAccounts = objAssets.getJSONArray("accounts");
			showBalanceEntities(objAssetAccounts, totalAssetValue, secondColumnWidth, 
			        valueWidth, tl, 0xFFC36FBC);
			
			JSONObject objLiabilities = objResult.getJSONObject("liabilities");
			JSONArray objLiabilitiesAccounts = objLiabilities.getJSONArray("accounts");
			TableLayout tableLiabilites = (TableLayout) getActivity()
	                .findViewById(R.id.balance_liabilities_table);
			
			WiTextView labelTotalLiabilitiesValue = (WiTextView)getActivity().findViewById(R.id.balance_total_liabilities_value);
			if(labelTotalLiabilitiesValue != null){
				double totalLiabilities = objLiabilities.getDouble("total");
			    labelTotalLiabilitiesValue.setText(WhooingCurrency.getFormattedValue(totalLiabilities, getActivity()));
                View bar = (View)getActivity().findViewById(R.id.bar_total_liabilities);
                int barWidth = FragmentUtil.getBarWidth(objLiabilities.getInt("total"), totalAssetValue, 
                        secondColumnWidth, valueWidth);
                android.widget.LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                        barWidth, viewHeight);
                lParams.setMargins(0, 0, rightMargin4Dip, 0);
                lParams.gravity = Gravity.CENTER_VERTICAL;
                bar.setLayoutParams(lParams);
            }
			
			showBalanceEntities(objLiabilitiesAccounts, totalAssetValue, secondColumnWidth, 
			        valueWidth, tableLiabilites, 0xFF5294FF);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void showBalanceEntities(JSONArray accounts, double totalAssetValue, 
	        int secondColumnWidth, int labelWidth, TableLayout tl, int color) throws JSONException{
	    if(accounts == null){
	        return;
	    }
	    GeneralProcessor genericProcessor = new GeneralProcessor(getActivity());
	    for(int i = 0; i < accounts.length(); i++){
	        JSONObject accountItem = (JSONObject) accounts.get(i);
            
            /* Create a new row to be added. */
            TableRow tr = new TableRow(getActivity());
            tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            tr.setWeightSum(1.0f);
            
            WiTextView accountText = new WiTextView(getActivity());
            AccountsEntity entity = genericProcessor.findAccountById(accountItem.getString("account_id"));
            if(entity == null){
            	return;
            }
            if(entity.type.compareTo("group") == 0){
            	accountText.setTypeface(null, Typeface.BOLD);
            }
            else{
            	accountText.setPadding(30, 0, 0, 0);
            }
            accountText.setText(entity.title);
            accountText.setLayoutParams(new LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, 0.4f));

            tr.addView(accountText);
            
            LinearLayout amountLayout = new LinearLayout(getActivity());
            amountLayout.setLayoutParams(new TableRow.LayoutParams(
                    0, 
                    LayoutParams.WRAP_CONTENT,0.6f));

            int barWidth = FragmentUtil.getBarWidth(accountItem.getDouble("money"), totalAssetValue, 
                    secondColumnWidth, labelWidth);
            Resources r = getResources();
            int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
            android.widget.LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    barWidth, px);
            
            //set up view for horizontally bar graph 
            View barView = new View(getActivity());
            barView.setBackgroundColor(color);
            int rightMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
            lParams.setMargins(0, 0, rightMargin, 0);
            lParams.gravity = Gravity.CENTER_VERTICAL;
            barView.setLayoutParams(lParams);
            
            //set up textview for showing amount
            WiTextView amountText = new WiTextView(getActivity());
            double money = accountItem.getDouble("money");
            amountText.setText(WhooingCurrency.getFormattedValue(money, getActivity()));
            amountLayout.addView(barView);
            amountLayout.addView(amountText);
            tr.addView(amountLayout);
            
            /* Add row to TableLayout. */
            tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
	    }
	}
	

    /* (non-Javadoc)
     * @see net.wisedog.android.whooing.engine.DataRepository.OnBsChangeListener#onBsUpdate(org.json.JSONObject)
     */
    public void onBsUpdate(JSONObject obj) {
        showBalance(obj);
    }
}
