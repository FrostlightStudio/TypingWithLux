package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.entries.get
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.descendants
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.dialogue.TickContext
import com.typewritermc.engine.paper.entry.entity.SimpleEntityDefinition
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
        val safeDialogueId = entry.id.takeWhile { it.isDigit() }
            .ifEmpty { abs(entry.id.hashCode()).toString() }
        val dialogueBuilder = Dialogue.Builder()
            .setDialogueID(safeDialogueId)
            .setRange(-1.0)
            .setDialogueSpeed(time)
            .setTypingSound(
                "minecraft:entity.armadillo.scute_drop",
                "master",
                1.0,
                1.0
            )
            .setSelectionSound(
                "luxdialogues:luxdialogues.sounds.selection",
                "master",
                1.0,
                1.0
            )
            .setAnswerNumbers(false)
            .setArrowImage("hand", "#cdff29", -7)
            .setDialogueBackgroundImage("dialogue-background", "#ffffff", -35)
            .setAnswerBackgroundImage("answer-background", "#f8ffe0", 90)
            .setDialogueText("#4f4a3e", 10)
            .setAnswerText("#4f4a3e", 13, "#4f4a3e")
            .setCharacterImage(data.imageName, "#ffffff", -16)
            .setCharacterNameText(
                data.characterName.parsePlaceholders(player),
                "#4f4a3e",
                20
            )
            .setNameImage(
                "name-start",
                "name-mid",
                "name-end",
                "#f8ffe0",
                0
            )
            .setFogImage("fog", "#000000")
            .setEffect("Slowness")
            .setPreventExit(false)

        // --- PAGE BUILDING
        val pageBuilder = Page.Builder()
            .setID("page-${safeDialogueId}")

        entry.text.split("\n").forEach { line ->
            pageBuilder.addLine(line)
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
