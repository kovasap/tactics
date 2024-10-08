# Design

## TODOS

 - design one dungeon and make it playable

## Combat

### Turn Order

All players (e.g. you, your opponent) have one character take their turn before
giving control to the next player.
They can choose any character to act, except those that are "exhausted".
A character becomes exhausted after they act, and becomes "refreshed" after a
certain amount of "time", depending on their air affinity.
"Time" per turn is determined by the total number of units left on the
battlefield, to prevent you fielding more units that just do nothing to have
your favorite units take more turns.

To start with, the time to refresh will be set such that most of the time every
character on the field must be used once before any refresh.

Some characters will have abilities that take place "on refresh" that occur
whether they take their turn or not.
Refreshing happens at regular intervals even if a character is not exhausted.

### Prescience

There is a toggle that will let you see the enemy intention at any given time
(the move they would take next).
I'm not sure yet if this should be viewable by the player in certain situations
or not.

### Actions

On every turn, each character has action points that they can use to use
actions.
Movement happens via actions; all characters have a "walk" action to move
between tiles.

#### Action Queue

When the player declares they want to make an action (or multiple actions) on
their turn, these actions are added to a queue of pending actions.
These pending actions are shown with a greyed out UI.
These pending actions are also used to calculate the opponent's action, which is
also optionally shown with a greyed out UI.

#### UI

When hovering over a tile (or its contents), the actions that can be used on
that tile (or its contents) are overlayed on that tile so they can be easily
clicked.

### Attacking and Defending

Abilities that do damage to a tile's contents have "accuracy" and "damage".

Accuracy targets the "poise" defense, and damage targets the "health" defense.
All poise must be overcome before damage is done.
Poise and rarely health can be recovered "on refresh".

#### Randomness

Ideally the game would have no to-hit chances.
If it looks like they are necessary, every skill should have a specific
condition that guarantees a hit.
For example, a spear strike at a range of two, or a sword slash when you have an
adjacent ally.

### Zone of Control

Melee classes exert a zone of control based on the melee attacks they can
perform.
Enemies cannot move out of it without using a "dodge" ability.

## Progression

Leveling should be more chunked, where character power level doesn't change as
the player grows them in various ways.
Then once a set of conditions is fulfilled, the character levels.
this is in opposition to a more gradual curve where characters get stronger bit
by bit.

Additionally, leveling should be set up so that you can't "grind" against lower
level enemies to become overpowered and trivialize higher level encounters.
This could be implemented by having a separate XP bar for each specific source
of XP (e.g. a monster type).
Once some number of the same XP source has been defeated, the XP bar for that
source "maxes out", and the character can no longer get ANY XP from that source.
This can be communicated to the player as "Character X has learned all they can
from this monster", or "Character X has mastered this monster".

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

All classes/jobs have a suite of abilities.
There will be many "restrictive" abilities that can only be used in certain
contexts (e.g. on an enemy flanked by multiple friendlies).
This should give each class a very unique play style, and make them useless in
certain situations if the game state wasn't set up for them properly in prior
turns.

Characters can level classes/jobs by taking them as their primary class/job, and
they can have a secondary to gain access to more abilities (like in horizon's
gate).
However in order to gain access to classes the character must have the proper
affinity.

## Overworld

The world contains many dungeons for the player to explore with different enemies, hazards, etc.
Each dungeon has multiple combat encounter "floors" each from a set of scenarios like:

 - Kill all monsters
 - Defend an object
 - Reach a certain tile

Characters that die in a dungeon will die permanently.
At any time, you can "escape" from a dungeon with all the loot you have so far.
Dungeons should be designed in such a way that it's expected for players to
retreat many times before finally completing the dungeon.

After encounters, random loot will be generated for the player.
Additionally, new classes / recruits can be found in the dungeons.


### World Layout

The world is a sprawling continent with six radially distributed regions, each
with affinity to a different element.
In the center of the world there is a large region that has no constant dominant
element, but rather goes through cycles of dominance over time.
The world has six seasons, one for each element, and the central region is
swayed to one element or another based on the season.


## Story - Old, probably wont use

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


