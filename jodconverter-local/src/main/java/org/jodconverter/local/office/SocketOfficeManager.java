package org.jodconverter.local.office;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jodconverter.core.office.AbstractOfficeManagerPool;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@link org.jodconverter.core.office.OfficeManager} pool implementation that does not depend on an
 * office installation to process conversion taks.
 */
public final class SocketOfficeManager extends AbstractOfficeManagerPool {

    /**
     * Creates a new builder instance.
     *
     * @return A new builder instance.
     */
    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link SocketOfficeManager} with default configuration.
     *
     * @param host The host to LibreOfficeOnline server.
     * @param port The port to the LibreOfficeOnline server.
     * @return A {@link SocketOfficeManager} with default configuration.
     */
    @NonNull
    public static SocketOfficeManager make(@NonNull final String host, @NonNull final Integer port) {
        return builder().host(host).port(port).build();
    }

    /**
     * Creates a new {@link SocketOfficeManager} with default configuration. The created manager will
     * then be the unique instance of the {@link
     * org.jodconverter.core.office.InstalledOfficeManagerHolder} class. Note that if the {@code
     * InstalledOfficeManagerHolder} class already holds an {@code OfficeManager} instance, the owner
     * of this existing manager is responsible to stopped it.
     *
     * @param host The host to LibreOfficeOnline server.
     * @param port The port to the LibreOfficeOnline server.
     * @return A {@link SocketOfficeManager} with default configuration.
     */
    @NonNull
    public static SocketOfficeManager install(@NonNull final String host, @NonNull final Integer port) {
        return builder().host(host).port(port).install().build();
    }

    private SocketOfficeManager(
            final Integer poolSize,
            final String host,
            final Integer port,
            final Long connectTimeout,
            final Long socketTimeout,
            final Long taskExecutionTimeout,
            final Long taskQueueTimeout) {
        super(poolSize, taskQueueTimeout);

        setEntries(
                IntStream.range(0, poolSize == null ? DEFAULT_POOL_SIZE : poolSize)
                        .mapToObj(
                                i ->
                                        new SocketOfficeManagerPoolEntry(
                                                host,
                                                port,
                                                connectTimeout,
                                                socketTimeout,
                                                taskExecutionTimeout))
                        .collect(Collectors.toList()));
    }

    /**
     * A builder for constructing a {@link SocketOfficeManager}.
     *
     * @see SocketOfficeManager
     */
    public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

        // The maximum size of the pool.
        private static final int MAX_POOL_SIZE = 1000;

        private Integer poolSize;
        private String host;
        private Integer port;
        private Long connectTimeout;
        private Long socketTimeout;

        // Private constructor so only RemoteOfficeManager can initialize an instance of this builder.
        private Builder() {
            super();
        }

        @NonNull
        public SocketOfficeManager build() {

            final SocketOfficeManager manager =
                    new SocketOfficeManager(
                            poolSize,
                            host,
                            port,
                            connectTimeout,
                            socketTimeout,
                            taskExecutionTimeout,
                            taskQueueTimeout);
            if (install) {
                InstalledOfficeManagerHolder.setInstance(manager);
            }
            return manager;
        }

        /**
         * Specifies the pool size of the manager.
         *
         * @param poolSize The pool size.
         * @return This builder instance.
         */
        @NonNull
        public Builder poolSize(@Nullable final Integer poolSize) {

            if (poolSize != null) {
                Validate.inclusiveBetween(
                        0,
                        MAX_POOL_SIZE,
                        poolSize,
                        String.format("poolSize %s must be between %d and %d", poolSize, 1, MAX_POOL_SIZE));
            }
            this.poolSize = poolSize;
            return this;
        }

        /**
         * Specifies the URL connection of the manager.
         *
         * @param host The host.
         * @return This builder instance.
         */
        @NonNull
        public Builder host(final String host) {

            this.host = host;
            return this;
        }

        /**
         * Specifies the URL connection of the manager.
         *
         * @param port The port.
         * @return This builder instance.
         */
        @NonNull
        public Builder port(final int port) {

            this.port = port;
            return this;
        }

        /**
         * The timeout in milliseconds until a connection is established. A timeout value of zero is
         * interpreted as an infinite timeout. A negative value is interpreted as undefined (system
         * default).
         *
         * <p>&nbsp; <b><i>Default</i></b>: 60000 (1 minute)
         *
         * @param connectTimeout The connect timeout, in milliseconds.
         * @return This builder instance.
         */
        @NonNull
        public Builder connectTimeout(@Nullable final Long connectTimeout) {

            if (connectTimeout != null) {
                Validate.inclusiveBetween(
                        0,
                        Integer.MAX_VALUE,
                        connectTimeout,
                        String.format("connectTimeout %s must greater than or equal to 0", connectTimeout));
            }
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Specifies the socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the timeout for
         * waiting for data or, put differently, a maximum period inactivity between two consecutive
         * data packets). A timeout value of zero is interpreted as an infinite timeout. A negative
         * value is interpreted as undefined (system default).
         *
         * <p>&nbsp; <b><i>Default</i></b>: 120000 (2 minutes)
         *
         * @param socketTimeout The socket timeout, in milliseconds.
         * @return This builder instance.
         */
        @NonNull
        public Builder socketTimeout(@Nullable final Long socketTimeout) {

            if (socketTimeout != null) {
                Validate.inclusiveBetween(
                        0,
                        Integer.MAX_VALUE,
                        socketTimeout,
                        String.format("socketTimeout %s must greater than or equal to 0", socketTimeout));
            }
            this.socketTimeout = socketTimeout;
            return this;
        }
    }
}
