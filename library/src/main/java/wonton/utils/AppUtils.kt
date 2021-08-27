package wonton.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.ConnectivityManagerCompat
import kotlinx.atomicfu.atomic
import splitties.init.appCtx

class AppUtils {
    //for add in application class
    private lateinit var callback: ConnectivityManager.NetworkCallback
    private lateinit var broadcastReceiver: BroadcastReceiver
    private val activityCount = atomic(0)

    companion object {
        val appVersion by lazy { PackageInfoCompat.getLongVersionCode(
            appCtx.packageManager.getPackageInfo(
                appCtx.packageName, PackageManager.GET_CONFIGURATIONS)) }
        val isDebug by lazy { appCtx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0 }
        var wifiConnected = false
        var mobileConnected = false
        var networkConnected = false
    }

    @Suppress("DEPRECATION")
    private fun initNetworkValue() {
        appCtx.getSystemService<ConnectivityManager>()?.let {
            networkConnected =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) it.isDefaultNetworkActive
                else it.activeNetworkInfo?.isConnected ?: false
            mobileConnected =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    it.getNetworkCapabilities(it.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        ?: false
                else it.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
            wifiConnected =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    it.getNetworkCapabilities(it.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        ?: false
                else it.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }

    @Suppress("DEPRECATION")
    private fun initNetworkWatcher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            callback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    networkConnected =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    wifiConnected =
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    mobileConnected =
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                }

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    networkConnected = true
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    networkConnected = false
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    networkConnected = false
                    wifiConnected = false
                    mobileConnected = false
                }
            }
            appCtx.getSystemService<ConnectivityManager>()
                ?.registerDefaultNetworkCallback(callback)
        } else {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val connectivityManager =
                        (context ?: appCtx).getSystemService<ConnectivityManager>()
                    val networkInfo = connectivityManager?.activeNetworkInfo
                    val fallbackNetworkInfo =
                        connectivityManager?.let { manager ->
                            intent?.let {
                                ConnectivityManagerCompat.getNetworkInfoFromBroadcast(manager, it)
                            }
                        }
                    // a set of dirty workarounds
                    networkConnected =
                        networkInfo?.isConnected == true ||
                                networkInfo?.isConnected != fallbackNetworkInfo?.isConnected ||
                                networkInfo ?: fallbackNetworkInfo != null
                    wifiConnected = (networkInfo
                        ?: fallbackNetworkInfo)?.type == ConnectivityManager.TYPE_WIFI
                    mobileConnected = (networkInfo
                        ?: fallbackNetworkInfo)?.type == ConnectivityManager.TYPE_MOBILE
                }
            }
            appCtx.registerReceiver(
                broadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
                Manifest.permission.ACCESS_NETWORK_STATE,
                null
            )
        }
    }

    private fun initApplicationEnd(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityCount.plusAssign(1)
            }

            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

            override fun onActivityDestroyed(activity: Activity) {
                activityCount.minusAssign(1)
                if (activityCount.value == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        application.applicationContext.getSystemService<ConnectivityManager>()
                            ?.unregisterNetworkCallback(callback)
                    } else {
                        application.applicationContext.unregisterReceiver(broadcastReceiver)
                    }
                    //WXSDKManager.getInstance().notifyTrimMemory()
                    //WXSDKManager.getInstance().notifySerializeCodeCache()
                    //MMKV.onExit()
                }
            }

        })
    }
}