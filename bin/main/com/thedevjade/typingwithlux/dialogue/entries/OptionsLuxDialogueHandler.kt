package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.entries.get
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.descendants
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.dialogue.TickContext
import com.typewritermc.engine.paper.entry.entity.SimpleEntityDefinition
import com.typewritermc.engine.paper.entry.entries.EventTrigger
import com.typewritermc.engine.paper.entry.matches
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.interaction.startBlockingActionBar
import com.typewritermc.engine.paper.interaction.stopBlockingActionBar
import com.typewritermc.engine.paper.logger
import org.aselstudios.luxdialoguesapi.Builders.Answer
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Player

class OptionsLuxDialogueHandler(
    player: Player,
    context: InteractionContext,
    entry: OptionsLuxDialogueEntry
) : DialogueMessenger<OptionsLuxDialogueEntry>(player, context, entry) {

    var dialogue: Dialogue? = null
    var selectedOption: Int? = null
    var hashedOptions: HashMap<Int, LuxOption> = HashMap()

    override val modifiers: List<Modifier>
        get() {
            val selected = selectedOption ?: -1
            val option = hashedOptions[selected]
            return if (selected != -1) option!!.modifiers else emptyList()
        }

    override val eventTriggers: List<EventTrigger>
        get() {
            val selected = selectedOption ?: -1
            val option = hashedOptions[selected]
            return if (selected != -1) option!!.eventTriggers else emptyList()
        }

    override fun init() {
        super.init()

        val npc = entry.speaker.get() as SimpleEntityDefinition
        val data = npc.data.descendants(LuxNpcData::class).firstOrNull()?.get()

        if (data == null) {
            state = MessengerState.FINISHED
            logger.severe("No npc data found for ${npc.name}")
            return
        }
        val totalTime: Int = (entry.duration.get(player, context).toMillis() * 20 / 1000).toInt()
        val chars = entry.text.length.coerceAtLeast(1)
        val time = (totalTime / chars).coerceAtLeast(1)

        val safeDialogueId = entry.id.takeWhile { it.isDigit() }.ifEmpty { kotlin.math.abs(entry.id.hashCode()).toString() }

        val dialogueBuilder = Dialogue.Builder()
            .setDialogueID(safeDialogueId)
            .setRange(-1.0)
            .setDialogueSpeed(time)
            .setTypingSound("minecraft:entity.armadillo.scute_drop", "MASTER", 1.0, 1.0)
            .setSelectionSound("luxdialogues:luxdialogues.sounds.selection", "MASTER", 1.0, 1.0)
            .setAnswerNumbers(true)
            .setArrowImage("hand", "#cdff29", -7)
            .setDialogueBackgroundImage("dialogue-background", "#ffffff", -5)
            .setAnswerBackgroundImage("answer-background", "#ffffff", 90)
            .setDialogueText("#4f4a3e", 40)
            .setAnswerText("#4f4a3e", 13, "#4f4a3e")
            .setCharacterImage(data.imageName, "#ffffff", -16)
            .setCharacterNameText(data.characterName.parsePlaceholders(player), "#4f4a3e", 2)
            .setNameImage("name-start", "name-mid", "name-end", "#f8ffe0", 0)
            .setFogImage("fog", "#000000")
            .setEffect("Slowness")
            .setPreventExit(true)
        val pageBuilder = Page.Builder().setID("main-page-${'$'}safeDialogueId")
        entry.text.split("\n").forEach { pageBuilder.addLine(it) }
        hashedOptions.forEach { entryOption ->
            val index = entryOption.key
            val option = entryOption.value
            if (option.criteria.isNotEmpty() && !option.criteria.matches(player, context)){
                return@forEach
            }
            val answer = Answer.Builder()
                .setAnswerID(index.toString())
                .setAnswerText(option.text.get(player, context))
                .addCallback { selectedOption = index }
                .build()
            pageBuilder.addAnswer(answer)
        }
        val page = pageBuilder.build()
        dialogueBuilder.addPage(page)
        dialogue = dialogueBuilder.build()
        LuxDialoguesAPI.getProvider().sendDialogue(player, dialogue!!, page.id)
    }

    override fun dispose() {
        super.dispose()
        player.startBlockingActionBar()
    }

    override fun tick(context: TickContext) {
        if (context.playTime.isZero) {
            player.stopBlockingActionBar()
        }

        super.tick(context)

        if (state != MessengerState.RUNNING) return

        player.stopBlockingActionBar()

        if (selectedOption != null) {
            state = MessengerState.FINISHED
        }
    }
}
