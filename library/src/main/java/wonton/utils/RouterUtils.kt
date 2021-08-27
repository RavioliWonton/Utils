package wonton.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.IdRes
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import splitties.init.appCtx

inline fun <reified T : Activity> Context.startActivityCompat(options: ActivityOptionsCompat? = null, intentBuilder: Intent.() -> Unit = {}) =
    Intent(this, T::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .apply(intentBuilder).let {
            if (it.resolveActivity(packageManager) != null)
                ContextCompat.startActivity(this, it,
                    (options ?: ActivityOptionsCompat.makeBasic()).toBundle())
        }


inline fun <reified T : Activity> Activity.startActivityCompat(options: ActivityOptionsCompat? = null, intentBuilder: Intent.() -> Unit = {}) =
    Intent(this, T::class.java).apply(intentBuilder).let {
        if (it.resolveActivity(packageManager) != null)
            ContextCompat.startActivity(this, it,
                (options ?: ActivityOptionsCompat.makeBasic()).toBundle())
    }


inline fun <reified T : Activity> Fragment.startActivityCompat(options: ActivityOptionsCompat? = null, intentBuilder: Intent.() -> Unit = {}) =
    Intent(requireActivity(), T::class.java).apply(intentBuilder).let {
        if (it.resolveActivity(requireActivity().packageManager) != null)
            ContextCompat.startActivity(requireContext(), it,
                (options ?: ActivityOptionsCompat.makeBasic()).toBundle())
    }


inline fun Context.startActionCompat(action: String, uri: Uri? = null, options: ActivityOptionsCompat? = null, intentBuilder: Intent.() -> Unit = {}) =
    Intent(action, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).apply(intentBuilder).let {
        if (it.resolveActivity(packageManager) != null)
            ContextCompat.startActivity(this, it,
                (options ?: ActivityOptionsCompat.makeBasic()).toBundle())
    }

inline fun Activity.startActionCompat(action: String, uri: Uri? = null, options: ActivityOptionsCompat? = null, intentBuilder: Intent.() -> Unit = {}) =
    Intent(action, uri).apply(intentBuilder).let {
        if (it.resolveActivity(packageManager) != null)
            ContextCompat.startActivity(this, it,
                (options ?: ActivityOptionsCompat.makeBasic()).toBundle())
    }


inline fun Fragment.startActionCompat(action: String, uri: Uri? = null, options: ActivityOptionsCompat? = null, intentBuilder: Intent.() -> Unit = {}) =
    Intent(action, uri).apply(intentBuilder).let {
        if (it.resolveActivity(requireActivity().packageManager) != null)
            ContextCompat.startActivity(requireContext(), it,
                (options ?: ActivityOptionsCompat.makeBasic()).toBundle())
    }


inline fun <reified O : Activity> ActivityResultLauncher<Intent>.launchActivity(options: ActivityOptionsCompat? = null, intentBuilder: Intent.() -> Unit = {}) =
    launch(
        Intent(appCtx, O::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).apply(intentBuilder),
        options ?: ActivityOptionsCompat.makeBasic())

fun Activity.navigate(@IdRes id: Int, direction: NavDirections) = findNavController(id).navigate(direction)
fun Activity.navigate(@IdRes id: Int, @IdRes direction: Int) = findNavController(id).navigate(direction)

fun Fragment.navigate(direction: NavDirections) = findNavController().navigate(direction)
fun Fragment.navigate(@IdRes direction: Int) = findNavController().navigate(direction)