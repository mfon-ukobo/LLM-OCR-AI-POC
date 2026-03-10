package com.newglobe.gemmaocr

import com.newglobe.gemmaocr.data.ModelConfig
import com.newglobe.gemmaocr.model.ModelManager
import com.newglobe.gemmaocr.model.ModelProvisionResult
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ModelManagerTest {
    @Test
    fun ensureModelReady_attachesBearerAuthAndDownloadsFile() = runTest {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("model-bytes"))
        server.start()
        try {
            val tmp = Files.createTempDirectory("model-test").toFile()
            val modelFile = File(tmp, "gemma.task")
            val manager = ModelManager()
            val config = ModelConfig(
                modelUrl = server.url("/model.task").toString(),
                token = "token-123",
                localPath = modelFile.absolutePath
            )

            val result = manager.ensureModelReady(config)
            val request = server.takeRequest()

            assertEquals("Bearer token-123", request.getHeader("Authorization"))
            assertTrue(result is ModelProvisionResult.Ready)
            assertTrue(modelFile.exists())
            assertEquals("model-bytes", modelFile.readText())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun ensureModelReady_skipsDownloadWhenCached() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            val tmp = Files.createTempDirectory("model-cache-test").toFile()
            val modelFile = File(tmp, "gemma.task")
            modelFile.writeText("cached")
            val manager = ModelManager()
            val config = ModelConfig(
                modelUrl = server.url("/model.task").toString(),
                token = "token-123",
                localPath = modelFile.absolutePath
            )

            val result = manager.ensureModelReady(config)

            assertTrue(result is ModelProvisionResult.Ready)
            assertEquals(0, server.requestCount)
            assertEquals("cached", modelFile.readText())
        } finally {
            server.shutdown()
        }
    }
}
