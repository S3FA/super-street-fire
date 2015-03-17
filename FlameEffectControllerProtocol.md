# Introduction #

Each flame effect will have a controller node, linked by a serial (RS-485) bus. The server will be able to communicate with the flame effect controllers by writing data to the serial interface (USB->RS-485) and read data back about their status from the same interface.

This is a work in progress, but will likely be very similar if not the same to what is described here now.

# Details #

The server has device ID 0, and the 32 flame effect controllers have device ID 1 - 32. Currently, the position of the nodes is undetermined.

The packets look like this:

`0xAA 0xAA [1 byte length] [variable payload] [1 byte checksum]`

  * Packets start with two framing bytes 0xAA 0xAA.
  * One byte is sent for the length of the packet payload. This does not include the framing bytes, the length, or the checksum. The maximum packet length is 11 (0x0B), as this is the maximum command size according to the current spec.
  * The payload consists of [length](length.md) bytes.
  * The checksum is one byte and computed by summing each of the payload bytes, then taking the one's complement of the sum.

Packet payloads from the server to the nodes are formed as follows:

`[dest node] ([command] [value]?)+`

Each field is one byte.

The [node](dest.md) field is the address of the node to act on the command. All nodes will see the command due the serial bus. Special values include:
  * 0x00 - server
  * 0xFF - broadcast

The possible command/value pairs are:
  * `'1' [value]` - sets flame effect output 1 to `[value]` %. `[value]` should be between 0 and 100 inclusive. Other values are ignored. If PWM is not configured for the flame effect, all non-zero values are considered ON.
  * `'2' [value]` - sets flame effect output 2 to `[value]` %. `[value]` should be between 0 and 100 inclusive. Other values are ignored. If PWM is not configured for the flame effect, all non-zero values are considered ON.
  * `'3' [value]` - sets flame effect output 3 to `[value]` %. `[value]` should be between 0 and 100 inclusive. Other values are ignored. If PWM is not configured for the flame effect, all non-zero values are considered ON.
  * `'4' [value]` - sets flame effect output 4 to `[value]` %. `[value]` should be between 0 and 100 inclusive. Other values are ignored. If PWM is not configured for the flame effect, all non-zero values are considered ON.
  * `'A' [value]` - sets AC output for hot surface igniter to ON if `[value]` == '1', to OFF if `[value]` == '0'. Other values are ignored.
  * `'E'` - emergency stop, turns all flame effect outputs and AC output off.
  * `'?'` - query board status.

Don't repeat the same command twice in a packet.

The broadcast address should be used particularly for the hot surface igniter control and the ESTOP command, but might make for some fun "fire everything" effects.

Flame effect nodes can return their status to the server. This can be used to determine whether the boards are active and working or to get more detailed information. This should not be used with the broadcast address, as all the boards will respond at the same time. These packets take the following format for the payload, using the same framing + length header and checksum trailer:

`[dest node = 0] [source node] 'S' [armed status] 'F' [flame status]`

  * The destination node is the server (0).
  * The source node is the node ID.
  * 'S' `[armed status]` - sends '0' if solenoid power is not present, '1' if present.
  * 'F' `[flame status]` - sends '0' if no fire is present, '1' if present, '?' if unknown.

There will also be other devices on the network:

  * 33 - Air cannon and platform lights for player 1 platform (air cannon on channel 1) - same as flame effect controller
  * 34 - Air cannon and platform lights for player 2 platform (air cannon on channel 1) - same as flame effect controller
  * 35 - Life bars and timer for player 1 platform
  * 36 - Life bars and timer for player 2 platform

The payload format for the life bars and timer is:

`[dest node] [2 bytes P1 life] [2 bytes P2 life] [2 bytes timer]`

The 2 bytes correspond to the bytes shifted out by the wifire16 boards (modified to use the wired interface).