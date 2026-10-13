package com.neurohabit.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = HabitStore(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Bg, surface = CardBg, onBackground = Color.White)) {
                App(store)
            }
        }
    }
}

@Composable
fun App(store: HabitStore) {
    var habits by remember { mutableStateOf(store.load()) }
    var tab by remember { mutableIntStateOf(0) }
    val ctx = LocalContext.current
    fun update(n: List<Habit>) { habits = n; store.save(n) }
    Scaffold(containerColor = Bg, bottomBar = {
        NavigationBar(containerColor = CardBg) {
            NavigationBarItem(tab == 0, { tab = 0 }, { Text("âœ…") }, label = { Text("Today") })
            NavigationBarItem(tab == 1, { tab = 1 }, { Text("ðŸ§ ") }, label = { Text("Brain Lab") })
        }
    }) { pad ->
        Box(Modifier.padding(pad).statusBarsPadding()) {
            if (tab == 0) TodayScreen(habits) { update(it) }
            else LabScreen { name, emoji ->
                update(habits + Habit(System.currentTimeMillis(), name, emoji, emptySet()))
                Toast.makeText(ctx, "Added to Today", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun TodayScreen(habits: List<Habit>, onChange: (List<Habit>) -> Unit) {
    val today = LocalDate.now().toString()
    var showAdd by remember { mutableStateOf(false) }
    val done = habits.count { today in it.done }
    val frac by animateFloatAsState(if (habits.isEmpty()) 0f else done / habits.size.toFloat(), tween(900), label = "p")
    val tip = LESSONS[LocalDate.now().dayOfYear % LESSONS.size]
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.size(84.dp), Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val sw = 10.dp.toPx(); val inset = sw / 2
                        val sz = Size(size.width - sw, size.height - sw)
                        drawArc(Color(0xFF2B3463), 0f, 360f, false, Offset(inset, inset), sz, style = Stroke(sw))
                        drawArc(Cyan, -90f, 360f * frac, false, Offset(inset, inset), sz, style = Stroke(sw, cap = StrokeCap.Round))
                    }
                    Text("$done/${habits.size}", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("Wire it in today", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(if (habits.isNotEmpty() && done == habits.size) "All done. Your synapses thank you." else "Each repeat strengthens the connection.", color = Muted)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("ðŸ§  Brain tip Â· ${tip.title}", color = Cyan, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(tip.takeaway, color = Color(0xFFD5DAF0))
                }
            }
        }
        items(habits, key = { it.id }) { h ->
            HabitCard(h, today,
                onToggle = {
                    onChange(habits.map {
                        if (it.id == h.id) it.copy(done = if (today in it.done) it.done - today else it.done + today) else it
                    })
                },
                onDelete = { onChange(habits.filter { it.id != h.id }) })
        }
        item { OutlinedButton({ showAdd = true }, Modifier.fillMaxWidth()) { Text("ï¼‹ New habit") } }
    }
    if (showAdd) AddDialog({ showAdd = false }) { n, e -> onChange(habits + Habit(System.currentTimeMillis(), n, e, emptySet())); showAdd = false }
}

private fun stage(streak: Int) = when {
    streak == 0 -> "new connection"
    streak < 3 -> "sprouting"
    streak < 7 -> "strengthening"
    streak < 21 -> "potentiated"
    else -> "well-worn path"
}

@Composable
fun HabitCard(h: Habit, today: String, onToggle: () -> Unit, onDelete: () -> Unit) {
    val isDone = today in h.done
    val streak = h.streak()
    val spark by animateFloatAsState(if (isDone) 1f else 0f, tween(800), label = "spark")
    val pop by animateFloatAsState(if (isDone) 1f else 0f, spring(Spring.DampingRatioMediumBouncy), label = "pop")
    val thick by animateFloatAsState(1f + minOf(streak, 30) / 30f * 8f, tween(900), label = "thick")
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(h.emoji, fontSize = 28.sp)
            Column(Modifier.weight(1f)) {
                Text(h.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Canvas(Modifier.width(54.dp).height(14.dp)) {
                        val cy = size.height / 2
                        drawLine(Violet, Offset(8.dp.toPx(), cy), Offset(size.width - 8.dp.toPx(), cy), thick.dp.toPx().coerceAtMost(size.height), StrokeCap.Round)
                        drawCircle(Cyan, 5.dp.toPx(), Offset(5.dp.toPx(), cy)); drawCircle(Pink, 5.dp.toPx(), Offset(size.width - 5.dp.toPx(), cy))
                    }
                    Text("$streak-day streak Â· ${stage(streak)}", color = Muted, fontSize = 12.sp)
                }
            }
            Box(Modifier.size(52.dp).clickable(onClick = onToggle), Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    if (spark in 0.02f..0.98f) repeat(8) { i ->
                        val a = i * 0.7853982f; val r = size.minDimension / 2
                        drawCircle(Amber.copy(1f - spark), 3.dp.toPx(), center + Offset(cos(a), sin(a)) * r * (.5f + .5f * spark))
                    }
                }
                Box(Modifier.size((30 + 6 * pop).dp).clip(CircleShape).background(if (isDone) Cyan else Color(0xFF2B3463)), Alignment.Center) {
                    if (isDone) Text("âœ“", color = Bg, fontWeight = FontWeight.Bold)
                }
            }
            TextButton(onDelete) { Text("âœ•", color = Muted) }
        }
    }
}

