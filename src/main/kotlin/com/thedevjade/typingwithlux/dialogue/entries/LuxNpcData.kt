package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entity.SinglePropertyCollectorSupplier
import com.typewritermc.engine.paper.entry.entries.EntityData
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.GenericEntityData
import io.papermc.paper.datacomponent.item.attribute.AttributeModifierDisplay.override
import net.kyori.adventure.sound.Sound.sound
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
    @Help("The sound to use with the npc")
    val soundName: String = "luxdialogues:luxdialogues.sounds.typing",
    override val priorityOverride: Optional<Int> = Optional.empty(),
) : GenericEntityData<LuxNpcProperty> {
    override fun type(): KClass<LuxNpcProperty> = LuxNpcProperty::class

    override fun build(player: Player): LuxNpcProperty = LuxNpcProperty(characterName, imageName, soundName)
}

data class LuxNpcProperty(val name: String, val image: String, val sound: String) : EntityProperty {
    companion object : SinglePropertyCollectorSupplier<LuxNpcProperty>(LuxNpcProperty::class)
}