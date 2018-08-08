package com.blue.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import com.blue.R


/**
 * 通用shape样式按钮
 */
class CommonShapeButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    /**
     * shape模式
     * 矩形（rectangle）、椭圆形(oval)、线形(line)、环形(ring)
     */
    private var shapeMode = 0
    
    private var fillColor = 0
    
    private var pressedColor = 0
    
    private var strokeColor = 0
    
    private var strokeWidth = 0
    
    private var cornerRad = 0 // Corner radius conflicts with AppCompatButton.cornerRadius
    /**
     * 圆角位置
     * topLeft、topRight、bottomRight、bottomLeft
     */
    private var cornerPosition = -1

    /**
     * 点击动效
     */
    private var activeEnable = false

    /**
     * 起始颜色
     */
    private var startColor = 0

    /**
     * 结束颜色
     */
    private var endColor = 0

    /**
     * 渐变方向
     * 0-GradientDrawable.Orientation.TOP_BOTTOM
     * 1-GradientDrawable.Orientation.LEFT_RIGHT
     */
    private var orientation = 0

    /**
     * drawable位置
     * -1-null、0-left、1-top、2-right、3-bottom
     */
    private var drawablePosition = -1

    /**
     * 普通shape样式
     */
    private val normalGradientDrawable: GradientDrawable by lazy { GradientDrawable() }
    /**
     * 按压shape样式
     */
    private val pressedGradientDrawable: GradientDrawable by lazy { GradientDrawable() }
    /**
     * shape样式集合
     */
    private val stateListDrawable: StateListDrawable by lazy { StateListDrawable() }
    // button内容总宽度
    private var contentWidth = 0f
    // button内容总高度
    private var contentHeight = 0f

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CommonShapeButton).apply {
            shapeMode = getInt(R.styleable.CommonShapeButton_csb_shapeMode, 0)
            fillColor = getColor(R.styleable.CommonShapeButton_csb_fillColor, 0xFFFFFFFF.toInt())
            pressedColor = getColor(R.styleable.CommonShapeButton_csb_pressedColor, 0xFF666666.toInt())
            strokeColor = getColor(R.styleable.CommonShapeButton_csb_strokeColor, 0)
            strokeWidth = getDimensionPixelSize(R.styleable.CommonShapeButton_csb_strokeWidth, 0)
            cornerRad = getDimensionPixelSize(R.styleable.CommonShapeButton_csb_cornerRadius, 0)
            cornerPosition = getInt(R.styleable.CommonShapeButton_csb_cornerPosition, -1)
            activeEnable = getBoolean(R.styleable.CommonShapeButton_csb_activeEnable, false)
            drawablePosition = getInt(R.styleable.CommonShapeButton_csb_drawablePosition, -1)
            startColor = getColor(R.styleable.CommonShapeButton_csb_startColor, 0xFFFFFFFF.toInt())
            endColor = getColor(R.styleable.CommonShapeButton_csb_endColor, 0xFFFFFFFF.toInt())
            orientation = getColor(R.styleable.CommonShapeButton_csb_orientation, 0)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 初始化normal状态
        with(normalGradientDrawable) {
            // 渐变色
            if (startColor != 0xFFFFFFFF.toInt() && endColor != 0xFFFFFFFF.toInt()) {
                colors = intArrayOf(startColor, endColor)
                when (orientation) {
                    0 -> orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    1 -> orientation = GradientDrawable.Orientation.LEFT_RIGHT
                }
            }
            // 填充色
            else {
                setColor(fillColor)
            }
            when (shapeMode) {
                0 -> shape = GradientDrawable.RECTANGLE
                1 -> shape = GradientDrawable.OVAL
                2 -> shape = GradientDrawable.LINE
                3 -> shape = GradientDrawable.RING
            }
            // 统一设置圆角半径
            if (cornerPosition == -1) {
                cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, cornerRad.toFloat(), resources.displayMetrics)
            }
            // 根据圆角位置设置圆角半径
            else {
                cornerRadii = getCornerRadiusByPosition()
            }
            // 默认的透明边框不绘制,否则会导致没有阴影
            if (strokeColor != 0) {
                setStroke(strokeWidth, strokeColor)
            }
        }

        // 是否开启点击动效
        background = if (activeEnable) {
            // 5.0以上水波纹效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(ColorStateList.valueOf(pressedColor), normalGradientDrawable, null)
            }
            // 5.0以下变色效果
            else {
                // 初始化pressed状态
                with(pressedGradientDrawable) {
                    setColor(pressedColor)
                    when (shapeMode) {
                        0 -> shape = GradientDrawable.RECTANGLE
                        1 -> shape = GradientDrawable.OVAL
                        2 -> shape = GradientDrawable.LINE
                        3 -> shape = GradientDrawable.RING
                    }
                    cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, cornerRad.toFloat(), resources.displayMetrics)
                    setStroke(strokeWidth, strokeColor)
                }

                // 注意此处的add顺序，normal必须在最后一个，否则其他状态无效
                // 设置pressed状态
                stateListDrawable.apply {
                    addState(intArrayOf(android.R.attr.state_pressed), pressedGradientDrawable)
                    // 设置normal状态
                    addState(intArrayOf(), normalGradientDrawable)
                }
            }
        } else {
            normalGradientDrawable
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 如果xml中配置了drawable则设置padding让文字移动到边缘与drawable靠在一起
        // button中配置的drawable默认贴着边缘
        if (drawablePosition > -1) {
            compoundDrawables?.let {
                val drawable: Drawable? = compoundDrawables[drawablePosition]
                drawable?.let {
                    // 图片间距
                    val drawablePadding = compoundDrawablePadding
                    when (drawablePosition) {
                        // 左右drawable
                        0, 2 -> {
                            // 图片宽度
                            val drawableWidth = it.intrinsicWidth
                            // 获取文字宽度
                            val textWidth = paint.measureText(text.toString())
                            // 内容总宽度
                            contentWidth = textWidth + drawableWidth + drawablePadding
                            val rightPadding = (width - contentWidth).toInt()
                            // 图片和文字全部靠在左侧
                            setPadding(0, 0, rightPadding, 0)
                        }
                        // 上下drawable
                        1, 3 -> {
                            // 图片高度
                            val drawableHeight = it.intrinsicHeight
                            // 获取文字高度
                            val fm = paint.fontMetrics
                            // 单行高度
                            val singeLineHeight = Math.ceil(fm.descent.toDouble() - fm.ascent.toDouble()).toFloat()
                            // 总的行间距
                            val totalLineSpaceHeight = (lineCount - 1) * lineSpacingExtra
                            val textHeight = singeLineHeight * lineCount + totalLineSpaceHeight
                            // 内容总高度
                            contentHeight = textHeight + drawableHeight + drawablePadding
                            // 图片和文字全部靠在上侧
                            val bottomPadding = (height - contentHeight).toInt()
                            setPadding(0, 0, 0, bottomPadding)
                        }
                    }
                }

            }
        }
        // 内容居中
        gravity = Gravity.CENTER
        // 可点击
        isClickable = true
        changeTintContextWrapperToActivity()
    }

    override fun onDraw(canvas: Canvas) {
        // 让图片和文字居中
        when {
            contentWidth > 0 && (drawablePosition == 0 || drawablePosition == 2) -> canvas.translate((width - contentWidth) / 2, 0f)
            contentHeight > 0 && (drawablePosition == 1 || drawablePosition == 3) -> canvas.translate(0f, (height - contentHeight) / 2)
        }
        super.onDraw(canvas)
    }

    /**
     * 从support23.3.0开始View中的getContext方法返回的是TintContextWrapper而不再是Activity
     * 如果使用xml注册onClick属性，就会通过反射到Activity中去找对应的方法
     * 5.0以下系统会反射到TintContextWrapper中去找对应的方法，程序直接crash
     * 所以这里需要针对5.0以下系统单独处理View中的getContext返回值
     */
    private fun changeTintContextWrapperToActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getActivity()?.let {
                var clazz: Class<*>? = this::class.java
                while (clazz != null) {
                    try {
                        val field = clazz.getDeclaredField("mContext")
                        field.isAccessible = true
                        field.set(this, it)
                        break
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    clazz = clazz.superclass
                }
            }
        }
    }

    /**
     * 从context中得到真正的activity
     */
    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    /**
     * 根据圆角位置获取圆角半径
     */
    private fun getCornerRadiusByPosition(): FloatArray {
        val result = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val cornerRadius = cornerRad.toFloat()
        if (containsFlag(cornerPosition, TOP_LEFT)) {
            result[0] = cornerRadius
            result[1] = cornerRadius
        }
        if (containsFlag(cornerPosition, TOP_RIGHT)) {
            result[2] = cornerRadius
            result[3] = cornerRadius
        }
        if (containsFlag(cornerPosition, BOTTOM_RIGHT)) {
            result[4] = cornerRadius
            result[5] = cornerRadius
        }
        if (containsFlag(cornerPosition, BOTTOM_LEFT)) {
            result[6] = cornerRadius
            result[7] = cornerRadius
        }
        return result
    }

    /**
     * 是否包含对应flag
     */
    private fun containsFlag(flagSet: Int, flag: Int): Boolean {
        return flagSet or flag == flagSet
    }

    private companion object {
        val TOP_LEFT = 1
        val TOP_RIGHT = 2
        val BOTTOM_RIGHT = 4
        val BOTTOM_LEFT = 8
    }

}
