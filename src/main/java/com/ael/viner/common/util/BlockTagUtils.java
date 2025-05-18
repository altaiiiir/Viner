package com.ael.viner.common.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockTagUtils {
  /**
   * Checks if a block ID is present in a list of block IDs.
   *
   * @param blockId The block ID to check (e.g., "minecraft:stone").
   * @param blockIdList The list of block IDs.
   * @return true if present, false otherwise.
   */
  public static boolean isBlockIdInList(String blockId, List<String> blockIdList) {
    return blockIdList.contains(blockId);
  }

  /**
   * Checks if a block ID is present in any of the given tag ID lists.
   *
   * @param blockId The block ID to check.
   * @param tagToBlockIds A mapping from tag ID to a set of block IDs (loader-specific code should
   *     provide this).
   * @param tagIdList The list of tag IDs to check.
   * @return true if the block ID is in any tag, false otherwise.
   */
  public static boolean isBlockIdInTags(
      String blockId,
      List<String> tagIdList,
      java.util.function.Function<String, Set<String>> tagToBlockIds) {
    for (String tagId : tagIdList) {
      Set<String> blockIds = tagToBlockIds.apply(tagId);
      if (blockIds != null && blockIds.contains(blockId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Splits config entries into block IDs and tag IDs. Tag IDs are expected to start with "#" (e.g.,
   * "#minecraft:logs").
   *
   * @param entries The list of config entries.
   * @return A SplitResult containing blockIds and tagIds.
   */
  public static SplitResult splitBlockAndTagIds(List<String> entries) {
    Set<String> blockIds = new HashSet<>();
    Set<String> tagIds = new HashSet<>();
    for (String entry : entries) {
      if (entry.startsWith("#")) {
        tagIds.add(entry.substring(1));
      } else {
        blockIds.add(entry);
      }
    }
    return new SplitResult(blockIds, tagIds);
  }

  public static class SplitResult {
    public final Set<String> blockIds;
    public final Set<String> tagIds;

    public SplitResult(Set<String> blockIds, Set<String> tagIds) {
      this.blockIds = blockIds;
      this.tagIds = tagIds;
    }
  }
}
