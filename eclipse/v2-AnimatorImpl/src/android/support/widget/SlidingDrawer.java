package android.support.widget;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

@SuppressLint("NewApi")
public class SlidingDrawer extends RelativeLayout implements OnTouchListener, AnimatorListener {

	private static final String TAG = "SlidingDrawer";

	public static final int ORIENTATION_TOP_DOWN = 1 << 0;
	public static final int ORIENTATION_BOTTOM_UP = 1 << 1;
	public static final int ORIENTATION_LEFT_TO_RIGHT = 1 << 2;
	public static final int ORIENTATION_RIGHT_TO_LEFT = 1 << 3;

	public static final int ORIENTATION_HORIZONTAL_DEFAULT = ORIENTATION_RIGHT_TO_LEFT;
	public static final int ORIENTATION_VERTICAL_DEFAULT = ORIENTATION_BOTTOM_UP;
	
	/**
	 * Callback invoked when the drawer is opened.
	 */
	public static interface OnDrawerOpenListener {
		/**
		 * Invoked when the drawer becomes fully open.
		 */
		public void onDrawerOpened();
	}

	/**
	 * Callback invoked when the drawer is closed.
	 */
	public static interface OnDrawerCloseListener {
		/**
		 * Invoked when the drawer becomes fully closed.
		 */
		public void onDrawerClosed();
	}

	/**
	 * Callback invoked when the drawer is scrolled.
	 */
	public static interface OnDrawerScrollListener {
		/**
		 * Invoked when the user starts dragging/flinging the drawer's handle.
		 */
		public void onScrollStarted();

		/**
		 * Invoked when the user stops dragging/flinging the drawer's handle.
		 */
		public void onScrollEnded();
	}

	private final int mOrientation;
	private boolean mAllowSingleTap;
	private boolean mAnimateOnClick;

	private final int mHandleId;
	private final int mContentId;

	private View mHandle;
	private View mContent;
	
	private Animator mTransAnimator;
	private boolean mExpanded;
	
	private TensionView mTensionView;

