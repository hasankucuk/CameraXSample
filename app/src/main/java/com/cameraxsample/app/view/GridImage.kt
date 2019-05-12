package com.cameraxsample.app.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView


class GridImage : AppCompatImageView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = this.measuredWidth
        setMeasuredDimension(w, w)
    }
}