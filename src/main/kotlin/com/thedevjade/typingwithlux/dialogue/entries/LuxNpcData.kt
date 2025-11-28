package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.GenericEntityData
import org.bukkit.entity.Player
import java.util.*
import kotlin.reflect.KClass

@Entry("lux_npc_data", "Data regarding what LuxDialogues should use", Colors.RED, "mdi:marker")
@Tags("lux_npc_data", "lux_npc")
class LuxNpcData(
    override val id: String = "",
    override val name: String = "",
    @Help("The name of the NPC")
    @Placeholder
    val characterName: String = "",
    @Help("The image to use with the npc")
    val imageName: String = "",
    @Help("Character image color")
    val characterColor: String = "#ffffff",
    @Help("Character image offset")
    val characterOffset: Int = -16,
    @Help("Typing sound")
    val typingSound: String = "minecraft:entity.armadillo.scute_drop",
    @Help("Typing sound category")
    val typingSoundCategory: String = "MASTER",
    @Help("Typing sound volume")
    val typingSoundVolume: Double = 1.0,
    @Help("Typing sound pitch")
    val typingSoundPitch: Double = 1.0,
    @Help("Selection sound")
    val selectionSound: String = "luxdialogues:luxdialogues.sounds.selection",
    @Help("Selection sound category")
    val selectionSoundCategory: String = "MASTER",
    @Help("Selection sound volume")
    val selectionSoundVolume: Double = 1.0,
    @Help("Selection sound pitch")
    val selectionSoundPitch: Double = 1.0,
    @Help("Arrow image name")
    val arrowImage: String = "hand",
    @Help("Arrow color")
    val arrowColor: String = "#ffffff",
    @Help("Arrow offset")
    val arrowOffset: Int = -7,
    @Help("Dialogue background image name")
    val dialogueBackgroundImage: String = "dialogue-background",
    @Help("Dialogue background color")
    val dialogueBackgroundColor: String = "#ffffff",
    @Help("Dialogue background offset")
    val dialogueBackgroundOffset: Int = -5,
    @Help("Answer background image name")
    val answerBackgroundImage: String = "answer-background",
    @Help("Answer background color")
    val answerBackgroundColor: String = "#ffffff",
    @Help("Answer background offset")
    val answerBackgroundOffset: Int = 160,
    @Help("Dialogue text color")
    val dialogueTextColor: String = "#ffffff",
    @Help("Dialogue text offset")
    val dialogueTextOffset: Int = 40,
    @Help("Answer text color")
    val answerTextColor: String = "#ffffff",
    @Help("Answer text offset")
    val answerTextOffset: Int = 13,
    @Help("Answer selected color")
    val answerSelectedColor: String = "#eba601",
    @Help("Character name color")
    val characterNameColor: String = "#9cf786",
    @Help("Character name offset")
    val characterNameOffset: Int = 2,
    @Help("Name start image")
    val nameStartImage: String = "name-start",
    @Help("Name mid image")
    val nameMidImage: String = "name-mid",
    @Help("Name end image")
    val nameEndImage: String = "name-end",
    @Help("Name background color")
    val nameBackgroundColor: String = "#ffffff",
    @Help("Name image offset")
    val nameImageOffset: Int = 0,
    @Help("Fog image name")
    val fogImage: String = "fog",
    @Help("Fog color")
    val fogColor: String = "#000000",
    @Help("Effect type (Slowness or Freeze)")
    val effect: String = "Slowness",
    @Help("Prevent player from exiting dialogue")
    val preventExit: Boolean = false,
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : GenericEntityData<LuxNpcProperty> {
    override fun type(): KClass<LuxNpcProperty> = LuxNpcProperty::class

    override fun build(player: Player): LuxNpcProperty = LuxNpcProperty(
        characterName, imageName, characterColor, characterOffset,
        typingSound, typingSoundCategory, typingSoundVolume, typingSoundPitch,
        selectionSound, selectionSoundCategory, selectionSoundVolume, selectionSoundPitch,
        arrowImage, arrowColor, arrowOffset,
        dialogueBackgroundImage, dialogueBackgroundColor, dialogueBackgroundOffset,
        answerBackgroundImage, answerBackgroundColor, answerBackgroundOffset,
        dialogueTextColor, dialogueTextOffset,
        answerTextColor, answerTextOffset, answerSelectedColor,
        characterNameColor, characterNameOffset,
        nameStartImage, nameMidImage, nameEndImage, nameBackgroundColor, nameImageOffset,
        fogImage, fogColor, effect, preventExit
    )
}

data class LuxNpcProperty(
    val characterName: String,
    val imageName: String,
    val characterColor: String,
    val characterOffset: Int,
    val typingSound: String,
    val typingSoundCategory: String,
    val typingSoundVolume: Double,
    val typingSoundPitch: Double,
    val selectionSound: String,
    val selectionSoundCategory: String,
    val selectionSoundVolume: Double,
    val selectionSoundPitch: Double,
    val arrowImage: String,
    val arrowColor: String,
    val arrowOffset: Int,
    val dialogueBackgroundImage: String,
    val dialogueBackgroundColor: String,
    val dialogueBackgroundOffset: Int,
    val answerBackgroundImage: String,
    val answerBackgroundColor: String,
    val answerBackgroundOffset: Int,
    val dialogueTextColor: String,
    val dialogueTextOffset: Int,
    val answerTextColor: String,
    val answerTextOffset: Int,
    val answerSelectedColor: String,
    val characterNameColor: String,
    val characterNameOffset: Int,
    val nameStartImage: String,
    val nameMidImage: String,
    val nameEndImage: String,
    val nameBackgroundColor: String,
    val nameImageOffset: Int,
    val fogImage: String,
    val fogColor: String,
    val effect: String,
    val preventExit: Boolean
) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<LuxNpcProperty>(LuxNpcProperty::class)
}
