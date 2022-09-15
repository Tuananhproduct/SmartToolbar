package com.example.customtoolbar

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources

/**
 * Created on 3/2/2018.
 */

class SmartToolbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {
    private var mMainLayout: View? = null
    private var smartToolbarLayout: LinearLayout? = null
    private var mMainLayoutParams: ViewGroup.LayoutParams? = null
    private var smtbLayoutParams: ViewGroup.LayoutParams? = null
    private var statusBarLayoutParams: ViewGroup.LayoutParams? = null
    private var activityRootView: ViewGroup? = null
    private var vStatusBar: View? = null
    private var imgLeftBtn: ImageView? = null
    private var imgRightBtn: ImageView? = null
    private var imgTitleIcon: ImageView? = null
    private var txtTitleText: TextView? = null
    private val DEFAULT_SHOW_LEFT_BUTTON = true
    private val DEFAULT_SHOW_RIGHT_BUTTON = true
    private val DEFAULT_TOOLBAR_BACKGROUND = Color.parseColor("#3F51B5")
    private val DEFAULT_TITLE_TEXT_COLOR = Color.BLACK
    private val DEFAULT_ERROR_NUM = -1
    private val DEFAULT_STATUS_BAR_HEIGHT = 24
    private val DEFAULT_TITLE_TEXT = "SampleTitleText"
    private val TAG = SmartToolbar::class.java.simpleName
    private var leftBtnIcon: Drawable?
    private var rightBtnIcon: Drawable?
    private var titleIcon: Drawable?
    private var titleText: String? = null
    private var toolbarBackgroundColor = 0
    private var toolbarBackgroundDrawable: Drawable? = null
    private var statusBackgroundColor = 0
    private var statusBackgroundDrawable: Drawable? = null
    private var isToolbarColorTypeDrawalbe = false
    private var isStatusBarHasOwnColor = false
    private var isInitializing = true
    private var layoutHeight = 0

    private val COLOR_STATUS_WHITE = 1
    private val COLOR_STATUS_BLACK = 2

    private fun hideActionBar(context: Context) {
        if (context is AppCompatActivity) {
            if (context.supportActionBar != null) context.supportActionBar!!
                .hide()
        } else if (context is Activity) {
            if (context.actionBar != null) context.actionBar!!
                .hide()
        }
    }

    private fun init(attrs: AttributeSet?) {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mMainLayout = inflate(context, R.layout.smart_toolbar_layout, this)
        vStatusBar = inflate(context, R.layout.smtb_status_bar_layout, null)
        vStatusBar?.bringToFront()
        smartToolbarLayout = findViewById(R.id.smtb_container)
        imgLeftBtn = findViewById(R.id.actionbar_left_btn)
        imgRightBtn = findViewById(R.id.actionbar_right_btn)
        txtTitleText = findViewById(R.id.actionbar_title)
        imgTitleIcon = findViewById(R.id.actionbar_title_image)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SmartToolbar)
        val isShowLeftBtn = typedArray.getBoolean(
            R.styleable.SmartToolbar_smtb_showLeftBtn,
            DEFAULT_SHOW_LEFT_BUTTON
        )
        setShowLeftButton(isShowLeftBtn)

