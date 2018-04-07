package io.nukkit.spp.test

import io.nukkit.spp.init
import org.junit.jupiter.api.Test as test
import org.junit.jupiter.api.BeforeEach as beforeEach
import org.junit.jupiter.api.Assertions.*
import java.net.URL

class TestSppURL {

    @beforeEach fun buildUp() = init()

    @test fun testURL() {
        val url = URL("spp://localhost:12157/ec2018/hub?broadcast_group=1234#spawn")
        assertEquals(url.protocol, "spp")
        assertEquals(url.host, "localhost")
        assertEquals(url.port, 12157)
        assertEquals(url.authority, "localhost:12157")
        assertEquals(url.path, "/ec2018/hub")
        assertEquals(url.query, "broadcast_group=1234") // 大厅ID：1234
        assertEquals(url.ref, "spawn")  // 出生点
    }

}