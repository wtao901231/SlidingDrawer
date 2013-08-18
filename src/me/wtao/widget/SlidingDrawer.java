package me.wtao.widget;

/**
 * Copyright (C) 2013 wtao.me
 * 
 * @author tagorewang <wtao901231@gmail.com>
 * @date 7/21/2013 modified
 * 
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

/**
 * SlidingDrawer hides content out of the screen and allows the user to drag a
 * handle to bring the content on screen. SlidingDrawer can be used vertically
 * or horizontally.<br>
 * 
 * A special widget composed of two children views: the handle, that the users
 * drags, and the content, attached to the handle and dragged with it.<br>
 * 
 * SlidingDrawer should be used as an overlay inside layouts. This means
 * SlidingDrawer should only be used inside of a FrameLayout or a RelativeLayout
 * for instance. The size of the SlidingDrawer defines how much space the
 * content will occupy once slid out so SlidingDrawer should usually use
 * match_parent for both its dimensions.<br>
 * 
 * Inside an XML layout, SlidingDrawer must define the id of the handle (f.e.
 * @+id/handle) and of the content (f.e. @+id/content).
 * 
 * @see R.styleable#SlidingDrawer_content
 * @see R.styleable#SlidingDrawer_handle
 * @see R.styleable#SlidingDrawer_orientation
 * @see R.styleable#SlidingDrawer_allowSingleTap
 * @see R.styleable#SlidingDrawer_animateOnClick
 * 
 */
public class SlidingDrawer extends ViewGroup {
	/**
	 * Drawing from top down is not very friendly with system title notification
	 * bar, so it has not been implemented yet. Design retains for the needs in
	 * the future.
	 */
	public static final int ORIENTATION_TOP_DOWN = 0x01;
	public static final int ORIENTATION_BOTTOM_UP = 0x02;
	public static final int ORIENTATION_LEFT_TO_RIGHT = 0x04;
	public static final int ORIENTATION_RIGHT_TO_LEFT = 0x08;

	/**
	 * when moving at the end of {@link SlidingDrawer}, weather
	 * {@link #EXPANDED_FULL_OPEN} or {@link #COLLAPSED_FULL_CLOSED}, if the
	 * offset is less than the TAP_THRESHOLD ({@value #TAP_THRESHOLD} dp), the
	 * moving will be treat as a single tap.
	 * 
	 * @see #isSingleTap()
	 */
	private static final int TAP_THRESHOLD = 6;
	/**
	 * max velocity ({@value #MAXIMUM_TAP_VELOCITY}) of tap, as a comparative
	 * critical point in {@link #performFling(int, float, boolean)}<br>
	 * Velocity unit is one pixel per second.
	 * 
	 * @see #VELOCITY_UNITS
	 */
	private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
	/**
	 * max value ({@value #MAXIMUM_MINOR_VELOCITY} px/s) of secondary subvector.
	 * in the horizontal direction it's subvector Y, while in the vertical
	 * direction subvector X.<br>
	 * Velocity unit is one pixel per second.
	 * 
	 * @see #VELOCITY_UNITS
	 */
	private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
	/**
	 * max value ({@value #MAXIMUM_MAJOR_VELOCITY} px/s) of main subvector. in
	 * the horizontal direction it's subvector X, while in the vertical
	 * direction subvector Y.<br>
	 * Velocity unit is one pixel per second.
	 * 
	 * @see #VELOCITY_UNITS
	 */
	private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
	/**
	 * max value ({@value #MAXIMUM_ACCELERATION} px/s/s) of acceleration.
	 * Acceleration unit is one pixel per square second.
	 * 
	 * @see #VELOCITY_UNITS
	 */
	private static final float MAXIMUM_ACCELERATION = 2000.0f;
	/**
	 * The units you would like the velocity in. A value of 1 provides pixels
	 * per millisecond, 1000 provides pixels per second, etc.<br>
	 * We set VELOCITY_UNITS as {@value #VELOCITY_UNITS}, that is one pixel per
	 * second.
	 * 
	 * @see #incrementAnimation()
	 * @see android.view.VelocityTracker#computeCurrentVelocity(int, float)
	 */
	private static final int VELOCITY_UNITS = 1000;

	private static final int MSG_ANIMATE = 1000;
	/**
	 * animation update rate is {@value #ANIMATION_FRAME_DURATION} fps
	 */
	private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

	private static final int EXPANDED_FULL_OPEN = -10001;
	private static final int COLLAPSED_FULL_CLOSED = -10002;

	private final int mHandleId;
	private final int mContentId;

	private View mHandle;
	private View mContent;

	private final Rect mFrame = new Rect();
	private final Rect mInvalidate = new Rect();
	private boolean mTracking;

	private boolean mLocked = false;
	private boolean mContentLocked = false;

	private int mPadding = 0; // TODO: l, t, r, b

	private VelocityTracker mVelocityTracker;

	/**
	 * sliding drawer orientation, witch can be
	 * {@link #ORIENTATION_LEFT_TO_RIGHT}, {@link #ORIENTATION_RIGHT_TO_LEFT}
	 * and {@link #ORIENTATION_BOTTOM_UP}, {@link #ORIENTATION_TOP_DOWN} is
	 * ignored but retain for design.
	 * 
	 * @see #checkOrientation
	 */
	private int mOrientation;
	private boolean mVertical;
	private boolean mExpanded;

