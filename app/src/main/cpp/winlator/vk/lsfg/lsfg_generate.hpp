// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: Copyright 2025 lsfg-vk
// SPDX-License-Identifier: GPL-3.0-or-later

#pragma once

#include <array>

#include "lsfg_common.hpp"

namespace lsfg {

class LsfgShaders;

class LsfgGenerate {
public:
    LsfgGenerate() = default;
    LsfgGenerate(const Device& device, const LsfgShaders& shaders, LsfgResources& resources,
                 VkDescriptorPool descriptor_pool, LsfgImagePair& frames, LsfgImage& motion,
                 LsfgImage& detail1, LsfgImage& detail2);

    void SetTarget(const Device& device, size_t slot, uint32_t target, VkImageView view);

    void ForgetTargets();

    void Dispatch(VkCommandBuffer cmdbuf, uint64_t frame_count, size_t slot, uint32_t target,
                  VkImage image, VkExtent2D extent);

    [[nodiscard]] bool Valid() const {
        return pass.Valid() && allocated;
    }

private:
    // --- ECS-style / data-oriented layout --------------------------------
    // Old layout was array-of-structs, three levels deep:
    //   generations[slot].targets[target].{descriptor_sets, view}
    //   generations[slot].buffer
    // Every Dispatch() call had to walk that chain just to reach two
    // descriptor sets it actually uses.
    //
    // Here each "entity" is one (slot, target) pair, and each field it
    // owns is its own flat, contiguous "component" array instead of a
    // field inside a nested struct:
    //   - descriptor_sets: HOT, read every Dispatch() call
    //   - views:           COLD, written only by SetTarget()/ForgetTargets()
    //   - buffers:         COLD, written once at construction, one per slot
    // Separating hot from cold means the hot path only ever touches the
    // one array it needs, packed tightly with no unrelated bytes (like a
    // rarely-read VkImageView) sharing its cache lines.
    static constexpr size_t ENTITY_COUNT = LSFG_GENERATION_SLOTS * LSFG_MAX_TARGETS;

    [[nodiscard]] static constexpr size_t EntityIndex(size_t slot, uint32_t target) {
        return slot * LSFG_MAX_TARGETS + target;
    }

    LsfgImagePair* frames{};
    LsfgImage* motion{};
    LsfgImage* detail1{};
    LsfgImage* detail2{};
    VkSampler sampler{VK_NULL_HANDLE};
    VkSampler edge_sampler{VK_NULL_HANDLE};

    LsfgPass pass;

    // Component tables, indexed by EntityIndex(slot, target).
    std::array<std::array<VkDescriptorSet, 2>, ENTITY_COUNT> descriptor_sets{};
    std::array<VkImageView, ENTITY_COUNT> views{};
    // One buffer per generation slot only (shared across that slot's targets).
    std::array<VkBuffer, LSFG_GENERATION_SLOTS> buffers{};

    bool allocated{};
};

}
