package me.wtao.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Generally, use the Log.v() Log.d() Log.i() Log.w() and Log.e() methods.<br>
 * <br>
 * The order in terms of verbosity, from least to most is ERROR, WARN, INFO,
 * DEBUG, VERBOSE. Verbose should never be compiled into an application except
 * during development. Debug logs are compiled in but stripped at runtime.
 * Error, warning and info logs are always kept.<br>
 * <br>
 * Tip: A good convention is to declare a TAG constant in your class:<br>
 * <br>
 * private static final String TAG = "&ltClassName&gt";<br>
 * <br>
 * and use that in subsequent calls to the log methods. In other way, using
 * {@link #Logcat()} to construct, it'll auto declare the TAG for you; Of course
 * you can specify any TAG as you lick using {@link #Logcat(String)}. that's
 * nice :)<br>
 * <br>
 * If you need {@link Toast} a light-weight notice when WARN and INFO, or a
 * ERROR dialog, you can use the {@link #Logcat(Context)}. And if you don't want
 * log no more, just {@link #setOff()}; {@link #setOn()} to re-enable the
 * Logcat.<br>
 * <br>
 * At last, Logcat println the message you set in the format as following:<br>
 * <br>
 * &ltCallerClassName#CallerClassMethod:__LINE__&gt &ltyour-message&gt<br>
 * <br>
 * f.m.<br>
 * <br>
 * &ltme.wtao.widget.SlidingDrawer#onLayout:359&gt entry<br>
 * <br>
 * which TAG is SlidingDrawer.<br>
 * 
 * @author tagorewang &ltwtao901231@gmail.com&gt
 * 
 * @see android.util.Log
 */
public class Logcat {
	private static int STACK_TRACE_CALLER = 3;

	private Context mContext;
	private String mTag;
	private boolean mOn;

	public Logcat() {
		mContext = null;
		mTag = getCallerTag();
		mOn = true;
	}

	public Logcat(Context context) {
		mContext = context;
		mTag = getCallerTag();
		mOn = true;
	}

	public Logcat(String tag) {
		this(null, tag);
	}

	public Logcat(Context context, String tag) {
		this(context, tag, true);
	}

	public Logcat(Context context, String tag, boolean isLoggable) {
		mContext = context;
		mTag = tag;
		mOn = isLoggable;
	}

	public void setOn() {
		mOn = true;
	}

	public void setOff() {
		mOn = false;
	}

	public void v(Object... msg_segs) {
		if (mOn) {
			String msg = messageBuilder(msg_segs);
			Log.v(mTag, msg);
		}
	}

	public void d(Object... msg_segs) {
		if (mOn) {
			String msg = messageBuilder(msg_segs);
			Log.d(mTag, msg);
		}
	}

	public void i(Object... msg_segs) {
		if (mOn) {
			String msg = messageBuilder(msg_segs);
			Log.i(mTag, msg);

			if (mContext != null) {
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void w(Object... msg_segs) {
		if (mOn) {
			String msg = messageBuilder(msg_segs);
			Log.w(mTag, msg);

			if (mContext != null) {
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void e(Object... msg_segs) {
		if (mOn) {
			String msg = messageBuilder(msg_segs);
			Log.e(mTag, msg);

			if (mContext != null) {
				if (mContext instanceof Activity) {
					Activity activity = (Activity) mContext;
					AlertDialog.Builder builder = new AlertDialog.Builder(
							activity);
					builder.setTitle(mTag);
					builder.setMessage(msg);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
				}
			}

			Exception e = new Exception();
			e.printStackTrace();
		}
	}

	private String messageBuilder(Object... msg_segs) {
		StringBuilder sb = new StringBuilder();

		sb.append('<');
		sb.append(getCallerClassName());
		sb.append('#');
		sb.append(getCallerClassMethod());
		sb.append(':');
		sb.append(getCallerLogLine());
		sb.append("> ");

		for (Object seg : msg_segs) {
			sb.append(seg);
		}

		return sb.toString();
	}

	private String getCallerTag() {
		final int caller = STACK_TRACE_CALLER - 1;
		String callerName = new Exception().getStackTrace()[caller]
				.getClassName();
		final String regEx = "[.]";
		String[] callNameSegs = callerName.split(regEx);
		final int lastIndex = callNameSegs.length - 1;

		return callNameSegs[lastIndex];
	}

	private String getCallerClassName() {
		return new Exception().getStackTrace()[STACK_TRACE_CALLER]
				.getClassName();
	}

	private String getCallerClassMethod() {
		return new Exception().getStackTrace()[STACK_TRACE_CALLER]
				.getMethodName();
	}

	private int getCallerLogLine() {
		return new Exception().getStackTrace()[STACK_TRACE_CALLER]
				.getLineNumber();
	}
}
