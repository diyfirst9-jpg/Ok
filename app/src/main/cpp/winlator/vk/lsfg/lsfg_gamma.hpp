// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: Copyright 2025 lsfg-vk
// SPDX-License-Identifier: GPL-3.0-or-later

#pragma once

#include <array>

#include "lsfg_common.hpp"

namespace lsfg {

class LsfgShaders;

constexpr size_t LSFG_GAMMA_STAGES = 5;
constexpr size_t LSFG_GAMMA_TEMPS = 3;

class LsfgGamma {
public:
    LsfgGamma() = default;
    LsfgGamma(const Device& device, const LsfgShaders& shaders, LsfgResources& resources,
              VkDescriptorPool descriptor_pool, LsfgImageHistory& inputs, LsfgImage& flow_input,
              LsfgImage* previous);

    void Dispatch(VkCommandBuffer cmdbuf, uint64_t frame_count, size_t slot);

    void PushStepBarriers(LsfgBarriers& barriers, uint64_t frame_count, size_t step);

    void DispatchStep(VkCommandBuffer cmdbuf, uint64_t frame_count, size_t slot,
                      size_t step);

    [[nodiscard]] LsfgImage& Output() {
        return out_image;
    }

    [[nodiscard]] bool Valid() const {
        return allocated;
    }

private:
    // --- ECS-style / data-oriented layout --------------------------------
    // "Entity" = one generation slot. Its two descriptor-set groups used
    // to be bundled together as fields of a single Generation struct held
    // in one array-of-structs. They're split into their own flat
    // "component" tables here so DispatchStep()'s hot lookup for a given
    // slot never has to load the other group's bytes into cache along
    // the way.
    LsfgImageHistory* inputs{};
    LsfgImage* flow_input{};
    LsfgImage* previous{};

    std::array<LsfgPass, LSFG_GAMMA_STAGES> passes;

    // Component tables, indexed by generation slot.
    std::array<std::array<VkDescriptorSet, LSFG_HISTORY_SLOTS>, LSFG_GENERATION_SLOTS>
        first_descriptor_sets{};
    std::array<std::array<VkDescriptorSet, LSFG_GAMMA_STAGES - 1>, LSFG_GENERATION_SLOTS>
        descriptor_sets{};

    std::array<LsfgImage, LSFG_GAMMA_TEMPS> temp1;
    LsfgImagePair temp2;
    LsfgImage out_image;
    bool allocated{};
};

}
