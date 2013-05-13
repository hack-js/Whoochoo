package net.wisedog.android.whooing.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragment;

import net.wisedog.android.whooing.Define;
import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.WhooingApplication;
import net.wisedog.android.whooing.db.AccountsEntity;
import net.wisedog.android.whooing.engine.DataRepository;
import net.wisedog.android.whooing.engine.GeneralProcessor;
import net.wisedog.android.whooing.network.ThreadRestAPI;
import net.wisedog.android.whooing.utils.FragmentUtil;
import net.wisedog.android.whooing.utils.WhooingCalendar;
import net.wisedog.android.whooing.utils.WhooingCurrency;
import net.wisedog.android.whooing.widget.WiTextView;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;

public final class ProfitLossFragment extends SherlockFragment{
    
    public static ProfitLossFragment newInstance() {
        ProfitLossFragment fragment = new ProfitLossFragment();
        return fragment;
    }	

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.profit_loss_fragment, null);
        return view;
    }   
    

    @Override
	public void onResume() {
        TableLayout tl = (TableLayout) getSherlockActivity()
                .findViewById(R.id.pl_fragment_expenses_table);
        if(tl.getChildCount() <= 2){
            DataRepository repository = WhooingApplication.getInstance().getRepo();
            if(repository.getPlValue() != null){
               showPl(repository.getPlValue());
            }
            else{
            	Bundle bundle = new Bundle();
                bundle.putString("start_date", WhooingCalendar.getPreMonthYYYYMMDD(1));
                bundle.putString("end_date", WhooingCalendar.getTodayYYYYMMDD());
                ThreadRestAPI thread = new ThreadRestAPI(mHandler,
                        Define.API_GET_PL, bundle);
                thread.start();
            }
        }        
		super.onResume();
	}

    /**
     * @param obj       Profit/Loss value formatted JSON
     */
    private void showPl(JSONObject obj) {
        WiTextView labelTotalExpensesValue = (WiTextView)getSherlockActivity().findViewById(R.id.pl_fragment_total_expenses_value);
        
        TableLayout tl = (TableLayout) getSherlockActivity().findViewById(R.id.pl_fragment_expenses_table);
        DisplayMetrics metrics = new DisplayMetrics();
        getSherlockActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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
            JSONObject objExpenses = objResult.getJSONObject("expenses");
            double totalExpensesValue = objExpenses.getDouble("total");
            
            JSONObject objIncome = objResult.getJSONObject("income");
            JSONArray objIncomeAccounts = objIncome.getJSONArray("accounts");
            double totalIncomeValue = objIncome.getDouble("total");
            double maxValue = 0.0f;
            if(Double.compare(totalExpensesValue, totalIncomeValue) > 0){
            	maxValue = totalExpensesValue;
            }
            else{
            	maxValue = totalIncomeValue;
            }
            
            if (labelTotalExpensesValue != null) {
            	double value1 = objExpenses.getDouble("total");
                labelTotalExpensesValue.setText(WhooingCurrency.getFormattedValue(value1));
                View bar = (View) getSherlockActivity().findViewById(R.id.bar_total_expense);
                int barWidth = FragmentUtil.getBarWidth(objExpenses.getInt("total"), totalExpensesValue,
                        secondColumnWidth, valueWidth);
                Log.i("wisedog", "BarWidth1 : " + barWidth);
                
                android.widget.LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                        barWidth, viewHeight);
                lParams.setMargins(0, 0, rightMargin4Dip, 0);
                lParams.gravity = Gravity.CENTER_VERTICAL;
                bar.setLayoutParams(lParams);
            }
            
            JSONArray objExpensesAccounts = objExpenses.getJSONArray("accounts");
            showPlEntities(objExpensesAccounts, totalExpensesValue, secondColumnWidth, 
                    valueWidth, tl, 0xFFF08537);
            
            
            TableLayout tableIncome = (TableLayout) getSherlockActivity()
                    .findViewById(R.id.pl_fragment_income_table);
            
            WiTextView labelTotalIncomeValue = (WiTextView)getSherlockActivity().findViewById(R.id.pl_fragment_total_income_value);
            if(labelTotalIncomeValue != null){
            	double value1 = objIncome.getDouble("total");
                labelTotalIncomeValue.setText(WhooingCurrency.getFormattedValue(value1));
                View bar = (View)getSherlockActivity().findViewById(R.id.bar_total_income);
                int barWidth = FragmentUtil.getBarWidth(objIncome.getInt("total"), totalIncomeValue, 
                        secondColumnWidth, valueWidth);
                android.widget.LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                        barWidth, viewHeight);
                lParams.setMargins(0, 0, rightMargin4Dip, 0);
                lParams.gravity = Gravity.CENTER_VERTICAL;
                bar.setLayoutParams(lParams);
            }
            
            showPlEntities(objIncomeAccounts, maxValue, secondColumnWidth, 
                    valueWidth, tableIncome, 0xFFBED431);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        
    }
    
    private void showPlEntities(JSONArray accounts, double totalAssetValue, 
            int secondColumnWidth, int labelWidth, TableLayout tl, int color) throws JSONException{
        if(accounts == null){
            return;
        }
        GeneralProcessor genericProcessor = new GeneralProcessor(getSherlockActivity());
        for(int i = 0; i < accounts.length(); i++){
            JSONObject accountItem = (JSONObject) accounts.get(i);
            
            /* Create a new row to be added. */
            TableRow tr = new TableRow(getSherlockActivity());
            tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            tr.setWeightSum(1.0f);
            
            WiTextView accountText = new WiTextView(getSherlockActivity());
            AccountsEntity entity = genericProcessor.findAccountById(accountItem.getString("account_id"));
            if(entity == null){
                continue;
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
            
            LinearLayout amountLayout = new LinearLayout(getSherlockActivity());
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
            View barView = new View(getSherlockActivity());
            barView.setBackgroundColor(color);
            int rightMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
            lParams.setMargins(0, 0, rightMargin, 0);
            lParams.gravity = Gravity.CENTER_VERTICAL;
            barView.setLayoutParams(lParams);
            
            //set up textview for showing amount
            WiTextView amountText = new WiTextView(getSherlockActivity());
            double value1 = accountItem.getDouble("money");
            amountText.setText(WhooingCurrency.getFormattedValue(value1));
            amountLayout.addView(barView);
            amountLayout.addView(amountText);
            tr.addView(amountLayout);
            
            /* Add row to TableLayout. */
            tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }
    }
    
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == Define.MSG_API_OK){
				JSONObject obj = (JSONObject) msg.obj;
				if(msg.arg1 == Define.API_GET_PL){
			        DataRepository repository = WhooingApplication.getInstance().getRepo();
			        repository.setPLValue(obj);
			        if(isAdded() == true){
			        	showPl(obj);
			        }
				}
			}
			super.handleMessage(msg);
		}
		
	};
}
