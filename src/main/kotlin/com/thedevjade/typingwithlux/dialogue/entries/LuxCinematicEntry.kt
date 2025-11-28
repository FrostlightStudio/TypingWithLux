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
import com.typewritermc.entity.entries.entity.custom.NpcDefinition
import com.typewritermc.entity.entries.entity.custom.NpcInstance
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

        val speaker = segment.speaker.get()
        val data = when (speaker) {
            is NpcDefinition -> speaker.data.descendants(LuxNpcData::class).firstOrNull()?.get()
            is NpcInstance -> speaker.definition.get()?.data?.descendants(LuxNpcData::class)?.firstOrNull()?.get()
            is SimpleEntityDefinition -> speaker.data.descendants(LuxNpcData::class).firstOrNull()?.get()
            else -> null
        }
        if (data == null) {
            stopSegment(segment)
            logger.severe("No npc data found for speaker")
            return
        }
        val safeDialogueId = entry.id.takeWhile { it.isDigit() }.ifEmpty { abs(entry.id.hashCode()).toString() }

        val dialogueBuilder = Dialogue.Builder()
            .setDialogueID(safeDialogueId)
            .setRange(-1.0)
            .setDialogueSpeed(4)
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
