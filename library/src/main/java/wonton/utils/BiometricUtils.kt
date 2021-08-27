package wonton.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

fun Context.canStrongBiometricAuthenticate() = BiometricManager.from(this)
    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

fun Context.canBiometricAuthenticate() = BiometricManager.from(this)
    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS

@RequiresApi(Build.VERSION_CODES.M)
fun getCryptoObject(alias: String) =
    BiometricPrompt.CryptoObject(
        Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .apply {
            val password = "anbao".toCharArray()
            val keyStore = KeyStore.getInstance(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) "AndroidKeyStore" else "AndroidCAStore")
            keyStore.load { KeyStore.PasswordProtection(password) }
            if (keyStore.getKey(alias, password) == null) {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) "AndroidKeyStore" else "AndroidCAStore")
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build())
                keyGenerator.generateKey()
            }
            val key = keyStore.getKey(alias, password) as SecretKey
            init(
                Cipher.ENCRYPT_MODE,
                key,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) SecureRandom.getInstanceStrong() else SecureRandom()
            )
        })

fun AppCompatActivity.biometricAuthenticate(info: BiometricPrompt.PromptInfo, crypto: BiometricPrompt.CryptoObject? = null,
                                            onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
                                            onFail: (() -> Unit)? = null,
                                            onError: ((errorCode: Int, errString: CharSequence) -> Unit)? = null) =
    crypto?.let { BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess.invoke(result)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFail?.invoke()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError?.invoke(errorCode, errString)
        }
    }).authenticate(info, it) } ?: run {
        BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess.invoke(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail?.invoke()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError?.invoke(errorCode, errString)
            }
        }).authenticate(info)
    }

fun Fragment.biometricAuthenticate(info: BiometricPrompt.PromptInfo, crypto: BiometricPrompt.CryptoObject? = null,
                                   onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
                                   onFail: (() -> Unit)? = null,
                                   onError: ((errorCode: Int, errString: CharSequence) -> Unit)? = null) =
    crypto?.let { BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess.invoke(result)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFail?.invoke()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onError?.invoke(errorCode, errString)
        }
    }).authenticate(info, it) } ?: run {
        BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess.invoke(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail?.invoke()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError?.invoke(errorCode, errString)
            }
        }).authenticate(info)
    }