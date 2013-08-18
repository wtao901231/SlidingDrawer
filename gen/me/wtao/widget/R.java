/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * aapt tool from the resource data it found.  It
 * should not be modified by hand.
 */

package me.wtao.widget;

public final class R {
    public static final class attr {
        /** 
             Indicates whether the drawer can be opened/closed by a single tap
             on the handle.  (If false, the user must drag or fling, or click
             using the trackball, to open/close the drawer.)  Default is true.
        
         <p>Must be a boolean value, either "<code>true</code>" or "<code>false</code>".
<p>This may also be a reference to a resource (in the form
"<code>@[<i>package</i>:]<i>type</i>:<i>name</i></code>") or
theme attribute (in the form
"<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>")
containing a value of this type.
         */
        public static final int allowSingleTap=0x7f010003;
        /** 
             Indicates whether the drawer should be opened/closed with an animation
             when the user clicks the handle. Default is true.
        
         <p>Must be a boolean value, either "<code>true</code>" or "<code>false</code>".
<p>This may also be a reference to a resource (in the form
"<code>@[<i>package</i>:]<i>type</i>:<i>name</i></code>") or
theme attribute (in the form
"<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>")
containing a value of this type.
         */
        public static final int animateOnClick=0x7f010004;
        /**  Identifier for the child that represents the drawer's content. 
         <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
         */
        public static final int content=0x7f010001;
        /**  Identifier for the child that represents the drawer's handle. 
         <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
         */
        public static final int handle=0x7f010000;
        /**  Orientation of the SlidingDrawer. 
         <p>Must be one of the following constant values.</p>
<table>
<colgroup align="left" />
<colgroup align="left" />
<colgroup align="left" />
<tr><th>Constant</th><th>Value</th><th>Description</th></tr>
<tr><td><code>topDown</code></td><td>1</td><td></td></tr>
<tr><td><code>bottomUp</code></td><td>2</td><td></td></tr>
<tr><td><code>leftToRight</code></td><td>4</td><td></td></tr>
<tr><td><code>rightToLeft</code></td><td>8</td><td></td></tr>
</table>
         */
        public static final int orientation=0x7f010002;
    }
    public static final class color {
        /**  A really bright Holo shade of blue 
         */
        public static final int holo_blue_bright=0x7f05000d;
        /**  A dark Holo shade of blue 
         */
        public static final int holo_blue_dark=0x7f050006;
        /**  A light Holo shade of blue 
         */
        public static final int holo_blue_light=0x7f050000;
        /**  A dark Holo shade of green 
         */
        public static final int holo_green_dark=0x7f050007;
        /**  A light Holo shade of green 
         */
        public static final int holo_green_light=0x7f050002;
        /**  A dark Holo shade of orange 
         */
        public static final int holo_orange_dark=0x7f05000c;
        /**  A light Holo shade of orange 
         */
        public static final int holo_orange_light=0x7f05000a;
        /**  A Holo shade of purple 
         */
        public static final int holo_purple=0x7f050009;
        /**  A dark Holo shade of red 
         */
        public static final int holo_red_dark=0x7f050008;
        /**  A light Holo shade of red 
         */
        public static final int holo_red_light=0x7f050004;
        public static final int theme_color=0x7f05000e;
        public static final int transparent_blue_light=0x7f050001;
        public static final int transparent_green_light=0x7f050003;
        public static final int transparent_orange_light=0x7f05000b;
        public static final int transparent_red_light=0x7f050005;
    }
    public static final class dimen {
        /**  Default screen margins, per the Android Design guidelines. 

         Customize dimensions originally defined in res/values/dimens.xml (such as
         screen margins) for sw720dp devices (e.g. 10" tablets) in landscape here.
    
         */
        public static final int activity_horizontal_margin=0x7f060000;
        public static final int activity_vertical_margin=0x7f060001;
        public static final int bottom_offset=0x7f06000a;
        public static final int handle_height=0x7f060008;
        public static final int handle_width=0x7f060007;
        public static final int shape_corner_radius=0x7f060002;
        public static final int shape_stroke_width=0x7f060003;
        public static final int sliding_drawer_outter_height=0x7f060005;
        public static final int sliding_drawer_outter_width=0x7f060006;
        public static final int top_offset=0x7f060009;
        public static final int view_margin=0x7f060004;
    }
    public static final class drawable {
        public static final int bk_holo_blue_light=0x7f020000;
        public static final int bk_holo_red_light=0x7f020001;
        public static final int ic_launcher=0x7f020002;
    }
    public static final class id {
        public static final int action_settings=0x7f040008;
        public static final int bottomUp=0x7f040001;
        public static final int content=0x7f040006;
        public static final int drawer_outter_layout=0x7f040004;
        public static final int drawer_right_to_left=0x7f040005;
        public static final int handle=0x7f040007;
        public static final int leftToRight=0x7f040002;
        public static final int rightToLeft=0x7f040003;
        public static final int topDown=0x7f040000;
    }
    public static final class layout {
        public static final int activity_demo=0x7f030000;
    }
    public static final class menu {
        public static final int demo=0x7f090000;
    }
    public static final class string {
        public static final int action_settings=0x7f070001;
        public static final int app_name=0x7f070000;
        public static final int content_prompt=0x7f070002;
        public static final int handle_prompt=0x7f070003;
    }
    public static final class style {
        /** 
        Base application theme, dependent on API level. This theme is replaced
        by AppBaseTheme from res/values-vXX/styles.xml on newer devices.
    

            Theme customizations available in newer API levels can go in
            res/values-vXX/styles.xml, while customizations related to
            backward-compatibility can go here.
        

        Base application theme for API 11+. This theme completely replaces
        AppBaseTheme from res/values/styles.xml on API 11+ devices.
    
 API 11 theme customizations can go here. 

        Base application theme for API 14+. This theme completely replaces
        AppBaseTheme from BOTH res/values/styles.xml and
        res/values-v11/styles.xml on API 14+ devices.
    
 API 14 theme customizations can go here. 
         */
        public static final int AppBaseTheme=0x7f080000;
        /**  Application theme. 
 All customizations that are NOT specific to a particular API-level can go here. 
         */
        public static final int AppTheme=0x7f080001;
    }
    public static final class styleable {
        /** 
      SlidingDrawer specific attributes. These attributes are used to configure
         a SlidingDrawer from XML.




    
           <p>Includes the following attributes:</p>
           <table>
           <colgroup align="left" />
           <colgroup align="left" />
           <tr><th>Attribute</th><th>Description</th></tr>
           <tr><td><code>{@link #SlidingDrawer_allowSingleTap me.wtao.widget:allowSingleTap}</code></td><td>
             Indicates whether the drawer can be opened/closed by a single tap
             on the handle.</td></tr>
           <tr><td><code>{@link #SlidingDrawer_animateOnClick me.wtao.widget:animateOnClick}</code></td><td>
             Indicates whether the drawer should be opened/closed with an animation
             when the user clicks the handle.</td></tr>
           <tr><td><code>{@link #SlidingDrawer_content me.wtao.widget:content}</code></td><td> Identifier for the child that represents the drawer's content.</td></tr>
           <tr><td><code>{@link #SlidingDrawer_handle me.wtao.widget:handle}</code></td><td> Identifier for the child that represents the drawer's handle.</td></tr>
           <tr><td><code>{@link #SlidingDrawer_orientation me.wtao.widget:orientation}</code></td><td> Orientation of the SlidingDrawer.</td></tr>
           </table>
           @see #SlidingDrawer_allowSingleTap
           @see #SlidingDrawer_animateOnClick
           @see #SlidingDrawer_content
           @see #SlidingDrawer_handle
           @see #SlidingDrawer_orientation
         */
        public static final int[] SlidingDrawer = {
            0x7f010000, 0x7f010001, 0x7f010002, 0x7f010003,
            0x7f010004
        };
        /**
          <p>
          @attr description
          
             Indicates whether the drawer can be opened/closed by a single tap
             on the handle.  (If false, the user must drag or fling, or click
             using the trackball, to open/close the drawer.)  Default is true.
        


          <p>Must be a boolean value, either "<code>true</code>" or "<code>false</code>".
<p>This may also be a reference to a resource (in the form
"<code>@[<i>package</i>:]<i>type</i>:<i>name</i></code>") or
theme attribute (in the form
"<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>")
containing a value of this type.
          <p>This is a private symbol.
          @attr name me.wtao.widget:allowSingleTap
        */
        public static final int SlidingDrawer_allowSingleTap = 3;
        /**
          <p>
          @attr description
          
             Indicates whether the drawer should be opened/closed with an animation
             when the user clicks the handle. Default is true.
        


          <p>Must be a boolean value, either "<code>true</code>" or "<code>false</code>".
<p>This may also be a reference to a resource (in the form
"<code>@[<i>package</i>:]<i>type</i>:<i>name</i></code>") or
theme attribute (in the form
"<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>")
containing a value of this type.
          <p>This is a private symbol.
          @attr name me.wtao.widget:animateOnClick
        */
        public static final int SlidingDrawer_animateOnClick = 4;
        /**
          <p>
          @attr description
           Identifier for the child that represents the drawer's content. 


          <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
          <p>This is a private symbol.
          @attr name me.wtao.widget:content
        */
        public static final int SlidingDrawer_content = 1;
        /**
          <p>
          @attr description
           Identifier for the child that represents the drawer's handle. 


          <p>Must be a reference to another resource, in the form "<code>@[+][<i>package</i>:]<i>type</i>:<i>name</i></code>"
or to a theme attribute in the form "<code>?[<i>package</i>:][<i>type</i>:]<i>name</i></code>".
          <p>This is a private symbol.
          @attr name me.wtao.widget:handle
        */
        public static final int SlidingDrawer_handle = 0;
        /**
          <p>
          @attr description
           Orientation of the SlidingDrawer. 


          <p>Must be one of the following constant values.</p>
<table>
<colgroup align="left" />
<colgroup align="left" />
<colgroup align="left" />
<tr><th>Constant</th><th>Value</th><th>Description</th></tr>
<tr><td><code>topDown</code></td><td>1</td><td></td></tr>
<tr><td><code>bottomUp</code></td><td>2</td><td></td></tr>
<tr><td><code>leftToRight</code></td><td>4</td><td></td></tr>
<tr><td><code>rightToLeft</code></td><td>8</td><td></td></tr>
</table>
          <p>This is a private symbol.
          @attr name me.wtao.widget:orientation
        */
        public static final int SlidingDrawer_orientation = 2;
    };
}
