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
import com.typewritermc.engine.paper.entry.entries.EntryTrigger
import com.typewritermc.engine.paper.entry.entries.EventTrigger
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.entries.Var
import org.bukkit.entity.Player
import kotlin.time.Duration

@Entry("lux_dialogue_options", "A LuxDialogues message that contains options", Colors.BLUE, "material-symbols:chat-rounded")
class OptionsLuxDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val speaker: Ref<SpeakerEntry> = emptyRef(),
    @Placeholder
    @MultiLine
    @Help("The text for the options.")
    val text: String = "",
    @Help("The options for this lux dialogue.")
    val options: List<LuxOption> = emptyList(),
    @Help("The duration it takes to type out the message. If the duration is zero, the message will be displayed instantly.")
    val duration: Var<java.time.Duration> = ConstVar(java.time.Duration.ZERO),
) : DialogueEntry {
    // May return null to skip the dialogue
    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<*>? {
        // You can use if statements to return a different messenger depending on different conditions

        return OptionsLuxDialogueHandler(player, context, this)
    }
}

data class LuxOption(
    @Help("Text for this option.")
    val text: Var<String> = ConstVar(""),
    @Help("The criteria that must be met for this option to show.")
    val criteria: List<Criteria> = emptyList(),
    @Help("The modifiers to apply when this option is chosen.")
    val modifiers: List<Modifier> = emptyList(),
    @Help("The triggers to fire when this option is chosen.")
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The ID of the page to navigate to after selecting this option.")
    val goTo: String = "",
    @Help("Reply messages to display after selecting this option.")
    val replyMessages: List<String> = emptyList()
) {
    val eventTriggers: List<EventTrigger> get() = triggers.map(::EntryTrigger)
}