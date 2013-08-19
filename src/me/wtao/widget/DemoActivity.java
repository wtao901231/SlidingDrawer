package me.wtao.widget;

import me.wtao.utils.Logcat;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class DemoActivity extends Activity {
	private Logcat mlogcat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mlogcat = new Logcat();

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// setContentView(R.layout.activity_demo_1);
		// setContentView(R.layout.layout_sliding_drawer);

		setContentView(R.layout.activity_demo_2);

		SlidingDrawer t2b = (SlidingDrawer) findViewById(R.id.drawer_top_down);
		// SlidingDrawer b2t = (SlidingDrawer)
		// findViewById(R.id.drawer_bottom_up);
		// SlidingDrawer l2r = (SlidingDrawer)
		// findViewById(R.id.drawer_left_to_right);
		// SlidingDrawer r2l = (SlidingDrawer)
		// findViewById(R.id.drawer_right_to_left);

		OnDrawerScrollListener onDrawerScrollListener = new OnDrawerScrollListener();
		t2b.setOnDrawerScrollListener(onDrawerScrollListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.demo, menu);
		return true;
	}

	private class OnDrawerScrollListener implements
			SlidingDrawer.OnDrawerScrollListener {

		@Override
		public void onPreScrollStarted() {
			mlogcat.d("entry");
		}

		@Override
		public void onScrollStarted() {
			mlogcat.d("entry");
		}

		@Override
		public void onScroll(boolean willBackward) {
			if (willBackward) {
				mlogcat.w("willBackward ? ", true);
			}
		}

		@Override
		public void onScrollEnded() {
			mlogcat.d("entry");
		}

	}

}
