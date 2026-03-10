package com.newglobe.gemmaocr.ui

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun normalized(): NormalizedRect {
        val l = left.coerceIn(0f, 1f)
        val t = top.coerceIn(0f, 1f)
        val r = right.coerceIn(0f, 1f)
        val b = bottom.coerceIn(0f, 1f)
        return NormalizedRect(
            left = minOf(l, r),
            top = minOf(t, b),
            right = maxOf(l, r),
            bottom = maxOf(t, b)
        )
    }

    fun isUsable(minSize: Float = 0.02f): Boolean {
        val n = normalized()
        return (n.right - n.left) >= minSize && (n.bottom - n.top) >= minSize
    }

    companion object {
        val Default = NormalizedRect(0.1f, 0.1f, 0.9f, 0.9f)
    }
}
