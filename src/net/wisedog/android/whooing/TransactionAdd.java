/**
 * 
 */
package net.wisedog.android.whooing;

import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.wisedog.android.whooing.db.AccountsDbOpenHelper;
import net.wisedog.android.whooing.db.AccountsEntity;
import net.wisedog.android.whooing.network.ThreadRestAPI;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Wisedog(me@wisedog.net)
 *
 */
public class TransactionAdd extends Activity {
    
    protected static final int DATE_DIALOG_ID = 0;
    private TextView    mDateDisplay;
    private List<AccountsEntity> mAccountsList = null;
    private AccountsEntity  mLeftAccount = null;
    private AccountsEntity  mRightAccount = null;
    

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_transaction);
        Intent intent = getIntent();
        if(intent.getBooleanExtra("showEntries", false))
            ;
        
        if(getAllAccountsInfo() > 0){
            mLeftAccount = mAccountsList.get(0);
            mRightAccount = mAccountsList.get(0);
        }

        initUi();
        
        ThreadRestAPI thread = new ThreadRestAPI(mHandler, this, Define.API_GET_ENTRIES_LATEST);
        thread.start();
    }
    
    /**
     * Initialize UI
     * */
    public void initUi(){
        if(mAccountsList == null){
            return;
        }
        mDateDisplay = (TextView)findViewById(R.id.add_transaction_text_date);
        Button dateChangeBtn = (Button)findViewById(R.id.add_transaction_change_btn);
        dateChangeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        
        //Initialize edittext
        ((EditText)findViewById(R.id.add_transaction_edit_item)).setText("");
        ((EditText)findViewById(R.id.add_transaction_edit_amount)).setText("");

        String date = DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
        mDateDisplay.setText(date);
        
        //init spinners
        String[] leftAccountsArray = new String[]{};
        //TODO 별도의 Activity에서 해당 작업 수행. Spinner 걷어내기, AccountsSelection연결하기
        //AccountsSelect인자는 left, right, all 
        
    }
    
    public void setLeftAccount(){
        
    }
    public void setRightAccount(){
        
    }
    
    
    /**
     * Get AccountsEntity List
     * @return  Size of accounts list
     * */
    public int getAllAccountsInfo(){
        AccountsDbOpenHelper dbHelper = new AccountsDbOpenHelper(this);
        mAccountsList = dbHelper.getAllAccountsInfo();
        if(mAccountsList != null){
            return mAccountsList.size();
        }
        
        return 0;
    }
    
    protected Handler mHandler = new Handler(){

        /* (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == Define.MSG_API_OK){
                if(msg.arg1 == Define.API_GET_ENTRIES_LATEST){
                    JSONObject obj = (JSONObject)msg.obj;
                    try {
                        testPrint(obj.getJSONArray("results"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            super.handleMessage(msg);
        }
        
    };
    
    public void testPrint(JSONArray array){
        if(array == null){
            return;
        }
        String totalStr = "";
        for(int i = 0; i< array.length(); i++){
            String str = "";
            try {
                JSONObject obj = (JSONObject) array.get(i);
                str = str + obj.getInt("entry_id");
                str = str + " / " + obj.getString("l_account");
                str = str + " / " + obj.getString("r_account");
                str = str + " / " + obj.getString("item");
                str = str + " / " + obj.getInt("money");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            totalStr = totalStr + str + "\n";
        }
        ((TextView)findViewById(R.id.add_transaction_text_test)).setText(totalStr);
    }

    private void getAccountsByDate(int year, int i, int dayOfMonth) {
        // TODO DB Open 
        //Convert Date(String) to integer
        // select * from accounts where open_date > date 
        
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDateDisplay.setText(new StringBuilder()
            .append(year).append("-")
            .append(monthOfYear+1).append("-")
            .append(dayOfMonth));
            getAccountsByDate(year, monthOfYear+1, dayOfMonth);
        }
    };
    
    public void onClickGo(View v){
        if(checkValidation() == false){
            Toast.makeText(this, "확인해", Toast.LENGTH_SHORT).show();
            return;
        }
        Button goBtn = (Button)findViewById(R.id.add_transaction_btn_go);
        goBtn.setEnabled(false);
        // TODO 아래 ListView에 해당항목 넣기
        // TODO 서버로 Entity Add 날리기 
    }
    
    /**
     * Check field values validation for put entry
     * @return  All is validated, return true, otherwise return false
     * */
    public boolean checkValidation(){
        //Check Item edit
        final EditText editItem = (EditText)findViewById(R.id.add_transaction_edit_item); 
        String itemStr = editItem.getText().toString();
        if(itemStr.equals("")){
            Toast.makeText(this, "Check Item", Toast.LENGTH_SHORT).show();
            runOnUiThread(new Runnable() {
                public void run() {
                    editItem.requestFocus();
                }
            });
            return false;
        }
        
        //Check Amount edit
        final EditText editAmount = (EditText)findViewById(R.id.add_transaction_edit_amount);
        String itemAmount = editAmount.getText().toString();
        if(itemAmount.equals("")){
            Toast.makeText(this, "Check Amount", Toast.LENGTH_SHORT).show();
            runOnUiThread(new Runnable() {
                public void run() {
                    editAmount.requestFocus();
                }
            });
            
            return false;
        }
        
        if(mLeftAccount == null || mRightAccount == null){
            Toast.makeText(this, "Check left/right", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if(mLeftAccount.account_id.equals(mRightAccount.account_id)){
            Toast.makeText(this, "Left and Right are the same", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch(id)
        {
        case DATE_DIALOG_ID:
            final Calendar c = Calendar.getInstance();
            return new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        }
        return null;
    }
    
    /**
     * @return      return left-account entity
     * 
     * */
    public AccountsEntity getLeftAccounts(){
        return mLeftAccount;
    }
    
    /**
     * @return      return right-account entity
     * */
    public AccountsEntity getRightAccounts(){
        return mRightAccount;
    }
}
