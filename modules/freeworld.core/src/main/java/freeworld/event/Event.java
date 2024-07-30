/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.event;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Event<T> {
    private final Sinks.Many<T> sink = Sinks.many().multicast().onBackpressureBuffer();
    private final Flux<T> flux = sink.asFlux();

    public Disposable subscribe(Consumer<T> listener) {
        return flux.subscribe(listener);
    }

    public void publish(T event) {
        sink.tryEmitNext(event);
    }
}
