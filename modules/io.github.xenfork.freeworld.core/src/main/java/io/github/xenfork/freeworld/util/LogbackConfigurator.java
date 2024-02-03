/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Logback configurator
 *
 * @author squid233
 * @since 0.1.0
 */
public final class LogbackConfigurator extends ContextAwareBase implements Configurator {
    private static final String DISABLE_ANSI = "freeworld.log.disableAnsi";
    private static final String PATTERN = "[%d{HH:mm:ss}] %-5level [%thread] \\(%logger{36}\\) %msg\\n";
    private static final String PATTERN_ANSI = "%cyan([%d{HH:mm:ss}]) %levelColor(%-5level) %cyan([%thread]) %green(\\(%logger{36}\\)) %levelColor(%msg)\\n";

    /**
     * Level filter that only accepts info or higher level
     *
     * @author squid233
     * @since 0.1.0
     */
    public static final class LevelFilter extends AbstractMatcherFilter<ILoggingEvent> {
        @Override
        public FilterReply decide(ILoggingEvent event) {
            return event.getLevel().isGreaterOrEqual(Level.INFO) ?
                FilterReply.ACCEPT :
                FilterReply.DENY;
        }
    }

    @Override
    public ExecutionStatus configure(LoggerContext loggerContext) {
        final PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        final String disableAnsi = System.getProperty(DISABLE_ANSI);
        if ((disableAnsi != null && disableAnsi.isBlank()) ||
            Boolean.parseBoolean(disableAnsi)) {
            layout.setPattern(PATTERN);
        } else {
            layout.setPattern(PATTERN_ANSI);
        }
        layout.getInstanceConverterMap().put("levelColor", LevelColorConverter.class.getName());
        layout.start();
        final var encoder = new LayoutWrappingEncoder<ILoggingEvent>();
        encoder.setContext(context);
        encoder.setLayout(layout);
        final var ca = new ConsoleAppender<ILoggingEvent>();
        ca.setContext(context);
        ca.setName("console");
        ca.setEncoder(encoder);
        ca.addFilter(new LevelFilter());
        ca.start();

        final PatternLayout fileLayout = new PatternLayout();
        fileLayout.setContext(context);
        fileLayout.setPattern(PATTERN);
        fileLayout.start();
        final var fileEncoder = new LayoutWrappingEncoder<ILoggingEvent>();
        fileEncoder.setContext(context);
        fileEncoder.setLayout(fileLayout);

        final var latest = new FileAppender<ILoggingEvent>();
        latest.setContext(context);
        latest.setName("latest");
        latest.setEncoder(fileEncoder);
        latest.setAppend(false);
        latest.setFile("logs/latest.log");
        latest.addFilter(new LevelFilter());
        latest.start();

        final var debug = new FileAppender<ILoggingEvent>();
        debug.setContext(context);
        debug.setName("debug");
        debug.setEncoder(fileEncoder);
        debug.setAppend(false);
        debug.setFile("logs/debug.log");
        debug.start();

        final Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(ca);
        rootLogger.addAppender(latest);
        rootLogger.addAppender(debug);

        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }
}
