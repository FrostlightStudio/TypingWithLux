package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.entries.get
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.descendants
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.dialogue.TickContext
import com.typewritermc.engine.paper.entry.entity.SimpleEntityDefinition
import com.typewritermc.entity.entries.entity.custom.NpcDefinition
import com.typewritermc.entity.entries.entity.custom.NpcInstance
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

        val speaker = entry.speaker.get()
        val data = when (speaker) {
            is NpcDefinition -> speaker.data.descendants(LuxNpcData::class).firstOrNull()?.get()
            is NpcInstance -> speaker.definition.get()?.data?.descendants(LuxNpcData::class)?.firstOrNull()?.get()
            is SimpleEntityDefinition -> speaker.data.descendants(LuxNpcData::class).firstOrNull()?.get()
            else -> null
        }

        if (data == null) {
            state = MessengerState.FINISHED
            logger.severe("No npc data found for speaker")
            return
        }
        val totalTime: Int = (entry.duration.get(player, context).toMillis() * 20 / 1000).toInt()
        val chars = entry.text.length.coerceAtLeast(1)
        val time = (totalTime / chars).coerceAtLeast(1)

        val safeDialogueId = entry.id.takeWhile { it.isDigit() }.ifEmpty { kotlin.math.abs(entry.id.hashCode()).toString() }

        // Initialize hashedOptions with entry.options
        entry.options.forEachIndexed { index, option ->
            hashedOptions[index] = option
        }

        val dialogueBuilder = Dialogue.Builder()
            .setDialogueID(safeDialogueId)
            .setRange(-1.0)
            .setDialogueSpeed(time)
            .setTypingSound(data.typingSound, data.typingSoundCategory, data.typingSoundVolume, data.typingSoundPitch)
            .setSelectionSound(data.selectionSound, data.selectionSoundCategory, data.selectionSoundVolume, data.selectionSoundPitch)
            .setAnswerNumbers(false)
            .setArrowImage(data.arrowImage, data.arrowColor, data.arrowOffset)
            .setDialogueBackgroundImage(data.dialogueBackgroundImage, data.dialogueBackgroundColor, data.dialogueBackgroundOffset)
            .setAnswerBackgroundImage(data.answerBackgroundImage, data.answerBackgroundColor, data.answerBackgroundOffset)
            .setDialogueText(data.dialogueTextColor, data.dialogueTextOffset)
            .setAnswerText(data.answerTextColor, data.answerTextOffset, data.answerSelectedColor)
            .setCharacterImage(data.imageName, data.characterColor, data.characterOffset)
            .setCharacterNameText(data.characterName.parsePlaceholders(player), data.characterNameColor, data.characterNameOffset)
            .setNameImage(data.nameStartImage, data.nameMidImage, data.nameEndImage, data.nameBackgroundColor, data.nameImageOffset)
            .setFogImage(data.fogImage, data.fogColor)
            .setEffect(data.effect)
            .setPreventExit(true)
        val pageBuilder = Page.Builder().setID("main-page-${'$'}safeDialogueId")
        entry.text.split("\n").forEach { pageBuilder.addLine(it) }
        hashedOptions.forEach { entryOption ->
            val index = entryOption.key
            val option = entryOption.value
            if (option.criteria.isNotEmpty() && !option.criteria.matches(player, context)){
                return@forEach
            }
            var answerBuilder = Answer.Builder()
                .setAnswerID(index.toString())
                .setAnswerText(option.text.get(player, context))
                .addCallback { selectedOption = index }

            // Add goTo if specified
            if (option.goTo.isNotEmpty()) {
                answerBuilder = answerBuilder.setGoTo(option.goTo)
            }

            // Add reply messages if specified
            option.replyMessages.forEach { message ->
                answerBuilder = answerBuilder.addReplyMessage(message)
            }

            val answer = answerBuilder.build()
            pageBuilder.addAnswer(answer)
        }
        val page = pageBuilder.build()
        dialogueBuilder.addPage(page)
        dialogue = dialogueBuilder.build()
        LuxDialoguesAPI.getProvider().sendDialogue(player, dialogue, page.id)
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
