package com.nightbeam.remnants.ballistics.fabric.client;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** Gives a vanilla reload listener the id Fabric's resource manager helper requires. */
record IdentifiedReloadListener(ResourceLocation id, PreparableReloadListener delegate) implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return id;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepareProfiler,
                                          ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return delegate.reload(barrier, manager, prepareProfiler, applyProfiler, backgroundExecutor, gameExecutor);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
