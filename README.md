SlidingDrawer
=============

The class android.widget.SlidingDrawer was deprecated in API level 17, not supported anymore. See more on http://developer.android.com/reference/android/widget/SlidingDrawer.html . I have done my own implementation on the source code for the Android Open Source Project. For now, it have supported all the orientations, which are top-down, bottom-up, left-to-right and right-to-left.

TODO:
1. design guide and api docs, see more on wtao.me;
2. unit test;
3. support set handle view dynamically;
4. expand SlidingDrawer.OnDrawerScrollListener, support listen to more states, f.e. add onPreScrollStarted() where you can set handle view before scrolling;
5. support self-define animation interpolator, for now it's just only a quadratic function;
