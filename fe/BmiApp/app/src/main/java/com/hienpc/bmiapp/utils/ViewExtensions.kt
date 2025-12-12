package com.hienpc.bmiapp.utils

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.hienpc.bmiapp.R

/**
 * Extension functions để handle UI states
 */

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

/**
 * Show loading shimmer
 */
fun ViewGroup.showLoadingShimmer() {
    // Remove existing shimmer if any
    findViewWithTag<View>("shimmer_loading")?.let {
        this.removeView(it)
    }
    
    // Inflate shimmer layout
    val shimmerView = View.inflate(context, R.layout.layout_loading_shimmer, null)
    shimmerView.tag = "shimmer_loading"
    this.addView(shimmerView)
}

fun ViewGroup.hideLoadingShimmer() {
    findViewWithTag<View>("shimmer_loading")?.let {
        this.removeView(it)
    }
}

/**
 * Show empty state
 */
fun ViewGroup.showEmptyState(
    title: String,
    message: String,
    iconRes: Int? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    // Remove existing empty state if any
    findViewWithTag<View>("empty_state")?.let {
        this.removeView(it)
    }
    
    // Inflate empty state layout
    val emptyView = View.inflate(context, R.layout.layout_empty_state, null)
    emptyView.tag = "empty_state"
    
    emptyView.findViewById<TextView>(R.id.tvEmptyTitle).text = title
    emptyView.findViewById<TextView>(R.id.tvEmptyMessage).text = message
    
    iconRes?.let {
        emptyView.findViewById<ImageView>(R.id.ivEmptyIcon).setImageResource(it)
    }
    
    val actionButton = emptyView.findViewById<Button>(R.id.btnEmptyAction)
    if (actionText != null && onActionClick != null) {
        actionButton.text = actionText
        actionButton.isVisible = true
        actionButton.setOnClickListener { onActionClick() }
    } else {
        actionButton.isVisible = false
    }
    
    this.addView(emptyView)
}

fun ViewGroup.hideEmptyState() {
    findViewWithTag<View>("empty_state")?.let {
        this.removeView(it)
    }
}

/**
 * Show error state
 */
fun ViewGroup.showErrorState(
    message: String,
    onRetryClick: () -> Unit
) {
    // Remove existing error state if any
    findViewWithTag<View>("error_state")?.let {
        this.removeView(it)
    }
    
    // Inflate error state layout
    val errorView = View.inflate(context, R.layout.layout_error_state, null)
    errorView.tag = "error_state"
    
    errorView.findViewById<TextView>(R.id.tvErrorMessage).text = message
    errorView.findViewById<Button>(R.id.btnRetry).setOnClickListener {
        onRetryClick()
    }
    
    this.addView(errorView)
}

fun ViewGroup.hideErrorState() {
    findViewWithTag<View>("error_state")?.let {
        this.removeView(it)
    }
}

/**
 * Clear all states (loading, empty, error)
 */
fun ViewGroup.clearAllStates() {
    hideLoadingShimmer()
    hideEmptyState()
    hideErrorState()
}

