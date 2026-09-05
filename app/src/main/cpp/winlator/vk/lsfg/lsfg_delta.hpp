// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: Copyright 2025 lsfg-vk
// SPDX-License-Identifier: GPL-3.0-or-later

#pragma once

#include <array>

#include "lsfg_common.hpp"

namespace lsfg {

class LsfgShaders;

constexpr size_t LSFG_DELTA_STAGES = 10;
constexpr size_t LSFG_DELTA_TEMPS = 3;

class LsfgDelta {
public:
    LsfgDelta() = default;
    LsfgDelta(const Device& device, const LsfgShaders& shaders, LsfgResources& resources,
              VkDescriptorPool descriptor_pool, LsfgImageHistory& inputs, LsfgImage& flow_input,
              LsfgImage* previous_gamma, LsfgImage* previous1, LsfgImage* previous2);

    void Dispatch(VkCommandBuffer cmdbuf, uint64_t frame_count, size_t slot);

    void PushStepBarriers(LsfgBarriers& barriers, uint64_t frame_count, size_t step);

    void DispatchStep(VkCommandBuffer cmdbuf, uint64_t frame_count, size_t slot,
                      size_t step);

    [[nodiscard]] LsfgImage& Output1() {
        return out_image1;
    }

    [[nodiscard]] LsfgImage& Output2() {
        return out_image2;
    }

    [[nodiscard]] bool Valid() const {
        return allocated;
    }

private:
    // --- ECS-style / data-oriented layout --------------------------------
    // "Entity" = one generation slot. Its three descriptor-set groups
    // used to be fields of a single Generation struct held in one
    // array-of-structs. Split into their own flat "component" tables so
    // DispatchStep()'s hot lookup for a given slot/step only ever touches
    // the one group it actually binds.
    LsfgImageHistory* inputs{};
    LsfgImage* flow_input{};
    LsfgImage* previous_gamma{};
    LsfgImage* previous1{};
    LsfgImage* previous2{};

    std::array<LsfgPass, LSFG_DELTA_STAGES> passes;

    // Component tables, indexed by generation slot.
    std::array<std::array<VkDescriptorSet, LSFG_HISTORY_SLOTS>, LSFG_GENERATION_SLOTS>
        first_descriptor_sets{};
    std::array<std::array<VkDescriptorSet, LSFG_HISTORY_SLOTS>, LSFG_GENERATION_SLOTS>
        sixth_descriptor_sets{};
    std::array<std::array<VkDescriptorSet, LSFG_DELTA_STAGES - 2>, LSFG_GENERATION_SLOTS>
        descriptor_sets{};

    std::array<LsfgImage, LSFG_DELTA_TEMPS> temp1;
    LsfgImagePair temp2;
    LsfgImage out_image1;
    LsfgImage out_image2;
    bool allocated{};
};

}
