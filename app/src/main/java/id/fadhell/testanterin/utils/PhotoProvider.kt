package id.fadhell.testanterin.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import com.yalantis.ucrop.UCrop
import id.fadhell.testanterin.BuildConfig

class PhotoProvider : ContentProvider() {

  val CONTENT_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".PhotoProvider"

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    return null
  }

  override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
    return null
  }

  override fun onCreate(): Boolean {
    return true
  }

  override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
    return 0
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
    return 0
  }

  override fun getType(uri: Uri): String? {
    return null
  }

  fun getPhotoUri(file: Intent): Uri {
    val outputUri = UCrop.getOutput(file)
    val builder = Uri.Builder()
      .authority(CONTENT_PROVIDER_AUTHORITY)
      .scheme("file")
      .path(outputUri?.path)
      .query(outputUri?.query)
      .fragment(outputUri?.fragment)

    return builder.build()
  }
}