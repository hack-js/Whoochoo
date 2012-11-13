/**
 * 
 */
package net.wisedog.android.whooing;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

/**
 * @author Wisedog(me@wisedog.net)
 *
 */
public class TransactionAdd extends Activity {
    
    protected static final int DATE_DIALOG_ID = 0;
    private TextView    mDateDisplay;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_transaction);
        //((TextView)findViewById(R.id.add_transaction_text_date)).setText("ASDF");
        mDateDisplay = (TextView)findViewById(R.id.add_transaction_text_date);
        Button dateChangeBtn = (Button)findViewById(R.id.add_transaction_change_btn);
        dateChangeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        String date = DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();
        mDateDisplay.setText(date);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDateDisplay.setText(new StringBuilder()
            .append(year).append("-")
            .append(monthOfYear+1).append("-")
            .append(dayOfMonth));
        }
    };

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

}
