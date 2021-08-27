package wonton.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

class ActivityRequestPermissionLauncher(caller: ComponentActivity, function: (granted: Boolean) -> Unit) : PermissionRequestLauncher<ComponentActivity>(caller, function)

class FragmentRequestPermissionLauncher(caller: Fragment, function: (granted: Boolean) -> Unit) : PermissionRequestLauncher<Fragment>(caller, function)

abstract class PermissionRequestLauncher<T>(caller: T, private val function: (granted: Boolean) -> Unit)
        where T : LifecycleOwner, T : ActivityResultCaller {
    private var value: ActivityResultLauncher<String>? = null

    init {
        caller.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> value = caller.registerForActivityResult(
                        ActivityResultContracts.RequestPermission()) { function.invoke(it) }
                    Lifecycle.Event.ON_DESTROY -> value = null
                    else -> Unit
                }
            }
        })
    }

    operator fun getValue(caller: T, property: KProperty<*>): ActivityResultLauncher<String> {
        return value!!
    }
}

class ActivityRequestPermissionGroupLauncher(caller: ComponentActivity, function: (granted: Boolean) -> Unit) : PermissionRequestLauncher<ComponentActivity>(caller, function)

class FragmentRequestPermissionGroupLauncher(caller: Fragment, function: (granted: Boolean) -> Unit) : PermissionRequestLauncher<Fragment>(caller, function)

abstract class PermissionGroupRequestLauncher<T>(caller: T, private val function: (grantedMap: ConcurrentHashMap<String, Boolean>) -> Unit)
        where T : LifecycleOwner, T : ActivityResultCaller {
    private var value: ActivityResultLauncher<Array<String>>? = null

    init {
        caller.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> value = caller.registerForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()) { function.invoke(
                        ConcurrentHashMap(it)
                    ) }
                    Lifecycle.Event.ON_DESTROY -> value = null
                    else -> Unit
                }
            }
        })
    }

    operator fun getValue(caller: T, property: KProperty<*>): ActivityResultLauncher<Array<String>> {
        return value!!
    }
}

fun Context.checkPermission(permission: String) = PermissionChecker.checkSelfPermission(this, permission)