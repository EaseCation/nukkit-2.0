package io.nukkit.spp

import java.net.*

var DEFAULT_PORT = 12157 // @PeratX qq号的前5位

fun init() {
    URL.setURLStreamHandlerFactory(SppURLStreamHandlerFactory())
}

private class SppURLStreamHandlerFactory : URLStreamHandlerFactory {

    override fun createURLStreamHandler(protocol: String?): URLStreamHandler? {
        return if(protocol.equals("spp")) SppURLStreamHandler()
        else null
    }
}

private class SppURLStreamHandler : URLStreamHandler() {
    override fun openConnection(u: URL?): URLConnection {
        return SppURLConnection(u)
    }
}

private class SppURLConnection(url: URL?) : URLConnection(url) {

    private var connection:Socket? = null

    override fun connect() {
        if (connected) return

        connection = Socket(url.host, url.port)

        // 协议细节，暂定

        connection!!.tcpNoDelay = true
        connection!!.reuseAddress = true
        connection!!.soTimeout = 30_000
        connection!!.setSoLinger(true, 5)
        connection!!.sendBufferSize = 1024
        connection!!.receiveBufferSize = 1024
        connection!!.keepAlive = true
        connection!!.oobInline = true
        connection!!.trafficClass = 0b10100 // 0x04 | 0x10
        connection!!.setPerformancePreferences(0, 1, 2)

        connected = true

        // 在这里回复信息
        // connection.getOutputStream().write(0x66)

    }

}