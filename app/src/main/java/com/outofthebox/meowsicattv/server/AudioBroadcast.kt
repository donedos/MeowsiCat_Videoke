package com.outofthebox.meowsicattv.server

import com.meowsicat.android.audio.BassMidiEngine
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 * Fans the live BASS PCM tap out to any number of connected HTTP viewers.
 *
 * The DSP tap only runs while at least one subscriber is attached, so an idle
 * broadcast costs nothing. Each subscriber gets its own bounded queue; when a
 * viewer can't keep up in real time the oldest frames are dropped so latency
 * stays bounded instead of growing without limit.
 */
object AudioBroadcast : BassMidiEngine.PcmFrameSink {
    const val SAMPLE_RATE = 44100
    const val CHANNELS = 2
    private const val SUBSCRIBER_CAPACITY = 60 // ~1.8 s of 30 ms frames

    class Subscriber internal constructor() {
        internal val queue = ArrayBlockingQueue<ByteArray>(SUBSCRIBER_CAPACITY)
        @Volatile internal var closed = false

        internal fun push(frame: ByteArray) {
            while (!queue.offer(frame)) {
                queue.poll() // drop oldest to bound latency
            }
        }

        /** Returns the next frame, or null on timeout or after close-and-drained. */
        fun poll(timeoutMs: Long): ByteArray? {
            if (closed && queue.isEmpty()) return null
            return queue.poll(timeoutMs, TimeUnit.MILLISECONDS)
        }

        fun close() {
            closed = true
            queue.clear()
        }
    }

    private val subscribers = CopyOnWriteArrayList<Subscriber>()
    private val lock = Any()
    @Volatile private var source: BassMidiEngine? = null
    @Volatile private var tapAttached = false

    /** Registers the live engine as the PCM source (or null to clear it). */
    fun setSource(engine: BassMidiEngine?) {
        synchronized(lock) {
            if (tapAttached && source != null && source !== engine) {
                source?.detachPcmTap()
                tapAttached = false
            }
            source = engine
            if (engine != null && subscribers.isNotEmpty() && !tapAttached) {
                tapAttached = engine.attachPcmTap(this)
            }
        }
    }

    fun subscribe(): Subscriber {
        val sub = Subscriber()
        subscribers.add(sub)
        synchronized(lock) {
            val eng = source
            if (eng != null && !tapAttached) {
                tapAttached = eng.attachPcmTap(this)
            }
        }
        return sub
    }

    fun unsubscribe(sub: Subscriber) {
        subscribers.remove(sub)
        sub.close()
        synchronized(lock) {
            if (subscribers.isEmpty() && tapAttached) {
                source?.detachPcmTap()
                tapAttached = false
            }
        }
    }

    /** Called on the BASS update thread for every ~30 ms of audio. */
    override fun onPcmFrame(frame: ByteArray) {
        for (sub in subscribers) {
            sub.push(frame)
        }
    }

    /** Ends every active stream (used when the server shuts down). */
    fun endAll() {
        for (sub in subscribers) {
            sub.close()
        }
    }
}
