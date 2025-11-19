package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.descendants
import com.typewritermc.engine.paper.entry.entity.SimpleEntityDefinition
import com.typewritermc.engine.paper.entry.entries.CinematicAction
import com.typewritermc.engine.paper.entry.entries.CinematicEntry
import com.typewritermc.engine.paper.entry.entries.Segment
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.temporal.SimpleCinematicAction
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import com.typewritermc.engine.paper.interaction.startBlockingActionBar
import com.typewritermc.engine.paper.interaction.stopBlockingActionBar
import com.typewritermc.engine.paper.logger
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI
import org.bukkit.entity.Player
import kotlin.math.abs

@Entry("lux_cinematic_entry", "Lux Dialogues in cinematics", Colors.BLUE, "material-symbols:cinematic-blur")
class LuxCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(Colors.BLUE, "material-symbols:cinematic-blur")
    val segments: List<LuxCinematicSegment> = emptyList(),

) : CinematicEntry {
    override fun create(player: Player): CinematicAction {
        return LuxTemporalAction(player, this)
    }
}

data class LuxCinematicSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
    @Help("The speaker of this lux dialogue.")
    val speaker: Ref<SpeakerEntry> = emptyRef(),
    @Help("The text for this dialogue")
    @MultiLine
    val text: String = "",
) : Segment

class LuxTemporalAction(
    val player: Player,
    val entry: LuxCinematicEntry,
) : SimpleCinematicAction<LuxCinematicSegment>() {

    override val segments: List<LuxCinematicSegment> = entry.segments

    override suspend fun startSegment(segment: LuxCinematicSegment) {
        super.startSegment(segment)

        val npc = segment.speaker.get() as SimpleEntityDefinition
        val data = npc.data.descendants(LuxNpcData::class).firstOrNull()?.get()
        if (data == null) {
            stopSegment(segment)
            logger.severe("No npc data found for ${'$'}{npc.name}")
            return
        }
        val safeDialogueId = entry.id.takeWhile { it.isDigit() }.ifEmpty { abs(entry.id.hashCode()).toString() }

        val dialogueBuilder = Dialogue.Builder()
            .setDialogueID(safeDialogueId)
            .setRange(-1.0)
            .setDialogueSpeed(4)
            .setTypingSound("luxdialogues:luxdialogues.sounds.typing", "MASTER", 1.0, 1.0)
            .setSelectionSound("luxdialogues:luxdialogues.sounds.selection", "MASTER", 1.0, 1.0)
            .setAnswerNumbers(false)
            .setArrowImage("hand", "#cdff29", -7)
            .setDialogueBackgroundImage("dialogue-background", "#ffffff", -60)
            .setAnswerBackgroundImage("answer-background", "#ffffff", 90)
            .setDialogueText("#4f4a3e", 10)
            .setAnswerText("#4f4a3e", 13, "#4f4a3e")
            .setCharacterImage(data.imageName, "#ffffff", -16)
            .setCharacterNameText(data.characterName.parsePlaceholders(player), "#4f4a3e", 20)
            .setNameImage("name-start", "name-mid", "name-end", "#ffffff", 0)
            .setFogImage("fog", "#000000")
            .setEffect("Slownes")
            .setPreventExit(true)
        val pageBuilder = Page.Builder()
            .setID("page-${'$'}{segment.startFrame}")
        segment.text.split("\n").forEach { line ->
            pageBuilder.addLine(line)
        }
        val page = pageBuilder.build()
        dialogueBuilder.addPage(page)
        val dialogue = dialogueBuilder.build()
        LuxDialoguesAPI.getProvider().sendDialogue(player, dialogue, page.id)
    }

    override suspend fun tickSegment(segment: LuxCinematicSegment, frame: Int) {
        super.tickSegment(segment, frame)
        player.stopBlockingActionBar()
    }

    override suspend fun stopSegment(segment: LuxCinematicSegment) {
        super.stopSegment(segment)
        player.startBlockingActionBar()
        LuxDialoguesAPI.getProvider().clearDialogue(player)
    }
}
