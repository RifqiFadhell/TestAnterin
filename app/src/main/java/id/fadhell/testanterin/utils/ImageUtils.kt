package id.fadhell.testanterin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yalantis.ucrop.UCrop
import id.fadhell.testanterin.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import android.widget.Toast
import android.view.View


class ImageUtils {

    companion object {

        const val IMAGE_NAME = "CroppedImage"

        fun convertUriToBitmap(uri: Uri, context: Context): Bitmap {
            val imageStream = context.getContentResolver().openInputStream(uri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            return selectedImage
        }

        fun encodeImage(bm: Bitmap): String {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b = baos.toByteArray()

            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        fun createCropActivity(context: Context, uri: Uri): UCrop {
            val uCrop = UCrop.of(uri, Uri.fromFile(
                    File(context.cacheDir, IMAGE_NAME + "-" + Calendar.getInstance().timeInMillis + ".jpg")))

            uCrop.withAspectRatio(1f, 1f)
            uCrop.withMaxResultSize(400, 400)

            val options = UCrop.Options()
            options.setHideBottomControls(true)
            options.setCropFrameColor(context.resources.getColor(R.color.vikingBlue))
            options.setCropGridColor(context.resources.getColor(R.color.vikingBlue))
            options.setToolbarColor(context.resources.getColor(R.color.vikingBlue))
            options.setStatusBarColor(context.resources.getColor(R.color.vikingBlue))
            uCrop.withOptions(options)

            return uCrop
        }

        fun loadImages(context: Context, imageView: ImageView, url: String?, isPlaceholder: Boolean) {
            Glide.with(context)
                    .applyDefaultRequestOptions(if (isPlaceholder) RequestOptions().placeholder(R.drawable.ic_avatar) else RequestOptions())
                    .load(url)
                    .into(imageView)
        }
    }
}