	/**
	 * Creates a new SlidingDrawer from a specified set of attributes defined in
	 * XML.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param attrs
	 *            The attributes defined in XML.
	 */
	public SlidingDrawer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Creates a new SlidingDrawer from a specified set of attributes defined in
	 * XML.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param attrs
	 *            The attributes defined in XML.
	 * @param defStyleAttr
	 *            An attribute in the current theme that contains a reference to
	 *            a style resource that supplies default values for the view.
	 *            Can be 0 to not look for defaults.
	 */
	public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	/**
	 * Creates a new SlidingDrawer from a specified set of attributes defined in
	 * XML.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param attrs
	 *            The attributes defined in XML.
	 * @param defStyleAttr
	 *            An attribute in the current theme that contains a reference to
	 *            a style resource that supplies default values for the view.
	 *            Can be 0 to not look for defaults.
	 * @param defStyleRes
	 *            A resource identifier of a style resource that supplies
	 *            default values for the view, used only if defStyleAttr is 0 or
	 *            can not be found in the theme. Can be 0 to not look for
	 *            defaults.
	 */
	public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs);

		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SlidingDrawer, defStyleAttr, defStyleRes);
		mOrientation = a.getInt(R.styleable.SlidingDrawer_orientation,
				ORIENTATION_VERTICAL_DEFAULT);
		mAllowSingleTap = a.getBoolean(
				R.styleable.SlidingDrawer_allowSingleTap, true);
		mAnimateOnClick = a.getBoolean(
				R.styleable.SlidingDrawer_animateOnClick, true);
		
		Drawable tensionDrawable = a.getDrawable(R.styleable.SlidingDrawer_tensionTween);
		if(null != tensionDrawable) {
			mTensionView = new TensionView(this);
			mTensionView.setBackgroundDrawable(tensionDrawable);
		}
		
		int handleId = a.getResourceId(R.styleable.SlidingDrawer_handle, 0);
		if (handleId == 0) {
			throw new IllegalArgumentException(
					"The handle attribute is required and must refer to a valid child.");
		}
		int contentId = a.getResourceId(R.styleable.SlidingDrawer_content, 0);
		if (contentId == 0) {
			throw new IllegalArgumentException(
					"The content attribute is required and must refer to a valid child.");
		}
		if (handleId == contentId) {
			throw new IllegalArgumentException(
					"The content and handle attributes must refer to different children.");
		}
		a.recycle();

		mHandleId = handleId;
		mContentId = contentId;
	}

	@Override
	protected void onFinishInflate() {
		mHandle = findViewById(mHandleId);
		if (mHandle == null) {
			throw new IllegalArgumentException(
					"The handle attribute is must refer to an existing child.");
		}
		if (mAllowSingleTap) {
			mHandle.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					trigglerDrawer(mAnimateOnClick);
				}
			});
		}
		mHandle.setOnTouchListener(this);

		mContent = findViewById(mContentId);
		if (mContent == null) {
			throw new IllegalArgumentException(
					"The content attribute is must refer to an existing child.");
		}
		
		
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		ViewGroup parentView = (ViewGroup) getParent();
		if(null == parentView) {
			throw new IllegalArgumentException(
					"The SlidingDrawer must be added to parent View.");
		}
		parentView.addView(mTensionView);
	}
	
	static class TensionView extends FrameLayout {

		private final WeakReference<SlidingDrawer> mHostViewRef;
		
		public TensionView(SlidingDrawer hostView) {
			super(hostView.getContext());
			mHostViewRef = new WeakReference<SlidingDrawer>(hostView);
		}
		
		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			updateViewTension();
		}
		
		public void updateViewTension() {
			SlidingDrawer hostView = mHostViewRef.get();
			if(null == hostView) {
				return;
			}
			int orientation = hostView.getOrientation();
			if(hostView.isHorizontal()) {
				int x = (int) ViewHelper.getX(hostView);
				if(ORIENTATION_RIGHT_TO_LEFT == orientation) {
					setTop(hostView.getTop());
					setLeft(x + hostView.getWidth());
					setRight(hostView.getRight());
					setBottom(hostView.getBottom());
				} else {
					setTop(hostView.getTop());
					setLeft(hostView.getLeft());
					setRight(x);
					setBottom(hostView.getBottom());
				}
			} else {
				int y = (int) ViewHelper.getY(hostView);
				if(ORIENTATION_BOTTOM_UP == orientation) {
					setTop(y + hostView.getHeight());
					setLeft(hostView.getLeft());
					setRight(hostView.getRight());
					setBottom(hostView.getBottom());
				} else {
					setTop(hostView.getTop());
					setLeft(hostView.getLeft());
					setRight(hostView.getRight());
					setBottom(y);
				}
			}
		}
		
	}
	
	public int getOrientation() {
		return mOrientation;
	}
	
	public boolean isHorizontal() {
		return (ORIENTATION_RIGHT_TO_LEFT == mOrientation) || (ORIENTATION_LEFT_TO_RIGHT == mOrientation);
	}
	
	public boolean isVertical() {
		return (ORIENTATION_BOTTOM_UP == mOrientation) || (ORIENTATION_TOP_DOWN == mOrientation);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		adjustLayoutMargin();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		setDrawerClosed(false); // TODO:
	}
	
	private void adjustLayoutMargin() {
		ViewGroup.LayoutParams lp = getLayoutParams();
		if(null == lp || !(lp instanceof ViewGroup.MarginLayoutParams)) {
			return;
		}
		
		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
		if(isHorizontal()) {
			if(ORIENTATION_RIGHT_TO_LEFT == mOrientation) {
				mlp.rightMargin = 0;
			} else {
				mlp.leftMargin = 0;
			}
		} else {
			if(ORIENTATION_BOTTOM_UP == mOrientation) {
				mlp.bottomMargin = 0;
			} else {
				mlp.topMargin = 0;
			}
		}
		setLayoutParams(mlp);
	}
	
	public void trigglerDrawer(boolean animate) {
		if(mExpanded) {
			setDrawerClosed(animate);
		} else {
			setsetDrawerOpen(animate);
		}
	}
	
	public void setsetDrawerOpen(boolean animate) {
		mExpanded = true;
		
		if(animate) {
			startTransAnimator(0);
		} else {
			if(isHorizontal()) {
				ViewHelper.setTranslationX(this, 0);
			} else {
				ViewHelper.setTranslationY(this, 0);
			}
		}
	}
	
	public void setDrawerClosed(boolean animate) {
		mExpanded = false;
		
		if(animate) {
			if(isHorizontal()) {
				if(ORIENTATION_RIGHT_TO_LEFT == mOrientation) {
					startTransAnimator(getWidth() - mHandle.getWidth());
				} else {
					startTransAnimator(mHandle.getWidth() - getWidth());
				}
			} else {
				if(ORIENTATION_BOTTOM_UP == mOrientation) {
					startTransAnimator(getHeight() - mHandle.getHeight());
				} else {
					startTransAnimator(mHandle.getHeight() - getHeight());
				}
			}
		} else {
			if(isHorizontal()) {
				if(ORIENTATION_RIGHT_TO_LEFT == mOrientation) {
					ViewHelper.setTranslationX(this, getWidth() - mHandle.getWidth());
				} else {
					ViewHelper.setTranslationX(this, mHandle.getWidth() - getWidth());
				}
			} else {
				if(ORIENTATION_BOTTOM_UP == mOrientation) {
					ViewHelper.setTranslationY(this, getHeight() - mHandle.getHeight());
				} else {
					ViewHelper.setTranslationY(this, mHandle.getHeight() - getHeight());
				}
			}
		}
	}
	
	public void startTransAnimator(float... position) {
		if(null != mTransAnimator) {
			mTransAnimator.cancel();
		}
		mTransAnimator = buildTransAnimator(position);
		mTransAnimator.setInterpolator(new OvershootInterpolator());
		mTransAnimator.setDuration(300); // TODO:
		mTransAnimator.start();
	}
	
	private Animator buildTransAnimator(float... position) {
		return ObjectAnimator.ofFloat(this, "transPosition", position);
	}

	private float mBaseAxisValue;
	private float mBaseTransPosition;
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			onDrawerScrollStarted(getMotionAxisValue(event));
			break;
		case MotionEvent.ACTION_MOVE:
			onDrawerScroll(getMotionAxisValue(event));
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			onDrawerScrollEnded(getMotionAxisValue(event));
			break;
		default:
			break;
		}
		
		return false;
	}
	
	private void onDrawerScrollStarted(float value) {
		mBaseAxisValue = value;
		mBaseTransPosition = getTransPosition();
	}
	
	protected float getTransPosition() {
		if(isHorizontal()) {
			return ViewHelper.getTranslationX(this);
		} else {
			return ViewHelper.getTranslationY(this);
		}
	}
	
	protected void setTransPosition(float position) {
		position = adjustTransPos1pxErr(position);
		
		if(isHorizontal()) {
			ViewHelper.setTranslationX(this, position);
		} else {
			ViewHelper.setTranslationY(this, position);
		}
		if(null != mTensionView) {
			mTensionView.updateViewTension();
		}
	}
	
	private float adjustTransPos1pxErr(float position) {
		return (int) position;
//		final float lastPos = getTransPosition();
//		final float diff = position - lastPos;
//		if(diff < 0) {
//			return (int) (position - 0.5f);
//		} else {
//			return (int) position;
//		}
	}
	
	private void onDrawerScroll(float value) {
		setTransPosition(value - mBaseAxisValue + mBaseTransPosition);
	}
	
	private void onDrawerScrollEnded(float value) {
		float deltaDistance = Math.abs(value - mBaseAxisValue);
		boolean willBounceBack = shouldBounceBack((int) deltaDistance);
		if(mExpanded) {
			if(willBounceBack) {
				setsetDrawerOpen(true);
			} else {
				setDrawerClosed(true);
			}
		} else {
			if(willBounceBack) {
				setDrawerClosed(true);
			} else {
				setsetDrawerOpen(true);
			}
		}
	}
	
	protected boolean shouldBounceBack(int deltaDistance) {
		return deltaDistance < ((isHorizontal() ? getWidth() : getHeight()) / 4);
	}
	
	private float getMotionAxisValue(MotionEvent event) {
		return (int) (isHorizontal() ? event.getRawX() : event.getRawY());
	}

	@Override
	public void onAnimationCancel(Animator animator) {
		
	}

	@Override
	public void onAnimationEnd(Animator animator) {
		if(isDrawerExpanded()) {
			mExpanded = true;
		} else {
			mExpanded = false;
		}
	}
	
	private boolean isDrawerExpanded() {
		if(isHorizontal()) {
			return (0 == ViewHelper.getTranslationX(this));
		} else {
			return (0 == ViewHelper.getTranslationY(this));
		}
	}

	@Override
	public void onAnimationRepeat(Animator animator) {
		
	}

	@Override
	public void onAnimationStart(Animator animator) {
		
	}

}
