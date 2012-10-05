package net.wisedog.android.whooing;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
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
		setContentView(R.layout.whooing_main);
		mActivity = this;
		//For Debug
		Define.TOKEN = "ca01f5d4b108ae0fb14c60be98b24f353b57ba50";
		Define.PIN = "731251";
		Define.TOKEN_SECRET = "c753d2953d283694b378332d8f3919be155748b7";
		Define.USER_ID = "93aa0354c21629add8f373887b15f0e3";
		Define.APP_SECTION = "s2128";
		
		/*
		SharedPreferences prefs = getSharedPreferences(Define.SHARED_PREFERENCE, MODE_PRIVATE);
		Define.TOKEN = prefs.getString(Define.KEY_SHARED_TOKEN, null);
		Define.PIN = prefs.getString(Define.KEY_SHARED_PIN, null);
		Define.TOKEN_SECRET = prefs.getString(Define.KEY_SHARED_TOKEN_SECRET, null);
		Define = prefs.getString(Define.KEY_SHARED_SECTION_ID, null);
		Define.USER_ID = prefs.getString(Define.KEY_SHARED_USER_ID, null);*/
    	if(Define.TOKEN == null || Define.PIN == null){
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
				Toast.makeText(mActivity, getString(R.string.msg_auth_fail), 1000).show();
			}
			else if(msg.what == Define.MSG_REQ_AUTH){
				Intent intent = new Intent(WhooingMain.this, WhooingAuth.class);
				intent.putExtra(Define.KEY_AUTHPAGE, msg.obj.toString());
				intent.putExtra("token", Define.TOKEN);
				
				startActivityForResult(intent, Define.REQUEST_AUTH);
				startActivity(intent);
			}
			else if(msg.what == Define.MSG_AUTH_DONE){
				ThreadRestAPI thread = new ThreadRestAPI(mHandler, mActivity, Define.API_GET_SECTIONS);
				thread.start();
			}
			else if(msg.what == Define.MSG_API_OK){
				if(msg.arg1 == Define.API_GET_MAIN){
					JSONObject obj = (JSONObject)msg.obj;
					TextView monthlyBudgetText = (TextView)findViewById(R.id.budget_monthly);
					TextView monthlyExpenseText = (TextView)findViewById(R.id.budget_monthly_expense);
					try {
						JSONObject total = obj.getJSONObject("budget").getJSONObject("aggregate")
								.getJSONObject("total");
						
						int budget = total.getInt("budget");
						int expenses = total.getInt("money");
						if(budget < expenses){
							monthlyExpenseText.setTextColor(Color.RED);
						}
						monthlyBudgetText.setText("예산:"+budget);
						monthlyExpenseText.setText("지출 : "+expenses);
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					TextView currentBalance = (TextView)findViewById(R.id.balance_num);
					TextView inoutBalance = (TextView)findViewById(R.id.inout_num);
					try{	//TODO	BS정보 바뀜. 수정할것
						JSONObject obj1 = obj.getJSONObject("bs").getJSONObject("capital");
						DecimalFormat df = new DecimalFormat("#,##0");
						currentBalance.setText(df.format(obj1.getLong("total")));
						
						JSONObject obj2 = obj.getJSONObject("bs").getJSONObject("liabilities");
						inoutBalance.setText(df.format(obj2.getLong("total")));						
						
					}catch(JSONException e){
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(IllegalArgumentException e){
						e.printStackTrace();
					}
					
					TextView creditCard = (TextView)findViewById(R.id.text_credit_card);
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					showGraph();
					
					
				}
				else if(msg.arg1 == Define.API_GET_SECTIONS){
					JSONObject result = (JSONObject)msg.obj;					
					try {
						JSONArray array = result.getJSONArray("results");					
						JSONObject obj = (JSONObject) array.get(0);
						String section = obj.getString("section_id");
						if(section != null){
							Define.APP_SECTION = section;
							Log.i("Wisedog", "APP SECTION:"+ Define.APP_SECTION);
							SharedPreferences prefs = mActivity.getSharedPreferences(Define.SHARED_PREFERENCE,
									Activity.MODE_PRIVATE);
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString(Define.KEY_SHARED_SECTION_ID, section);
							editor.commit();
							dialog.dismiss();
							Toast.makeText(mActivity, getString(R.string.msg_auth_success),
									1000).show();
						}
						else{
							throw new JSONException("Error in getting section id");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
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
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(IllegalArgumentException e){
						e.printStackTrace();
					}
				}
			}
		}

			
	};
	
	public void showGraph(){
		String[] titles = new String[] { "Air temperature" };
	    List<double[]> x = new ArrayList<double[]>();
	    for (int i = 0; i < titles.length; i++) {
	      x.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 });
	    }
	    List<double[]> values = new ArrayList<double[]>();
	    values.add(new double[] { 12.3, 12.5, 13.8, 16.8, 20.4, 24.4, 26.4, 26.1, 23.6, 20.3, 17.2,
	        13.9 });
	    int[] colors = new int[] { Color.BLUE, Color.YELLOW };
	    PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT };
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(2);
	    setRenderer(renderer, colors, styles);
	    int length = renderer.getSeriesRendererCount();
	    for (int i = 0; i < length; i++) {
	      XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
	      r.setLineWidth(3f);
	    }
	    setChartSettings(renderer, "Average temperature", "Month", "Temperature", 0.5, 12.5, 0, 32,
	        Color.LTGRAY, Color.LTGRAY);
	    renderer.setXLabels(12);
	    renderer.setYLabels(10);
	    renderer.setShowGrid(true);
	    renderer.setXLabelsAlign(Align.RIGHT);
	    renderer.setYLabelsAlign(Align.RIGHT);
	    renderer.setZoomButtonsVisible(true);
	    renderer.setPanLimits(new double[] { -10, 20, -10, 40 });
	    renderer.setZoomLimits(new double[] { -10, 20, -10, 40 });
	    renderer.setZoomRate(1.05f);

	    renderer.setLabelsColor(Color.WHITE);
	    renderer.setXLabelsColor(Color.GREEN);
	    renderer.setYLabelsColor(0, colors[0]);
	    renderer.setYLabelsColor(1, colors[1]);

	    renderer.setYTitle("Hours", 1);
	    renderer.setYAxisAlign(Align.RIGHT, 1);
	    renderer.setYLabelsAlign(Align.LEFT, 1);

	    renderer.addYTextLabel(20, "Test", 0);
	    renderer.addYTextLabel(10, "New Test", 1);

	    XYMultipleSeriesDataset dataset = buildDataset(titles, x, values);
	    values.clear();
	    values.add(new double[] { 4.3, 4.9, 5.9, 8.8, 10.8, 11.9, 13.6, 12.8, 11.4, 9.5, 7.5, 5.5 });
	    addXYSeries(dataset, new String[] { "Sunshine hours" }, x, values, 1);
	    //Intent intent = ChartFactory.getCubicLineChartIntent(this, dataset, renderer, 0.3f,"Average temperature");
	    GraphicalView gv = ChartFactory.getCubeLineChartView(this, dataset, renderer, (float) 0.5);
				// 그래프를 LinearLayout에 추가
				LinearLayout llBody = (LinearLayout) findViewById(R.id.test_layout);
				llBody.addView(gv);
	}
	
	public void showGraph1() {
		// TODO Auto-generated method stub
		// 표시할 수치값
				List<double[]> values = new ArrayList<double[]>();
				values.add(new double[] { 14, 12, 14, 15 });

				/** 그래프 출력을 위한 그래픽 속성 지정객체 */
				XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

				// 상단 표시 제목과 글자 크기
				renderer.setChartTitle("2011년도 판매량");
				renderer.setChartTitleTextSize(20);

				// 분류에 대한 이름
				String[] titles = new String[] { "월별 판매량" };

				// 항목을 표시하는데 사용될 색상값
				int[] colors = new int[] { Color.YELLOW };

				// 분류명 글자 크기 및 각 색상 지정
				renderer.setLegendTextSize(15);
				int length = colors.length;
				for (int i = 0; i < length; i++) {
					SimpleSeriesRenderer r = new SimpleSeriesRenderer();
					r.setColor(colors[i]);
					renderer.addSeriesRenderer(r);
				}

				// X,Y축 항목이름과 글자 크기
				renderer.setXTitle("월");
				renderer.setYTitle("판매량");
				renderer.setAxisTitleTextSize(12);

				// 수치값 글자 크기 / X축 최소,최대값 / Y축 최소,최대값
				renderer.setLabelsTextSize(10);
				renderer.setXAxisMin(0.5);
				renderer.setXAxisMax(12.5);
				renderer.setYAxisMin(0);
				renderer.setYAxisMax(24);

				// X,Y축 라인 색상
				renderer.setAxesColor(Color.WHITE);
				// 상단제목, X,Y축 제목, 수치값의 글자 색상
				renderer.setLabelsColor(Color.CYAN);
				renderer.setBackgroundColor(Color.WHITE);

				// X축의 표시 간격
				renderer.setXLabels(1);
				// Y축의 표시 간격
				renderer.setYLabels(5);

				// X,Y축 정렬방향
				renderer.setXLabelsAlign(Align.LEFT);
				renderer.setYLabelsAlign(Align.LEFT);
				// X,Y축 스크롤 여부 ON/OFF
				renderer.setPanEnabled(false, false);
				// ZOOM기능 ON/OFF
				renderer.setZoomEnabled(false, false);
				// ZOOM 비율
				renderer.setZoomRate(1.0f);
				// 막대간 간격
				renderer.setBarSpacing(0.5f);

				// 설정 정보 설정
				XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
				for (int i = 0; i < titles.length; i++) {
					CategorySeries series = new CategorySeries(titles[i]);
					double[] v = values.get(i);
					int seriesLength = v.length;
					for (int k = 0; k < seriesLength; k++) {
						series.add(v[k]);
					}
					dataset.addSeries(series.toXYSeries());
				}

				// 그래프 객체 생성
//				GraphicalView gv = ChartFactory.getBarChartView(this, dataset,
//						renderer, Type.STACKED);
				
				GraphicalView gv = //ChartFactory.getLineChart(this, dataset, renderer, Type.DEFAULT);
				ChartFactory.getLineChartView(this, dataset, renderer);
				// 그래프를 LinearLayout에 추가
				LinearLayout llBody = (LinearLayout) findViewById(R.id.test_layout);
				llBody.addView(gv);
	}	
	 
	 protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
		      String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
		      int labelsColor) {
		    renderer.setChartTitle(title);
		    renderer.setXTitle(xTitle);
		    renderer.setYTitle(yTitle);
		    renderer.setXAxisMin(xMin);
		    renderer.setXAxisMax(xMax);
		    renderer.setYAxisMin(yMin);
		    renderer.setYAxisMax(yMax);
		    renderer.setAxesColor(axesColor);
		    renderer.setLabelsColor(labelsColor);
		  }
	 
	 protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
		    renderer.setAxisTitleTextSize(16);
		    renderer.setChartTitleTextSize(20);
		    renderer.setLabelsTextSize(15);
		    renderer.setLegendTextSize(15);
		    renderer.setPointSize(5f);
		    renderer.setMargins(new int[] { 20, 30, 15, 20 });
		    int length = colors.length;
		    for (int i = 0; i < length; i++) {
		      XYSeriesRenderer r = new XYSeriesRenderer();
		      r.setColor(colors[i]);
		      r.setPointStyle(styles[i]);
		      renderer.addSeriesRenderer(r);
		    }
		  }
	/**
	   * Builds an XY multiple dataset using the provided values.
	   * 
	   * @param titles the series titles
	   * @param xValues the values for the X axis
	   * @param yValues the values for the Y axis
	   * @return the XY multiple dataset
	   */
	  protected XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues,
	      List<double[]> yValues) {
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	    addXYSeries(dataset, titles, xValues, yValues, 0);
	    return dataset;
	  }

	  public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
	      List<double[]> yValues, int scale) {
	    int length = titles.length;
	    for (int i = 0; i < length; i++) {
	      XYSeries series = new XYSeries(titles[i], scale);
	      double[] xV = xValues.get(i);
	      double[] yV = yValues.get(i);
	      int seriesLength = xV.length;
	      for (int k = 0; k < seriesLength; k++) {
	        series.add(xV[k], yV[k]);
	      }
	      dataset.addSeries(series);
	    }
	  }

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
				SharedPreferences prefs = getSharedPreferences(Define.SHARED_PREFERENCE,
						MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Define.KEY_SHARED_PIN, Define.PIN);
				Log.i("Wisedog", "PIN:"+Define.PIN);
				editor.commit();
				ThreadHandshake thread = new ThreadHandshake(mHandler, this, true);
	    		thread.start();
			}			
		}
		else if(requestCode == RESULT_CANCELED){
			if(requestCode == Define.REQUEST_AUTH){
				dialog.dismiss();
				Toast.makeText(mActivity, getString(R.string.msg_auth_fail), 1000).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
}
