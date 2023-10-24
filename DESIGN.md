# Design

## Gameplay

Grid based tactics with phased turns.  All of your characters move, then all opposing characters move.

When moving your character (hovering over possible tiles to move to), all enemy movements will be shown as ghost images on the tiles they would move to, given your proposed move.  Upon ending your turn, the opponent's moves will execute exactly as shown.  This is explained as your main character having a prescience granted to them by a being important to the story.

## Progression

Leveling should be more chunked, where character power level doesn't change as the player grows them in various ways.  Then once a set of conditions is fulfilled, the character levels.  this is in opposition to a more gradual curve where characters get stronger bit by bit.  

Every story scenario should have enemy characters one chunk above the last scenario.  This makes it a implicit objective for the player to make sure each of their characters is leveled one Chunk in each scenario to keep up.

### Character Development and Classes

Characters have affinities for different elements, as inspired by
https://github.com/kovasap/journey_game/blob/master/spec/spec.md#elements.

See detailed affinity descriptions:

https://github.com/kovasap/tactics/blob/7e33ae9d132f9ece8dc1b5664b5df067e8dca173/src/main/app/interface/view/character.cljs#L138-L191

Affinities will let characters adopt classes.  The classes need to be unlocked first via quest scenarios discovered on the world map.  See classes:

https://github.com/kovasap/tactics/blob/7e33ae9d132f9ece8dc1b5664b5df067e8dca173/src/main/app/interface/constant_game_data.cljs#L24-L75

Unlike how it is in the code right now, classes should each have a hardcoded set of stats (as opposed to affinities defining stats).  This should:

1. Make it clearer that "if a character is this class, they behave like this"
1. Add a gameplay element of, before each schenario, making sure you have a good
   party composition by changing character classes.

To give characters more uniqueness, they could each have a "favored" set of affinities that give them _minor_ bonuses to certain stats regardless of class.

## Overworld

Similar to Wargroove, you advance to the next chapter by travelling on an
overworld.
However, unlike wargroove, there are more chapters spiderwebbing out into
different biomes.
After completing optional chapters in these biomes, NPCs will teach you about
new classes your characters can now adopt.

### World Layout

The world is a sprawling continent with six radially distributed regions, each
with affinity to a different element.
In the center of the world there is a large region that has no constant dominant
element, but rather goes through cycles of dominance over time.
The world has six seasons, one for each element, and the central region is
swayed to one element or another based on the season.


## Story

Main character has the ability to see the immediate intentions of others.

At first, they just notice this ability and start using it to prevent people
from doing bad things.
Eventually they uncover a deep conspiracy.
When investigating the conspiracy, there is a pivotal moment where their
intention reading seems to have been wrong (and maybe some small moments leading
up to this)!

...

Eventually they learn that their intention reading is granted to them by a
spirit/demon that will sometimes warp what they see to serve their own
intentions.

### Outline

#### Preface

Florian is woken up by his roommate Leo - they planned to go to the market
together and are running late.

#### Chapter 1: Scrap at the Market

As they get to the market, Florian has a vision of someone assassinating someone
else before it happens.  He reflexively bumps the assassin and draws his anger.

The assassin accuses Floian of "being in league with the academy heretics".

The would be target, Asmond, joins up with Florian and Leo as they fight off the
assassins.

The battle involves killing one assassin and then fleeing to a safe tile as a
much more powerful assassin chases them.

After the battle, Asmond, Florian, and Leo regroup and try to figure out what is
going on.  They decide to go to the city gaurd to report the incident.

#### Chapter 2: Corrupted

The gaurds immediately jail them for the murder of the one assassin.

While they are in jail, some other assassins come to finish them off.

Of of the assassins, Viriis has some doubts about what their co-assassins are
doing.
If someone talks to her during the fight she will join the party.

Upon defeating the assassins, they all decide to flee the city.

#### Chapter 3: Survival and Answers

The group get a room in a local village inn.
This inn promptly gets attacked by bandits.

One of the bandits is recruitable.

#### Chapter 4:

#### Chapter 5:


## Characters

### Florian

Main Character.  A scholar studying the nature of light at Esruia university.


### Leo

Florian's roommate.  A skirmisher studying to be an officer in the army.


### Asmond

Classmate studying biology.  A necromancer (come up with better name for this class).


### Viriis

Assassin hired off the street to kill Florian and co.


