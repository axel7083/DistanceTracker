package com.github.axel7083.distancetracker.core.service

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import com.github.axel7083.distancetracker.R
import com.github.axel7083.distancetracker.databinding.OverlayViewBinding

class OverlayPipCustomView : FrameLayout {

    private var _binding: OverlayViewBinding? = null
    private val binding get() = _binding!!

    private var onClosed: () -> Unit = {}
    private var onHided: () -> Unit = {}

    var isMoving: Boolean = false

    constructor(ctx: Context) : super(ctx) {
        initView(ctx)
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        setAttributes(attrs)
        initView(ctx)
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle) {
        setAttributes(attrs)
        initView(ctx)
    }

    private fun initView(context: Context) {
        _binding = OverlayViewBinding.bind(
            inflate(
                ContextThemeWrapper(context, R.style.Theme_BackgroundLocationTracker),
                R.layout.overlay_view,
                this
            ).requireViewById(R.id.constraints_root)
        )
        setListeners()
    }

    private fun setAttributes(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.OverlayCustomView,
            0, 0).apply {
            try {
                /*playerViewSize = getInteger(R.styleable.OverlayCustomView_ov_player_size, 0)
                playerType = getInteger(R.styleable.OverlayCustomView_ov_size_changeable, 0)
                sizeChangeable = getBoolean(R.styleable.OverlayCustomView_ov_size_changeable, true)*/
            } finally {
                recycle()
            }
        }
    }

    fun getTouchView() = binding.touchView

    fun setProgress(value: Int, max: Int = 100) {
        binding.progressBar.apply {
            this.isIndeterminate = value == -1
            this.progress = value
            this.max = max
        }
    }

    fun setTitle(str: String) {
        binding.titleTv.text = str
    }

    private fun setListeners() {
        binding.imageClose.setOnClickListener {
            onClosed.invoke()
        }

        binding.imageHide.setOnClickListener {
            onHided.invoke()
        }
    }

    fun setOnClosedListener(
        onClosed: () -> Unit
    ) {
        this.onClosed = onClosed
    }

    fun setOnHidedListener(
        onHided: () -> Unit
    ) {
        this.onHided = onHided
    }

}