package com.blue.view.java;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Gravity;

import com.blue.R;

import java.lang.reflect.Field;

/**
 * add java implementation
 * Created by blue on 2018/10/23.
 */

public class CommonShapeButton extends AppCompatButton {

    private int TOP_LEFT = 1;
    private int TOP_RIGHT = 2;
    private int BOTTOM_RIGHT = 4;
    private int BOTTOM_LEFT = 8;

    /**
     * shape模式
     * 矩形（rectangle）、椭圆形(oval)、线形(line)、环形(ring)
     */
    private int mShapeMode = 0;

    /**
     * 填充颜色
     */
    private int mFillColor = 0;

    /**
     * 按压颜色
     */
    private int mPressedColor = 0;

    /**
     * 描边颜色
     */
    private int mStrokeColor = 0;

    /**
     * 描边宽度
     */
    private int mStrokeWidth = 0;

    /**
     * 圆角半径
     */
    private int mCornerRadius = 0;
    /**
     * 圆角位置
     */
    private int mCornerPosition = 0;

    /**
     * 点击动效
     */
    private boolean mActiveEnable = false;

    /**
     * 起始颜色
     */
    private int mStartColor = 0;

    /**
     * 结束颜色
     */
    private int mEndColor = 0;

    /**
     * 渐变方向
     * 0-GradientDrawable.Orientation.TOP_BOTTOM
     * 1-GradientDrawable.Orientation.LEFT_RIGHT
     */
    private int mOrientation = 0;

    /**
     * drawable位置
     * -1-null、0-left、1-top、2-right、3-bottom
     */
    private int mDrawablePosition = -1;

    /**
     * 普通shape样式
     */
    private GradientDrawable normalGradientDrawable = new GradientDrawable();
    /**
     * 按压shape样式
     */
    private GradientDrawable pressedGradientDrawable = new GradientDrawable();
    /**
     * shape样式集合
     */
    private StateListDrawable stateListDrawable = new StateListDrawable();
    // button内容总宽度
    private float contentWidth = 0f;
    // button内容总高度
    private float contentHeight = 0f;

    public CommonShapeButton(Context context) {
        super(context);
    }

