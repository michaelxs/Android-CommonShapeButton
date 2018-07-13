# CommonShapeButton
[中文文档](https://blog.csdn.net/xsxsxs827/article/details/80708637)<br>
To remove all shape files from the project, provide a generic shape style button.<br>
![](https://github.com/michaelxs/CommonShapeButton/blob/master/screenshots/show.gif)<br>
## Custom attribute
```xml
<declare-styleable name="CommonShapeButton">
    <attr name="csb_shapeMode" format="enum">
        <enum name="rectangle" value="0" />
        <enum name="oval" value="1" />
        <enum name="line" value="2" />
        <enum name="ring" value="3" />
    </attr>
    <attr name="csb_fillColor" format="color" />
    <attr name="csb_pressedColor" format="color" />
    <attr name="csb_strokeColor" format="color" />
    <attr name="csb_strokeWidth" format="dimension" />
    <attr name="csb_cornerRadius" format="dimension" />
    <attr name="csb_cornerPosition">
        <flag name="topLeft" value="1" />
        <flag name="topRight" value="2" />
        <flag name="bottomRight" value="4" />
        <flag name="bottomLeft" value="8" />
    </attr>
    <attr name="csb_activeEnable" format="boolean" />
    <attr name="csb_drawablePosition" format="enum">
        <enum name="left" value="0" />
        <enum name="top" value="1" />
        <enum name="right" value="2" />
        <enum name="bottom" value="3" />
    </attr>
    <attr name="csb_startColor" format="color" />
    <attr name="csb_endColor" format="color" />
    <attr name="csb_orientation" format="enum">
        <enum name="TOP_BOTTOM" value="0" />
        <enum name="LEFT_RIGHT" value="1" />
    </attr>
</declare-styleable>
```
## How to use
Text style
```xml
<com.blue.view.CommonShapeButton
    android:layout_width="300dp"
    android:layout_height="50dp"
    android:layout_margin="10dp"
    android:text="text+corner+fill"
    android:textColor="#fff"
    app:csb_cornerRadius="50dp"
    app:csb_fillColor="#00bc71" />
```
Button style
```xml
<com.blue.view.CommonShapeButton
    style="@style/CommonShapeButtonStyle"
    android:layout_width="300dp"
    android:layout_height="50dp"
    android:layout_margin="10dp"
    android:text="button+fill+stroke+ripple"
    android:textColor="#fff"
    app:csb_activeEnable="true"
    app:csb_fillColor="#00bc71"
    app:csb_strokeColor="#000"
    app:csb_strokeWidth="1dp" />
```
## License
[Apache-2.0](https://opensource.org/licenses/Apache-2.0)
