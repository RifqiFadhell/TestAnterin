package id.fadhell.testanterin.utils

import android.content.Context
import android.widget.ImageView
import id.fadhell.testanterin.utils.ImageUtils.Companion.loadImages

fun ImageView.loadImage(context: Context, url: String?, isPlaceholder: Boolean) {
    loadImages(context, this, url, isPlaceholder)
}