    public CommonShapeButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs);
    }

    public CommonShapeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommonShapeButton);
        mShapeMode = typedArray.getInt(R.styleable.CommonShapeButton_csb_shapeMode, 0);
        mFillColor = typedArray.getColor(R.styleable.CommonShapeButton_csb_fillColor, 0xFFFFFFFF);
        mPressedColor = typedArray.getColor(R.styleable.CommonShapeButton_csb_pressedColor, 0xFF666666);
        mStrokeColor = typedArray.getColor(R.styleable.CommonShapeButton_csb_strokeColor, 0);
        mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.CommonShapeButton_csb_strokeWidth, 0);
        mCornerRadius = typedArray.getDimensionPixelSize(R.styleable.CommonShapeButton_csb_cornerRadius, 0);
        mCornerPosition = typedArray.getInt(R.styleable.CommonShapeButton_csb_cornerPosition, -1);
        mActiveEnable = typedArray.getBoolean(R.styleable.CommonShapeButton_csb_activeEnable, false);
        mDrawablePosition = typedArray.getInt(R.styleable.CommonShapeButton_csb_drawablePosition, -1);
        mStartColor = typedArray.getColor(R.styleable.CommonShapeButton_csb_startColor, 0xFFFFFFFF);
        mEndColor = typedArray.getColor(R.styleable.CommonShapeButton_csb_endColor, 0xFFFFFFFF);
        mOrientation = typedArray.getColor(R.styleable.CommonShapeButton_csb_orientation, 0);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 初始化normal状态
        // 渐变色
        if (mStartColor != 0xFFFFFFFF && mEndColor != 0xFFFFFFFF) {
            normalGradientDrawable.setColors(new int[]{mStartColor, mEndColor});
            if (mOrientation == 0) {
                normalGradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            } else if (mOrientation == 1) {
                normalGradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            }
        }
        // 填充色
        else {
            normalGradientDrawable.setColor(mFillColor);
        }
        if (mShapeMode == 0) {
            normalGradientDrawable.setShape(GradientDrawable.RECTANGLE);
        } else if (mShapeMode == 1) {
            normalGradientDrawable.setShape(GradientDrawable.OVAL);
        } else if (mShapeMode == 2) {
            normalGradientDrawable.setShape(GradientDrawable.LINE);
        } else if (mShapeMode == 3) {
            normalGradientDrawable.setShape(GradientDrawable.RING);
        }
        // 统一设置圆角半径
        if (mCornerPosition == -1) {
            normalGradientDrawable.setCornerRadius(mCornerRadius);
        }
        // 根据圆角位置设置圆角半径
        else {
            normalGradientDrawable.setCornerRadii(getCornerRadiusByPosition());
        }
        // 默认的透明边框不绘制,否则会导致没有阴影
        if (mStrokeColor != 0) {
            normalGradientDrawable.setStroke(mStrokeWidth, mStrokeColor);
        }

        // 是否开启点击动效
        if (mActiveEnable) {
            // 5.0以上水波纹效果
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setBackground(new RippleDrawable(ColorStateList.valueOf(mPressedColor), normalGradientDrawable, null));
            }
            // 5.0以下变色效果
            else {
                // 初始化pressed状态
                pressedGradientDrawable.setColor(mPressedColor);
                if (mShapeMode == 0) {
                    pressedGradientDrawable.setShape(GradientDrawable.RECTANGLE);
                } else if (mShapeMode == 1) {
                    pressedGradientDrawable.setShape(GradientDrawable.OVAL);
                } else if (mShapeMode == 2) {
                    pressedGradientDrawable.setShape(GradientDrawable.LINE);
                } else if (mShapeMode == 3) {
                    pressedGradientDrawable.setShape(GradientDrawable.RING);
                }
                pressedGradientDrawable.setCornerRadius(mCornerRadius);
                pressedGradientDrawable.setStroke(mStrokeWidth, mStrokeColor);

                // 注意此处的add顺序，normal必须在最后一个，否则其他状态无效
                // 设置pressed状态
                stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedGradientDrawable);
                // 设置normal状态
                stateListDrawable.addState(new int[]{}, normalGradientDrawable);
                setBackground(stateListDrawable);
            }
        } else {
            setBackground(normalGradientDrawable);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 如果xml中配置了drawable则设置padding让文字移动到边缘与drawable靠在一起
        // button中配置的drawable默认贴着边缘
        if (mDrawablePosition > -1) {
            Drawable[] drawables = getCompoundDrawables();
            Drawable drawable = drawables[mDrawablePosition];
            // 图片间距
            int drawablePadding = getCompoundDrawablePadding();
            // 左右drawable
            if (mDrawablePosition == 0 || mDrawablePosition == 2) {
                // 图片宽度
                int drawableWidth = drawable.getIntrinsicWidth();
                // 获取文字宽度
                float textWidth = getPaint().measureText(getText().toString());
                // 内容总宽度
                contentWidth = textWidth + drawableWidth + drawablePadding;
                int rightPadding = (int) (getWidth() - contentWidth);
                // 图片和文字全部靠在左侧
                setPadding(0, 0, rightPadding, 0);
            }
            // 上下drawable
            else if (mDrawablePosition == 1 || mDrawablePosition == 3) {
                // 图片高度
                int drawableHeight = drawable.getIntrinsicHeight();
                // 获取文字高度
                Paint.FontMetrics fm = getPaint().getFontMetrics();
                // 单行高度
                float singeLineHeight = (float) Math.ceil(fm.descent - fm.ascent);
                // 总的行间距
                float totalLineSpaceHeight = (getLineCount() - 1) * getLineSpacingExtra();
                float textHeight = singeLineHeight * getLineCount() + totalLineSpaceHeight;
                // 内容总高度
                contentHeight = textHeight + drawableHeight + drawablePadding;
                // 图片和文字全部靠在上侧
                int bottomPadding = (int) (getHeight() - contentHeight);
                setPadding(0, 0, 0, bottomPadding);
            }
        }
        // 内容居中
        setGravity(Gravity.CENTER);
        // 可点击
        if (mActiveEnable) {
            setClickable(true);
        }
        changeTintContextWrapperToActivity();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 让图片和文字居中
        if (contentWidth > 0 && (mDrawablePosition == 0 || mDrawablePosition == 2)) {
            canvas.translate((getWidth() - contentWidth) / 2, 0f);
        } else if (contentHeight > 0 && (mDrawablePosition == 1 || mDrawablePosition == 3)) {
            canvas.translate(0f, (getHeight() - contentHeight) / 2);
        }
        super.onDraw(canvas);
    }

    /**
     * 从support23.3.0开始View中的getContext方法返回的是TintContextWrapper而不再是Activity
     * 如果使用xml注册onClick属性，就会通过反射到Activity中去找对应的方法
     * 5.0以下系统会反射到TintContextWrapper中去找对应的方法，程序直接crash
     * 所以这里需要针对5.0以下系统单独处理View中的getContext返回值
     */
    private void changeTintContextWrapperToActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Activity activity = getActivity();
            Class clazz = this.getClass();
            while (clazz != null) {
                try {
                    Field field = clazz.getDeclaredField("mContext");
                    field.setAccessible(true);
                    field.set(this, activity);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                clazz = clazz.getSuperclass();
            }
        }
    }

    /**
     * 从context中得到真正的activity
     */
    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /**
     * 根据圆角位置获取圆角半径
     */
    private float[] getCornerRadiusByPosition() {
        float[] result = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
        if (containsFlag(mCornerPosition, TOP_LEFT)) {
            result[0] = mCornerRadius;
            result[1] = mCornerRadius;
        }
        if (containsFlag(mCornerPosition, TOP_RIGHT)) {
            result[2] = mCornerRadius;
            result[3] = mCornerRadius;
        }
        if (containsFlag(mCornerPosition, BOTTOM_RIGHT)) {
            result[4] = mCornerRadius;
            result[5] = mCornerRadius;
        }
        if (containsFlag(mCornerPosition, BOTTOM_LEFT)) {
            result[6] = mCornerRadius;
            result[7] = mCornerRadius;
        }
        return result;
    }

    /**
     * 是否包含对应flag
     * 按位或
     */
    private boolean containsFlag(int flagSet, int flag) {
        return (flagSet | flag) == flagSet;
    }
}
