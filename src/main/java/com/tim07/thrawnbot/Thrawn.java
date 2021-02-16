package com.tim07.thrawnbot;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import java.awt.*;
import java.util.Random;

/**
 * The Thrawn listener should only reply with a random quote being represented in the quotes param
 * @author u/tim07
 * @see  EmbedBuilder being sent into the original message channel
 */


public class Thrawn extends AbstractMessageCreateListener {

    // Thrawn quotes : Modify here!
    String[] quotes = {
            "It is said that one should keep one's allies within view, and one's enemies within reach.\n" +
            "A valid statement. One must be able to read an ally's strengths, so as to determine how to best use them. One must similarly be able to read his enemy's weaknesses, so as to determine how to best defeat him.\n" +
            "But what of friends?\n" +
            "There is no accepted answer, perhaps true friendship is so exceedingly rare. But I had formulated my own.\n" +
            "A friend need not be kept within sight or within reach. A friend must be allowed the freedom to find and follow his own path. If one is fortunate, those paths will for a time join. But if paths separate, it is comforting to know that a friend still graces the universe with his skills, and his viewpoint, and his present. For if one is remembered by a friend, one is never truly gone.",
            "Thrawn shrugged. \"There are two ways to destroy a person, Jorj. Kill him, or ruin his reputation.\"",
            "All people have regrets. Warriors are no exceptions.\n" +
            "One would hope it was possible to distinguish between events caused by one's carelessness or lack of ability and those caused by circumstances or forces beyond a one's control. But in practice, there is no difference. All forms of regret sear equally into the mind and soul. All forms leave scars of equal bitterness.\n" +
            "And always, beneath the scar, lurks the thought and fear that there was something else that could have been done. Some action, or inaction, that would have changed things for the better. Such questions can sometimes be learned form. All to often, they merely add to the scar tissue.\n" +
            "A warrior must learn to set those regrets aside as best he can. Knowing full well that they will never be far away.",
            "Never make the mistake of believing forbearance equates to acceptance, or that all positions are equally valid.",
            "An enemy will almost never be anything except an enemy. All one can do with an enemy is defeat him. But and adversary can sometimes become an ally.\n" +
            "There is a cost, of course. In all things in life there is a cost. In dealing with an adversary, sometimes the cost paid in power or position.\n" +
            "Sometimes the cost is greater. Sometimes the risk is one's future, or even one's life.\n" +
            "But in all such situations, the calculation is straightforward: whether or not the potential gain is worth the potential loss.\n" +
            "And the warrior must never forget that he and his adversary are not the only ones in that equation. Sometimes, all the universe may hang in the balance.",
            "No battle plan can anticipate all contingencies. There are always unexpected factors including those stemming from the opponent's initiative. A battle must thus becomes a balance between plan and improvisation, between error and correction.\n" +
            "It is a narrow line. But it is a line one's opponent must also walk. For all the balance of experience and cleverness, it is often the warrior who acts quickest who will prevail.",
            "Military leadership is a journey, not a destination. It is continually challenged, and must continually prove it self anew against fresh obstacles. Sometimes those obstacles are external events. Other times they are the doubts of those being led. Still other times they are a result of the leaders's own failures and shortcomings.\n" +
            "Political power and influence are different. Once certain levels have been reached, there is no need to prove leadership or competence. A person with such power is accustomed to having every word carefully considered, and every whim treated as an order. And all who recognize that power know to bow to it.\n" +
            "A few have the courage or the foolishness to resist. Some succeed in standing firm against the storm. More often, they find their paths yet again turned form their hopes for goal.",
            "A great tactician creates plans. A good tactician recognizes the soundness of a plan presented on him. A fair tactician must see the plan succeed before offering approval.\n" +
            "Those with no tactical ability at all may never understand or accept it. Nor will such people understand or accept the tactician. To those without that ability, those who posses it are a mystery.\n" +
            "And when a mind is too deficient in understanding, the resulting gap is often filled with with resentment.",
            "A friend need not be kept either within sight or within reach. A friend must be allowed the freedom to find and follow his own path.\n" +
            "If one is fortunate, those paths will for a time join. But if the paths separate, it is comforting to know that a friend still graces the universe with his skills, and his viewpoint, and his presence.\n" +
            "For if one is remembered by a friend, one is never truly gone.",
            "\"But it was so artistically done...\"",
            "Thrawn: \"Do you know the difference between an error and a mistake, Ensign?\"\n" +
            "Ensign: \"No, sir.\"\n" +
            "Thrawn: \"Anyone can make an error, Ensign. But that error doesn't become a mistake until you refuse to correct it.\" [points at Pietersen, Rukh kills him] \"Dispose of it. The error, Ensign, has now been corrected. You may begin training a replacement.\"",
    "\"I am Grand Admiral Thrawn, Warlord of the Empire, servant of the Emperor. I seek the Guardian of the mountain.\"",
    "\"The only [puzzle] worth solving. The complete, total and utter destruction of the Rebellion.\"",
    "\"The conquering of worlds, of course. The final defeat of the Rebellion. The re-establishment of the glory that was once the Empire's New Order.\"",
    "\"You served too long under Lord Vader, Captain. I Have no qualms about accepting a useful idea merely because it wasn't my own. My position and ego are not at stake here.\"",
    "\"The Empire is at war, Captain. We cannot afford the luxury of men whose minds are so limited they cannot adapt to unexpected situations.\"",
    "\"The insanity of men and aliens who have learned the hard way that they can't match me face-to-face. And so they attempt to use my own tactical skill and insight against me. They pretend to walk into my trap, gambling that I'll notice the subtlety of their movements and interpret that as genuine intent. And while I then congratulate myself on my perception they prepare their actual attack.\""};

    private final Random random = new Random();
    /**
     * Listener on message being pushed into the server channel
     * @param event message with extra information
     */

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        super.onMessageCreate(event);
        if (super.blacklisted){
            return;
        }
        // Checks if message is meant to be here...
        if (event.getMessageContent().equalsIgnoreCase("%thrawn")){
            // Creates random index and sets a quote
            int index = random.nextInt(quotes.length);
            String messageString = quotes[index];
            // Send EmbedBuilder into channel
            event.getChannel().sendMessage(new EmbedBuilder().setTitle("Zitat " + index + " von " + (quotes.length - 1)).setDescription(messageString).setColor(Color.CYAN));
        }
    }
}
