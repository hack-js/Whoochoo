package net.wisedog.android.whooing.db;

import org.json.JSONObject;

public class AccountsEntity {
    public String accountType;
    public String account_id;
    public String type;
    public String title;
    public String memo;
    public int open_date;
    public int close_date;
    public String category;
    
    //only for liabilities
    public String opt_use_date;
    public int opt_pay_date;
    public String opt_pay_account_id;
    
    public AccountsEntity(String account_type, JSONObject jsonObj) throws org.json.JSONException{
        if(jsonObj == null || account_type == null){
            return;
        }
        accountType = account_type;
        account_id = jsonObj.getString("account_id");
        type = jsonObj.getString("type");
        title = jsonObj.getString("title");
        memo = jsonObj.getString("memo");
        open_date = jsonObj.getInt("open_date");
        close_date = jsonObj.getInt("close_date");
        category = jsonObj.getString("category");
            
        if(account_type.equals("liabilities")){
            opt_use_date = jsonObj.getString("opt_use_date");
            opt_pay_date = jsonObj.getInt("opt_pay_date");
            opt_pay_account_id = jsonObj.getString("opt_pay_account_id");
        }
    }
}
