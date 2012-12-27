package net.wisedog.android.whooing.activity;

import net.wisedog.android.whooing.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

public class MainFragmentActivity extends SherlockFragmentActivity{
	MainFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.whooing_tabs);
        
        mAdapter = new MainFragmentAdapter(getSupportFragmentManager(), this);

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Plus").setIcon(R.drawable.menu_plus_button_white)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SubMenu subMenu1 = menu.addSubMenu("Lists");
		subMenu1.add("History");
		subMenu1.add("Menu2");
		subMenu1.add("Setting");

		MenuItem subMenu1Item = subMenu1.getItem();
		subMenu1Item.setIcon(R.drawable.menu_lists_button);
		subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("wisedog", "ITEM : " + item.getTitle());
        if (item.getTitle().equals("Plus")) {
            Intent intent = new Intent(this, TransactionAdd.class);
            intent.putExtra("title", "거래추가");
            startActivityForResult(intent, 1);
        }
        else if(item.getTitle().equals("History")){
            Intent intent = new Intent(this, TransactionEntries.class);
            intent.putExtra("title", "History");
            startActivityForResult(intent, 1);
        }

        return super.onOptionsItemSelected(item);
    }

}
