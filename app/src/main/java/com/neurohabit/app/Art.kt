package com.neurohabit.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

private const val TAU = 6.2831855f
private val artMod = Modifier.fillMaxWidth().height(190.dp)

@Composable
fun loop(ms: Int, reverse: Boolean = false): State<Float> =
    rememberInfiniteTransition(label = "loop").animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(ms, easing = LinearEasing), if (reverse) RepeatMode.Reverse else RepeatMode.Restart),
        label = "t")

@Composable
fun NeuronArt() {
    val t by loop(3200)
    Canvas(artMod) {
        val w = size.width; val cy = size.height / 2
        val soma = Offset(w * .14f, cy); val end = Offset(w * .74f, cy); val post = Offset(w * .92f, cy)
        for (a in listOf(-.9f, -.4f, .3f, .8f))
            drawLine(Violet.copy(.6f), soma, soma + Offset(-w * .1f, a * size.height * .28f), 4.dp.toPx(), StrokeCap.Round)
        drawLine(Color(0xFF3A4470), soma, end, 5.dp.toPx(), StrokeCap.Round)
        for (i in 0..3) drawRoundRect(Amber.copy(.55f), Offset(w * (.26f + i * .12f), cy - 9.dp.toPx()),
            Size(w * .08f, 18.dp.toPx()), CornerRadius(9.dp.toPx()))
        drawCircle(Violet, 22.dp.toPx(), soma)
        drawCircle(Color(0xFF2B3463), 16.dp.toPx(), end)
        drawCircle(Pink.copy(.5f), 14.dp.toPx(), post)
        if (t < .78f) {
            val px = soma.x + (end.x - soma.x) * (t / .78f)
            drawCircle(Cyan.copy(.25f), 22.dp.toPx(), Offset(px, cy)); drawCircle(Cyan, 8.dp.toPx(), Offset(px, cy))
        } else {
            val q = (t - .78f) / .22f
            repeat(5) { i ->
                drawCircle(Pink.copy(1f - q * .5f), 3.dp.toPx(),
                    Offset(end.x + (post.x - end.x - 14.dp.toPx()) * q, cy + (i - 2) * 7.dp.toPx() * (.4f + q)))
            }
            drawCircle(Cyan.copy(.5f * (1 - q)), 24.dp.toPx(), end)
        }
    }
}

@Composable
fun DopamineArt() {
    val t by loop(3600)
    Canvas(artMod) {
        val w = size.width; val h = size.height
        val c = Offset(w * .74f, h / 2); val pulse = 1f + .12f * sin(t * TAU)
        drawCircle(Pink.copy(.15f), 62.dp.toPx() * pulse, c)
        drawCircle(Pink.copy(.35f), 40.dp.toPx() * pulse, c)
        drawCircle(Pink, 18.dp.toPx(), c)
        repeat(10) { i ->
            val f = (t + i / 10f) % 1f
            val x = w * .06f + (c.x - w * .06f) * f
            val y = h / 2 + sin((f * 6f + i)) * h * .3f * (1 - f)
            drawCircle(Amber.copy(1f - .5f * f), 5.dp.toPx(), Offset(x, y))
        }
    }
}

@Composable
fun SynapseArt() {
    val t by loop(3000, true)
    val s = FastOutSlowInEasing.transform(t)
    Canvas(artMod) {
        val w = size.width; val cy = size.height / 2
        drawRoundRect(Violet.copy(.8f), Offset(w * .04f, cy - 46.dp.toPx()), Size(w * .36f, 92.dp.toPx()), CornerRadius(40.dp.toPx()))
        repeat(4) { i -> drawCircle(Amber, 4.dp.toPx(), Offset(w * .33f, cy + (i - 1.5f) * 14.dp.toPx())) }
        val r = (16 + 20 * s).dp.toPx()
        val c = Offset(w * .5f + r, cy)
        drawLine(Pink.copy(.6f), Offset(c.x, cy), Offset(w * .97f, cy), (6 + 6 * s).dp.toPx())
        drawCircle(Pink, r, c)
        val n = 2 + (s * 7).toInt()
        for (k in 0 until n) {
            val a = PI.toFloat() * (.62f + .76f * k / max(1, n - 1))
            drawCircle(Cyan, 3.5f.dp.toPx(), c + Offset(cos(a), sin(a)) * r)
        }
    }
}

@Composable
fun MemoryArt() {
    val t by loop(4500)
    val lit = (t * 12).toInt()
    Canvas(artMod) {
        val slot = size.width / 10.5f; val y = size.height / 2 - slot / 2
        for (i in 0 until 9) {
            val col = if (i < 7) Cyan else Amber
            val on = i <= lit
            drawRoundRect(if (on) col else col.copy(.15f), Offset(slot * .4f + i * slot * 1.08f, y),
                Size(slot * .9f, slot * .9f), CornerRadius(10.dp.toPx()))
        }
    }
}

@Composable
fun SkillArt() {
    val t by loop(7500)
    val reps = (t * 5).toInt() + 1; val f = (t * 5) % 1f
    Canvas(artMod) {
        val w = size.width; val h = size.height
        fun yAt(x: Float) = h / 2 + sin(x / w * TAU * 1.5f) * h * .28f
        val path = Path().apply {
            moveTo(w * .05f, yAt(w * .05f))
            var x = w * .05f; while (x < w * .95f) { x += 6f; lineTo(x, yAt(x)) }
        }
        drawPath(path, Cyan.copy(min(1f, .2f + reps * .16f)), style = Stroke((2 + reps * 3).dp.toPx(), cap = StrokeCap.Round))
        val dx = w * .05f + w * .9f * f
        drawCircle(Amber, 9.dp.toPx(), Offset(dx, yAt(dx)))
    }
}

@Composable
fun StressArt() {
    val t by loop(8000)
    val k = .5f - .5f * cos(TAU * t)
    Box(artMod, Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val base = min(size.width, size.height) / 2
            drawCircle(Cyan.copy(.12f), base * (.5f + .45f * k))
            drawCircle(Cyan.copy(.25f), base * (.4f + .38f * k))
            drawCircle(Violet, base * (.28f + .3f * k))
        }
        Text(if (t < .5f) "Breathe in" else "Breathe out", color = androidx.compose.ui.graphics.Color.White, fontSize = 15.sp)
    }
}

@Composable
fun SleepArt() {
    val t by loop(9000)
    val lv = floatArrayOf(0f, 1f, 2f, 3f, 3f, 2f, 1f, .4f, 1f, 2f, 3f, 2f, 1f, .4f, 1f, 2f, 1f, .4f, .4f, 0f)
    Canvas(artMod) {
        val w = size.width; val h = size.height; val n = lv.size
        fun pt(i: Float): Offset {
            val a = i.toInt().coerceIn(0, n - 2); val fr = i - a
            val v = lv[a] + (lv[a + 1] - lv[a]) * fr
            return Offset(w * .06f + w * .88f * i / (n - 1f), h * .18f + h * .6f * v / 3f)
        }
        for (i in 0 until n - 1) {
            val rem = lv[i] == .4f || lv[i + 1] == .4f
            drawLine(if (rem) Pink else Cyan, pt(i.toFloat()), pt(i + 1f), 4.dp.toPx(), StrokeCap.Round)
        }
        drawCircle(Amber, 7.dp.toPx(), pt(t * (n - 1.001f)))
        val m = Offset(w * .92f, h * .1f)
        drawCircle(Amber, 12.dp.toPx(), m); drawCircle(CardBg, 10.dp.toPx(), m + Offset(7.dp.toPx(), -3.dp.toPx()))
    }
}
