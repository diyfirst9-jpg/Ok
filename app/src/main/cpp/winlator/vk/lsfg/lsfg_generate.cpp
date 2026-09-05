// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: Copyright 2025 lsfg-vk
// SPDX-License-Identifier: GPL-3.0-or-later

#include "lsfg_generate.hpp"
#include "lsfg_dll.h"
#include "lsfg_shaders.hpp"

#include <vector>

namespace lsfg {

namespace {

constexpr uint32_t DISPATCH_TILE_SHIFT = 4;

[[nodiscard]] uint32_t GroupCount(uint32_t size) {
    return (size + (1u << DISPATCH_TILE_SHIFT) - 1) >> DISPATCH_TILE_SHIFT;
}

VkImageMemoryBarrier MakeTargetBarrier(VkImage image, VkAccessFlags src_access,
                                       VkAccessFlags dst_access, VkImageLayout old_layout) {
    VkImageMemoryBarrier barrier{};
    barrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    barrier.srcAccessMask = src_access;
    barrier.dstAccessMask = dst_access;
    barrier.oldLayout = old_layout;
    barrier.newLayout = VK_IMAGE_LAYOUT_GENERAL;
    barrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.image = image;
    barrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    barrier.subresourceRange.levelCount = 1;
    barrier.subresourceRange.layerCount = 1;
    return barrier;
}

}

LsfgGenerate::LsfgGenerate(const Device& device, const LsfgShaders& shaders,
                           LsfgResources& resources, VkDescriptorPool descriptor_pool,
                           LsfgImagePair& frames_, LsfgImage& motion_, LsfgImage& detail1_,
                           LsfgImage& detail2_)
    : frames{&frames_}, motion{&motion_}, detail1{&detail1_}, detail2{&detail2_} {
    pass = LsfgPass(device, shaders, LSFG_SHADER_GENERATE,
                    {{1, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER},
                     {2, VK_DESCRIPTOR_TYPE_SAMPLER},
                     {5, VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE},
                     {1, VK_DESCRIPTOR_TYPE_STORAGE_IMAGE}});
    if (!pass.Valid()) return;

    sampler = resources.GetSampler();
    edge_sampler =
        resources.GetSampler(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE, VK_COMPARE_OP_ALWAYS, false);

    // Cold, per-slot component: filled once, sequentially.
    for (size_t slot = 0; slot < LSFG_GENERATION_SLOTS; ++slot)
        buffers[slot] = resources.GetBuffer(LsfgSlotTimestamp(slot));

    const uint32_t total = static_cast<uint32_t>(ENTITY_COUNT * 2);
    const std::vector<VkDescriptorSet> sets =
        AllocateLsfgDescriptorSets(device, descriptor_pool, pass.SetLayout(), total);
    if (sets.size() != total) return;

    // Hot component: one flat, sequential fill instead of a nested walk.
    size_t next = 0;
    for (auto& entity_sets : descriptor_sets)
        for (auto& set : entity_sets)
            set = sets[next++];

    allocated = true;
}

void LsfgGenerate::SetTarget(const Device& device, size_t slot, uint32_t target,
                             VkImageView view) {
    const size_t entity = EntityIndex(slot, target);
    if (views[entity] == view) return;
    views[entity] = view;

    auto& sets = descriptor_sets[entity];
    for (size_t i = 0; i < sets.size(); ++i) {
        LsfgDescriptorWriter(sets[i])
            .AddUniformBuffer(buffers[slot], LsfgResources::BufferSize())
            .AddSampler(sampler)
            .AddSampler(edge_sampler)
            .AddSampledImage((*frames)[1 - i])
            .AddSampledImage((*frames)[i])
            .AddSampledImage(*motion)
            .AddSampledImage(*detail1)
            .AddSampledImage(*detail2)
            .AddStorageView(view)
            .Build(device);
    }
}

void LsfgGenerate::ForgetTargets() {
    views.fill(VK_NULL_HANDLE); // one contiguous sweep instead of nested loops
}

void LsfgGenerate::Dispatch(VkCommandBuffer cmdbuf, uint64_t frame_count, size_t slot,
                            uint32_t target, VkImage image, VkExtent2D extent) {
    const auto& sets = descriptor_sets[EntityIndex(slot, target)];

    LsfgBarriers(cmdbuf)
        .WriteToReadAll(*frames)
        .WriteToRead(*motion)
        .WriteToRead(*detail1)
        .WriteToRead(*detail2)
        .DiscardToWrite(image)
        .Build();

    pass.Bind(cmdbuf, sets[frame_count % sets.size()]);
    vkd.CmdDispatch(cmdbuf, GroupCount(extent.width), GroupCount(extent.height), 1);

    const VkImageMemoryBarrier after = MakeTargetBarrier(
        image, VK_ACCESS_SHADER_WRITE_BIT,
        VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_TRANSFER_READ_BIT,
        VK_IMAGE_LAYOUT_GENERAL);
    vkd.CmdPipelineBarrier(cmdbuf, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                         VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT |
                             VK_PIPELINE_STAGE_TRANSFER_BIT,
                         0, 0, nullptr, 0, nullptr, 1, &after);
}

}
