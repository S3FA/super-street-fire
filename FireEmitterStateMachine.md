# Fire Emitter State Machine #

| | **Off** | **P1\_ATK** | **P2\_ATK** | **P1\_ATK\_P2\_ATK** | **P1\_BLK** | **P2\_BLK** | **P1\_ATK\_BLK** | **P2\_ATK\_BLK** | **Blocked(nextState)** |
|:|:--------|:------------|:------------|:---------------------|:------------|:------------|:-----------------|:-----------------|:-----------------------|
| **On(1, ATK)** |P1\_ATK|P1\_ATK|P1\_ATK\_P2\_ATK|P1\_ATK\_P2\_ATK|P1\_ATK\_BLK| _Blocked(P2\_BLK)_ |P1\_ATK\_BLK| _Blocked(P2\_ATK\_BLK)_ |nextState|
| **On(1, BLK)** |P1\_BLK|P1\_ATK\_BLK| Blocked(P1\_BLK) |Blocked(P1\_ATK\_BLK)|P1\_BLK|ERROR|P1\_ATK\_BLK|ERROR|nextState|
| **On(2, ATK)** |P2\_ATK|P1\_ATK\_P2\_ATK|P2\_ATK|P1\_ATK\_P2\_ATK| _Blocked(P1\_BLK)_ |P2\_ATK\_BLK| _Blocked(P1\_ATK\_BLK)_ |P2\_ATK\_BLK|nextState|
| **On(2, BLK)** |P2\_BLK| Blocked(P2\_BLK)|P2\_ATK\_BLK|Blocked(P2\_ATK\_BLK)|ERROR|P2\_BLK|ERROR|P2\_ATK\_BLK|nextState|
| **Off(1, ATK)** |Off|Off|P2\_ATK|P2\_ATK|P1\_BLK|P2\_BLK|P1\_BLK|P2\_ATK\_BLK|nextState|
| **Off(1, BLK)** |Off|P1\_ATK|P2\_ATK|P1\_ATK\_P2\_ATK|Off|P2\_BLK|P1\_ATK|P2\_ATK\_BLK|nextState|
| **Off(2, ATK)** |Off|P1\_ATK|Off|P1\_ATK|P1\_BLK|P2\_BLK|P1\_ATK\_BLK|P2\_BLK|nextState|
| **Off(2, BLK)** |Off|P1\_ATK|P2\_ATK|P1\_ATK\_P2\_ATK|P1\_BLK|Off|P1\_ATK\_BLK|P2\_ATK|nextState|

## Events ##
**On(1, ATK)** : Player 1 has an attack that has just entered the emitter and is requesting to turn the emitter on so that it has an attack flame owned by player 1.

**On(1, BLK)** : Player 1 has a block that has just entered the emitter and is requesting to turn the emitter on so that it has a block flame owned by player 1.

**On(2, ATK)** : Player 2 has an attack that has just entered the emitter and is requesting to turn the emitter on so that it has an attack flame owned by player 2.

**On(2, BLK)** : Player 2 has a block that has just entered the emitter and is requesting to turn the emitter on so that it has a block flame owned by player 2.

**Off(1, ATK)** : Player 1 has an attack that has just exited the emitter and is requesting to turn the emitter off so that it no longer has an attack flame owned by player 1.

**Off(1, BLK)** : Player 1 has a block that has just exited the emitter and is requesting to turn the emitter off so that it no longer has a block flame owned by player 1.

**Off(2, ATK)** : Player 2 has an attack that has just exited the emitter and is requesting to turn the emitter off so that it no longer has an attack flame owned by player 2.

**Off(2, BLK)** : Player 2 has a block that has just exited the emitter and is requesting to turn the emitter off so that it no longer has a block flame owned by player 2.

## States ##

**Off** : The fire emitter is turned off completely.

**P1\_ATK** : The fire emitter is turned on, only for an attack flame owned by player 1.

**P2\_ATK** : The fire emitter is turned on, only for an attack flame owned by player 2.

**P1\_ATK\_P2\_ATK** : The fire emitter is turned on for attack flames owned by players 1 and 2.

**P1\_BLK** : The fire emitter is turned on, only for a block flame owned by player 1.

**P2\_BLK** : The fire emitter is turned on, only for a block flame owned by player 2.

**P1\_ATK\_BLK** : The fire emitter is turned on for an attack and block flame owned by player 1.

**P2\_ATK\_BLK** : The fire emitter is turned on for an attack and block flame owned by player 2.

**Blocked(nextState)** : A block occurred - this is a transitory state and currently it immediately transitions to the given nextState.

NOTE: Blocks in italics are "perfect" blocks i.e., they existed before the attack got there and are therefore as effective as possible. The other blocks are subject to the game rules on block timing.