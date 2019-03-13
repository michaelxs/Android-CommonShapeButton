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

    private companion object {
        val TOP_LEFT = 1
        val TOP_RIGHT = 2
        val BOTTOM_RIGHT = 4
        val BOTTOM_LEFT = 8
    }

    /**
     * shape模式
     * 矩形（rectangle）、椭圆形(oval)、线形(line)、环形(ring)
     */
    private var mShapeMode = 0

    /**
     * 填充颜色
     */
    private var mFillColor = 0

    /**
     * 按压颜色
     */
    private var mPressedColor = 0

    /**
     * 描边颜色
     */
    private var mStrokeColor = 0

    /**
     * 描边宽度
     */
    private var mStrokeWidth = 0

    /**
     * 圆角半径
     */
    private var mCornerRadius = 0
    /**
     * 圆角位置
     */
    private var mCornerPosition = 0

    /**
     * 点击动效
     */
    private var mActiveEnable = false

    /**
     * 起始颜色
     */
    private var mStartColor = 0

    /**
     * 结束颜色
     */
    private var mEndColor = 0

    /**
     * 渐变方向
     * 0-GradientDrawable.Orientation.TOP_BOTTOM
     * 1-GradientDrawable.Orientation.LEFT_RIGHT
     */
    private var mOrientation = 0

    /**
     * drawable位置
     * -1-null、0-left、1-top、2-right、3-bottom
     */
    private var mDrawablePosition = -1

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
            mShapeMode = getInt(R.styleable.CommonShapeButton_csb_shapeMode, 0)
            mFillColor = getColor(R.styleable.CommonShapeButton_csb_fillColor, 0xFFFFFFFF.toInt())
            mPressedColor = getColor(R.styleable.CommonShapeButton_csb_pressedColor, 0xFF666666.toInt())
            mStrokeColor = getColor(R.styleable.CommonShapeButton_csb_strokeColor, 0)
            mStrokeWidth = getDimensionPixelSize(R.styleable.CommonShapeButton_csb_strokeWidth, 0)
            mCornerRadius = getDimensionPixelSize(R.styleable.CommonShapeButton_csb_cornerRadius, 0)
            mCornerPosition = getInt(R.styleable.CommonShapeButton_csb_cornerPosition, -1)
            mActiveEnable = getBoolean(R.styleable.CommonShapeButton_csb_activeEnable, false)
            mDrawablePosition = getInt(R.styleable.CommonShapeButton_csb_drawablePosition, -1)
            mStartColor = getColor(R.styleable.CommonShapeButton_csb_startColor, 0xFFFFFFFF.toInt())
            mEndColor = getColor(R.styleable.CommonShapeButton_csb_endColor, 0xFFFFFFFF.toInt())
            mOrientation = getColor(R.styleable.CommonShapeButton_csb_orientation, 0)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 初始化normal状态
        with(normalGradientDrawable) {
            // 渐变色
            if (mStartColor != 0xFFFFFFFF.toInt() && mEndColor != 0xFFFFFFFF.toInt()) {
                colors = intArrayOf(mStartColor, mEndColor)
                when (mOrientation) {
                    0 -> orientation = GradientDrawable.Orientation.TOP_BOTTOM
                    1 -> orientation = GradientDrawable.Orientation.LEFT_RIGHT
                }
            }
            // 填充色
            else {
                setColor(mFillColor)
            }
            when (mShapeMode) {
                0 -> shape = GradientDrawable.RECTANGLE
                1 -> shape = GradientDrawable.OVAL
                2 -> shape = GradientDrawable.LINE
                3 -> shape = GradientDrawable.RING
            }
            // 统一设置圆角半径
            if (mCornerPosition == -1) {
                cornerRadius = mCornerRadius.toFloat()
            }
            // 根据圆角位置设置圆角半径
            else {
                cornerRadii = getCornerRadiusByPosition()
            }
            // 默认的透明边框不绘制,否则会导致没有阴影
            if (mStrokeColor != 0) {
                setStroke(mStrokeWidth, mStrokeColor)
            }
        }

        // 是否开启点击动效
        background = if (mActiveEnable) {
            // 5.0以上水波纹效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RippleDrawable(ColorStateList.valueOf(mPressedColor), normalGradientDrawable, null)
            }
            // 5.0以下变色效果
            else {
                // 初始化pressed状态
                with(pressedGradientDrawable) {
                    setColor(mPressedColor)
                    when (mShapeMode) {
                        0 -> shape = GradientDrawable.RECTANGLE
                        1 -> shape = GradientDrawable.OVAL
                        2 -> shape = GradientDrawable.LINE
                        3 -> shape = GradientDrawable.RING
                    }
                    cornerRadius = mCornerRadius.toFloat()
                    setStroke(mStrokeWidth, mStrokeColor)
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
        if (mDrawablePosition > -1) {
            compoundDrawables?.let {
                val drawable: Drawable? = compoundDrawables[mDrawablePosition]
                drawable?.let {
                    // 图片间距
                    val drawablePadding = compoundDrawablePadding
                    when (mDrawablePosition) {
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
        if (mActiveEnable) {
            isClickable = true
        }
        changeTintContextWrapperToActivity()
    }

    override fun onDraw(canvas: Canvas) {
        // 让图片和文字居中
        when {
            contentWidth > 0 && (mDrawablePosition == 0 || mDrawablePosition == 2) -> canvas.translate((width - contentWidth) / 2, 0f)
            contentHeight > 0 && (mDrawablePosition == 1 || mDrawablePosition == 3) -> canvas.translate(0f, (height - contentHeight) / 2)
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
        val cornerRadius = mCornerRadius.toFloat()
        if (containsFlag(mCornerPosition, TOP_LEFT)) {
            result[0] = cornerRadius
            result[1] = cornerRadius
        }
        if (containsFlag(mCornerPosition, TOP_RIGHT)) {
            result[2] = cornerRadius
            result[3] = cornerRadius
        }
        if (containsFlag(mCornerPosition, BOTTOM_RIGHT)) {
            result[4] = cornerRadius
            result[5] = cornerRadius
        }
        if (containsFlag(mCornerPosition, BOTTOM_LEFT)) {
            result[6] = cornerRadius
            result[7] = cornerRadius
        }
        return result
    }

    /**
     * 是否包含对应flag
     * 按位或
     */
    private fun containsFlag(flagSet: Int, flag: Int): Boolean {
        return flagSet or flag == flagSet
    }
}