        val leftBtnIconResId =
            typedArray.getResourceId(R.styleable.SmartToolbar_smtb_leftBtnIcon, DEFAULT_ERROR_NUM)
        if (leftBtnIconResId > DEFAULT_ERROR_NUM) setLeftButtonIcon(
            AppCompatResources.getDrawable(
                context, leftBtnIconResId
            )
        )
        val isShowRightBtn = typedArray.getBoolean(
            R.styleable.SmartToolbar_smtb_showRightBtn,
            DEFAULT_SHOW_RIGHT_BUTTON
        )
        setShowRightButton(isShowRightBtn)
        val rightBtnIconResId =
            typedArray.getResourceId(R.styleable.SmartToolbar_smtb_rightBtnIcon, DEFAULT_ERROR_NUM)
        if (rightBtnIconResId > DEFAULT_ERROR_NUM) setRightButtonIcon(
            AppCompatResources.getDrawable(
                context, rightBtnIconResId
            )
        )
        val titleText = typedArray.getString(R.styleable.SmartToolbar_smtb_titleText)
        setTitleText(titleText)
        val fontText = typedArray.getString(R.styleable.SmartToolbar_smtb_fontPath)
        setFont(fontText)
        val titleColor =
            typedArray.getInt(R.styleable.SmartToolbar_smtb_titleColor, DEFAULT_TITLE_TEXT_COLOR)
        setTitleTextColor(titleColor)
        val isShowTitleIcon =
            typedArray.getBoolean(R.styleable.SmartToolbar_smtb_showTitleIcon, false)
        setShowTitleIcon(isShowTitleIcon)
        val titleIconResId =
            typedArray.getResourceId(R.styleable.SmartToolbar_smtb_titleIcon, DEFAULT_ERROR_NUM)
        if (titleIconResId > DEFAULT_ERROR_NUM) setTitleIcon(
            AppCompatResources.getDrawable(
                context,
                titleIconResId
            )
        )
        initBackground(typedArray)
        initStatusBarColor(typedArray)
        val leftBtnIconWidth = typedArray.getDimension(
            R.styleable.SmartToolbar_smtb_leftBtnIconWidth,
            resources.getDimension(R.dimen.default_icon_width)
        )
        val leftBtnIconHeight = typedArray.getDimension(
            R.styleable.SmartToolbar_smtb_leftBtnIconHeight,
            resources.getDimension(R.dimen.default_icon_height)
        )
        updateLayoutWidth(imgLeftBtn, leftBtnIconWidth.toInt())
        updateLayoutHeight(imgLeftBtn, leftBtnIconHeight.toInt())
        val rightBtnIconWidth = typedArray.getDimension(
            R.styleable.SmartToolbar_smtb_rightBtnIconWidth,
            resources.getDimension(R.dimen.default_icon_width)
        )
        val rightBtnIconHeight = typedArray.getDimension(
            R.styleable.SmartToolbar_smtb_rightBtnIconHeight,
            resources.getDimension(R.dimen.default_icon_height)
        )
        updateLayoutWidth(imgRightBtn, rightBtnIconWidth.toInt())
        updateLayoutHeight(imgRightBtn, rightBtnIconHeight.toInt())
        val titleIconWidth = typedArray.getDimension(
            R.styleable.SmartToolbar_smtb_titleIconWidth,
            resources.getDimension(R.dimen.default_icon_width)
        )
        val titleIconHeight = typedArray.getDimension(
            R.styleable.SmartToolbar_smtb_titleIconHeight,
            resources.getDimension(R.dimen.default_icon_height)
        )
        updateLayoutWidth(imgTitleIcon, titleIconWidth.toInt())
        updateLayoutHeight(imgTitleIcon, titleIconHeight.toInt())
        val titleTextSize = typedArray.getDimension(
            R.styleable.SmartToolbar_smtb_titleTextSize,
            resources.getDimension(R.dimen.default_text_size)
        )
        setTitleTextSize(pxToDp(titleTextSize.toInt()))
        isInitializing = false
        typedArray.recycle()
    }

    private fun isTypeReference(typedArray: TypedArray, res: Int): Boolean {
        val type = typedArray.getType(res)
        return type == 3
    }

    fun setColorTextStatusBar(activity: Activity, resColor: Int){
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        if (resColor == COLOR_STATUS_WHITE){
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }else{
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


    }

    private fun setViewVisibility(view: View?, status: Boolean) {
        if (status) {
            view!!.visibility = VISIBLE
        } else {
            view!!.visibility = GONE
        }
    }

    fun setFont(resFont: String?) {
        if (resFont != null) {
            val typeFace = Typeface.createFromAsset(context.assets, resFont)
            txtTitleText?.typeface = typeFace
        }
    }

    fun setShowLeftButton(isShowLeftBtn: Boolean) {
        setViewVisibility(imgLeftBtn, isShowLeftBtn)
    }

    fun setLeftButtonIcon(leftBtnIcon: Drawable?) {
        if (leftBtnIcon != null) {
            this.leftBtnIcon = leftBtnIcon
            imgLeftBtn!!.setImageDrawable(leftBtnIcon)
        }
    }

    fun setShowRightButton(isShowRightBtn: Boolean) {
        setViewVisibility(imgRightBtn, isShowRightBtn)
    }

    fun setRightButtonIcon(rightBtnIcon: Drawable?) {
        if (rightBtnIcon != null) {
            this.rightBtnIcon = rightBtnIcon
            imgRightBtn!!.setImageDrawable(rightBtnIcon)
        }
    }

    fun setTitleText(titleText: String?) {
        if (titleText != null) {
            this.titleText = titleText
            txtTitleText!!.text = titleText
        } else {
            txtTitleText!!.text = DEFAULT_TITLE_TEXT
        }
    }

    fun setTitleTextColor(color: Int) {
        if (color != 0) txtTitleText!!.setTextColor(color) else txtTitleText!!.setTextColor(
            DEFAULT_TITLE_TEXT_COLOR
        )
    }

    fun setShowTitleIcon(isShowTitleIcon: Boolean) {
        if (isShowTitleIcon) {
            txtTitleText!!.visibility = GONE
            imgTitleIcon!!.visibility = VISIBLE
        } else {
            txtTitleText!!.visibility = VISIBLE
            imgTitleIcon!!.visibility = GONE
        }
    }

    fun setTitleIcon(titleIcon: Drawable?) {
        if (titleIcon != null) {
            this.titleIcon = titleIcon
            imgTitleIcon!!.setImageDrawable(titleIcon)
        }
    }

    override fun setBackgroundColor(color: Int) {
        if (!isInitializing) toolbarBackgroundColor = color
        smartToolbarLayout!!.setBackgroundColor(color)
        if (!isStatusBarHasOwnColor && !isInitializing) setStatusBarColor(color)
    }

    override fun setBackground(background: Drawable) {
        if (smartToolbarLayout != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (!isInitializing) toolbarBackgroundDrawable = background
                smartToolbarLayout!!.background = background
                if (!isStatusBarHasOwnColor && !isInitializing) setStatusBarColor(background)
            }
        }
    }

    private fun initBackground(typedArray: TypedArray) {
        val drawableResId =
            typedArray.getResourceId(R.styleable.SmartToolbar_android_background, DEFAULT_ERROR_NUM)
        if (drawableResId > DEFAULT_ERROR_NUM) {
            isToolbarColorTypeDrawalbe = true
            toolbarBackgroundDrawable = AppCompatResources.getDrawable(context, drawableResId)
            background = toolbarBackgroundDrawable!!
        } else {
            isToolbarColorTypeDrawalbe = false
            toolbarBackgroundColor = typedArray.getColor(
                R.styleable.SmartToolbar_android_background,
                DEFAULT_TOOLBAR_BACKGROUND
            )
            setBackgroundColor(toolbarBackgroundColor)
        }
    }

    fun setStatusBarColor(color: Int) {
        if (!isInitializing) {
            statusBackgroundColor = color
            isStatusBarHasOwnColor = true
        }
        vStatusBar!!.setBackgroundColor(color)
    }

    fun setStatusBarColor(color: Drawable?) {
        if (!isInitializing) {
            statusBackgroundDrawable = color
            isStatusBarHasOwnColor = true
        }
        vStatusBar!!.background = color
    }

    private fun initStatusBarColor(typedArray: TypedArray) {
        val drawableResId = typedArray.getResourceId(
            R.styleable.SmartToolbar_smtb_statusBarColor,
            DEFAULT_ERROR_NUM
        )
        if (drawableResId > DEFAULT_ERROR_NUM) {
            isStatusBarHasOwnColor = true
            statusBackgroundDrawable = AppCompatResources.getDrawable(context, drawableResId)
            setStatusBarColor(statusBackgroundDrawable)
        } else {
            statusBackgroundColor = typedArray.getColor(
                R.styleable.SmartToolbar_smtb_statusBarColor,
                toolbarBackgroundColor
            )
            if (statusBackgroundColor != toolbarBackgroundColor) {
                isStatusBarHasOwnColor = true
                setStatusBarColor(statusBackgroundColor)
            } else {
                isStatusBarHasOwnColor = false
                if (isToolbarColorTypeDrawalbe) {
                    setStatusBarColor(toolbarBackgroundDrawable)
                }
            }
        }
    }

    fun showCustomStatusBar(activity: Activity) {
        val window = activity.window
        setColorTextStatusBar(activity, COLOR_STATUS_BLACK)

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (!isStatusBarHasOwnColor && isToolbarColorTypeDrawalbe) {
            setStatusBarColor(DEFAULT_TOOLBAR_BACKGROUND)
        }
        mMainLayoutParams = mMainLayout!!.layoutParams
        smtbLayoutParams = smartToolbarLayout!!.layoutParams
        val viewTreeObserver = mMainLayout!!.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mMainLayout!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    layoutHeight =
                        mMainLayout!!.measuredHeight + dpToPx(DEFAULT_STATUS_BAR_HEIGHT)
                    mMainLayoutParams?.height = layoutHeight
                    mMainLayout!!.layoutParams = mMainLayoutParams
                    smtbLayoutParams?.height = layoutHeight
                    smartToolbarLayout!!.layoutParams = smtbLayoutParams
                    smartToolbarLayout!!.setPadding(
                        smartToolbarLayout!!.paddingLeft,
                        dpToPx(DEFAULT_STATUS_BAR_HEIGHT),
                        smartToolbarLayout!!.paddingRight,
                        smartToolbarLayout!!.paddingBottom
                    )
                    invalidate()
                }
            })
        }
        val viewTreeObserverStatusBar = vStatusBar!!.viewTreeObserver
        if (viewTreeObserverStatusBar.isAlive) {
            viewTreeObserverStatusBar.addOnGlobalLayoutListener(object :
                OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    vStatusBar!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    statusBarLayoutParams = vStatusBar!!.layoutParams
                    statusBarLayoutParams?.height = dpToPx(DEFAULT_STATUS_BAR_HEIGHT)
                    vStatusBar!!.layoutParams = statusBarLayoutParams
                    invalidate()
                }
            })
        }
        activityRootView = activity.findViewById(android.R.id.content)
        activityRootView?.addView(vStatusBar)
    }

    private fun updateLayoutWidth(view: View?, newWidth: Int) {
        view!!.layoutParams.width = newWidth
    }

    private fun updateLayoutHeight(view: View?, newHeight: Int) {
        view!!.layoutParams.height = newHeight
    }

    fun setLeftButtonIconWidth(width: Int) {
        updateLayoutWidth(imgLeftBtn, dpToPx(width))
    }

    fun setLeftButtonIconHeight(height: Int) {
        updateLayoutHeight(imgLeftBtn, dpToPx(height))
    }

    fun setRightButtonIconWidth(width: Int) {
        updateLayoutWidth(imgRightBtn, dpToPx(width))
    }

    fun setRightButtonIconHeight(height: Int) {
        updateLayoutHeight(imgRightBtn, dpToPx(height))
    }

    fun setTitleIconWidth(width: Int) {
        updateLayoutWidth(imgTitleIcon, dpToPx(width))
    }

    fun setTitleIconHeight(height: Int) {
        updateLayoutHeight(imgTitleIcon, dpToPx(height))
    }

    fun setTitleTextSize(textSize: Int) {
        txtTitleText!!.textSize = textSize.toFloat()
    }

    fun setOnLeftButtonClickListener(listener: OnClickListener?) {
        imgLeftBtn!!.setOnClickListener(listener)
    }

    fun setOnRightButtonClickListener(listener: OnClickListener?) {
        imgRightBtn!!.setOnClickListener(listener)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    init {
        leftBtnIcon = AppCompatResources.getDrawable(context, R.drawable.ic_arrow_back_white_24dp)
        rightBtnIcon = AppCompatResources.getDrawable(context, R.drawable.ic_close_white_24dp)
        titleIcon = AppCompatResources.getDrawable(context, R.drawable.ic_close_white_24dp)
        hideActionBar(context)
        init(attrs)
    }
}