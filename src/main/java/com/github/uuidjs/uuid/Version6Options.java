package com.github.uuidjs.uuid;

/**
 * Port of {@code export type Version6Options = Version1Options;}.
 *
 * <p>TypeScript expresses this as a pure type alias. Java has no type aliases, so
 * this is a subclass that adds nothing — it exists only to preserve the public API
 * surface. {@code V6.v6(...)} accepts {@link Version1Options}, so the two are
 * interchangeable exactly as in the original.
 */
public final class Version6Options extends Version1Options {}