	private int mHandleHeight;
	private int mHandleWidth;

	private OnDrawerOpenListener mOnDrawerOpenListener;
	private OnDrawerCloseListener mOnDrawerCloseListener;
	private OnDrawerScrollListener mOnDrawerScrollListener;

	private GestureDetector mCollapseGestureDetector;

	private final Handler mHandler = new SlidingHandler();
	private float mAnimatedAcceleration;
	private float mAnimatedVelocity;
	private float mAnimationPosition;
	private long mAnimationLastTime;
	private long mCurrentAnimationTime;
	private int mTouchDelta;
	private boolean mAnimating;
	private boolean mAllowSingleTap;
	private boolean mAnimateOnClick;

	/**
	 * ceiling of {@link android.util.DisplayMetrics.density} *
	 * {@link #TAP_THRESHOLD}
	 * 
	 * @see #SlidingDrawer(Context, AttributeSet, int)
	 */
	private final int mTapThreshold;
	/**
	 * ceiling of {@link android.util.DisplayMetrics.density} *
	 * {@link #MAXIMUM_TAP_VELOCITY}
	 * 
	 * @see #SlidingDrawer(Context, AttributeSet, int)
	 */
	private final int mMaximumTapVelocity;
	/**
	 * ceiling of {@link android.util.DisplayMetrics.density} *
	 * {@link #MAXIMUM_MINOR_VELOCITY}
	 * 
	 * @see #SlidingDrawer(Context, AttributeSet, int)
	 */
	private final int mMaximumMinorVelocity;
	/**
	 * ceiling of {@link android.util.DisplayMetrics.density} *
	 * {@link #MAXIMUM_MAJOR_VELOCITY}
	 * 
	 * @see #SlidingDrawer(Context, AttributeSet, int)
	 */
	private final int mMaximumMajorVelocity;
	/**
	 * ceiling of {@link android.util.DisplayMetrics.density} *
	 * {@link #MAXIMUM_ACCELERATION}
	 * 
	 * @see #SlidingDrawer(Context, AttributeSet, int)
	 */
	private final int mMaximumAcceleration;
	/**
	 * ceiling of {@link android.util.DisplayMetrics.density} *
	 * {@link #VELOCITY_UNITS}
	 * 
	 * @see #SlidingDrawer(Context, AttributeSet, int)
	 */
	private final int mVelocityUnits;

	/**
	 * Callback invoked when the drawer is opened.
	 */
	public static interface OnDrawerOpenListener {		
		public void onDrawerWillOpen();

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
		public void onPreScrollStarted();

		/**
		 * Invoked when the user starts dragging/flinging the drawer's handle.
		 */
		public void onScrollStarted();

		public void onScroll(boolean willBackward);

