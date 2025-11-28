package com.thedevjade.typingwithlux.dialogue.entries

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.entries.Var
import org.bukkit.entity.Player
import kotlin.time.Duration

@Entry("lux_dialogue_message", "A regular LuxDialogue that does not contain options", Colors.BLUE, "material-symbols:chat-rounded")
class RegularLuxDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val speaker: Ref<SpeakerEntry> = emptyRef(),
    @MultiLine
    @Placeholder
    @Help("The text to display to the player.")
    val text: String = "",
    @Help("The duration it takes to type out the message. If the duration is zero, the message will be displayed instantly.")
    val duration: Var<java.time.Duration> = ConstVar(java.time.Duration.ZERO),
    @Help("Optional page ID to navigate to after this dialogue finishes.")
    val goTo: String = "",
    @Help("Optional timer (in ticks) to automatically close the dialogue.")
    val timer: Int = 0,
) : DialogueEntry {
    // May return null to skip the dialogue
    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<*>? {
        // You can use if statements to return a different messenger depending on different conditions

        return RegularLuxDialogueHandler(player, context, this)
    }
}