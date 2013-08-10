SlidingDrawer
=============

The class android.widget.SlidingDrawer was deprecated in API level 17, not supported anymore. See more on http://developer.android.com/reference/android/widget/SlidingDrawer.html . I have done my own implementation on the source code for the Android Open Source Project. For now, it have supported all the orientations, which are top-down, bottom-up, left-to-right and right-to-left.<br>

TODO:<br>
1. design guide and api docs, see more on http://wtao.me/reference/android/widget/SlidingDrawer.html;<br>
2. unit test;<br>
3. support set handle view dynamically;<br>
4. expand SlidingDrawer.OnDrawerScrollListener, support listen to more states, f.e. add onPreScrollStarted() where you can set handle view before scrolling;<br>
5. support self-define animation interpolator, for now it's just only a quadratic function;<br>

android.widget.SlidingDrawer 控件从 API17 开始已经被弃用，不再被支持。详情请访问官方文档 http://developer.android.com/reference/android/widget/SlidingDrawer.html 。我基于安卓开源项目实现了自己的 me.wtao.widget.SlidingDrawer 控件。目前，它已经支持各个方向，包括从顶部下拉、从底部上拉、从左向右拉和从右向左拉。<br>

TODO:<br>
1、编写设计文档和 API 接口文档，详情请访问我的博客 http://wtao.me/reference/android/widget/SlidingDrawer.html;<br>
2、单元测试；<br>
3、支持动态设置把手(handle)的视图；<br>
4、扩展接口 SlidingDrawer.OnDrawerScrollListener， 支持监听更多状态，比如增加方法 onPreScrollStarted()，你可以在拉动之前设置把手(handle)视图；<br>
5、支持自定义动画插值器，目前它只是简单的二次函数；<br>