		/**
		 * Invoked when the user stops dragging/flinging the drawer's handle.
		 */
		public void onScrollEnded();
	}

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
	 * @param defStyle
	 *            The style to apply to this widget.
	 */
	public SlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SlidingDrawer, defStyle, 0);

		mOrientation = a.getInt(R.styleable.SlidingDrawer_orientation,
				ORIENTATION_LEFT_TO_RIGHT);
		checkOrientation();
		mVertical = (mOrientation == ORIENTATION_TOP_DOWN || mOrientation == ORIENTATION_BOTTOM_UP);

		mAllowSingleTap = a.getBoolean(
				R.styleable.SlidingDrawer_allowSingleTap, false);
		mAnimateOnClick = a.getBoolean(
				R.styleable.SlidingDrawer_animateOnClick, false);

		int handleId = a.getResourceId(R.styleable.SlidingDrawer_handle, 0);
		if (handleId == 0) {
			throw new IllegalArgumentException(
					"The handle attribute is required and must refer "
							+ "to a valid child.");
		}

		int contentId = a.getResourceId(R.styleable.SlidingDrawer_content, 0);
		if (contentId == 0) {
			throw new IllegalArgumentException(
					"The content attribute is required and must refer "
							+ "to a valid child.");
		}

		if (handleId == contentId) {
			throw new IllegalArgumentException(
					"The content and handle attributes must refer "
							+ "to different children.");
		}

		mHandleId = handleId;
		mContentId = contentId;

		final float density = getResources().getDisplayMetrics().density;
		mTapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);
		mMaximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);
		mMaximumMinorVelocity = (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
		mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
		mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);
		mVelocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);

		a.recycle();

		setAlwaysDrawnWithCacheEnabled(false);

		mCollapseGestureDetector = new GestureDetector(context,
				new CollapseGestureDetector());
	}

	/**
	 * Sets the listener that receives a notification when the drawer becomes
	 * open.
	 * 
	 * @param onDrawerOpenListener
	 *            The listener to be notified when the drawer is opened.
	 */
	public void setOnDrawerOpenListener(
			OnDrawerOpenListener onDrawerOpenListener) {
		mOnDrawerOpenListener = onDrawerOpenListener;
	}

	/**
	 * Sets the listener that receives a notification when the drawer becomes
	 * close.
	 * 
	 * @param onDrawerCloseListener
	 *            The listener to be notified when the drawer is closed.
	 */
	public void setOnDrawerCloseListener(
			OnDrawerCloseListener onDrawerCloseListener) {
		mOnDrawerCloseListener = onDrawerCloseListener;
	}

	/**
	 * Sets the listener that receives a notification when the drawer starts or
	 * ends a scroll. A fling is considered as a scroll. A fling will also
	 * trigger a drawer opened or drawer closed event.
	 * 
	 * @param onDrawerScrollListener
	 *            The listener to be notified when scrolling starts or stops.
	 */
	public void setOnDrawerScrollListener(
			OnDrawerScrollListener onDrawerScrollListener) {
		mOnDrawerScrollListener = onDrawerScrollListener;
	}

	public final int getOrientation() {
		return mOrientation;
	}

	/**
	 * Returns the handle of the drawer.
	 * 
	 * @return The View reprenseting the handle of the drawer, identified by the
	 *         "handle" id in XML.
	 */
	public View getHandle() {
		return mHandle;
	}

	/**
	 * Returns the content of the drawer.
	 * 
	 * @return The View reprenseting the content of the drawer, identified by
	 *         the "content" id in XML.
	 */
	public View getContent() {
		return mContent;
	}

	/**
	 * Unlocks the SlidingDrawer so that touch events are processed.
	 * 
	 * @see #lock()
	 */
	public void unlock() {
		mLocked = false;
	}

	/**
	 * Locks the SlidingDrawer so that touch events are ignores.
	 * 
	 * @see #unlock()
	 */
	public void lock() {
		mLocked = true;
	}

	public void lockContent() {
		mContentLocked = true;
	}

	public void unlockContent() {
		mContentLocked = false;
	}

	/**
	 * Indicates whether the drawer is currently fully opened.
	 * 
	 * @return True if the drawer is opened, false otherwise.
	 */
	public boolean isOpened() {
		return mExpanded;
	}

	/**
	 * Indicates whether the drawer is scrolling or flinging.
	 * 
	 * @return True if the drawer is scroller or flinging, false otherwise.
	 */
	public boolean isMoving() {
		return mTracking || mAnimating;
	}

	@Override
	protected void onFinishInflate() {
		mHandle = findViewById(mHandleId);
		if (mHandle == null) {
			throw new IllegalArgumentException(
					"The handle attribute is must refer to an"
							+ " existing child.");
		}
		if (mAnimateOnClick) {
			mHandle.setOnClickListener(new DrawerToggler());
		}

		mContent = findViewById(mContentId);
		if (mContent == null) {
			throw new IllegalArgumentException(
					"The content attribute is must refer to an"
							+ " existing child.");
		}
		mContent.setVisibility(View.GONE);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthSpecMode == MeasureSpec.UNSPECIFIED
				|| heightSpecMode == MeasureSpec.UNSPECIFIED) {
			throw new RuntimeException(
					"SlidingDrawer cannot have UNSPECIFIED dimensions");
		}

		final View handle = mHandle;
		measureChild(handle, widthMeasureSpec, heightMeasureSpec);

		if (isVertical()) {
			int height = heightSpecSize - handle.getMeasuredHeight();
			mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize,
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height,
					MeasureSpec.EXACTLY));
		} else {
			// int width = widthSpecSize - handle.getMeasuredWidth();
			// mContent.measure(MeasureSpec.makeMeasureSpec(width,
			// MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
			// heightSpecSize, MeasureSpec.EXACTLY));

			int width = widthSpecSize;
			mContent.measure(MeasureSpec.makeMeasureSpec(width,
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
					heightSpecSize, MeasureSpec.EXACTLY));
		}

		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		final long drawingTime = getDrawingTime();
		final View handle = mHandle;

		if (mTracking || mAnimating) {
			final Bitmap cache = mContent.getDrawingCache();
			if (cache != null) {
				switch (mOrientation) {
				case ORIENTATION_BOTTOM_UP: {
					canvas.drawBitmap(cache, 0, handle.getBottom(), null);
					break;
				}
				case ORIENTATION_LEFT_TO_RIGHT: {
					// canvas.drawBitmap(cache, -mContent.getMeasuredWidth()
					// + handle.getLeft(), 0, null);
					canvas.drawBitmap(cache, -mContent.getMeasuredWidth()
							+ handle.getRight(), 0, null);
					break;
				}
				case ORIENTATION_RIGHT_TO_LEFT: {
					canvas.drawBitmap(cache, handle.getRight(), 0, null);
					break;
				}
				}
			} else {
				canvas.save();

				// canvas.translate (dx, dy) -> original point (x0+dx, y0+dy);
				// that is, prepare for drawing in {(x0+dx, y0+dy) ->
				// (x0+dx+w, y0+dy+h)} matix, where w and h may be dynamic
				switch (mOrientation) {
				case ORIENTATION_BOTTOM_UP: {
					// TODO: see the other if-branch for comparison O_o?
					canvas.translate(0, handle.getTop());
					break;
				}
				case ORIENTATION_LEFT_TO_RIGHT: {
					canvas.translate(
							-mContent.getMeasuredWidth() + handle.getRight(), 0);
					break;
				}
				case ORIENTATION_RIGHT_TO_LEFT: {
					// TODO: see the other if-branch for comparison O_o?
					canvas.translate(handle.getLeft(), 0);
					break;
				}
				}
				drawChild(canvas, mContent, drawingTime);

				canvas.restore();
			}
		} else if (mExpanded) {
			drawChild(canvas, mContent, drawingTime);
		}
		drawChild(canvas, handle, drawingTime);
		// overlap handle on the content
		// if (mHandle.getVisibility() == View.VISIBLE) {
		// drawChild(canvas, handle, drawingTime);
		// }
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mTracking) {
			return;
		}

		int width = r - l;
		int height = b - t;

		if (isHorizontal()) {
			width -= mPadding;
		}

		final View handle = mHandle;

		// child is handle, content is auto adjusted
		int childWidth = handle.getMeasuredWidth();
		int childHeight = handle.getMeasuredHeight();

		int childLeft = 0;
		int childTop = 0;

		final View content = mContent;

		switch (mOrientation) {
		case ORIENTATION_BOTTOM_UP: {
			childLeft = (width - childWidth) / 2;
			childTop = mExpanded ? 0 : height - childHeight;

			content.layout(0, childHeight, content.getMeasuredWidth(),
					childHeight + content.getMeasuredHeight());
			break;
		}
		case ORIENTATION_LEFT_TO_RIGHT: {
			childLeft = mExpanded ? width - childWidth : 0;
			childTop = (height - childHeight) / 2;

			content.layout(0, 0, content.getMeasuredWidth(),
					content.getMeasuredHeight());
			break;
		}
		case ORIENTATION_RIGHT_TO_LEFT: {
			childLeft = mExpanded ? 0 : width - childWidth;
			childTop = (height - childHeight) / 2;

			content.layout(childWidth, 0,
					childWidth + content.getMeasuredWidth(),
					content.getMeasuredHeight());
			break;
		}
		}

		handle.layout(childLeft, childTop, childLeft + childWidth, childTop
				+ childHeight);

		mHandleHeight = handle.getHeight();
		mHandleWidth = handle.getWidth();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mLocked) {
			return false;
		}

		// any other event filters from here
		if (mExpanded) {
			if (mCollapseGestureDetector.onTouchEvent(event)) {
				return true;
			}
		}

		return false; // always return false to receive any following events
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mLocked) {
			return true;
		}

		final int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			float x = event.getX();
			float y = event.getY();

			final Rect frame = mFrame;
			final View handle = mHandle;

			handle.getHitRect(frame);
			if (!mTracking && ((mContentLocked || !mExpanded))
					&& !frame.contains((int) x, (int) y)) {
				return false;
			}

			mTracking = true;

			handle.setPressed(true);

			if (mOnDrawerCloseListener != null) {
				mOnDrawerScrollListener.onPreScrollStarted();
			}

			// Must be called before prepareTracking()
			prepareContent();

			// Must be called after prepareContent()
			if (mOnDrawerScrollListener != null) {
				mOnDrawerScrollListener.onScrollStarted();
			}

			if (isVertical()) {
				mTouchDelta = (int) y - mHandle.getTop();
				final int yBase = mHandle.getTop();
				prepareTracking(yBase);
			} else {
				mTouchDelta = (int) x - mHandle.getLeft();
				final int xBase = mHandle.getLeft();
				prepareTracking(xBase);
			}

			mVelocityTracker.addMovement(event); // keep track of ACTION_DOWN
		} else if (mTracking) {
			mVelocityTracker.addMovement(event); // we also need keep track of
													// the following events

			if (action == MotionEvent.ACTION_MOVE) {
				moveHandle((int) (isVertical() ? event.getY() : event.getX())
						- mTouchDelta);
			} else if (action == MotionEvent.ACTION_UP
					|| action == MotionEvent.ACTION_CANCEL) {
				// get the tap velocity, must after VelocityTracker, if don't
				// want to lose some velocity tracks
				final float velocity = computeVelocity();

				// target position
				int position = (isVertical() ? mHandle.getTop() : mHandle
						.getLeft());

				if (Math.abs(velocity) < mMaximumTapVelocity && isSingleTap()
						&& mAllowSingleTap) {
					// single tap to fling
					playSoundEffect(SoundEffectConstants.CLICK);

					if (mExpanded) {
						animateClose(position);
					} else {
						animateOpen(position);
					}
				} else {
					// drag to fling
					performFling(position, velocity, false);
				}
			}
		}

		if (mTracking || mAnimating || (!mContentLocked && mExpanded)) {
			return true;
		} else {
			close(); // always but need optimizing in the future
			return false;
		}
	}

	private float computeVelocity() {
		final VelocityTracker velocityTracker = mVelocityTracker;
		velocityTracker.computeCurrentVelocity(mVelocityUnits);

		float yVelocity = velocityTracker.getYVelocity();
		float xVelocity = velocityTracker.getXVelocity();
		boolean negative;

		final boolean vertical = isVertical();
		if (vertical) {
			// in the vertical direction, the direction of the vector as
			// same as subvector Y
			negative = yVelocity < 0;
			if (xVelocity < 0) {
				xVelocity = -xVelocity;
			}
			if (xVelocity > mMaximumMinorVelocity) {
				xVelocity = mMaximumMinorVelocity;
			}
		} else {
			// in the horizontal direction, the direction of the vector
			// as same as subvector X
			negative = xVelocity < 0;
			if (yVelocity < 0) {
				yVelocity = -yVelocity;
			}
			if (yVelocity > mMaximumMinorVelocity) {
				yVelocity = mMaximumMinorVelocity;
			}
		}

		// Vector VELOCITY has length SQRT(x^2+ y^2)
		float velocity = (float) Math.hypot(xVelocity, yVelocity);
		if (negative) {
			velocity = -velocity;
		}

		return velocity;
	}

	private boolean isSingleTap() {
		boolean ret = false;

		switch (mOrientation) {
		case ORIENTATION_BOTTOM_UP: {
			ret = (mExpanded && mHandle.getTop() < mTapThreshold)
					|| (!mExpanded && mHandle.getBottom() > getBottom()
							- getTop() - mTapThreshold);
			break;
		}
		case ORIENTATION_LEFT_TO_RIGHT: {
			ret = ((mExpanded && mHandle.getRight() > getRight() - getLeft()
					- mTapThreshold) || (!mExpanded && mHandle.getLeft() < mTapThreshold));
			break;
		}
		case ORIENTATION_RIGHT_TO_LEFT: {
			ret = ((mExpanded && mHandle.getRight() > mTapThreshold) || (!mExpanded && mHandle
					.getLeft() < getRight() - getLeft() - mTapThreshold));
			break;
		}
		}

		return ret;
	}

	private void animateClose(int position) {
		prepareTracking(position);
		switch (mOrientation) {
		case ORIENTATION_BOTTOM_UP: {
			performFling(position, mMaximumAcceleration, true);
			break;
		}
		case ORIENTATION_LEFT_TO_RIGHT: {
			performFling(position, -mMaximumAcceleration, true);
			break;
		}
		case ORIENTATION_RIGHT_TO_LEFT: {
			performFling(position, mMaximumAcceleration, true);
			break;
		}
		}
	}

	private void animateOpen(int position) {
		prepareTracking(position);
		switch (mOrientation) {
		case ORIENTATION_BOTTOM_UP: {
			performFling(position, -mMaximumAcceleration, true);
			break;
		}
		case ORIENTATION_LEFT_TO_RIGHT: {
			performFling(position, mMaximumAcceleration, true);
			break;
		}
		case ORIENTATION_RIGHT_TO_LEFT: {
			performFling(position, -mMaximumAcceleration, true);
			break;
		}
		}
	}

	/**
	 * 
	 * @param position
	 *            start position
	 * @param velocity
	 *            initialized velocity
	 * @param autoAnimated
	 * @link {@link #animateOpen()} and {@link #animateClose()}
	 */
	private void performFling(int position, float velocity, boolean autoAnimated) {
		mAnimationPosition = position;
		mAnimatedVelocity = velocity;

		final boolean invertedCoord = (mOrientation == ORIENTATION_LEFT_TO_RIGHT || mOrientation == ORIENTATION_TOP_DOWN);
		final int ROLLBACK_OFFSET_THRESHOLD = (mVertical ? getHeight()
				: getWidth()) / 4;

		boolean willOnFling;
		boolean willRollback;

		if (mExpanded) {
			// we're EXPANDED
			int ROLLBACE_CRITICAL_POINT;
			if (mVertical) {
				ROLLBACE_CRITICAL_POINT = getTop() + ROLLBACK_OFFSET_THRESHOLD;
			} else {
				if (mOrientation == ORIENTATION_LEFT_TO_RIGHT) {
					ROLLBACE_CRITICAL_POINT = getRight()
							- ROLLBACK_OFFSET_THRESHOLD;
				} else {
					ROLLBACE_CRITICAL_POINT = getLeft()
							+ ROLLBACK_OFFSET_THRESHOLD;
				}
			}

			if (invertedCoord) {
				willOnFling = velocity < -mMaximumMajorVelocity;
				willRollback = position > ROLLBACE_CRITICAL_POINT;
			} else {
				willOnFling = velocity > mMaximumMajorVelocity;
				willRollback = position < ROLLBACE_CRITICAL_POINT;
			}

			if (autoAnimated || willOnFling || !willRollback) {
				// We are expanded and are now going to animate CLOSE.
				if (invertedCoord) {
					mAnimatedAcceleration = -mMaximumAcceleration;
					if (velocity > 0) {
						mAnimatedVelocity = 0;
					}
				} else {
					mAnimatedAcceleration = mMaximumAcceleration;
					if (velocity < 0) {
						mAnimatedVelocity = 0;
					}
				}
			} else {
				// We are expanded, but they didn't move sufficiently to cause
				// us to retract. Animate back to the expanded position. so
				// animate BACK to expanded!
				if (invertedCoord) {
					mAnimatedAcceleration = mMaximumAcceleration;
					if (velocity < 0) {
						mAnimatedVelocity = 0;
					}
				} else {
					mAnimatedAcceleration = -mMaximumAcceleration;
					if (velocity > 0) {
						mAnimatedVelocity = 0;
					}
				}
			}
		} else {
			// we're COLLAPSED
			int ROLLBACE_CRITICAL_POINT;
			if (mVertical) {
				ROLLBACE_CRITICAL_POINT = getBottom()
						- ROLLBACK_OFFSET_THRESHOLD;
			} else {
				if (mOrientation == ORIENTATION_LEFT_TO_RIGHT) {
					ROLLBACE_CRITICAL_POINT = getLeft()
							+ ROLLBACK_OFFSET_THRESHOLD;
				} else {
					ROLLBACE_CRITICAL_POINT = getRight()
							- ROLLBACK_OFFSET_THRESHOLD;
				}
			}

			if (!invertedCoord) {
				willOnFling = velocity < -mMaximumMajorVelocity;
				willRollback = position > ROLLBACE_CRITICAL_POINT;
			} else {
				willOnFling = velocity > mMaximumMajorVelocity;
				willRollback = position < ROLLBACE_CRITICAL_POINT;
			}

			if (autoAnimated || willOnFling || !willRollback) {
				// We are expanded and are now going to animate OPEN.
				if (!invertedCoord) {
					mAnimatedAcceleration = -mMaximumAcceleration;
					if (velocity > 0) {
						mAnimatedVelocity = 0;
					}
				} else {
					mAnimatedAcceleration = mMaximumAcceleration;
					if (velocity < 0) {
						mAnimatedVelocity = 0;
					}
				}
			} else {
				// We are expanded, but they didn't move sufficiently to cause
				// us to retract. Animate back to the expanded position. so
				// animate BACK to collapsed!

				if (!invertedCoord) {
					mAnimatedAcceleration = mMaximumAcceleration;
					if (velocity < 0) {
						mAnimatedVelocity = 0;
					}
				} else {

					mAnimatedAcceleration = -mMaximumAcceleration;
					if (velocity > 0) {
						mAnimatedVelocity = 0;
					}
				}
			}
		}

		long now = SystemClock.uptimeMillis();
		mAnimationLastTime = now;
		mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
		mAnimating = true;
		mHandler.removeMessages(MSG_ANIMATE);
		mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE),
				mCurrentAnimationTime);
		stopTracking();
	}

	private void prepareTracking(int position) {
		mTracking = true;
		mVelocityTracker = VelocityTracker.obtain();
		boolean willOpen = !mExpanded;
		if (willOpen) {
			// tap to expanded very quickly, if allow
			mAnimatedAcceleration = mMaximumAcceleration;
			mAnimatedVelocity = mMaximumMajorVelocity;

			// TODO: put it in its place, ready to go if given more action, f.e.
			// dragging, animating or others
			switch (mOrientation) {
			case ORIENTATION_BOTTOM_UP: {
				mAnimationPosition = getHeight() - mHandleHeight;
				break;
			}
			case ORIENTATION_LEFT_TO_RIGHT: {
				mAnimationPosition = 0;
				break;
			}
			case ORIENTATION_RIGHT_TO_LEFT: {
				mAnimationPosition = getWidth() - mHandleWidth;
				break;
			}
			}
			moveHandle((int) mAnimationPosition);

			// reset animator frame time
			mHandler.removeMessages(MSG_ANIMATE);
			long now = SystemClock.uptimeMillis();
			mAnimationLastTime = now;
			mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
			mAnimating = true;
		} else {
			if (mAnimating) {
				mAnimating = false;
				mHandler.removeMessages(MSG_ANIMATE);
			}
			moveHandle(position);
		}
	}

	private void moveHandle(int position) {
		final View handle = mHandle;
		if (isVertical()) {
			if (position == EXPANDED_FULL_OPEN) {
				handle.offsetTopAndBottom(handle.getTop());
				invalidate();
			} else if (position == COLLAPSED_FULL_CLOSED) {
				handle.offsetTopAndBottom(getBottom() - getTop()
						- mHandleHeight - handle.getTop());
				invalidate();
			} else {
				final int top = handle.getTop();
				int deltaY = position - top;
				if (position < 0) {
					deltaY = -top;
				} else if (deltaY > getBottom() - getTop() - mHandleHeight
						- top) {
					deltaY = getBottom() - getTop() - mHandleHeight - top;
				}
				handle.offsetTopAndBottom(deltaY);

				final Rect frame = mFrame;
				final Rect region = mInvalidate;

				handle.getHitRect(frame);
				region.set(frame);

				region.union(frame.left, frame.top - deltaY, frame.right,
						frame.bottom - deltaY);
				region.union(0, frame.bottom - deltaY, getWidth(), frame.bottom
						- deltaY + mContent.getHeight());

				invalidate(region);
			}
		} else {
			final int leftHalfOffset = -handle.getLeft(); // leftHalfOffset < 0
			final int rightHalfOffset = getRight() - getLeft() - mHandleWidth
					- handle.getLeft(); // rightHalfOffset > 0

			if (position == EXPANDED_FULL_OPEN) {
				if (mOrientation == ORIENTATION_LEFT_TO_RIGHT) {
					handle.offsetLeftAndRight(rightHalfOffset);
				} else {
					handle.offsetLeftAndRight(leftHalfOffset);
				}
				invalidate();
			} else if (position == COLLAPSED_FULL_CLOSED) {
				if (mOrientation == ORIENTATION_LEFT_TO_RIGHT) {
					handle.offsetLeftAndRight(leftHalfOffset);
				} else {
					handle.offsetLeftAndRight(rightHalfOffset);
				}
				invalidate();
			} else {
				final int left = handle.getLeft();
				int deltaX = position - left;

				// make it within the range
				if (position < 0) {
					// left half offset left-and-right
					deltaX = leftHalfOffset;
				} else if (deltaX > rightHalfOffset) {
					// right half offset left-and-right
					deltaX = rightHalfOffset;
				}
				handle.offsetLeftAndRight(deltaX);

				final Rect frame = mFrame;
				final Rect region = mInvalidate;

				// invalidate handle
				handle.getHitRect(frame);
				region.set(frame);
				region.union(frame.left - deltaX, frame.top, frame.right
						- deltaX, frame.bottom);

				// invalidate content
				if (mOrientation == ORIENTATION_LEFT_TO_RIGHT) {
					region.union(0, 0,
							frame.left - deltaX + mContent.getWidth(),
							getHeight());
				} else {
					region.union(frame.right - deltaX, 0, frame.right - deltaX
							+ mContent.getWidth(), getHeight());
				}

				invalidate(region);
			}
		}
	}

	private void prepareContent() {
		if (mAnimating) {
			return;
		}

		// Something changed in the content, we need to honor the layout request
		// before creating the cached bitmap
		final View content = mContent;
		if (content.isLayoutRequested()) {
			if (isVertical()) {
				final int childHeight = mHandleHeight;
				int height = getBottom() - getTop() - childHeight;
				content.measure(MeasureSpec.makeMeasureSpec(getRight()
						- getLeft(), MeasureSpec.EXACTLY), MeasureSpec
						.makeMeasureSpec(height, MeasureSpec.EXACTLY));
				if (mOrientation == ORIENTATION_BOTTOM_UP) {
					content.layout(0, childHeight, content.getMeasuredWidth(),
							childHeight + content.getMeasuredHeight());
				}
			} else {
				// final int childWidth = mHandle.getWidth();
				// int width = getRight() - getLeft() - childWidth;
				// content.measure(MeasureSpec.makeMeasureSpec(width,
				// MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
				// getBottom() - getTop(), MeasureSpec.EXACTLY));
				// if (mOrientation == ORIENTATION_LEFT_TO_RIGHT) {
				// content.layout(0, 0, content.getMeasuredWidth(),
				// content.getMeasuredHeight());
				// } else {
				// content.layout(childWidth, 0,
				// childWidth + content.getMeasuredWidth(),
				// content.getMeasuredHeight());
				// }

				// TODO: leftToRight
				int width = getRight() - getLeft();
				content.measure(MeasureSpec.makeMeasureSpec(width,
						MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
						getBottom() - getTop(), MeasureSpec.EXACTLY));
				content.layout(0, 0, content.getMeasuredWidth(),
						content.getMeasuredHeight());
			}
		}
		// Try only once... we should really loop but it's not a big deal
		// if the draw was cancelled, it will only be temporary anyway
		content.getViewTreeObserver().dispatchOnPreDraw();

		// Creating the cached bitmap
		content.buildDrawingCache();

		// content.setVisibility(View.GONE);
	}

	private void stopTracking() {
		mHandle.setPressed(false);
		mTracking = false;

		if (mOnDrawerScrollListener != null) {
			mOnDrawerScrollListener.onScrollEnded();
		}

		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private void doAnimation() {
		boolean willOpen = false;
		boolean willClose = false;

		if (mAnimating) {
			incrementAnimation();

			switch (mOrientation) {
			case ORIENTATION_BOTTOM_UP: {
				willOpen = (mAnimationPosition < 0);
				willClose = (mAnimationPosition >= (getHeight() - 1));
				break;
			}
			case ORIENTATION_LEFT_TO_RIGHT: {
				willOpen = (mAnimationPosition >= (getWidth() - 1));
				willClose = (mAnimationPosition < 0);
				break;
			}
			case ORIENTATION_RIGHT_TO_LEFT: {
				willOpen = (mAnimationPosition < 0);
				willClose = (mAnimationPosition >= (getWidth() - 1));
				break;
			}
			}

			if (willOpen) {
				mAnimating = false;
				openDrawer();
			} else if (willClose) {
				mAnimating = false;
				closeDrawer();
			} else {
				moveHandle((int) mAnimationPosition);
				mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
				mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE),
						mCurrentAnimationTime);
			}
		}
	}

	private void incrementAnimation() {
		long now = SystemClock.uptimeMillis();
		float t = (now - mAnimationLastTime) / 1000.0f; // ms -> s
		final float position = mAnimationPosition;
		final float v = mAnimatedVelocity; // px/s
		final float a = mAnimatedAcceleration; // px/s/s
		mAnimationPosition = position + (v * t) + (0.5f * a * t * t); // px
		mAnimatedVelocity = v + (a * t); // px/s
		mAnimationLastTime = now; // ms
	}

	/**
	 * Toggles the drawer open and close. Takes effect immediately.
	 * 
	 * @see #open()
	 * @see #close()
	 * @see #animateClose()
	 * @see #animateOpen()
	 * @see #animateToggle()
	 */
	public void toggle() {
		if (!mExpanded) {
			openDrawer();
		} else {
			closeDrawer();
		}

		refresh();
	}

	/**
	 * Toggles the drawer open and close with an animation.
	 * 
	 * @see #open()
	 * @see #close()
	 * @see #animateClose()
	 * @see #animateOpen()
	 * @see #toggle()
	 */
	public void animateToggle() {
		if (!mExpanded) {
			animateOpen();
		} else {
			animateClose();
		}
	}

	/**
	 * Opens the drawer immediately.
	 * 
	 * @see #toggle()
	 * @see #close()
	 * @see #animateOpen()
	 */
	public void open() {
		openDrawer();

		refresh();

		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
	}

	/**
	 * Closes the drawer immediately.
	 * 
	 * @see #toggle()
	 * @see #open()
	 * @see #animateClose()
	 */
	public void close() {
		closeDrawer();

		refresh();
	}

	private void refresh() {
		invalidate();
		requestLayout();
	}

	/**
	 * Closes the drawer with an animation.
	 * 
	 * @see #close()
	 * @see #open()
	 * @see #animateOpen()
	 * @see #animateToggle()
	 * @see #toggle()
	 */
	public void animateClose() {
		prepareContent();

		final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
		if (scrollListener != null) {
			scrollListener.onScrollStarted();
		}

		animateClose(isVertical() ? mHandle.getTop() : mHandle.getLeft());

		if (scrollListener != null) {
			scrollListener.onScrollEnded();
		}
	}

	/**
	 * Opens the drawer with an animation.
	 * 
	 * @see #close()
	 * @see #open()
	 * @see #animateClose()
	 * @see #animateToggle()
	 * @see #toggle()
	 */
	public void animateOpen() {
		prepareContent();

		final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
		if (scrollListener != null) {
			scrollListener.onScrollStarted();
		}

		animateOpen(isVertical() ? mHandle.getTop() : mHandle.getLeft());

		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

		if (scrollListener != null) {
			scrollListener.onScrollEnded();
		}
	}

	// /**
	// * The reason for commenting out: backwards compatibility.<br>
	// * These methods are added in API level 14.
	// *
	// * @author tagorewang - 2013/7/24 commented out
	// */
	// @Override
	// public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
	// super.onInitializeAccessibilityEvent(event);
	// event.setClassName(SlidingDrawer.class.getName());
	// }
	//
	// @Override
	// public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info)
	// {
	// super.onInitializeAccessibilityNodeInfo(info);
	// info.setClassName(SlidingDrawer.class.getName());
	// }

	/**
	 * Note that we need always notify {@link OnDrawerCloseListener}, ensure
	 * changing back to the {@link Hotspot.OnHotspotListener} state, so we
	 * comment out the block in the fun. We are at the expense of some little
	 * performance.
	 */
	private void closeDrawer() {
		moveHandle(COLLAPSED_FULL_CLOSED);
		mContent.setVisibility(View.GONE);
		mContent.destroyDrawingCache();

		// if (!mExpanded) {
		// return;
		// }

		mExpanded = false;
		if (mOnDrawerCloseListener != null) {
			mOnDrawerCloseListener.onDrawerClosed();
		}
	}

	private void openDrawer() {
		moveHandle(EXPANDED_FULL_OPEN);
		mContent.setVisibility(View.VISIBLE);

		if (mExpanded) {
			return;
		}

		mExpanded = true;

		if (mOnDrawerOpenListener != null) {
			mOnDrawerOpenListener.onDrawerOpened();
		}
	}

	private boolean isHorizontal() {
		return !isVertical();
	}

	private boolean isVertical() {
		return mVertical;
	}

	private void checkOrientation() {
		String bits = Integer.toBinaryString(mOrientation);
		boolean isValidLen = (bits.length() <= 4);
		boolean isValidFlagBit = false;
		if (isValidLen) {
			final String rgEx = "0";
			isValidFlagBit = (bits.split(rgEx).length == 1);
		}
		if (isValidLen && isValidFlagBit
				&& mOrientation == ORIENTATION_TOP_DOWN) {
			throw new IllegalArgumentException(
					"The orientation attribute is required, or the assigned orientation is undefined.");
		}
	}

	private class DrawerToggler implements OnClickListener {
		public void onClick(View v) {
			if (mLocked) {
				return;
			}
			// mAllowSingleTap isn't relevant here; you're *always*
			// allowed to open/close the drawer by clicking with the
			// trackball.

			if (mAnimateOnClick) {
				animateToggle();
			} else {
				toggle();
			}
		}
	}

	private class SlidingHandler extends Handler {
		public void handleMessage(Message m) {
			switch (m.what) {
			case MSG_ANIMATE:
				doAnimation();
				break;
			}
		}
	}

	/**
	 * Fling to collapse gesture detector listener, initialized in
	 * {@link SlidingDrawer#SlidingDrawer(Context, AttributeSet, int)} and
	 * called back by {@link SlidingDrawer#mOnCollapseGestureListener} at
	 * {@link SlidingDrawer#onInterceptTouchEvent(MotionEvent)}.
	 * 
	 * @author tagorewang
	 */
	private class CollapseGestureDetector extends
			GestureDetector.SimpleOnGestureListener {

		/**
		 * @see #onScroll(MotionEvent, MotionEvent, float, float)
		 */
		private static final float MAXIMUM_MULTIPLE_CRITICAL = 4.0f;

		private boolean mOnScrollFilter;
		private boolean mOnScrollStarted;

		@Override
		public boolean onDown(MotionEvent e) {
			mOnScrollFilter = false;
			mOnScrollStarted = false;
			return false;
		}

		/**
		 * if {@value #MAXIMUM_MULTIPLE_CRITICAL} * |distanceX| < |distanceY| is
		 * false, we treat the scrolling as vertical scrolling, not horizontal;
		 * and we must scroll from right to left at first time, otherwise it
		 * won't handle any following events.
		 * 
		 * @see #MAXIMUM_MULTIPLE_CRITICAL
		 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (Math.abs(distanceY * MAXIMUM_MULTIPLE_CRITICAL) > Math
					.abs(distanceX)) {
				// if not horizontal scrolling
				return false;
			}

			if (!mOnScrollStarted && distanceX > 0) {
				mOnScrollStarted = true;
				if (!mOnScrollFilter) {
					onTouchEvent(e1);

				}
			} else {
				mOnScrollFilter = true;
			}

			if (!mOnScrollFilter && mOnScrollStarted) {
				return onTouchEvent(e2);
			} else {
				return false;
			}
		}

	}
}
