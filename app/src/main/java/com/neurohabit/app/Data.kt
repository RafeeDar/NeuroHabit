package com.neurohabit.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.time.LocalDate

val Bg = Color(0xFF0B1020); val CardBg = Color(0xFF151B33)
val Cyan = Color(0xFF5CE1E6); val Violet = Color(0xFF9B7BFF)
val Pink = Color(0xFFFF7BB5); val Amber = Color(0xFFFFC857); val Muted = Color(0xFF8A93B8)

data class Habit(val id: Long, val name: String, val emoji: String, val done: Set<String>) {
    /** Consecutive days ending today (or yesterday if today isn't done yet). */
    fun streak(): Int {
        var d = LocalDate.now()
        if (d.toString() !in done) d = d.minusDays(1)
        var n = 0
        while (d.toString() in done) { n++; d = d.minusDays(1) }
        return n
    }
}

class HabitStore(ctx: Context) {
    private val sp = ctx.getSharedPreferences("habits", Context.MODE_PRIVATE)
    fun load(): List<Habit> {
        if (!sp.contains("h")) return listOf(
            Habit(1, "Wind down for bed at the same time", "馃寵", emptySet()),
            Habit(2, "10 min deliberate practice", "馃幆", emptySet()),
            Habit(3, "2-min calm breathing", "馃尙锔�", emptySet()))
        return sp.getString("h", "")!!.split("\n").filter { it.isNotBlank() }.map {
            val p = it.split("|")
            Habit(p[0].toLong(), p[1], p[2], p.getOrElse(3) { "" }.split(",").filter { d -> d.isNotBlank() }.toSet())
        }
    }
    fun save(list: List<Habit>) {
        sp.edit().putString("h", list.joinToString("\n") {
            "${it.id}|${it.name.replace("|", " ").replace("\n", " ")}|${it.emoji}|${it.done.joinToString(",")}"
        }).apply()
    }
}

class Lesson(
    val tag: String, val title: String, val body: String, val fact: String, val takeaway: String,
    val caption: String, val habit: String, val emoji: String,
    val interactive: Boolean = false, val art: @Composable () -> Unit)

val LESSONS = listOf(
    Lesson("CHAPTER 2 路 NEURONS", "Spark: how neurons talk",
        "Your brain holds billions of cells wired into networks. A neuron sends a signal as an action potential: a wave of sodium and potassium ions flipping the voltage along its axon. Insulated (myelinated) axons let the wave skip along at up to 100 metres per second.",
        "Action potentials are all-or-nothing. They never vary in size, only in how often they fire, so frequency carries the message.",
        "Repetition is the language neurons speak. Small, frequent actions beat rare heroic ones, so every habit here starts tiny.",
        "A pulse travels the axon, then neurotransmitters cross the synapse.",
        "2-minute daily starter", "鈽€锔�") { NeuronArt() },
    Lesson("CHAPTER 3 路 CHEMICAL MESSENGERS", "Dopamine: the reward signal",
        "Electrical signals can't bridge the gap between neurons, so chemical messengers (neurotransmitters) carry them across the synapse. Dopamine acts on brain centres tied to positive emotion, making certain situations feel rewarding.",
        "Dopamine makes some situations rewarding. The booklet notes that addictive drugs hijack these natural reward systems.",
        "Give every win an instant payoff. That's why checking off a habit triggers a burst of animation and celebration here.",
        "Dopamine molecules drift toward a glowing reward centre.",
        "Celebrate one small win", "馃帀") { DopamineArt() },
    Lesson("CHAPTER 10 路 PLASTICITY", "Neurons that practise, grow",
        "When a synapse is activated several times in quick succession, NMDA receptors open and let calcium flood in. That signal inserts more AMPA receptors (long-term potentiation), and with continued use the synapse can enlarge or sprout partners. Synapses that go unused can wither.",
        "Brains like exercise: synaptic connections become more numerous and better organised when used a lot. Attention and emotion help too.",
        "Consistency is the growth signal. Your streak meter on the Today tab is a metaphor: each repeat thickens the connection.",
        "Weak to strong: spine grows, receptors (cyan dots) multiply.",
        "10 min deliberate practice", "馃幆") { SynapseArt() },
    Lesson("CHAPTER 11 路 LEARNING & MEMORY", "Working memory: 7 卤 2",
        "Working memory holds information in an active state for a short time, mostly in frontal and parietal cortex. Its capacity is limited, usually around 7 卤 2 items, which is why phone numbers are short.",
        "Most people recall about 7 or 8 letters perfectly in the letter-span test; very few manage 10.",
        "Don't rely on willpower and memory. Decide the 'when and where' of a habit in advance and keep each plan to a few items.",
        "Seven slots light up; the last two are the '卤 2'.",
        "Write tomorrow's top 3", "馃摑", interactive = true) { MemoryArt() },
    Lesson("CHAPTER 11 路 LEARNING & MEMORY", "Skills take practice, feelings are fast",
        "Skills like piano or cycling are learned through deliberate and extensive practice, relying on the basal ganglia and cerebellum. Emotional learning, handled by the amygdala, is much more rapid.",
        "Skill learning is slow and practice-driven; emotional learning is quick, especially for fear.",
        "Be patient with skill habits, and attach a pleasant feeling to the routine so your fast emotional learning works for you.",
        "Each repetition cuts the same path a little deeper.",
        "Practise a skill", "馃幐") { SkillArt() },
    Lesson("CHAPTER 12 路 STRESS", "Stress: fight or flight",
        "Stress is a mismatch between what body and brain anticipate and the challenge they actually meet. The brain triggers noradrenaline and adrenaline release: pounding heart, higher blood pressure, goosebumps, butterflies in the stomach, all preparing you to fight or flee.",
        "The booklet describes this immediate response as the sympathetic nervous system's fight-or-flight reaction.",
        "Calm helps learning. Use the breathing circle (an app tool, not from the booklet) before a hard practice session.",
        "Follow the circle: in as it grows, out as it shrinks.",
        "2-min calm breathing", "馃尙锔�") { StressArt() },
    Lesson("CHAPTER 14 路 SLEEP", "Sleep: your brain's night shift",
        "Sleep follows a circadian rhythm, kept in time by the suprachiasmatic nucleus, a tiny clock in the hypothalamus that is reset by light from the eyes. Through the night the EEG cycles through slow-wave stages, with REM sleep returning several times.",
        "People living in caves with no time cues drifted to a sleep-wake cycle of about 25 hours. Most of us have 4 to 6 REM episodes a night.",
        "Anchor a consistent bedtime and morning light: your clock runs on regular input.",
        "A night's cycle: deep stages (low) and REM (pink).",
        "Wind down for bed at the same time", "馃寵") { SleepArt() },
)
