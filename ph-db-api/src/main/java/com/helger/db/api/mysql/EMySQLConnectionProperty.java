/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.db.api.mysql;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.CodingStyleguideUnaware;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.name.IHasName;
import com.helger.commons.version.Version;

/**
 * List of MySQL connection properties. <a href=
 * "http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-configuration-properties.html"
 * >Source</a>
 *
 * @author Philip Helger
 */
@CodingStyleguideUnaware
public enum EMySQLConnectionProperty implements IHasName
{
 /** The user to connect as */
  user ("user", null, null),
 /** The password to use when connecting */
  password ("password", null, null),
 /**
  * The name of the class that the driver should use for creating socket
  * connections to the server. This class must implement the interface
  * 'com.mysql.jdbc.SocketFactory' and have public no-args constructor.
  */
  socketFactory ("socketFactory", com.mysql.jdbc.StandardSocketFactory.class.getName (), new Version (3, 0, 3)),
 /**
  * Timeout for socket connect (in milliseconds), with 0 being no timeout. Only
  * works on JDK-1.4 or newer. Defaults to '0'.
  */
  connectTimeout ("connectTimeout", "0", new Version (3, 0, 1)),
 /** Timeout on network socket operations (0, the default means no timeout). */
  socketTimeout ("socketTimeout", "0", new Version (3, 0, 1)),
 /**
  * A comma-delimited list of classes that implement
  * "com.mysql.jdbc.ConnectionLifecycleInterceptor" that should notified of
  * connection lifecycle events (creation, destruction, commit, rollback,
  * setCatalog and setAutoCommit) and potentially alter the execution of these
  * commands. ConnectionLifecycleInterceptors are "stackable", more than one
  * interceptor may be specified via the configuration property as a
  * comma-delimited list, with the interceptors executed in order from left to
  * right.
  */
  connectionLifecycleInterceptors ("connectionLifecycleInterceptors", null, new Version (5, 1, 4)),
 /**
  * Load the comma-delimited list of configuration properties before parsing the
  * URL or applying user-specified properties. These configurations are
  * explained in the 'Configurations' of the documentation.
  */
  useConfigs ("useConfigs", null, new Version (3, 1, 5)),
 /**
  * Set the CLIENT_INTERACTIVE flag, which tells MySQL to timeout connections
  * based on INTERACTIVE_TIMEOUT instead of WAIT_TIMEOUT
  */
  interactiveClient ("interactiveClient", Boolean.FALSE.toString (), new Version (3, 1, 0)),
 /**
  * Hostname or IP address given to explicitly configure the interface that the
  * driver will bind the client side of the TCP/IP connection to when
  * connecting.
  */
  localSocketAddress ("localSocketAddress", null, new Version (5, 0, 5)),
 /**
  * An implementation of com.mysql.jdbc.ConnectionPropertiesTransform that the
  * driver will use to modify URL properties passed to the driver before
  * attempting a connection
  */
  propertiesTransform ("propertiesTransform", null, new Version (3, 1, 4)),
 /**
  * Use zlib compression when communicating with the server (true/false)?
  * Defaults to 'false'.
  */
  useCompression ("useCompression", Boolean.FALSE.toString (), new Version (3, 0, 17)),
 /**
  * Maximum allowed packet size to send to server. If not set, the value of
  * system variable 'max_allowed_packet' will be used to initialize this upon
  * connecting. This value will not take effect if set larger than the value of
  * 'max_allowed_packet'.
  */
  maxAllowedPacket ("maxAllowedPacket", "-1", new Version (5, 1, 8)),
 /** If connecting using TCP/IP, should the driver set SO_KEEPALIVE? */
  tcpKeepAlive ("tcpKeepAlive", Boolean.TRUE.toString (), new Version (5, 0, 7)),
 /**
  * If connecting using TCP/IP, should the driver set SO_TCP_NODELAY (disabling
  * the Nagle Algorithm)?
  */
  tcpNoDelay ("tcpNoDelay", Boolean.TRUE.toString (), new Version (5, 0, 7)),
 /**
  * If connecting using TCP/IP, should the driver set SO_RCV_BUF to the given
  * value? The default value of '0', means use the platform default value for
  * this property)
  */
  tcpRcvBuf ("tcpRcvBuf", "0", new Version (5, 0, 7)),
 /**
  * If connecting using TCP/IP, should the driver set SO_SND_BUF to the given
  * value? The default value of '0', means use the platform default value for
  * this property)
  */
  tcpSndBuf ("tcpSndBuf", "0", new Version (5, 0, 7)),
 /**
  * If connecting using TCP/IP, should the driver set traffic class or
  * type-of-service fields ?See the documentation for
  * java.net.Socket.setTrafficClass() for more information.
  */
  tcpTrafficClass ("tcpTrafficClass", "0", new Version (5, 0, 7)),
 /**
  * Should the driver try to re-establish stale and/or dead connections? If
  * enabled the driver will throw an exception for a queries issued on a stale
  * or dead connection, which belong to the current transaction, but will
  * attempt reconnect before the next query issued on the connection in a new
  * transaction. The use of this feature is not recommended, because it has side
  * effects related to session state and data consistency when applications
  * don't handle SQLExceptions properly, and is only designed to be used when
  * you are unable to configure your application to handle SQLExceptions
  * resulting from dead and stale connections properly. Alternatively, as a last
  * option, investigate setting the MySQL server variable "wait_timeout" to a
  * high value, rather than the default of 8 hours.
  */
  autoReconnect ("autoReconnect", Boolean.FALSE.toString (), new Version (1, 1)),
 /**
  * Use a reconnection strategy appropriate for connection pools (defaults to
  * 'false')
  */
  autoReconnectForPools ("autoReconnectForPools", Boolean.FALSE.toString (), new Version (3, 1, 3)),
 /**
  * When failing over in autoReconnect mode, should the connection be set to
  * 'read-only'?
  */
  failOverReadOnly ("failOverReadOnly", Boolean.TRUE.toString (), new Version (3, 0, 12)),
 /**
  * Maximum number of reconnects to attempt if autoReconnect is true, default is
  * '3'.
  */
  maxReconnects ("maxReconnects", "3", new Version (1, 1)),
 /**
  * If autoReconnect is set to true, should the driver attempt reconnections at
  * the end of every transaction?
  */
  reconnectAtTxEnd ("reconnectAtTxEnd", Boolean.FALSE.toString (), new Version (3, 0, 10)),
 /**
  * When using loadbalancing, the number of times the driver should cycle
  * through available hosts, attempting to connect. Between cycles, the driver
  * will pause for 250ms if no servers are available.
  */
  retriesAllDown ("retriesAllDown", "120", new Version (5, 1, 6)),
 /**
  * If autoReconnect is enabled, the initial time to wait between re-connect
  * attempts (in seconds, defaults to '2').
  */
  initialTimeout ("initialTimeout", "2", new Version (1, 1)),
 /**
  * When autoReconnect is enabled, and failoverReadonly is false, should we pick
  * hosts to connect to on a round-robin basis?
  */
  roundRobinLoadBalance ("roundRobinLoadBalance", Boolean.FALSE.toString (), new Version (3, 1, 2)),
 /**
  * Number of queries to issue before falling back to master when failed over
  * (when using multi-host failover). Whichever condition is met first,
  * 'queriesBeforeRetryMaster' or 'secondsBeforeRetryMaster' will cause an
  * attempt to be made to reconnect to the master. Defaults to 50.
  */
  queriesBeforeRetryMaster ("queriesBeforeRetryMaster", "50", new Version (3, 0, 2)),
 /** How long should the driver wait, when failed over, before attempting */
  secondsBeforeRetryMaster ("secondsBeforeRetryMaster", "30", new Version (3, 0, 2)),
 /**
  * =If set to a non-zero value, the driver will report close the connection and
  * report failure when Connection.ping() or Connection.isValid(int) is called
  * if the connnection's count of commands sent to the server exceeds this
  * value.
  */
  selfDestructOnPingMaxOperations ("selfDestructOnPingMaxOperations", "0", new Version (5, 1, 6)),
 /**
  * If set to a non-zero value, the driver will report close the connection and
  * report failure when Connection.ping() or Connection.isValid(int) is called
  * if the connnection's lifetime exceeds this value.
  */
  selfDestructOnPingSecondsLifetime ("selfDestructOnPingSecondsLifetime", "0", new Version (5, 1, 6)),
 /**
  * A globally unique name that identifies the resource that this datasource or
  * connection is connected to, used for XAResource.isSameRM() when the driver
  * can't determine this value based on hostnames used in the URL
  */
  resourceId ("resourceId", null, new Version (5, 0, 1)),
 /**
  * Allow the use of ';' to delimit multiple queries during one statement
  * (true/false), defaults to 'false', and does not affect the addBatch() and
  * executeBatch() methods, which instead rely on rewriteBatchStatements.
  */
  allowMultiQueries ("allowMultiQueries", Boolean.FALSE.toString (), new Version (3, 1, 1)),
 /**
  * Use SSL when communicating with the server (true/false), defaults to 'false'
  */
  useSSL ("useSSL", Boolean.FALSE.toString (), new Version (3, 0, 2)),
 /** Require SSL connection if useSSL=true? (defaults to 'false'). */
  requireSSL ("requireSSL", Boolean.FALSE.toString (), new Version (3, 1, 0)),
 /**
  * If "useSSL" is set to "true" , should the driver verify the server's
  * certificate? When using this feature, the keystore parameters should be
  * specified by the "clientCertificateKeyStore" properties, rather than system
  * properties.
  */
  verifyServerCertificate ("verifyServerCertificate", Boolean.TRUE.toString (), new Version (5, 1, 6)),
 /** URL to the client certificate KeyStore (if not specified, use defaults) */
  clientCertificateKeyStoreUrl ("clientCertificateKeyStoreUrl", null, new Version (5, 1, 0)),
 /**
  * KeyStore type for client certificates (NULL or empty means use the default,
  * which is "JKS". Standard keystore types supported by the JVM are "JKS" and
  * "PKCS12", your environment may have more available depending on what
  * security products are installed and available to the JVM."
  */
  clientCertificateKeyStoreType ("clientCertificateKeyStoreType", "JKS", new Version (5, 1, 0)),
 /** Password for the client certificates KeyStore */
  clientCertificateKeyStorePassword ("clientCertificateKeyStorePassword", null, new Version (5, 1, 0)),
 /**
  * URL to the trusted root certificate KeyStore (if not specified, use
  * defaults)
  */
  trustCertificateKeyStoreUrl ("trustCertificateKeyStoreUrl", null, new Version (5, 1, 0)),
 /**
  * KeyStore type for trusted root certificates (NULL or empty means use the
  * default, which is "JKS". Standard keystore types supported by the JVM are
  * "JKS" and "PKCS12" , your environment may have more available depending on
  * what security products are installed and available to the JVM.
  */
  trustCertificateKeyStoreType ("trustCertificateKeyStoreType", "JKS", new Version (5, 1, 0)),
 /** Password for the trusted root certificates KeyStore */
  trustCertificateKeyStorePassword ("trustCertificateKeyStorePassword", null, new Version (5, 1, 0)),
 /**
  * Should the driver allow use of 'LOAD DATA LOCAL INFILE...' (defaults to
  * 'true').
  */
  allowLoadLocalInfile ("allowLoadLocalInfile", Boolean.TRUE.toString (), new Version (3, 0, 3)),
 /** Should the driver allow URLs in 'LOAD DATA LOCAL INFILE' statements? */
  allowUrlInLocalInfile ("allowUrlInLocalInfile", Boolean.FALSE.toString (), new Version (3, 1, 4)),
 /**
  * Take measures to prevent exposure sensitive information in error messages
  * and clear data structures holding sensitive data when possible? (defaults to
  * 'false')
  */
  paranoid ("paranoid", Boolean.FALSE.toString (), new Version (3, 0, 1)),
 /**
  * What character encoding is used for passwords? Leaving this set to the
  * default value (null), uses the platform character set, which works for
  * ISO8859_1 (i.e. "latin1") passwords. For passwords in other character
  * encodings, the encoding will have to be specified with this property, as
  * it's not possible for the driver to auto-detect this.
  */
  passwordCharacterEncoding ("passwordCharacterEncoding", null, new Version (5, 1, 7)),
 /**
  * If 'cacheCallableStmts' is enabled, how many callable statements should be
  * cached?
  */
  callableStmtCacheSize ("callableStmtCacheSize", "100", new Version (3, 1, 2)),
 /**
  * The number of queries to cache ResultSetMetadata for if
  * cacheResultSetMetaData is set to 'true' (default 50)
  */
  metadataCacheSize ("metadataCacheSize", "50", new Version (3, 1, 1)),
 /**
  * Should the driver refer to the internal values of autocommit and transaction
  * isolation that are set by Connection.setAutoCommit() and
  * Connection.setTransactionIsolation() and transaction state as maintained by
  * the protocol, rather than querying the database or blindly sending commands
  * to the database for commit() or rollback() method calls?
  */
  useLocalSessionState ("useLocalSessionState", Boolean.FALSE.toString (), new Version (3, 1, 7)),
 /**
  * Should the driver use the in-transaction state provided by the MySQL
  * protocol to determine if a commit() or rollback() should actually be sent to
  * the database?
  */
  useLocalTransactionState ("useLocalTransactionState", Boolean.FALSE.toString (), new Version (5, 1, 7)),
 /**
  * If prepared statement caching is enabled, how many prepared statements
  * should be cached?
  */
  prepStmtCacheSize ("prepStmtCacheSize", "25", new Version (3, 0, 10)),
 /**
  * If prepared statement caching is enabled, what's the largest SQL the driver
  * will cache the parsing for?
  */
  prepStmtCacheSqlLimit ("prepStmtCacheSqlLimit", "256", new Version (3, 0, 10)),
 /**
  * Should the driver always communicate with the database when
  * Connection.setTransactionIsolation() is called? If set to false, the driver
  * will only communicate with the database when the requested transaction
  * isolation is different than the whichever is newer, the last value that was
  * set via Connection.setTransactionIsolation(), or the value that was read
  * from the server when the connection was established. Note that
  * useLocalSessionState=true will force the same behavior as
  * alwaysSendSetIsolation=false, regardless of how alwaysSendSetIsolation is
  * set.
  */
  alwaysSendSetIsolation ("alwaysSendSetIsolation", Boolean.TRUE.toString (), new Version (3, 1, 7)),
 /**
  * Should the driver maintain various internal timers to enable idle time
  * calculations as well as more verbose error messages when the connection to
  * the server fails? Setting this property to false removes at least two calls
  * to System.getCurrentTimeMillis() per query.
  */
  maintainTimeStats ("maintainTimeStats", Boolean.TRUE.toString (), new Version (3, 1, 9)),
 /**
  * If connected to MySQL &gt; 5.0.2, and setFetchSize() &gt; 0 on a statement,
  * should that statement use cursor-based fetching to retrieve rows?
  */
  useCursorFetch ("useCursorFetch", Boolean.FALSE.toString (), new Version (5, 0, 0)),
 /** Chunk to use when sending BLOB/CLOBs via ServerPreparedStatements */
  blobSendChunkSize ("blobSendChunkSize", "1048576", new Version (3, 1, 9)),
 /** Should the driver cache the parsing stage of CallableStatements */
  cacheCallableStmts ("cacheCallableStmts", Boolean.FALSE.toString (), new Version (3, 1, 2)),
 /**
  * Should the driver cache the parsing stage of PreparedStatements of
  * client-side prepared statements, the "check" for suitability of server-side
  * prepared and server-side prepared statements themselves?
  */
  cachePrepStmts ("cachePrepStmts", Boolean.FALSE.toString (), new Version (3, 0, 10)),
 /**
  * Should the driver cache ResultSetMetaData for Statements and
  * PreparedStatements? (Req. JDK-1.4+, true/false, default 'false')
  */
  cacheResultSetMetadata ("cacheResultSetMetadata", Boolean.FALSE.toString (), new Version (3, 1, 1)),
 /**
  * Should the driver cache the results of 'SHOW VARIABLES' and 'SHOW COLLATION'
  * on a per-URL basis?
  */
  cacheServerConfiguration ("cacheServerConfiguration", Boolean.FALSE.toString (), new Version (3, 1, 5)),
 /**
  * The driver will call setFetchSize(n) with this value on all newly-created
  * Statements
  */
  defaultFetchSize ("defaultFetchSize", "0", new Version (3, 1, 9)),
 /**
  * The JDBC specification requires the driver to automatically track and close
  * resources, however if your application doesn't do a good job of explicitly
  * calling close() on statements or result sets, this can cause memory leakage.
  * Setting this property to true relaxes this constraint, and can be more
  * memory efficient for some applications.
  */
  dontTrackOpenResources ("dontTrackOpenResources", Boolean.FALSE.toString (), new Version (3, 1, 7)),
 /**
  * Should the driver retrieve the default calendar when required, or cache it
  * per connection/session?
  */
  dynamicCalendars ("dynamicCalendars", Boolean.FALSE.toString (), new Version (3, 1, 5)),
 /**
  * If using MySQL-4.1 or newer, should the driver only issue 'set autocommit=n'
  * queries when the server's state doesn't match the requested state by
  * Connection.setAutoCommit(boolean)?
  */
  elideSetAutoCommits ("elideSetAutoCommits", Boolean.FALSE.toString (), new Version (3, 1, 3)),
 /**
  * When enabled, query timeouts set via Statement.setQueryTimeout() use a
  * shared java.util.Timer instance for scheduling. Even if the timeout doesn't
  * expire before the query is processed, there will be memory used by the
  * TimerTask for the given timeout which won't be reclaimed until the time the
  * timeout would have expired if it hadn't been cancelled by the driver.
  * High-load environments might want to consider disabling this functionality.
  */
  enableQueryTimeouts ("enableQueryTimeouts", Boolean.TRUE.toString (), new Version (5, 0, 6)),
 /**
  * Should the driver close result sets on Statement.close() as required by the
  * JDBC specification?
  */
  holdResultsOpenOverStatementClose ("holdResultsOpenOverStatementClose", Boolean.FALSE.toString (), new Version (3,
                                                                                                                  1,
                                                                                                                  7)),
 /**
  * What size result set row should the JDBC driver consider "large", and thus
  * use a more memory-efficient way of representing the row internally?
  */
  largeRowSizeThreshold ("largeRowSizeThreshold", "2048", new Version (5, 1, 1)),
 /**
  * If using a load-balanced connection to connect to SQL nodes in a MySQL
  * Cluster/NDB configuration (by using the URL prefix
  * "jdbc:mysql:loadbalance://"), which load balancing algorithm should the
  * driver use: (1) "random" - the driver will pick a random host for each
  * request. This tends to work better than round-robin, as the randomness will
  * somewhat account for spreading loads where requests vary in response time,
  * while round-robin can sometimes lead to overloaded nodes if there are
  * variations in response times across the workload. (2) "bestResponseTime" -
  * the driver will route the request to the host that had the best response
  * time for the previous transaction."
  */
  loadBalanceStrategy ("loadBalanceStrategy", "random", new Version (5, 0, 6)),
 /**
  * If 'emulateLocators' is configured to 'true', what size buffer should be
  * used when fetching BLOB data for getBinaryInputStream?
  */
  locatorFetchBufferSize ("locatorFetchBufferSize", "1048576", new Version (3, 2, 1)),
 /**
  * Should the driver use multiqueries (irregardless of the setting of
  * "allowMultiQueries") as well as rewriting of prepared statements for INSERT
  * into multi-value inserts when executeBatch() is called? Notice that this has
  * the potential for SQL injection if using plain java.sql.Statements and your
  * code doesn't sanitize input correctly. Notice that for prepared statements,
  * server-side prepared statements can not currently take advantage of this
  * rewrite option, and that if you don't specify stream lengths when using
  * PreparedStatement.set*Stream(), the driver won't be able to determine the
  * optimum number of parameters per batch and you might receive an error from
  * the driver that the resultant packet is too large.
  * Statement.getGeneratedKeys() for these rewritten statements only works when
  * the entire batch includes INSERT statements.
  */
  rewriteBatchedStatements ("rewriteBatchedStatements", Boolean.FALSE.toString (), new Version (3, 1, 13)),
 /**
  * Use newer result set row unpacking code that skips a copy from network
  * buffers to a MySQL packet instance and instead reads directly into the
  * result set row data buffers.
  */
  useDirectRowUnpack ("useDirectRowUnpack", Boolean.TRUE.toString (), new Version (5, 1, 1)),
 /**
  * Should the driver use a per-connection cache of character set information
  * queried from the server when necessary, or use a built-in static mapping
  * that is more efficient, but isn't aware of custom character sets or
  * character sets implemented after the release of the JDBC driver?
  */
  useDynamicCharsetInfo ("useDynamicCharsetInfo", Boolean.TRUE.toString (), new Version (5, 0, 6)),
 /**
  * Use internal String-&gt;Date/Time/Timestamp conversion routines to avoid
  * excessive object creation?
  */
  useFastDateParsing ("useFastDateParsing", Boolean.TRUE.toString (), new Version (5, 0, 5)),
 /**
  * Use internal String-&gt;Integer conversion routines to avoid excessive
  * object creation?
  */
  useFastIntParsing ("useFastIntParsing", Boolean.TRUE.toString (), new Version (3, 1, 4)),
 /**
  * Always use the character encoding routines built into the JVM, rather than
  * using lookup tables for single-byte character sets?
  */
  useJvmCharsetConverters ("useJvmCharsetConverters", Boolean.FALSE.toString (), new Version (5, 0, 1)),
 /**
  * Use newer, optimized non-blocking, buffered input stream when reading from
  * the server?
  */
  useReadAheadInput ("useReadAheadInput", Boolean.TRUE.toString (), new Version (3, 1, 5)),
 /**
  * The name of a class that implements "com.mysql.jdbc.log.Log" that will be
  * used to log messages to. (default is "com.mysql.jdbc.log.StandardLogger",
  * which logs to STDERR)
  */
  logger ("logger", com.mysql.jdbc.log.StandardLogger.class.getName (), new Version (3, 1, 1)),
 /**
  * Should the driver gather performance metrics, and report them via the
  * configured logger every 'reportMetricsIntervalMillis' milliseconds?
  */
  gatherPerfMetrics ("gatherPerfMetrics", Boolean.FALSE.toString (), new Version (3, 1, 2)),
 /**
  * Trace queries and their execution/fetch times to the configured logger
  * (true/false) defaults to 'false'
  */
  profileSQL ("profileSQL", Boolean.FALSE.toString (), new Version (3, 1, 0)),
 /**
  * Deprecated, use 'profileSQL' instead. Trace queries and their
  * execution/fetch times on STDERR (true/false) defaults to 'false'
  */
  profileSql ("profileSql", null, new Version (2, 0, 14)),
 /**
  * If 'gatherPerfMetrics' is enabled, how often should they be logged (in ms)?
  */
  reportMetricsIntervalMillis ("reportMetricsIntervalMillis", "30000", new Version (3, 1, 2)),
 /**
  * Controls the maximum length/size of a query that will get logged when
  * profiling or tracing
  */
  maxQuerySizeToLog ("maxQuerySizeToLog", "2048", new Version (3, 1, 3)),
 /** The maximum number of packets to retain when 'enablePacketDebug' is true */
  packetDebugBufferSize ("packetDebugBufferSize", "20", new Version (3, 1, 3)),
 /**
  * If 'logSlowQueries' is enabled, how long should a query (in ms) before it is
  * logged as 'slow'?
  */
  slowQueryThresholdMillis ("slowQueryThresholdMillis", "2000", new Version (3, 1, 2)),
 /**
  * If 'useNanosForElapsedTime' is set to true, and this property is set to a
  * non-zero value, the driver will use this threshold (in nanosecond units) to
  * determine if a query was slow.
  */
  slowQueryThresholdNanos ("slowQueryThresholdNanos", "0", new Version (5, 0, 7)),
 /**
  * Should the driver issue 'usage' warnings advising proper and efficient usage
  * of JDBC and MySQL Connector/J to the log (true/false, defaults to 'false')?
  */
  useUsageAdvisor ("useUsageAdvisor", Boolean.FALSE.toString (), new Version (3, 1, 1)),
 /**
  * Should the driver dump the SQL it is executing, including server-side
  * prepared statements to STDERR?
  */
  autoGenerateTestcaseScript ("autoGenerateTestcaseScript", Boolean.FALSE.toString (), new Version (3, 1, 9)),
 /**
  * Instead of using slowQueryThreshold* to determine if a query is slow enough
  * to be logged, maintain statistics that allow the driver to determine queries
  * that are outside the 99th percentile?
  */
  autoSlowLog ("autoSlowLog", Boolean.TRUE.toString (), new Version (5, 1, 4)),
 /**
  * The name of a class that implements the
  * com.mysql.jdbc.JDBC4ClientInfoProvider interface in order to support
  * JDBC-4.0's Connection.get/setClientInfo() methods
  */
  clientInfoProvider ("clientInfoProvider", com.mysql.jdbc.JDBC4CommentClientInfoProvider.class.getName (), new Version (5,
                                                                                                                         1,
                                                                                                                         0)),
 /**
  * Should the driver dump the field-level metadata of a result set into the
  * exception message when ResultSet.findColumn() fails?
  */
  dumpMetadataOnColumnNotFound ("dumpMetadataOnColumnNotFound", Boolean.FALSE.toString (), new Version (3, 1, 13)),
 /**
  * Should the driver dump the contents of the query sent to the server in the
  * message for SQLExceptions?
  */
  dumpQueriesOnException ("dumpQueriesOnException", Boolean.FALSE.toString (), new Version (3, 1, 3)),
 /**
  * When enabled, a ring-buffer of 'packetDebugBufferSize' packets will be kept,
  * and dumped when exceptions are thrown in key areas in the driver's code
  */
  enablePacketDebug ("enablePacketDebug", Boolean.FALSE.toString (), new Version (3, 1, 3)),
 /**
  * If 'logSlowQueries' is enabled, should the driver automatically issue an
  * 'EXPLAIN' on the server and send the results to the configured log at a WARN
  * level?
  */
  explainSlowQueries ("explainSlowQueries", Boolean.FALSE.toString (), new Version (3, 1, 2)),
 /**
  * Include the output of "SHOW ENGINE INNODB STATUS" in exception messages when
  * deadlock exceptions are detected?
  */
  includeInnodbStatusInDeadlockExceptions ("includeInnodbStatusInDeadlockExceptions", Boolean.FALSE.toString (), new Version (5,
                                                                                                                              0,
                                                                                                                              7)),
 /**
  * Include a current Java thread dump in exception messages when deadlock
  * exceptions are detected?
  */
  includeThreadDumpInDeadlockExceptions ("includeThreadDumpInDeadlockExceptions", Boolean.FALSE.toString (), new Version (5,
                                                                                                                          1,
                                                                                                                          15)),
 /**
  * Include the name of the current thread as a comment visible in
  * "SHOW PROCESSLIST", or in Innodb deadlock dumps, useful in correlation with
  * "includeInnodbStatusInDeadlockExceptions=true" and
  * "includeThreadDumpInDeadlockExceptions=true".
  */
  includeThreadNamesAsStatementComment ("includeThreadNamesAsStatementComment", Boolean.FALSE.toString (), new Version (5,
                                                                                                                        1,
                                                                                                                        15)),
 /**
  * Should queries that take longer than 'slowQueryThresholdMillis' be logged?
  */
  logSlowQueries ("logSlowQueries", Boolean.FALSE.toString (), new Version (3, 1, 2)),
 /**
  * Should the driver log XA commands sent by MysqlXaConnection to the server,
  * at the DEBUG level of logging?
  */
  logXaCommands ("logXaCommands", Boolean.FALSE.toString (), new Version (5, 0, 5)),
 /**
  * Name of a class that implements the interface
  * com.mysql.jdbc.profiler.ProfilerEventHandler that will be used to handle
  * profiling/tracing events.
  */
  profilerEventHandler ("profilerEventHandler", com.mysql.jdbc.profiler.LoggingProfilerEventHandler.class.getName (), new Version (5,
                                                                                                                                   1,
                                                                                                                                   6)),
 /**
  * If the usage advisor is enabled, how many rows should a result set contain
  * before the driver warns that it is suspiciously large?
  */
  resultSetSizeThreshold ("resultSetSizeThreshold", "100", new Version (5, 0, 5)),
 /** Should trace-level network protocol be logged? */
  traceProtocol ("traceProtocol", Boolean.FALSE.toString (), new Version (3, 1, 2)),
 /**
  * For profiling/debugging functionality that measures elapsed time, should the
  * driver try to use nanoseconds resolution if available (JDK &ge; 1.5)?
  */
  useNanosForElapsedTime ("useNanosForElapsedTime", Boolean.FALSE.toString (), new Version (5, 0, 7)),
 /**
  * Should the driver use Unicode character encodings when handling strings?
  * Should only be used when the driver can't determine the character set
  * mapping, or you are trying to 'force' the driver to use a character set that
  * MySQL either doesn't natively support (such as UTF-8), true/false, defaults
  * to 'true'
  */
  useUnicode ("useUnicode", Boolean.TRUE.toString (), new Version ("1.1g")),
 /**
  * If 'useUnicode' is set to true, what character encoding should the driver
  * use when dealing with strings? (defaults is to 'autodetect')
  */
  characterEncoding ("characterEncoding", null, new Version ("1.1g")),
 /** Character set to tell the server to return results as. */
  characterSetResults ("characterSetResults", null, new Version (3, 0, 13)),
 /**
  * If set, tells the server to use this collation via 'set
  * collation_connection'
  */
  connectionCollation ("connectionCollation", null, new Version (3, 0, 13)),
 /**
  * Tells the driver to treat [MEDIUM/LONG]BLOB columns as [LONG]VARCHAR columns
  * holding text encoded in UTF-8 that has characters outside the BMP (4-byte
  * encodings), which MySQL server can't handle natively.
  */
  useBlobToStoreUTF8OutsideBMP ("useBlobToStoreUTF8OutsideBMP", Boolean.FALSE.toString (), new Version (5, 1, 3)),
 /**
  * When "useBlobToStoreUTF8OutsideBMP" is set to "true" , column names matching
  * the given regex will still be treated as BLOBs unless they match the regex
  * specified for "utf8OutsideBmpIncludedColumnNamePattern". The regex must
  * follow the patterns used for the java.util.regex package.
  */
  utf8OutsideBmpExcludedColumnNamePattern ("utf8OutsideBmpExcludedColumnNamePattern", null, new Version (5, 1, 3)),
 /**
  * Used to specify exclusion rules to
  * "utf8OutsideBmpExcludedColumnNamePattern". The regex must follow the
  * patterns used for the java.util.regex package.
  */
  utf8OutsideBmpIncludedColumnNamePattern ("utf8OutsideBmpIncludedColumnNamePattern", null, new Version (5, 1, 3)),
 /**
  * Enables JMX-based management of load-balanced connection groups, including
  * live addition/removal of hosts from load-balancing pool.
  */
  loadBalanceEnableJMX ("loadBalanceEnableJMX", Boolean.FALSE.toString (), new Version (5, 1, 13)),
 /**
  * A comma-separated list of name/value pairs to be sent as SET SESSION ... to
  * the server when the driver connects.
  */
  sessionVariables ("sessionVariables", null, new Version (3, 1, 8)),
 /**
  * Prior to JDBC-4.0, the JDBC specification had a bug related to what could be
  * given as a "column name" to ResultSet methods like findColumn(), or getters
  * that took a String property. JDBC-4.0 clarified "column name"
  * " to mean the label, as given in an "AS" clause and returned by
  * ResultSetMetaData.getColumnLabel(), and if no AS clause, the column name.
  * Setting this property to "true" will give behavior that is congruent to
  * JDBC-3.0 and earlier versions of the JDBC specification, but which because
  * of the specification bug could give unexpected results. This property is
  * preferred over "useOldAliasMetadataBehavior" unless you need the specific
  * behavior that it provides with respect to ResultSetMetadata.
  */
  useColumnNamesInFindColumn ("useColumnNamesInFindColumn", Boolean.FALSE.toString (), new Version (5, 1, 7)),
 /**
  * Should the driver allow NaN or +/- INF values in
  * PreparedStatement.setDouble()?
  */
  allowNanAndInf ("allowNanAndInf", Boolean.FALSE.toString (), new Version (3, 1, 5)),
 /**
  * Should the driver automatically call .close() on streams/readers passed as
  * arguments via set*() methods?
  */
  autoClosePStmtStreams ("autoClosePStmtStreams", Boolean.FALSE.toString (), new Version (3, 1, 12)),
 /**
  * Should the driver automatically detect and de-serialize objects stored in
  * BLOB fields?
  */
  autoDeserialize ("autoDeserialize", Boolean.FALSE.toString (), new Version (3, 1, 5)),
 /**
  * Should the driver always treat BLOBs as Strings - specifically to work
  * around dubious metadata returned by the server for GROUP BY clauses?
  */
  blobsAreStrings ("blobsAreStrings", Boolean.FALSE.toString (), new Version (5, 0, 8)),
 /**
  * Capitalize type names in DatabaseMetaData? (usually only useful when using
  * WebObjects, true/false, defaults to 'false')
  */
  capitalizeTypeNames ("capitalizeTypeNames", Boolean.TRUE.toString (), new Version (2, 0, 7)),
 /**
  * The character encoding to use for sending and retrieving TEXT, MEDIUMTEXT
  * and LONGTEXT values instead of the configured connection characterEncoding
  */
  clobCharacterEncoding ("clobCharacterEncoding", null, new Version (5, 0, 0)),
 /**
  * This will cause a 'streaming' ResultSet to be automatically closed, and any
  * outstanding data still streaming from the server to be discarded if another
  * query is executed before all the data has been read from the server.
  */
  clobberStreamingResults ("clobberStreamingResults", Boolean.FALSE.toString (), new Version (3, 0, 9)),
 /**
  * Should the driver compensate for the update counts of "ON DUPLICATE KEY"
  * INSERT statements (2 = 1, 0 = 1) when using prepared statements?
  */
  compensateOnDuplicateKeyUpdateCounts ("compensateOnDuplicateKeyUpdateCounts", Boolean.FALSE.toString (), new Version (5,
                                                                                                                        1,
                                                                                                                        7)),
 /**
  * Should the driver continue processing batch commands if one statement fails.
  * The JDBC spec allows either way (defaults to 'true').
  */
  continueBatchOnError ("continueBatchOnError", Boolean.TRUE.toString (), new Version (3, 0, 3)),
 /**
  * Creates the database given in the URL if it doesn't yet exist. Assumes the
  * configured user has permissions to create databases.
  */
  createDatabaseIfNotExist ("createDatabaseIfNotExist", Boolean.FALSE.toString (), new Version (3, 1, 9)),
 /**
  * Should the driver allow conversions from empty string fields to numeric
  * values of '0'?
  */
  emptyStringsConvertToZero ("emptyStringsConvertToZero", Boolean.TRUE.toString (), new Version (3, 1, 8)),
 /**
  * Should the driver emulate java.sql.Blobs with locators? With this feature
  * enabled, the driver will delay loading the actual Blob data until the one of
  * the retrieval methods (getInputStream(), getBytes(), and so forth) on the
  * blob data stream has been accessed. For this to work, you must use a column
  * alias with the value of the column to the actual name of the Blob. The
  * feature also has the following restrictions: The SELECT that created the
  * result set must reference only one table, the table must have a primary key;
  * the SELECT must alias the original blob column name, specified as a string,
  * to an alternate name; the SELECT must cover all columns that make up the
  * primary key.
  */
  emulateLocators ("emulateLocators", Boolean.FALSE.toString (), new Version (3, 1, 0)),
 /**
  * Should the driver detect prepared statements that are not supported by the
  * server, and replace them with client-side emulated versions?
  */
  emulateUnsupportedPstmts ("emulateUnsupportedPstmts", Boolean.TRUE.toString (), new Version (3, 1, 7)),
 /**
  * Comma-delimited list of classes that implement
  * com.mysql.jdbc.ExceptionInterceptor. These classes will be instantiated one
  * per Connection instance, and all SQLExceptions thrown by the driver will be
  * allowed to be intercepted by these interceptors, in a chained fashion, with
  * the first class listed as the head of the chain.
  */
  exceptionInterceptors ("exceptionInterceptors", null, new Version (5, 1, 8)),
 /**
  * Should the driver always treat data from functions returning BLOBs as
  * Strings - specifically to work around dubious metadata returned by the
  * server for GROUP BY clauses?
  */
  functionsNeverReturnBlobs ("functionsNeverReturnBlobs", Boolean.FALSE.toString (), new Version (5, 0, 8)),
 /**
  * Should the driver generate simplified parameter metadata for
  * PreparedStatements when no metadata is available either because the server
  * couldn't support preparing the statement, or server-side prepared statements
  * are disabled?
  */
  generateSimpleParameterMetadata ("generateSimpleParameterMetadata", Boolean.FALSE.toString (), new Version (5, 0, 5)),
 /**
  * Ignore non-transactional table warning for rollback? (defaults to 'false').
  */
  ignoreNonTxTables ("ignoreNonTxTables", Boolean.FALSE.toString (), new Version (3, 0, 9)),
 /**
  * Should the driver throw java.sql.DataTruncation exceptions when data is
  * truncated as is required by the JDBC specification when connected to a
  * server that supports warnings (MySQL 4.1.0 and newer)? This property has no
  * effect if the server sql-mode includes STRICT_TRANS_TABLES.
  */
  jdbcCompliantTruncation ("jdbcCompliantTruncation", Boolean.TRUE.toString (), new Version (3, 1, 2)),
 /**
  * When load-balancing is enabled for auto-commit statements (via
  * loadBalanceAutoCommitStatementThreshold), the statement counter will only
  * increment when the SQL matches the regular expression. By default, every
  * statement issued matches.
  */
  loadBalanceAutoCommitStatementRegex ("loadBalanceAutoCommitStatementRegex", null, new Version (5, 1, 15)),
 /**
  * When auto-commit is enabled, the number of statements which should be
  * executed before triggering load-balancing to rebalance. Default value of 0
  * causes load-balanced connections to only rebalance when exceptions are
  * encountered, or auto-commit is disabled and transactions are explicitly
  * committed or rolled back.
  */
  loadBalanceAutoCommitStatementThreshold ("loadBalanceAutoCommitStatementThreshold", "0", new Version (5, 1, 15)),
 /**
  * Time in milliseconds between checks of servers which are unavailable, by
  * controlling how long a server lives in the global blacklist.
  */
  loadBalanceBlacklistTimeout ("loadBalanceBlacklistTimeout", "0", new Version (5, 1, 0)),
 /**
  * Logical group of load-balanced connections within a classloader, used to
  * manage different groups independently. If not specified, live management of
  * load-balanced connections is disabled.
  */
  loadBalanceConnectionGroup ("loadBalanceConnectionGroup", null, new Version (5, 1, 13)),
 /**
  * Fully-qualified class name of custom exception checker. The class must
  * implement com.mysql.jdbc.LoadBalanceExceptionChecker interface, and is used
  * to inspect SQLExceptions and determine whether they should trigger fail-over
  * to another host in a load-balanced deployment.
  */
  loadBalanceExceptionChecker ("loadBalanceExceptionChecker", com.mysql.jdbc.StandardLoadBalanceExceptionChecker.class.getName (), new Version (5,
                                                                                                                                                1,
                                                                                                                                                13)),
 /**
  * Time in milliseconds to wait for ping response from each of load-balanced
  * physical connections when using load-balanced Connection.
  */
  loadBalancePingTimeout ("loadBalancePingTimeout", "0", new Version (5, 1, 13)),
 /**
  * Comma-delimited list of classes/interfaces used by default load-balanced
  * exception checker to determine whether a given SQLException should trigger
  * failover. The comparison is done using Class.isInstance(SQLException) using
  * the thrown SQLException.
  */
  loadBalanceSQLExceptionSubclassFailover ("loadBalanceSQLExceptionSubclassFailover", null, new Version (5, 1, 13)),
 /**
  * Comma-delimited list of SQLState codes used by default load-balanced
  * exception checker to determine whether a given SQLException should trigger
  * failover. The SQLState of a given SQLException is evaluated to determine
  * whether it begins with any value in the comma-delimited list.
  */
  loadBalanceSQLStateFailover ("loadBalanceSQLStateFailover", null, new Version (5, 1, 13)),
 /**
  * Should the load-balanced Connection explicitly check whether the connection
  * is live when swapping to a new physical connection at commit/rollback?
  */
  loadBalanceValidateConnectionOnSwapServer ("loadBalanceValidateConnectionOnSwapServer", Boolean.FALSE.toString (), new Version (5,
                                                                                                                                  1,
                                                                                                                                  13)),
 /**
  * The maximum number of rows to return (0, the default means return all rows).
  */
  maxRows ("maxRows", "-1", null),
 /**
  * What value should the driver automatically set the server setting
  * 'net_write_timeout' to when the streaming result sets feature is in use?
  * (value has unit of seconds, the value '0' means the driver will not try and
  * adjust this value)
  */
  netTimeoutForStreamingResults ("netTimeoutForStreamingResults", "600", new Version (5, 1, 0)),
 /**
  * When determining procedure parameter types for CallableStatements, and the
  * connected user can't access procedure bodies through "SHOW CREATE PROCEDURE"
  * or select on mysql.proc should the driver instead create basic metadata (all
  * parameters reported as IN VARCHARs, but allowing registerOutParameter() to
  * be called on them anyway) instead of throwing an exception?
  */
  noAccessToProcedureBodies ("noAccessToProcedureBodies", Boolean.FALSE.toString (), new Version (5, 0, 3)),
 /**
  * Don't ensure that
  * ResultSet.getDatetimeType().toString().equals(ResultSet.getString())
  */
  noDatetimeStringSync ("noDatetimeStringSync", Boolean.FALSE.toString (), new Version (3, 1, 7)),
 /**
  * Don't convert TIME values using the server timezone if 'useTimezone'='true'
  */
  noTimezoneConversionForTimeType ("noTimezoneConversionForTimeType", Boolean.FALSE.toString (), new Version (5, 0, 0)),
 /**
  * When DatabaseMetadataMethods ask for a 'catalog' parameter, does the value
  * null mean use the current catalog? (this is not JDBC-compliant, but follows
  * legacy behavior from earlier versions of the driver)
  */
  nullCatalogMeansCurrent ("nullCatalogMeansCurrent", Boolean.TRUE.toString (), new Version (3, 1, 8)),
 /**
  * Should DatabaseMetaData methods that accept *pattern parameters treat null
  * the same as '%' (this is not JDBC-compliant, however older versions of the
  * driver accepted this departure from the specification)
  */
  nullNamePatternMatchesAll ("nullNamePatternMatchesAll", Boolean.TRUE.toString (), new Version (3, 1, 8)),
 /**
  * Should the driver return "true" for
  * DatabaseMetaData.supportsIntegrityEnhancementFacility() even if the database
  * doesn't support it to workaround applications that require this method to
  * return "true" to signal support of foreign keys, even though the SQL
  * specification states that this facility contains much more than just foreign
  * key support (one such application being OpenOffice)?
  */
  overrideSupportsIntegrityEnhancementFacility ("overrideSupportsIntegrityEnhancementFacility", Boolean.FALSE.toString (), new Version (3,
                                                                                                                                        1,
                                                                                                                                        12)),
 /**
  * If a result set column has the CHAR type and the value does not fill the
  * amount of characters specified in the DDL for the column, should the driver
  * pad the remaining characters with space (for ANSI compliance)?
  */
  padCharsWithSpace ("padCharsWithSpace", Boolean.FALSE.toString (), new Version (5, 0, 6)),
 /** Follow the JDBC spec to the letter. */
  pedantic ("pedantic", Boolean.FALSE.toString (), new Version (3, 0, 0)),
 /**
  * When using XAConnections, should the driver ensure that operations on a
  * given XID are always routed to the same physical connection? This allows the
  * XAConnection to support "XA START ... JOIN" after "XA END" has been called
  */
  pinGlobalTxToPhysicalConnection ("pinGlobalTxToPhysicalConnection", Boolean.FALSE.toString (), new Version (5, 0, 1)),
 /**
  * When using ResultSets that are CONCUR_UPDATABLE, should the driver
  * pre-populate the "insert" row with default values from the DDL for the table
  * used in the query so those values are immediately available for ResultSet
  * accessors? This functionality requires a call to the database for metadata
  * each time a result set of this type is created. If disabled (the default),
  * the default values will be populated by the an internal call to refreshRow()
  * which pulls back default values and/or values changed by triggers.
  */
  populateInsertRowWithDefaultValues ("populateInsertRowWithDefaultValues", Boolean.FALSE.toString (), new Version (5,
                                                                                                                    0,
                                                                                                                    5)),
 /** Should the driver process escape codes in queries that are prepared? */
  processEscapeCodesForPrepStmts ("processEscapeCodesForPrepStmts", Boolean.TRUE.toString (), new Version (3, 1, 12)),
 /**
  * If the timeout given in Statement.setQueryTimeout() expires, should the
  * driver forcibly abort the Connection instead of attempting to abort the
  * query?
  */
  queryTimeoutKillsConnection ("queryTimeoutKillsConnection", Boolean.FALSE.toString (), new Version (5, 1, 9)),
 /**
  * If the version of MySQL the driver connects to does not support
  * transactions, still allow calls to commit(), rollback() and setAutoCommit()
  * (true/false, defaults to 'false')?
  */
  relaxAutoCommit ("relaxAutoCommit", Boolean.FALSE.toString (), new Version (2, 0, 13)),
 /**
  * Should the driver retain the Statement reference in a ResultSet after
  * ResultSet.close() has been called. This is not JDBC-compliant after
  * JDBC-4.0.
  */
  retainStatementAfterResultSetClose ("retainStatementAfterResultSetClose", Boolean.FALSE.toString (), new Version (3,
                                                                                                                    1,
                                                                                                                    11)),
 /**
  * Should the driver issue a rollback() when the logical connection in a pool
  * is closed?
  */
  rollbackOnPooledClose ("rollbackOnPooledClose", Boolean.TRUE.toString (), new Version (3, 0, 15)),
 /**
  * Enables workarounds for bugs in Sun's JDBC compliance testsuite version 1.3
  */
  runningCTS13 ("runningCTS13", Boolean.FALSE.toString (), new Version (3, 1, 7)),
 /**
  * Override detection/mapping of timezone. Used when timezone from server
  * doesn't map to Java timezone
  */
  serverTimezone ("serverTimezone", null, new Version (3, 0, 2)),
 /**
  * A comma-delimited list of classes that implement
  * "com.mysql.jdbc.StatementInterceptor" that should be placed "in between"
  * query execution to influence the results. StatementInterceptors are
  * "chainable", the results returned by the "current" interceptor will be
  * passed on to the next in in the chain, from left-to-right order, as
  * specified in this property.
  */
  statementInterceptors ("statementInterceptors", null, new Version (5, 1, 1)),
 /** Used only in older versions of compliance test */
  strictFloatingPoint ("strictFloatingPoint", Boolean.FALSE.toString (), new Version (3, 0, 0)),
 /**
  * Should the driver do strict checking (all primary keys selected) of
  * updatable result sets (true, false, defaults to 'true')?
  */
  strictUpdates ("strictUpdates", Boolean.TRUE.toString (), new Version (3, 0, 4)),
 /**
  * Should the driver treat the datatype TINYINT(1) as the BIT type (because the
  * server silently converts BIT -&gt; TINYINT(1) when creating tables)?
  */
  tinyInt1isBit ("tinyInt1isBit", Boolean.TRUE.toString (), new Version (3, 0, 16)),
 /**
  * If the driver converts TINYINT(1) to a different type, should it use BOOLEAN
  * instead of BIT for future compatibility with MySQL-5.0, as MySQL-5.0 has a
  * BIT type?
  */
  transformedBitIsBoolean ("transformedBitIsBoolean", Boolean.FALSE.toString (), new Version (3, 1, 9)),
 /**
  * Should the driver treat java.util.Date as a TIMESTAMP for the purposes of
  * PreparedStatement.setObject()?
  */
  treatUtilDateAsTimestamp ("treatUtilDateAsTimestamp", Boolean.TRUE.toString (), new Version (5, 0, 5)),
 /**
  * Create PreparedStatements for prepareCall() when required, because UltraDev
  * is broken and issues a prepareCall() for _all_ statements? (true/false,
  * defaults to 'false')
  */
  ultraDevHack ("ultraDevHack", Boolean.FALSE.toString (), new Version (2, 0, 3)),
 /**
  * Don't set the CLIENT_FOUND_ROWS flag when connecting to the server (not
  * JDBC-compliant, will break most applications that rely on "found" rows vs.
  * "affected rows" for DML statements), but does cause "correct" update counts
  * from "INSERT ... ON DUPLICATE KEY UPDATE" statements to be returned by the
  * server.
  */
  useAffectedRows ("useAffectedRows", Boolean.FALSE.toString (), new Version (5, 1, 7)),
 /**
  * Convert between session timezone and GMT before creating Date and Timestamp
  * instances (value of "false" is legacy behavior, "true" leads to more
  * JDBC-compliant behavior.
  */
  useGmtMillisForDatetimes ("useGmtMillisForDatetimes", Boolean.FALSE.toString (), new Version (3, 1, 12)),
 /**
  * Add '@hostname' to users in DatabaseMetaData.getColumn/TablePrivileges()
  * (true/false), defaults to 'true'.
  */
  useHostsInPrivileges ("useHostsInPrivileges", Boolean.TRUE.toString (), new Version (3, 0, 2)),
 /**
  * When connected to MySQL-5.0.7 or newer, should the driver use the
  * INFORMATION_SCHEMA to derive information used by DatabaseMetaData?
  */
  useInformationSchema ("useInformationSchema", Boolean.FALSE.toString (), new Version (5, 0, 0)),
 /**
  * Should the driver use JDBC-compliant rules when converting
  * TIME/TIMESTAMP/DATETIME values' timezone information for those JDBC
  * arguments which take a java.util.Calendar argument? (Notice that this option
  * is exclusive of the "useTimezone=true" configuration option.)
  */
  useJDBCCompliantTimezoneShift ("useJDBCCompliantTimezoneShift", Boolean.FALSE.toString (), new Version (5, 0, 0)),
 /**
  * Use code for DATE/TIME/DATETIME/TIMESTAMP handling in result sets and
  * statements that consistently handles timezone conversions from client to
  * server and back again, or use the legacy code for these datatypes that has
  * been in the driver for backwards-compatibility?
  */
  useLegacyDatetimeCode ("useLegacyDatetimeCode", Boolean.TRUE.toString (), new Version (5, 1, 6)),
 /**
  * Should the driver use the legacy behavior for "AS" clauses on columns and
  * tables, and only return aliases (if any) for
  * ResultSetMetaData.getColumnName() or ResultSetMetaData.getTableName() rather
  * than the original column/table name? In 5.0.x, the default value was true.
  */
  useOldAliasMetadataBehavior ("useOldAliasMetadataBehavior", Boolean.FALSE.toString (), new Version (5, 0, 4)),
 /**
  * Use the UTF-8 behavior the driver did when communicating with 4.0 and older
  * servers
  */
  useOldUTF8Behavior ("useOldUTF8Behavior", Boolean.FALSE.toString (), new Version (3, 1, 6)),
 /**
  * Don't prepend 'standard' SQLState error messages to error messages returned
  * by the server.
  */
  useOnlyServerErrorMessages ("useOnlyServerErrorMessages", Boolean.TRUE.toString (), new Version (3, 0, 15)),
 /**
  * If migrating from an environment that was using server-side prepared
  * statements, and the configuration property "useJDBCCompliantTimeZoneShift"
  * set to "true" , use compatible behavior when not using server-side prepared
  * statements when sending TIMESTAMP values to the MySQL server.
  */
  useSSPSCompatibleTimezoneShift ("useSSPSCompatibleTimezoneShift", Boolean.FALSE.toString (), new Version (5, 0, 5)),
 /** Use server-side prepared statements if the server supports them? */
  useServerPrepStmts ("useServerPrepStmts", Boolean.FALSE.toString (), new Version (3, 1, 0)),
 /**
  * Use SQL Standard state codes instead of 'legacy' X/Open/SQL state codes
  * (true/false), default is 'true'
  */
  useSqlStateCodes ("useSqlStateCodes", Boolean.TRUE.toString (), new Version (3, 1, 3)),
 /**
  * Honor stream length parameter in PreparedStatement/ResultSet.setXXXStream()
  * method calls (true/false, defaults to 'true')?
  */
  useStreamLengthsInPrepStmts ("useStreamLengthsInPrepStmts", Boolean.TRUE.toString (), new Version (3, 0, 2)),
 /**
  * Convert time/date types between client and server timezones (true/false,
  * defaults to 'false')?
  */
  useTimezone ("useTimezone", Boolean.FALSE.toString (), new Version (3, 0, 2)),
 /** Don't use BufferedInputStream for reading data from the server */
  useUnbufferedInput ("useUnbufferedInput", Boolean.TRUE.toString (), new Version (3, 0, 11)),
 /**
  * Should the JDBC driver treat the MySQL type "YEAR" as a java.sql.Date, or as
  * a SHORT?
  */
  yearIsDateType ("yearIsDateType", Boolean.TRUE.toString (), new Version (3, 1, 9)),
 /**
  * What should happen when the driver encounters DATETIME values that are
  * composed entirely of zeros (used by MySQL to represent invalid dates)? Valid
  * values are "exception"", "round" and "convertToNull".
  */
  zeroDateTimeBehavior ("zeroDateTimeBehavior", "exception", new Version (3, 1, 4));

  private final String m_sName;
  private final String m_sDefaultValue;
  private final Version m_aMinVersion;

  private EMySQLConnectionProperty (@Nonnull @Nonempty final String sName,
                                    @Nullable final String sDefaultValue,
                                    @Nullable final Version aMinVersion)
  {
    m_sName = sName;
    m_sDefaultValue = sDefaultValue;
    m_aMinVersion = aMinVersion;
  }

  @Nonnull
  @Nonempty
  public String getName ()
  {
    return m_sName;
  }

  @Nullable
  public String getDefaultValue ()
  {
    return m_sDefaultValue;
  }

  @Nullable
  public Version getMinVersion ()
  {
    return m_aMinVersion;
  }
}
