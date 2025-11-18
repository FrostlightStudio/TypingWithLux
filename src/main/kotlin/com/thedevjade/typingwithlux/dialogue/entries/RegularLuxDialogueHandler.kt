package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.entries.get
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.descendants
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.dialogue.TickContext
import com.typewritermc.engine.paper.entry.entity.SimpleEntityDefinition
import com.typewritermc.engine.paper.entry.entity.SimpleEntityInstance
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.interaction.startBlockingActionBar
import com.typewritermc.engine.paper.interaction.stopBlockingActionBar
import com.typewritermc.engine.paper.logger
import com.typewritermc.entity.entries.entity.custom.NpcDefinition
import com.typewritermc.entity.entries.entity.custom.NpcInstance
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class RegularLuxDialogueHandler(player: Player, context: InteractionContext, entry: RegularLuxDialogueEntry) :
    DialogueMessenger<RegularLuxDialogueEntry>(player, context, entry) {

        var dialogue: Dialogue? = null


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

        val dialogueBuilder: Dialogue.Builder  = Dialogue.Builder()
            .setDialogueID(entry.id)
            .setRange(-1.0)
            .setDialogueSpeed(time)
            .setTypingSound("minecraft:entity.armadillo.scute_drop")
            .setTypingSoundPitch(1.0)
            .setTypingSoundVolume(1.0)
            .setSelectionSound("luxdialogues:luxdialogues.sounds.selection")
            .setAnswerNumbers(false)
            .setArrowImage("hand", "#cdff29", -7)
            .setDialogueBackgroundImage("dialogue-background", "#f8ffe0", 0)
            .setAnswerBackgroundImage("answer-background", "#f8ffe0", 90)
            .setDialogueText("#4f4a3e", 10)
            .setAnswerText("#4f4a3e", 13, "#4f4a3e")
            .setCharacterImage(data.imageName, -16)
            .setCharacterNameText(data.characterName.parsePlaceholders(player), "#4f4a3e", 20)
            .setNameStartImage("name-start")
            .setNameMidImage("name-mid")
            .setNameEndImage("name-end")
            .setNameImageColor("#f8ffe0")
            .setFogImage("fog", "#000000")
            .setEffect("Slowness")
            .setPreventExit(false)
        val page: Page.Builder = Page.Builder()
        entry.text.split("\n").forEach { page.addLine(it) }

        dialogueBuilder.addPage(page.build())


        dialogue = dialogueBuilder.build()



        LuxDialoguesAPI.getProvider().sendDialogue(player, dialogue)


    }

    var endMethodHasNotRan: Boolean = true

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

        if (LuxDialoguesAPI.getProvider().isInDialogue(player)) return
        state = MessengerState.FINISHED
    }
}