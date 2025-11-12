package com.ael.viner.common.util;

import java.util.*;
import java.util.function.Function;

public class VineMiningLogic {
  /**
   * Pure Java logic for collecting connected blocks of the same block ID.
   *
   * @param start The starting position.
   * @param blockId The block ID to match.
   * @param blockIdAt Function to get the block ID at a given position.
   * @param vineableLimit The maximum number of blocks to collect.
   * @return List of connected positions.
   */
  public static List<Position> collectConnectedBlocks(
      Position start, String blockId, Function<Position, String> blockIdAt, int vineableLimit) {
    List<Position> connected = new ArrayList<>();
    Set<Position> visited = new HashSet<>();
    Queue<Position> queue = new LinkedList<>();
    queue.add(start);

    while (!queue.isEmpty() && connected.size() < vineableLimit) {
      Position current = queue.poll();
      if (visited.contains(current) || !blockId.equals(blockIdAt.apply(current))) continue;
      visited.add(current);
      connected.add(current);

      // Add all adjacent positions (6 directions)
      for (int dx = -1; dx <= 1; dx++) {
        for (int dy = -1; dy <= 1; dy++) {
          for (int dz = -1; dz <= 1; dz++) {
            if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) == 1) {
              queue.add(new Position(current.x() + dx, current.y() + dy, current.z() + dz));
            }
          }
        }
      }
    }
    return connected;
  }
}