@Composable
fun AddDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("ðŸ’§") }
    AlertDialog(onDismissRequest = onDismiss, containerColor = CardBg,
        title = { Text("New habit", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, singleLine = true, placeholder = { Text("e.g. Read 5 pages after dinner") })
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ðŸ’§", "ðŸ“š", "ðŸƒ", "ðŸ§˜", "ðŸŽ¸", "ðŸŒ™", "âœï¸", "ðŸ¥—").forEach {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(if (it == emoji) Violet else Color(0xFF2B3463)).clickable { emoji = it }, Alignment.Center) { Text(it) }
                    }
                }
            }
        },
        confirmButton = { TextButton({ if (name.isNotBlank()) onAdd(name.trim(), emoji) }) { Text("Add") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}

@Composable
fun LabScreen(onHabit: (String, String) -> Unit) {
    val pager = rememberPagerState { LESSONS.size }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("Brain Lab", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Swipe through the science behind habits â†’", color = Muted)
        Spacer(Modifier.height(12.dp))
        HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 16.dp) { LessonPage(LESSONS[it], onHabit) }
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.Center) {
            repeat(LESSONS.size) { i ->
                val sel = pager.currentPage == i
                val w by animateDpAsState(if (sel) 22.dp else 8.dp, label = "dot")
                Box(Modifier.padding(3.dp).height(8.dp).width(w).clip(CircleShape).background(if (sel) Cyan else Color(0xFF2B3463)))
            }
        }
    }
}

@Composable
fun LessonPage(l: Lesson, onHabit: (String, String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(12.dp)) { l.art(); Text(l.caption, color = Muted, fontSize = 12.sp) }
        }
        Text(l.tag, color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(l.title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(l.body, color = Color(0xFFD5DAF0), lineHeight = 22.sp)
        FactCard("ðŸ“– From the booklet", l.fact, Violet)
        FactCard("ðŸ’¡ App takeaway", l.takeaway, Cyan)
        if (l.interactive) LetterSpan()
        Button({ onHabit(l.habit, l.emoji) }, Modifier.fillMaxWidth()) { Text("ï¼‹ Add habit: ${l.emoji} ${l.habit}") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun FactCard(label: String, text: String, accent: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(accent.copy(.12f)).padding(14.dp)) {
        Text(label, color = accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(text, color = Color(0xFFD5DAF0))
    }
}

@Composable
fun LetterSpan() {
    var len by remember { mutableIntStateOf(3) }
    var phase by remember { mutableIntStateOf(0) } // 0 idle, 1 showing, 2 answering, 3 result
    var target by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    LaunchedEffect(phase) { if (phase == 1) { delay(1500L + len * 450L); phase = 2 } }
    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ðŸ§ª Try the letter-span test", color = Color.White, fontWeight = FontWeight.SemiBold)
            when (phase) {
                1 -> Text(target, fontSize = 34.sp, letterSpacing = 6.sp, color = Cyan, fontWeight = FontWeight.Bold)
                2 -> {
                    Text("Type the letters in order", color = Muted)
                    OutlinedTextField(input, { input = it.uppercase() }, singleLine = true)
                    Button({ ok = input.trim() == target; len = if (ok) len + 1 else maxOf(3, len - 1); phase = 3 }) { Text("Check") }
                }
                3 -> {
                    Text(if (ok) "âœ… Correct! $target" else "âŒ It was $target", color = if (ok) Cyan else Pink)
                    Button({ phase = 0 }) { Text("Continue") }
                }
                else -> Button({
                    target = (1..len).map { "BCDFGHJKLMNPQRSTVWXZ".random() }.joinToString(""); input = ""; phase = 1
                }) { Text("Start Â· $len letters") }
            }
        }
    }
}
