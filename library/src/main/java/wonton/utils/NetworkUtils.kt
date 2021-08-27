package wonton.utils

import android.os.Build
import androidx.annotation.RawRes
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.TlsVersion
import retrofit2.Converter
import retrofit2.Retrofit
import splitties.init.appCtx
import timber.log.Timber
import java.io.IOException
import java.lang.reflect.Type
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.*

class EmptyConverterFactory private constructor() : Converter.Factory() {
    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> =
        Converter<ResponseBody, Any?> { body ->
            if (body.contentLength() == 0L) null
            else retrofit.nextResponseBodyConverter<Any>(this, type, annotations).convert(body)
        }

    companion object {
        fun create() = EmptyConverterFactory()
    }
}

/**
 * Implementation of [SSLSocketFactory] that adds [TlsVersion.TLS_1_2] (optionally [TlsVersion.TLS_1_3]) as an
 * enabled protocol for every [SSLSocket] created by [delegate].
 *
 * WARNING: YOU MUST USE COMPATIBILITY SOLUTION LIKE CONSCRYPT UNDER API 26 OR SUPPORT FOR TLS 1.3 WILL NOT WORK.
 *
 * [See this discussion for more details.](https://github.com/square/okhttp/issues/2372#issuecomment-244807676)
 *
 * @see SSLSocket
 * @see SSLSocketFactory
 */
class Tls12SocketFactory(private val delegate: SSLSocketFactory, private val supportTls13: Boolean) : SSLSocketFactory() {
    companion object {
        /**
         * @return [X509TrustManager] from [TrustManagerFactory]
         *
         * @throws [NoSuchElementException] if not found. According to the Android docs for [TrustManagerFactory], this
         * should never happen because PKIX is the only supported algorithm
         */
        private val trustManager by lazy {
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            trustManagerFactory.trustManagers
                .first { it is X509TrustManager } as X509TrustManager
        }

        private fun getSupportCAListTrustManager(@RawRes supportCARawIds: IntArray): X509TrustManager {
            val password = "password".toCharArray()
            val cf = CertificateFactory.getInstance("X.509")

            val keyStore =
                KeyStore.getInstance(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) "AndroidKeyStore" else "AndroidCAStore")
            keyStore.load { KeyStore.PasswordProtection(password) }
            supportCARawIds.forEachIndexed { index, id ->
                val certificate = cf.generateCertificate(appCtx.resources.openRawResource(id))
                keyStore.setCertificateEntry("root_$index", certificate)
            }

            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)
            return tmf.trustManagers
                .first { it is X509TrustManager } as X509TrustManager
        }

        /**
         * If on [Build.VERSION_CODES.LOLLIPOP] or lower, sets [OkHttpClient.Builder.sslSocketFactory] to an instance of
         * [Tls12SocketFactory] that wraps the default [SSLContext.getSocketFactory] for [TlsVersion.TLS_1_2] (optionally [TlsVersion.TLS_1_3]) .
         *
         * Does nothing when called on [Build.VERSION_CODES.LOLLIPOP_MR1] or higher.
         *
         * For some reason, Android supports TLS v1.2 from [Build.VERSION_CODES.JELLY_BEAN], but the spec only has it
         * enabled by default from API [Build.VERSION_CODES.KITKAT]. Furthermore, some devices on
         * [Build.VERSION_CODES.LOLLIPOP] don't have it enabled, despite the spec saying they should.
         *
         * @return the (potentially modified) [OkHttpClient.Builder]
         */
        @JvmStatic
        @SafeVarargs
        fun OkHttpClient.Builder.enableTls12(supportTls13: Boolean = false, @RawRes vararg supportCARawIds: Int) = apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                try {
                    val sslContext = SSLContext.getInstance(if(supportTls13) TlsVersion.TLS_1_3.javaName() else TlsVersion.TLS_1_2.javaName())
                    if (supportCARawIds.isNotEmpty()) {
                        val supportTrustManager = getSupportCAListTrustManager(supportCARawIds)
                        sslContext.init(null, arrayOf(supportTrustManager),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) SecureRandom.getInstanceStrong() else SecureRandom())
                        sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory, supportTls13), supportTrustManager)
                    } else {
                        sslContext.init(null, arrayOf(trustManager), if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            SecureRandom.getInstanceStrong() else SecureRandom())
                        sslSocketFactory(Tls12SocketFactory(sslContext.socketFactory, supportTls13), trustManager)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error while setting ${if(supportTls13) TlsVersion.TLS_1_3.javaName() else TlsVersion.TLS_1_2.javaName()} compatibility")
                }
            }
        }
    }

    /**
     * Forcefully adds [TlsVersion.TLS_1_2] (optionally [TlsVersion.TLS_1_3]) as an enabled protocol if called on an [SSLSocket]
     *
     * @return the (potentially modified) [Socket]
     */
    private fun Socket.patchForTls12() = (this as? SSLSocket)?.apply {
        enabledProtocols += TlsVersion.TLS_1_2.javaName()
        if (supportTls13) enabledProtocols += TlsVersion.TLS_1_3.javaName()
    } ?: this

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
        delegate.createSocket(s, host, port, autoClose).patchForTls12()

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket = delegate.createSocket(host, port)
        .patchForTls12()

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket =
        delegate.createSocket(host, port, localHost, localPort).patchForTls12()

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket =
        delegate.createSocket(host, port).patchForTls12()

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket =
        delegate.createSocket(address, port, localAddress, localPort).patchForTls12()
}