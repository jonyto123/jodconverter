package org.jodconverter.local.office;

import org.jodconverter.core.office.AbstractOfficeManagerPoolEntry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.task.OfficeTask;

/**
 * A RemoteOfficeManagerPoolEntry is responsible to execute tasks submitted through a {@link
 * SocketOfficeManager} that does not depend on an office installation. It will send conversion
 * request to a LibreOffice Online server and wait until the task is done or a configured task
 * execution timeout is reached.
 *
 * @see SocketOfficeManager
 */
class SocketOfficeManagerPoolEntry extends AbstractOfficeManagerPoolEntry {

    // The default connect timeout
    private static final long DEFAULT_CONNECT_TIMEOUT = 60_000L; // 2 minutes
    // The default socket timeout
    private static final long DEFAULT_SOCKET_TIMEOUT = 120_000L; // 2 minutes

    private final OfficeConnection officeConnection;
    private final long connectTimeout;
    private final long socketTimeout;

    /**
     * Creates a new pool entry with the specified configuration.
     *
     * @param connectTimeout The timeout in milliseconds until a connection is established. A timeout
     *     value of zero is interpreted as an infinite timeout. A negative value is interpreted as
     *     undefined (system default).
     * @param socketTimeout The socket timeout ({@code SO_TIMEOUT}) in milliseconds, which is the
     *     timeout for waiting for data or, put differently, a maximum period inactivity between two
     *     consecutive data packets). A timeout value of zero is interpreted as an infinite timeout. A
     *     negative value is interpreted as undefined (system default).
     * @param taskExecutionTimeout The maximum time allowed to process a task. If the processing time
     *     of a task is longer than this timeout, this task will be aborted and the next task is
     *     processed.
     */
    /* default */ SocketOfficeManagerPoolEntry(
            final String host,
            final Integer port,
            final Long connectTimeout,
            final Long socketTimeout,
            final Long taskExecutionTimeout) {
        super(taskExecutionTimeout);

        //TODO: Missing connection retry on Listener event! {@OfficeConnectionEventListener}
        officeConnection = new OfficeConnection(new OfficeUrl(host, port));
        this.connectTimeout = connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
        this.socketTimeout = socketTimeout == null ? DEFAULT_SOCKET_TIMEOUT : socketTimeout;
    }

    @Override
    protected void doExecute(final OfficeTask task) throws OfficeException {

        task.execute(this.officeConnection);
    }

    @Override
    protected void doStart() throws OfficeConnectionException {
        setAvailable(true);
        officeConnection.connect();
    }

    @Override
    protected void doStop() {
        // Nothing to stop here.
        officeConnection.disconnect();
    }
}
