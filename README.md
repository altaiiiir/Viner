# Viner Mod for Minecraft

## Overview

Viner is an innovative Minecraft mod inspired by the classic Veinminer, designed to enhance the mining experience by enabling players to mine entire veins of blocks in one go. This efficiency-focused mod saves time and effort, making resource gathering more enjoyable and less tedious.

## Features

- **Vein Mining Capability:** Allows players to mine all connected blocks of the same type, significantly speeding up mining activities.
- **Customizable Block Lists:** Users can configure which blocks are vein-minable via the mod's settings, tailoring the experience to their preferences.
- **Advanced Packet Handling:** Implements custom network packets, such as `VeinMiningPacket`, `VinerKeyPressedPacket`, and `MouseScrollPacket`, to manage data transmission efficiently between client and server.
- **Dynamic Registries:** Utilizes `VinerBlockRegistry` and `VinerPlayerRegistry` for flexible and efficient block and player management.
- **Optimized Mining Algorithms:** Employs sophisticated algorithms to determine vein boundaries and ensure efficient mining without unnecessary block checks.
- **Mining Algorithm:** Users can configure the shape of the blocks to mine by implementing a configurable block traversal algorithm.
- **Event-Driven System:** Leverages Minecraft's event system to hook into various game actions, ensuring seamless integration and responsive gameplay.

## Technical Highlights

### Networking
The mod introduces an efficient networking layer to handle custom actions like vein mining activation and configuration toggling. By using abstract packet classes like `AbstractPacket`, the mod establishes a robust system for sending and receiving data packets, ensuring smooth operation even in multiplayer environments.

### Event-Based Architecture
Viner operates on an event-driven architecture, listening for specific game events (e.g., key presses, block breaks) to trigger vein mining logic. This approach allows for high modularity and easier maintenance, as each component reacts to events independently.

### Algorithms and Efficiency
At the core of Viner's vein mining logic are algorithms designed to quickly identify connected blocks of the same type. This involves traversing the block grid in a manner that minimizes unnecessary checks, using efficient data structures to keep track of visited and pending blocks.

## Installation

1. Ensure Minecraft and Forge are installed.
2. Download the latest Viner mod release.
3. Place the mod file in your Minecraft's `mods` folder.
4. Launch the game and configure the mod to your liking through the config file `viner-common.toml`.

## Configuration

Viner's settings can be customized from the in-game mod configuration menu or by editing the configuration file directly. This includes specifying which blocks are vein-minable, adjusting key bindings, customizable mining shapes, and more.

## Contributing

Contributions are welcome! Whether it's reporting bugs, suggesting features, or contributing code, feel free to open an issue or pull request on our GitHub repository.

## License

Viner is distributed under GNU General Public License v3.0 only. Please refer to the GNU General Public License v3.0 only file for more details.

