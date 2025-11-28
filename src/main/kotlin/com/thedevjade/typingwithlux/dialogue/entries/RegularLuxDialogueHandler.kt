package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.entries.get
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.descendants
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.dialogue.TickContext
import com.typewritermc.engine.paper.entry.entity.SimpleEntityDefinition
import com.typewritermc.entity.entries.entity.custom.NpcDefinition
import com.typewritermc.entity.entries.entity.custom.NpcInstance
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.interaction.startBlockingActionBar
import com.typewritermc.engine.paper.interaction.stopBlockingActionBar
import com.typewritermc.engine.paper.logger
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Player
import kotlin.math.abs

class RegularLuxDialogueHandler(
    player: Player,
    context: InteractionContext,
    entry: RegularLuxDialogueEntry
) : DialogueMessenger<RegularLuxDialogueEntry>(player, context, entry) {

    var dialogue: Dialogue? = null
    var endMethodHasNotRan: Boolean = true

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
        val safeDialogueId = entry.id.takeWhile { it.isDigit() }
            .ifEmpty { abs(entry.id.hashCode()).toString() }
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
            .setPreventExit(data.preventExit)

        // --- PAGE BUILDING
        val pageBuilder = Page.Builder()
            .setID("page-${safeDialogueId}")

        entry.text.split("\n").forEach { line ->
            pageBuilder.addLine(line)
        }

        // Add optional goTo navigation
        if (entry.goTo.isNotEmpty()) {
            pageBuilder.setGoTo(entry.goTo)
        }

        // Add optional timer
        if (entry.timer > 0) {
            pageBuilder.setTimer(entry.timer)
        }

        val builtPage = pageBuilder.build()

        dialogueBuilder.addPage(builtPage)

        dialogue = dialogueBuilder.build()

        // --- SEND DIALOGUE (requires pageId)
        LuxDialoguesAPI.getProvider().sendDialogue(player, dialogue, builtPage.id)
    }

    override fun dispose() {
        super.dispose()
        if (endMethodHasNotRan) player.startBlockingActionBar()
    }

    override fun end() {
        super.end()
        endMethodHasNotRan = false
    }

    override fun tick(context: TickContext) {
        if (context.playTime.isZero) {
            player.stopBlockingActionBar()
        }

        super.tick(context)

        if (state != MessengerState.RUNNING) return

        player.stopBlockingActionBar()

        if (!LuxDialoguesAPI.getProvider().isInDialogue(player)) {
            state = MessengerState.FINISHED
        }
    }
}
