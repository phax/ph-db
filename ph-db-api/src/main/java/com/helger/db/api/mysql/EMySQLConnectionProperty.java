/*
 * Copyright (C) 2014-2026 Philip Helger (www.helger.com)
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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.annotation.style.CodingStyleguideUnaware;
import com.helger.base.name.IHasName;
import com.helger.base.version.Version;

/**
 * List of MySQL connection properties. <a href=
 * "http://dev.mysql.com/doc/connector-j/6.0/en/connector-j-reference-configuration-properties.html"
 * >Source</a> <br>
 * Automatically created by MainMySQLPropertyExtractor tool
 *
 * @author Philip Helger
 */
@CodingStyleguideUnaware
public enum EMySQLConnectionProperty implements IHasName
{
  /**
   * The user to connect as
   */
  user ("user", null, null),
  /**
   * The password to use when connecting
   */
  password ("password", null, null),
  /**
   * A comma-delimited list of user-defined key:value pairs (in addition to standard MySQL-defined
   * key:value pairs) to be passed to MySQL Server for display as connection attributes in the
   * PERFORMANCE_SCHEMA.SESSION_CONNECT_ATTRS table. Example usage:
   * connectionAttributes=key1:value1,key2:value2 This functionality is available for use with MySQL
   * Server version 5.6 or later only. Earlier versions of MySQL Server do not support connection
   * attributes, causing this configuration option to be ignored. Setting connectionAttributes=none
   * will cause connection attribute processing to be bypassed, for situations where Connection
   * creation/initialization speed is critical.
   *
   * @since MySQL 5.1.25
   */
  connectionAttributes ("connectionAttributes", null, new Version (5, 1, 25)),
  /**
   * A comma-delimited list of classes that implement
   * "com.mysql.cj.api.jdbc.interceptors.ConnectionLifecycleInterceptor" that should notified of
   * connection lifecycle events (creation, destruction, commit, rollback, setCatalog and
   * setAutoCommit) and potentially alter the execution of these commands.
   * ConnectionLifecycleInterceptors are "stackable", more than one interceptor may be specified via
   * the configuration property as a comma-delimited list, with the interceptors executed in order
   * from left to right.
   *
   * @since MySQL 5.1.4
   */
  connectionLifecycleInterceptors ("connectionLifecycleInterceptors", null, new Version (5, 1, 4)),
  /**
   * Load the comma-delimited list of configuration properties before parsing the URL or applying
   * user-specified properties. These configurations are explained in the 'Configurations' of the
   * documentation.
   *
   * @since MySQL 3.1.5
   */
  useConfigs ("useConfigs", null, new Version (3, 1, 5)),
  /**
   * Comma-delimited list of classes that implement
   * com.mysql.cj.api.mysqla.authentication.AuthenticationPlugin and which will be used for
   * authentication unless disabled by "disabledAuthenticationPlugins" property.
   *
   * @since MySQL 5.1.19
   */
  authenticationPlugins ("authenticationPlugins", null, new Version (5, 1, 19)),
  /**
   * Creates the database given in the URL if it doesn't yet exist. Assumes the configured user has
   * permissions to create databases.
   *
   * @since MySQL 3.1.9
   */
  createDatabaseIfNotExist ("createDatabaseIfNotExist", "false", new Version (3, 1, 9)),
  /**
   * Name of a class implementing com.mysql.cj.api.mysqla.authentication.AuthenticationPlugin which
   * will be used as the default authentication plugin (see below). It is an error to use a class
   * which is not listed in "authenticationPlugins" nor it is one of the built-in plugins. It is an
   * error to set as default a plugin which was disabled with "disabledAuthenticationPlugins"
   * property. It is an error to set this value to null or the empty string (i.e. there must be at
   * least a valid default authentication plugin specified for the connection, meeting all
   * constraints listed above).
   *
   * @since MySQL 5.1.19
   */
  defaultAuthenticationPlugin ("defaultAuthenticationPlugin",
                               "com.mysql.cj.mysqla.authentication.MysqlNativePasswordPlugin",
                               new Version (5, 1, 19)),
  /**
   * Should the driver detect custom charsets/collations installed on server (true/false, defaults
   * to 'false'). If this option set to 'true' driver gets actual charsets/collations from server
   * each time connection establishes. This could slow down connection initialization significantly.
   *
   * @since MySQL 5.1.29
   */
  detectCustomCollations ("detectCustomCollations", "false", new Version (5, 1, 29)),
  /**
   * Comma-delimited list of classes implementing
   * com.mysql.cj.api.mysqla.authentication.AuthenticationPlugin or mechanisms, i.e.
   * "mysql_native_password". The authentication plugins or mechanisms listed will not be used for
   * authentication which will fail if it requires one of them. It is an error to disable the
   * default authentication plugin (either the one named by "defaultAuthenticationPlugin" property
   * or the hard-coded one if "defaultAuthenticationPlugin" property is not set).
   *
   * @since MySQL 5.1.19
   */
  disabledAuthenticationPlugins ("disabledAuthenticationPlugins", null, new Version (5, 1, 19)),
  /**
   * If "disconnectOnExpiredPasswords" is set to "false" and password is expired then server enters
   * "sandbox" mode and sends ERR(08001, ER_MUST_CHANGE_PASSWORD) for all commands that are not
   * needed to set a new password until a new password is set.
   *
   * @since MySQL 5.1.23
   */
  disconnectOnExpiredPasswords ("disconnectOnExpiredPasswords", "true", new Version (5, 1, 23)),
  /**
   * Set the CLIENT_INTERACTIVE flag, which tells MySQL to timeout connections based on
   * INTERACTIVE_TIMEOUT instead of WAIT_TIMEOUT
   *
   * @since MySQL 3.1.0
   */
  interactiveClient ("interactiveClient", "false", new Version (3, 1, 0)),
  /**
   * What character encoding is used for passwords? Leaving this set to the default value (null),
   * uses the value set in "characterEncoding" if there is one, otherwise uses UTF-8 as default
   * encoding. If the password contains non-ASCII characters, the password encoding must match what
   * server encoding was set to when the password was created. For passwords in other character
   * encodings, the encoding will have to be specified with this property (or with
   * "characterEncoding"), as it's not possible for the driver to auto-detect this.
   *
   * @since MySQL 5.1.7
   */
  passwordCharacterEncoding ("passwordCharacterEncoding", null, new Version (5, 1, 7)),
  /**
   * An implementation of com.mysql.cj.api.conf.ConnectionPropertiesTransform that the driver will
   * use to modify URL properties passed to the driver before attempting a connection
   *
   * @since MySQL 3.1.4
   */
  propertiesTransform ("propertiesTransform", null, new Version (3, 1, 4)),
  /**
   * Should the driver issue a rollback() when the logical connection in a pool is closed?
   *
   * @since MySQL 3.0.15
   */
  rollbackOnPooledClose ("rollbackOnPooledClose", "true", new Version (3, 0, 15)),
  /**
   * Don't set the CLIENT_FOUND_ROWS flag when connecting to the server (not JDBC-compliant, will
   * break most applications that rely on "found" rows vs. "affected rows" for DML statements), but
   * does cause "correct" update counts from "INSERT ... ON DUPLICATE KEY UPDATE" statements to be
   * returned by the server.
   *
   * @since MySQL 5.1.7
   */
  useAffectedRows ("useAffectedRows", "false", new Version (5, 1, 7)),
  /**
   * What character encoding should the driver use when dealing with strings? (defaults is to
   * 'autodetect')
   *
   * @since MySQL 1.1g
   */
  characterEncoding ("characterEncoding", null, new Version (1, 0, 0)),
  /**
   * Character set to tell the server to return results as.
   *
   * @since MySQL 3.0.13
   */
  characterSetResults ("characterSetResults", null, new Version (3, 0, 13)),
  /**
   * If set, tells the server to use this collation via 'set collation_connection'
   *
   * @since MySQL 3.0.13
   */
  connectionCollation ("connectionCollation", null, new Version (3, 0, 13)),
  /**
   * A comma-separated list of name/value pairs to be sent as SET SESSION ... to the server when the
   * driver connects.
   *
   * @since MySQL 3.1.8
   */
  sessionVariables ("sessionVariables", null, new Version (3, 1, 8)),
  /**
   * Use the UTF-8 behavior the driver did when communicating with 4.0 and older servers
   *
   * @since MySQL 3.1.6
   */
  useOldUTF8Behavior ("useOldUTF8Behavior", "false", new Version (3, 1, 6)),
  /**
   * Name or IP address of SOCKS host to connect through.
   *
   * @since MySQL 5.1.34
   */
  socksProxyHost ("socksProxyHost", null, new Version (5, 1, 34)),
  /**
   * Port of SOCKS server.
   *
   * @since MySQL 5.1.34
   */
  socksProxyPort ("socksProxyPort", "1080", new Version (5, 1, 34)),
  /**
   * The name of the class that the driver should use for creating socket connections to the server.
   * This class must implement the interface 'com.mysql.cj.api.io.SocketFactory' and have public
   * no-args constructor.
   *
   * @since MySQL 3.0.3
   */
  socketFactory ("socketFactory", "com.mysql.cj.core.io.StandardSocketFactory", new Version (3, 0, 3)),
  /**
   * Timeout for socket connect (in milliseconds), with 0 being no timeout. Only works on JDK-1.4 or
   * newer. Defaults to '0'.
   *
   * @since MySQL 3.0.1
   */
  connectTimeout ("connectTimeout", "0", new Version (3, 0, 1)),
  /**
   * Timeout on network socket operations (0, the default means no timeout).
   *
   * @since MySQL 3.0.1
   */
  socketTimeout ("socketTimeout", "0", new Version (3, 0, 1)),
  /**
   * Hostname or IP address given to explicitly configure the interface that the driver will bind
   * the client side of the TCP/IP connection to when connecting.
   *
   * @since MySQL 5.0.5
   */
  localSocketAddress ("localSocketAddress", null, new Version (5, 0, 5)),
  /**
   * Maximum allowed packet size to send to server. If not set, the value of system variable
   * 'max_allowed_packet' will be used to initialize this upon connecting. This value will not take
   * effect if set larger than the value of 'max_allowed_packet'. Also, due to an internal
   * dependency with the property "blobSendChunkSize", this setting has a minimum value of "8203" if
   * "useServerPrepStmts" is set to "true".
   *
   * @since MySQL 5.1.8
   */
  maxAllowedPacket ("maxAllowedPacket", "65535", new Version (5, 1, 8)),
  /**
   * If connecting using TCP/IP, should the driver set SO_KEEPALIVE?
   *
   * @since MySQL 5.0.7
   */
  tcpKeepAlive ("tcpKeepAlive", "true", new Version (5, 0, 7)),
  /**
   * If connecting using TCP/IP, should the driver set SO_TCP_NODELAY (disabling the Nagle
   * Algorithm)?
   *
   * @since MySQL 5.0.7
   */
  tcpNoDelay ("tcpNoDelay", "true", new Version (5, 0, 7)),
  /**
   * If connecting using TCP/IP, should the driver set SO_RCV_BUF to the given value? The default
   * value of '0', means use the platform default value for this property)
   *
   * @since MySQL 5.0.7
   */
  tcpRcvBuf ("tcpRcvBuf", "0", new Version (5, 0, 7)),
  /**
   * If connecting using TCP/IP, should the driver set SO_SND_BUF to the given value? The default
   * value of '0', means use the platform default value for this property)
   *
   * @since MySQL 5.0.7
   */
  tcpSndBuf ("tcpSndBuf", "0", new Version (5, 0, 7)),
  /**
   * If connecting using TCP/IP, should the driver set traffic class or type-of-service fields ?See
   * the documentation for java.net.Socket.setTrafficClass() for more information.
   *
   * @since MySQL 5.0.7
   */
  tcpTrafficClass ("tcpTrafficClass", "0", new Version (5, 0, 7)),
  /**
   * Use zlib compression when communicating with the server (true/false)? Defaults to 'false'.
   *
   * @since MySQL 3.0.17
   */
  useCompression ("useCompression", "false", new Version (3, 0, 17)),
  /**
   * Don't use BufferedInputStream for reading data from the server
   *
   * @since MySQL 3.0.11
   */
  useUnbufferedInput ("useUnbufferedInput", "true", new Version (3, 0, 11)),
  /**
   * Allow the use of ';' to delimit multiple queries during one statement (true/false), defaults to
   * 'false', and does not affect the addBatch() and executeBatch() methods, which instead rely on
   * rewriteBatchStatements.
   *
   * @since MySQL 3.1.1
   */
  allowMultiQueries ("allowMultiQueries", "false", new Version (3, 1, 1)),
  /**
   * Use SSL when communicating with the server (true/false), default is 'true' when connecting to
   * MySQL 5.5.45+, 5.6.26+ or 5.7.6+, otherwise default is 'false'
   *
   * @since MySQL 3.0.2
   */
  useSSL ("useSSL", "false", new Version (3, 0, 2)),
  /**
   * Require server support of SSL connection if useSSL=true? (defaults to 'false').
   *
   * @since MySQL 3.1.0
   */
  requireSSL ("requireSSL", "false", new Version (3, 1, 0)),
  /**
   * If "useSSL" is set to "true", should the driver verify the server's certificate? When using
   * this feature, the keystore parameters should be specified by the "clientCertificateKeyStore*"
   * properties, rather than system properties. Default is 'false' when connecting to MySQL 5.5.45+,
   * 5.6.26+ or 5.7.6+ and "useSSL" was not explicitly set to "true". Otherwise default is 'true'
   *
   * @since MySQL 5.1.6
   */
  verifyServerCertificate ("verifyServerCertificate", "true", new Version (5, 1, 6)),
  /**
   * URL to the client certificate KeyStore (if not specified, use defaults)
   *
   * @since MySQL 5.1.0
   */
  clientCertificateKeyStoreUrl ("clientCertificateKeyStoreUrl", null, new Version (5, 1, 0)),
  /**
   * KeyStore type for client certificates (NULL or empty means use the default, which is "JKS".
   * Standard keystore types supported by the JVM are "JKS" and "PKCS12", your environment may have
   * more available depending on what security products are installed and available to the JVM.
   *
   * @since MySQL 5.1.0
   */
  clientCertificateKeyStoreType ("clientCertificateKeyStoreType", "JKS", new Version (5, 1, 0)),
  /**
   * Password for the client certificates KeyStore
   *
   * @since MySQL 5.1.0
   */
  clientCertificateKeyStorePassword ("clientCertificateKeyStorePassword", null, new Version (5, 1, 0)),
  /**
   * URL to the trusted root certificate KeyStore (if not specified, use defaults)
   *
   * @since MySQL 5.1.0
   */
  trustCertificateKeyStoreUrl ("trustCertificateKeyStoreUrl", null, new Version (5, 1, 0)),
  /**
   * KeyStore type for trusted root certificates (NULL or empty means use the default, which is
   * "JKS". Standard keystore types supported by the JVM are "JKS" and "PKCS12", your environment
   * may have more available depending on what security products are installed and available to the
   * JVM.
   *
   * @since MySQL 5.1.0
   */
  trustCertificateKeyStoreType ("trustCertificateKeyStoreType", "JKS", new Version (5, 1, 0)),
  /**
   * Password for the trusted root certificates KeyStore
   *
   * @since MySQL 5.1.0
   */
  trustCertificateKeyStorePassword ("trustCertificateKeyStorePassword", null, new Version (5, 1, 0)),
  /**
   * If "useSSL" is set to "true", overrides the cipher suites enabled for use on the underlying SSL
   * sockets. This may be required when using external JSSE providers or to specify cipher suites
   * compatible with both MySQL server and used JVM.
   *
   * @since MySQL 5.1.35
   */
  enabledSSLCipherSuites ("enabledSSLCipherSuites", null, new Version (5, 1, 35)),
  /**
   * Should the driver allow use of 'LOAD DATA LOCAL INFILE...' (defaults to 'true').
   *
   * @since MySQL 3.0.3
   */
  allowLoadLocalInfile ("allowLoadLocalInfile", "true", new Version (3, 0, 3)),
  /**
   * Should the driver allow URLs in 'LOAD DATA LOCAL INFILE' statements?
   *
   * @since MySQL 3.1.4
   */
  allowUrlInLocalInfile ("allowUrlInLocalInfile", "false", new Version (3, 1, 4)),
  /**
   * Allows special handshake roundtrip to get server RSA public key directly from server.
   *
   * @since MySQL 5.1.31
   */
  allowPublicKeyRetrieval ("allowPublicKeyRetrieval", "false", new Version (5, 1, 31)),
  /**
   * Take measures to prevent exposure sensitive information in error messages and clear data
   * structures holding sensitive data when possible? (defaults to 'false')
   *
   * @since MySQL 3.0.1
   */
  paranoid ("paranoid", "false", new Version (3, 0, 1)),
  /**
   * File path to the server RSA public key file for sha256_password authentication. If not
   * specified, the public key will be retrieved from the server.
   *
   * @since MySQL 5.1.31
   */
  serverRSAPublicKeyFile ("serverRSAPublicKeyFile", null, new Version (5, 1, 31)),
  /**
   * Should the driver continue processing batch commands if one statement fails. The JDBC spec
   * allows either way (defaults to 'true').
   *
   * @since MySQL 3.0.3
   */
  continueBatchOnError ("continueBatchOnError", "true", new Version (3, 0, 3)),
  /**
   * The JDBC specification requires the driver to automatically track and close resources, however
   * if your application doesn't do a good job of explicitly calling close() on statements or result
   * sets, this can cause memory leakage. Setting this property to true relaxes this constraint, and
   * can be more memory efficient for some applications. Also the automatic closing of the Statement
   * and current ResultSet in Statement.closeOnCompletion() and Statement.getMoreResults
   * ([Statement.CLOSE_CURRENT_RESULT | Statement.CLOSE_ALL_RESULTS]), respectively, ceases to
   * happen. This property automatically sets holdResultsOpenOverStatementClose=true.
   *
   * @since MySQL 3.1.7
   */
  dontTrackOpenResources ("dontTrackOpenResources", "false", new Version (3, 1, 7)),
  /**
   * If the timeout given in Statement.setQueryTimeout() expires, should the driver forcibly abort
   * the Connection instead of attempting to abort the query?
   *
   * @since MySQL 5.1.9
   */
  queryTimeoutKillsConnection ("queryTimeoutKillsConnection", "false", new Version (5, 1, 9)),
  /**
   * A comma-delimited list of classes that implement
   * "com.mysql.cj.api.jdbc.interceptors.StatementInterceptor" that should be placed "in between"
   * query execution to influence the results. StatementInterceptors are "chainable", the results
   * returned by the "current" interceptor will be passed on to the next in in the chain, from
   * left-to-right order, as specified in this property.
   *
   * @since MySQL 5.1.1
   */
  statementInterceptors ("statementInterceptors", null, new Version (5, 1, 1)),
  /**
   * Should the driver allow NaN or +/- INF values in PreparedStatement.setDouble()?
   *
   * @since MySQL 3.1.5
   */
  allowNanAndInf ("allowNanAndInf", "false", new Version (3, 1, 5)),
  /**
   * Should the driver automatically call .close() on streams/readers passed as arguments via set*()
   * methods?
   *
   * @since MySQL 3.1.12
   */
  autoClosePStmtStreams ("autoClosePStmtStreams", "false", new Version (3, 1, 12)),
  /**
   * Should the driver compensate for the update counts of "ON DUPLICATE KEY" INSERT statements (2 =
   * 1, 0 = 1) when using prepared statements?
   *
   * @since MySQL 5.1.7
   */
  compensateOnDuplicateKeyUpdateCounts ("compensateOnDuplicateKeyUpdateCounts", "false", new Version (5, 1, 7)),
  /**
   * Should the driver detect prepared statements that are not supported by the server, and replace
   * them with client-side emulated versions?
   *
   * @since MySQL 3.1.7
   */
  emulateUnsupportedPstmts ("emulateUnsupportedPstmts", "true", new Version (3, 1, 7)),
  /**
   * Should the driver generate simplified parameter metadata for PreparedStatements when no
   * metadata is available either because the server couldn't support preparing the statement, or
   * server-side prepared statements are disabled?
   *
   * @since MySQL 5.0.5
   */
  generateSimpleParameterMetadata ("generateSimpleParameterMetadata", "false", new Version (5, 0, 5)),
  /**
   * Should the driver process escape codes in queries that are prepared? Default escape processing
   * behavior in non-prepared statements must be defined with the property 'enableEscapeProcessing'.
   *
   * @since MySQL 3.1.12
   */
  processEscapeCodesForPrepStmts ("processEscapeCodesForPrepStmts", "true", new Version (3, 1, 12)),
  /**
   * Use server-side prepared statements if the server supports them?
   *
   * @since MySQL 3.1.0
   */
  useServerPrepStmts ("useServerPrepStmts", "false", new Version (3, 1, 0)),
  /**
   * Honor stream length parameter in PreparedStatement/ResultSet.setXXXStream() method calls
   * (true/false, defaults to 'true')?
   *
   * @since MySQL 3.0.2
   */
  useStreamLengthsInPrepStmts ("useStreamLengthsInPrepStmts", "true", new Version (3, 0, 2)),
  /**
   * This will cause a 'streaming' ResultSet to be automatically closed, and any outstanding data
   * still streaming from the server to be discarded if another query is executed before all the
   * data has been read from the server.
   *
   * @since MySQL 3.0.9
   */
  clobberStreamingResults ("clobberStreamingResults", "false", new Version (3, 0, 9)),
  /**
   * Should the driver allow conversions from empty string fields to numeric values of '0'?
   *
   * @since MySQL 3.1.8
   */
  emptyStringsConvertToZero ("emptyStringsConvertToZero", "true", new Version (3, 1, 8)),
  /**
   * Should the driver close result sets on Statement.close() as required by the JDBC specification?
   *
   * @since MySQL 3.1.7
   */
  holdResultsOpenOverStatementClose ("holdResultsOpenOverStatementClose", "false", new Version (3, 1, 7)),
  /**
   * Should the driver throw java.sql.DataTruncation exceptions when data is truncated as is
   * required by the JDBC specification when connected to a server that supports warnings (MySQL
   * 4.1.0 and newer)? This property has no effect if the server sql-mode includes
   * STRICT_TRANS_TABLES.
   *
   * @since MySQL 3.1.2
   */
  jdbcCompliantTruncation ("jdbcCompliantTruncation", "true", new Version (3, 1, 2)),
  /**
   * The maximum number of rows to return (0, the default means return all rows).
   */
  maxRows ("maxRows", "-1", null),
  /**
   * What value should the driver automatically set the server setting 'net_write_timeout' to when
   * the streaming result sets feature is in use? (value has unit of seconds, the value '0' means
   * the driver will not try and adjust this value)
   *
   * @since MySQL 5.1.0
   */
  netTimeoutForStreamingResults ("netTimeoutForStreamingResults", "600", new Version (5, 1, 0)),
  /**
   * If a result set column has the CHAR type and the value does not fill the amount of characters
   * specified in the DDL for the column, should the driver pad the remaining characters with space
   * (for ANSI compliance)?
   *
   * @since MySQL 5.0.6
   */
  padCharsWithSpace ("padCharsWithSpace", "false", new Version (5, 0, 6)),
  /**
   * When using ResultSets that are CONCUR_UPDATABLE, should the driver pre-populate the "insert"
   * row with default values from the DDL for the table used in the query so those values are
   * immediately available for ResultSet accessors? This functionality requires a call to the
   * database for metadata each time a result set of this type is created. If disabled (the
   * default), the default values will be populated by the an internal call to refreshRow() which
   * pulls back default values and/or values changed by triggers.
   *
   * @since MySQL 5.0.5
   */
  populateInsertRowWithDefaultValues ("populateInsertRowWithDefaultValues", "false", new Version (5, 0, 5)),
  /**
   * Should the driver do strict checking (all primary keys selected) of updatable result sets
   * (true, false, defaults to 'true')?
   *
   * @since MySQL 3.0.4
   */
  strictUpdates ("strictUpdates", "true", new Version (3, 0, 4)),
  /**
   * Should the driver treat the datatype TINYINT(1) as the BIT type (because the server silently
   * converts BIT -&gt; TINYINT(1) when creating tables)?
   *
   * @since MySQL 3.0.16
   */
  tinyInt1isBit ("tinyInt1isBit", "true", new Version (3, 0, 16)),
  /**
   * If the driver converts TINYINT(1) to a different type, should it use BOOLEAN instead of BIT for
   * future compatibility with MySQL-5.0, as MySQL-5.0 has a BIT type?
   *
   * @since MySQL 3.1.9
   */
  transformedBitIsBoolean ("transformedBitIsBoolean", "false", new Version (3, 1, 9)),
  /**
   * Pre-JDBC4 DatabaseMetaData API has only the getProcedures() and getProcedureColumns() methods,
   * so they return metadata info for both stored procedures and functions. JDBC4 was extended with
   * the getFunctions() and getFunctionColumns() methods and the expected behaviours of previous
   * methods are not well defined. For JDBC4 and higher, default 'true' value of the option means
   * that calls of DatabaseMetaData.getProcedures() and DatabaseMetaData.getProcedureColumns()
   * return metadata for both procedures and functions as before, keeping backward compatibility.
   * Setting this property to 'false' decouples Connector/J from its pre-JDBC4 behaviours for
   * DatabaseMetaData.getProcedures() and DatabaseMetaData.getProcedureColumns(), forcing them to
   * return metadata for procedures only.
   *
   * @since MySQL 5.1.26
   */
  getProceduresReturnsFunctions ("getProceduresReturnsFunctions", "true", new Version (5, 1, 26)),
  /**
   * When determining procedure parameter types for CallableStatements, and the connected user can't
   * access procedure bodies through "SHOW CREATE PROCEDURE" or select on mysql.proc should the
   * driver instead create basic metadata (all parameters reported as INOUT VARCHARs) instead of
   * throwing an exception?
   *
   * @since MySQL 5.0.3
   */
  noAccessToProcedureBodies ("noAccessToProcedureBodies", "false", new Version (5, 0, 3)),
  /**
   * When DatabaseMetadataMethods ask for a 'catalog' parameter, does the value null mean use the
   * current catalog? (this is not JDBC-compliant, but follows legacy behavior from earlier versions
   * of the driver)
   *
   * @since MySQL 3.1.8
   */
  nullCatalogMeansCurrent ("nullCatalogMeansCurrent", "false", new Version (3, 1, 8)),
  /**
   * Should DatabaseMetaData methods that accept *pattern parameters treat null the same as '%'
   * (this is not JDBC-compliant, however older versions of the driver accepted this departure from
   * the specification)
   *
   * @since MySQL 3.1.8
   */
  nullNamePatternMatchesAll ("nullNamePatternMatchesAll", "false", new Version (3, 1, 8)),
  /**
   * Add '@hostname' to users in DatabaseMetaData.getColumn/TablePrivileges() (true/false), defaults
   * to 'true'.
   *
   * @since MySQL 3.0.2
   */
  useHostsInPrivileges ("useHostsInPrivileges", "true", new Version (3, 0, 2)),
  /**
   * When connected to MySQL-5.0.7 or newer, should the driver use the INFORMATION_SCHEMA to derive
   * information used by DatabaseMetaData?
   *
   * @since MySQL 5.0.0
   */
  useInformationSchema ("useInformationSchema", "false", new Version (5, 0, 0)),
  /**
   * Should the driver automatically detect and de-serialize objects stored in BLOB fields?
   *
   * @since MySQL 3.1.5
   */
  autoDeserialize ("autoDeserialize", "false", new Version (3, 1, 5)),
  /**
   * Chunk size to use when sending BLOB/CLOBs via ServerPreparedStatements. Note that this value
   * cannot exceed the value of "maxAllowedPacket" and, if that is the case, then this value will be
   * corrected automatically.
   *
   * @since MySQL 3.1.9
   */
  blobSendChunkSize ("blobSendChunkSize", "1048576", new Version (3, 1, 9)),
  /**
   * Should the driver always treat BLOBs as Strings - specifically to work around dubious metadata
   * returned by the server for GROUP BY clauses?
   *
   * @since MySQL 5.0.8
   */
  blobsAreStrings ("blobsAreStrings", "false", new Version (5, 0, 8)),
  /**
   * The character encoding to use for sending and retrieving TEXT, MEDIUMTEXT and LONGTEXT values
   * instead of the configured connection characterEncoding
   *
   * @since MySQL 5.0.0
   */
  clobCharacterEncoding ("clobCharacterEncoding", null, new Version (5, 0, 0)),
  /**
   * Should the driver emulate java.sql.Blobs with locators? With this feature enabled, the driver
   * will delay loading the actual Blob data until the one of the retrieval methods
   * (getInputStream(), getBytes(), and so forth) on the blob data stream has been accessed. For
   * this to work, you must use a column alias with the value of the column to the actual name of
   * the Blob. The feature also has the following restrictions: The SELECT that created the result
   * set must reference only one table, the table must have a primary key; the SELECT must alias the
   * original blob column name, specified as a string, to an alternate name; the SELECT must cover
   * all columns that make up the primary key.
   *
   * @since MySQL 3.1.0
   */
  emulateLocators ("emulateLocators", "false", new Version (3, 1, 0)),
  /**
   * Should the driver always treat data from functions returning BLOBs as Strings - specifically to
   * work around dubious metadata returned by the server for GROUP BY clauses?
   *
   * @since MySQL 5.0.8
   */
  functionsNeverReturnBlobs ("functionsNeverReturnBlobs", "false", new Version (5, 0, 8)),
  /**
   * If 'emulateLocators' is configured to 'true', what size buffer should be used when fetching
   * BLOB data for getBinaryInputStream?
   *
   * @since MySQL 3.2.1
   */
  locatorFetchBufferSize ("locatorFetchBufferSize", "1048576", new Version (3, 2, 1)),
  /**
   * Don't ensure that ResultSet.getDatetimeType().toString().equals(ResultSet.getString())
   *
   * @since MySQL 3.1.7
   */
  noDatetimeStringSync ("noDatetimeStringSync", "false", new Version (3, 1, 7)),
  /**
   * Send fractional part from TIMESTAMP seconds. If set to false, the nanoseconds value of
   * TIMESTAMP values will be truncated before sending any data to the server. This option applies
   * only to prepared statements, callable statements or updatable result sets.
   *
   * @since MySQL 5.1.37
   */
  sendFractionalSeconds ("sendFractionalSeconds", "true", new Version (5, 1, 37)),
  /**
   * Override detection/mapping of time zone. Used when time zone from server doesn't map to Java
   * time zone
   *
   * @since MySQL 3.0.2
   */
  serverTimezone ("serverTimezone", null, new Version (3, 0, 2)),
  /**
   * Should the driver treat java.util.Date as a TIMESTAMP for the purposes of
   * PreparedStatement.setObject()?
   *
   * @since MySQL 5.0.5
   */
  treatUtilDateAsTimestamp ("treatUtilDateAsTimestamp", "true", new Version (5, 0, 5)),
  /**
   * Should the JDBC driver treat the MySQL type "YEAR" as a java.sql.Date, or as a SHORT?
   *
   * @since MySQL 3.1.9
   */
  yearIsDateType ("yearIsDateType", "true", new Version (3, 1, 9)),
  /**
   * What should happen when the driver encounters DATETIME values that are composed entirely of
   * zeros (used by MySQL to represent invalid dates)? Valid values are "exception", "round" and
   * "convertToNull".
   *
   * @since MySQL 3.1.4
   */
  zeroDateTimeBehavior ("zeroDateTimeBehavior", "exception", new Version (3, 1, 4)),
  /**
   * Should the driver try to re-establish stale and/or dead connections? If enabled the driver will
   * throw an exception for a queries issued on a stale or dead connection, which belong to the
   * current transaction, but will attempt reconnect before the next query issued on the connection
   * in a new transaction. The use of this feature is not recommended, because it has side effects
   * related to session state and data consistency when applications don't handle SQLExceptions
   * properly, and is only designed to be used when you are unable to configure your application to
   * handle SQLExceptions resulting from dead and stale connections properly. Alternatively, as a
   * last option, investigate setting the MySQL server variable "wait_timeout" to a high value,
   * rather than the default of 8 hours.
   *
   * @since MySQL 1.1
   */
  autoReconnect ("autoReconnect", "false", new Version (1, 1, 0)),
  /**
   * Use a reconnection strategy appropriate for connection pools (defaults to 'false')
   *
   * @since MySQL 3.1.3
   */
  autoReconnectForPools ("autoReconnectForPools", "false", new Version (3, 1, 3)),
  /**
   * When failing over in autoReconnect mode, should the connection be set to 'read-only'?
   *
   * @since MySQL 3.0.12
   */
  failOverReadOnly ("failOverReadOnly", "true", new Version (3, 0, 12)),
  /**
   * Maximum number of reconnects to attempt if autoReconnect is true, default is '3'.
   *
   * @since MySQL 1.1
   */
  maxReconnects ("maxReconnects", "3", new Version (1, 1, 0)),
  /**
   * If autoReconnect is set to true, should the driver attempt reconnections at the end of every
   * transaction?
   *
   * @since MySQL 3.0.10
   */
  reconnectAtTxEnd ("reconnectAtTxEnd", "false", new Version (3, 0, 10)),
  /**
   * When using loadbalancing or failover, the number of times the driver should cycle through
   * available hosts, attempting to connect. Between cycles, the driver will pause for 250ms if no
   * servers are available.
   *
   * @since MySQL 5.1.6
   */
  retriesAllDown ("retriesAllDown", "120", new Version (5, 1, 6)),
  /**
   * If autoReconnect is enabled, the initial time to wait between re-connect attempts (in seconds,
   * defaults to '2').
   *
   * @since MySQL 1.1
   */
  initialTimeout ("initialTimeout", "2", new Version (1, 1, 0)),
  /**
   * When autoReconnect is enabled, and failoverReadonly is false, should we pick hosts to connect
   * to on a round-robin basis?
   *
   * @since MySQL 3.1.2
   */
  roundRobinLoadBalance ("roundRobinLoadBalance", "false", new Version (3, 1, 2)),
  /**
   * Number of queries to issue before falling back to the primary host when failed over (when using
   * multi-host failover). Whichever condition is met first, 'queriesBeforeRetryMaster' or
   * 'secondsBeforeRetryMaster' will cause an attempt to be made to reconnect to the primary host.
   * Setting both properties to 0 disables the automatic fall back to the primary host at
   * transaction boundaries. Defaults to 50.
   *
   * @since MySQL 3.0.2
   */
  queriesBeforeRetryMaster ("queriesBeforeRetryMaster", "50", new Version (3, 0, 2)),
  /**
   * How long should the driver wait, when failed over, before attempting to reconnect to the
   * primary host? Whichever condition is met first, 'queriesBeforeRetryMaster' or
   * 'secondsBeforeRetryMaster' will cause an attempt to be made to reconnect to the master. Setting
   * both properties to 0 disables the automatic fall back to the primary host at transaction
   * boundaries. Time in seconds, defaults to 30
   *
   * @since MySQL 3.0.2
   */
  secondsBeforeRetryMaster ("secondsBeforeRetryMaster", "30", new Version (3, 0, 2)),
  /**
   * By default, a replication-aware connection will fail to connect when configured master hosts
   * are all unavailable at initial connection. Setting this property to 'true' allows to establish
   * the initial connection, by failing over to the slave servers, in read-only state. It won't
   * prevent subsequent failures when switching back to the master hosts i.e. by setting the
   * replication connection to read/write state.
   *
   * @since MySQL 5.1.27
   */
  allowMasterDownConnections ("allowMasterDownConnections", "false", new Version (5, 1, 27)),
  /**
   * By default, a replication-aware connection will fail to connect when configured slave hosts are
   * all unavailable at initial connection. Setting this property to 'true' allows to establish the
   * initial connection. It won't prevent failures when switching to slaves i.e. by setting the
   * replication connection to read-only state. The property 'readFromMasterWhenNoSlaves' should be
   * used for this purpose.
   *
   * @since MySQL 5.1.38
   */
  allowSlaveDownConnections ("allowSlaveDownConnections", "false", new Version (5, 1, 38)),
  /**
   * Enables JMX-based management of load-balanced connection groups, including live
   * addition/removal of hosts from load-balancing pool. Enables JMX-based management of replication
   * connection groups, including live slave promotion, addition of new slaves and removal of master
   * or slave hosts from load-balanced master and slave connection pools.
   *
   * @since MySQL 5.1.27
   */
  ha_enableJMX ("ha.enableJMX", "false", new Version (5, 1, 27)),
  /**
   * Sets the grace period to wait for a host being removed from a load-balanced connection, to be
   * released when it is currently the active host.
   *
   * @since MySQL 5.1.39
   */
  loadBalanceHostRemovalGracePeriod ("loadBalanceHostRemovalGracePeriod", "15000", new Version (5, 1, 39)),
  /**
   * Replication-aware connections distribute load by using the master hosts when in read/write
   * state and by using the slave hosts when in read-only state. If, when setting the connection to
   * read-only state, none of the slave hosts are available, an SQLExeception is thrown back.
   * Setting this property to 'true' allows to fail over to the master hosts, while setting the
   * connection state to read-only, when no slave hosts are available at switch instant.
   *
   * @since MySQL 5.1.38
   */
  readFromMasterWhenNoSlaves ("readFromMasterWhenNoSlaves", "false", new Version (5, 1, 38)),
  /**
   * =If set to a non-zero value, the driver will report close the connection and report failure
   * when Connection.ping() or Connection.isValid(int) is called if the connection's count of
   * commands sent to the server exceeds this value.
   *
   * @since MySQL 5.1.6
   */
  selfDestructOnPingMaxOperations ("selfDestructOnPingMaxOperations", "0", new Version (5, 1, 6)),
  /**
   * If set to a non-zero value, the driver will report close the connection and report failure when
   * Connection.ping() or Connection.isValid(int) is called if the connection's lifetime exceeds
   * this value.
   *
   * @since MySQL 5.1.6
   */
  selfDestructOnPingSecondsLifetime ("selfDestructOnPingSecondsLifetime", "0", new Version (5, 1, 6)),
  /**
   * If using a load-balanced connection to connect to SQL nodes in a MySQL Cluster/NDB
   * configuration (by using the URL prefix "jdbc:mysql:loadbalance://"), which load balancing
   * algorithm should the driver use: (1) "random" - the driver will pick a random host for each
   * request. This tends to work better than round-robin, as the randomness will somewhat account
   * for spreading loads where requests vary in response time, while round-robin can sometimes lead
   * to overloaded nodes if there are variations in response times across the workload. (2)
   * "bestResponseTime" - the driver will route the request to the host that had the best response
   * time for the previous transaction.
   *
   * @since MySQL 5.0.6
   */
  ha_loadBalanceStrategy ("ha.loadBalanceStrategy", "random", new Version (5, 0, 6)),
  /**
   * When load-balancing is enabled for auto-commit statements (via
   * loadBalanceAutoCommitStatementThreshold), the statement counter will only increment when the
   * SQL matches the regular expression. By default, every statement issued matches.
   *
   * @since MySQL 5.1.15
   */
  loadBalanceAutoCommitStatementRegex ("loadBalanceAutoCommitStatementRegex", null, new Version (5, 1, 15)),
  /**
   * When auto-commit is enabled, the number of statements which should be executed before
   * triggering load-balancing to rebalance. Default value of 0 causes load-balanced connections to
   * only rebalance when exceptions are encountered, or auto-commit is disabled and transactions are
   * explicitly committed or rolled back.
   *
   * @since MySQL 5.1.15
   */
  loadBalanceAutoCommitStatementThreshold ("loadBalanceAutoCommitStatementThreshold", "0", new Version (5, 1, 15)),
  /**
   * Time in milliseconds between checks of servers which are unavailable, by controlling how long a
   * server lives in the global blacklist.
   *
   * @since MySQL 5.1.0
   */
  loadBalanceBlacklistTimeout ("loadBalanceBlacklistTimeout", "0", new Version (5, 1, 0)),
  /**
   * Logical group of load-balanced connections within a classloader, used to manage different
   * groups independently. If not specified, live management of load-balanced connections is
   * disabled.
   *
   * @since MySQL 5.1.13
   */
  loadBalanceConnectionGroup ("loadBalanceConnectionGroup", null, new Version (5, 1, 13)),
  /**
   * Fully-qualified class name of custom exception checker. The class must implement
   * com.mysql.cj.api.jdbc.ha.LoadBalanceExceptionChecker interface, and is used to inspect
   * SQLExceptions and determine whether they should trigger fail-over to another host in a
   * load-balanced deployment.
   *
   * @since MySQL 5.1.13
   */
  loadBalanceExceptionChecker ("loadBalanceExceptionChecker",
                               "com.mysql.cj.jdbc.ha.StandardLoadBalanceExceptionChecker",
                               new Version (5, 1, 13)),
  /**
   * Time in milliseconds to wait for ping response from each of load-balanced physical connections
   * when using load-balanced Connection.
   *
   * @since MySQL 5.1.13
   */
  loadBalancePingTimeout ("loadBalancePingTimeout", "0", new Version (5, 1, 13)),
  /**
   * Comma-delimited list of classes/interfaces used by default load-balanced exception checker to
   * determine whether a given SQLException should trigger failover. The comparison is done using
   * Class.isInstance(SQLException) using the thrown SQLException.
   *
   * @since MySQL 5.1.13
   */
  loadBalanceSQLExceptionSubclassFailover ("loadBalanceSQLExceptionSubclassFailover", null, new Version (5, 1, 13)),
  /**
   * Comma-delimited list of SQLState codes used by default load-balanced exception checker to
   * determine whether a given SQLException should trigger failover. The SQLState of a given
   * SQLException is evaluated to determine whether it begins with any value in the comma-delimited
   * list.
   *
   * @since MySQL 5.1.13
   */
  loadBalanceSQLStateFailover ("loadBalanceSQLStateFailover", null, new Version (5, 1, 13)),
  /**
   * Should the load-balanced Connection explicitly check whether the connection is live when
   * swapping to a new physical connection at commit/rollback?
   *
   * @since MySQL 5.1.13
   */
  loadBalanceValidateConnectionOnSwapServer ("loadBalanceValidateConnectionOnSwapServer",
                                             "false",
                                             new Version (5, 1, 13)),
  /**
   * When using XAConnections, should the driver ensure that operations on a given XID are always
   * routed to the same physical connection? This allows the XAConnection to support "XA START ...
   * JOIN" after "XA END" has been called
   *
   * @since MySQL 5.0.1
   */
  pinGlobalTxToPhysicalConnection ("pinGlobalTxToPhysicalConnection", "false", new Version (5, 0, 1)),
  /**
   * A globally unique name that identifies the resource that this datasource or connection is
   * connected to, used for XAResource.isSameRM() when the driver can't determine this value based
   * on hostnames used in the URL
   *
   * @since MySQL 5.0.1
   */
  resourceId ("resourceId", null, new Version (5, 0, 1)),
  /**
   * If 'cacheCallableStmts' is enabled, how many callable statements should be cached?
   *
   * @since MySQL 3.1.2
   */
  callableStmtCacheSize ("callableStmtCacheSize", "100", new Version (3, 1, 2)),
  /**
   * The number of queries to cache ResultSetMetadata for if cacheResultSetMetaData is set to 'true'
   * (default 50)
   *
   * @since MySQL 3.1.1
   */
  metadataCacheSize ("metadataCacheSize", "50", new Version (3, 1, 1)),
  /**
   * Should the driver refer to the internal values of autocommit and transaction isolation that are
   * set by Connection.setAutoCommit() and Connection.setTransactionIsolation() and transaction
   * state as maintained by the protocol, rather than querying the database or blindly sending
   * commands to the database for commit() or rollback() method calls?
   *
   * @since MySQL 3.1.7
   */
  useLocalSessionState ("useLocalSessionState", "false", new Version (3, 1, 7)),
  /**
   * Should the driver use the in-transaction state provided by the MySQL protocol to determine if a
   * commit() or rollback() should actually be sent to the database?
   *
   * @since MySQL 5.1.7
   */
  useLocalTransactionState ("useLocalTransactionState", "false", new Version (5, 1, 7)),
  /**
   * If prepared statement caching is enabled, how many prepared statements should be cached?
   *
   * @since MySQL 3.0.10
   */
  prepStmtCacheSize ("prepStmtCacheSize", "25", new Version (3, 0, 10)),
  /**
   * If prepared statement caching is enabled, what's the largest SQL the driver will cache the
   * parsing for?
   *
   * @since MySQL 3.0.10
   */
  prepStmtCacheSqlLimit ("prepStmtCacheSqlLimit", "256", new Version (3, 0, 10)),
  /**
   * Name of a class implementing com.mysql.cj.api.CacheAdapterFactory, which will be used to create
   * caches for the parsed representation of client-side prepared statements.
   *
   * @since MySQL 5.1.1
   */
  parseInfoCacheFactory ("parseInfoCacheFactory",
                         "com.mysql.cj.jdbc.util.PerConnectionLRUFactory",
                         new Version (5, 1, 1)),
  /**
   * Name of a class implementing com.mysql.cj.api.CacheAdapterFactory&lt;String, Map&lt;String,
   * String&gt;&gt;, which will be used to create caches for MySQL server configuration values
   *
   * @since MySQL 5.1.1
   */
  serverConfigCacheFactory ("serverConfigCacheFactory",
                            "com.mysql.cj.core.util.PerVmServerConfigCacheFactory",
                            new Version (5, 1, 1)),
  /**
   * Should the driver always communicate with the database when
   * Connection.setTransactionIsolation() is called? If set to false, the driver will only
   * communicate with the database when the requested transaction isolation is different than the
   * whichever is newer, the last value that was set via Connection.setTransactionIsolation(), or
   * the value that was read from the server when the connection was established. Note that
   * useLocalSessionState=true will force the same behavior as alwaysSendSetIsolation=false,
   * regardless of how alwaysSendSetIsolation is set.
   *
   * @since MySQL 3.1.7
   */
  alwaysSendSetIsolation ("alwaysSendSetIsolation", "true", new Version (3, 1, 7)),
  /**
   * Should the driver maintain various internal timers to enable idle time calculations as well as
   * more verbose error messages when the connection to the server fails? Setting this property to
   * false removes at least two calls to System.getCurrentTimeMillis() per query.
   *
   * @since MySQL 3.1.9
   */
  maintainTimeStats ("maintainTimeStats", "true", new Version (3, 1, 9)),
  /**
   * If connected to MySQL &gt; 5.0.2, and setFetchSize() &gt; 0 on a statement, should that
   * statement use cursor-based fetching to retrieve rows?
   *
   * @since MySQL 5.0.0
   */
  useCursorFetch ("useCursorFetch", "false", new Version (5, 0, 0)),
  /**
   * Should the driver cache the parsing stage of CallableStatements
   *
   * @since MySQL 3.1.2
   */
  cacheCallableStmts ("cacheCallableStmts", "false", new Version (3, 1, 2)),
  /**
   * Should the driver cache the parsing stage of PreparedStatements of client-side prepared
   * statements, the "check" for suitability of server-side prepared and server-side prepared
   * statements themselves?
   *
   * @since MySQL 3.0.10
   */
  cachePrepStmts ("cachePrepStmts", "false", new Version (3, 0, 10)),
  /**
   * Should the driver cache ResultSetMetaData for Statements and PreparedStatements? (Req.
   * JDK-1.4+, true/false, default 'false')
   *
   * @since MySQL 3.1.1
   */
  cacheResultSetMetadata ("cacheResultSetMetadata", "false", new Version (3, 1, 1)),
  /**
   * Should the driver cache the results of 'SHOW VARIABLES' and 'SHOW COLLATION' on a per-URL
   * basis?
   *
   * @since MySQL 3.1.5
   */
  cacheServerConfiguration ("cacheServerConfiguration", "false", new Version (3, 1, 5)),
  /**
   * The driver will call setFetchSize(n) with this value on all newly-created Statements
   *
   * @since MySQL 3.1.9
   */
  defaultFetchSize ("defaultFetchSize", "0", new Version (3, 1, 9)),
  /**
   * Stops checking if every INSERT statement contains the "ON DUPLICATE KEY UPDATE" clause. As a
   * side effect, obtaining the statement's generated keys information will return a list where
   * normally it wouldn't. Also be aware that, in this case, the list of generated keys returned may
   * not be accurate. The effect of this property is canceled if set simultaneously with
   * 'rewriteBatchedStatements=true'.
   *
   * @since MySQL 5.1.32
   */
  dontCheckOnDuplicateKeyUpdateInSQL ("dontCheckOnDuplicateKeyUpdateInSQL", "false", new Version (5, 1, 32)),
  /**
   * If using MySQL-4.1 or newer, should the driver only issue 'set autocommit=n' queries when the
   * server's state doesn't match the requested state by Connection.setAutoCommit(boolean)?
   *
   * @since MySQL 3.1.3
   */
  elideSetAutoCommits ("elideSetAutoCommits", "false", new Version (3, 1, 3)),
  /**
   * Sets the default escape processing behavior for Statement objects. The method
   * Statement.setEscapeProcessing() can be used to specify the escape processing behavior for an
   * individual Statement object. Default escape processing behavior in prepared statements must be
   * defined with the property 'processEscapeCodesForPrepStmts'.
   *
   * @since MySQL 5.1.37
   */
  enableEscapeProcessing ("enableEscapeProcessing", "true", new Version (5, 1, 37)),
  /**
   * When enabled, query timeouts set via Statement.setQueryTimeout() use a shared java.util.Timer
   * instance for scheduling. Even if the timeout doesn't expire before the query is processed,
   * there will be memory used by the TimerTask for the given timeout which won't be reclaimed until
   * the time the timeout would have expired if it hadn't been cancelled by the driver. High-load
   * environments might want to consider disabling this functionality.
   *
   * @since MySQL 5.0.6
   */
  enableQueryTimeouts ("enableQueryTimeouts", "true", new Version (5, 0, 6)),
  /**
   * What size result set row should the JDBC driver consider "large", and thus use a more
   * memory-efficient way of representing the row internally?
   *
   * @since MySQL 5.1.1
   */
  largeRowSizeThreshold ("largeRowSizeThreshold", "2048", new Version (5, 1, 1)),
  /**
   * Should the driver issue appropriate statements to implicitly set the transaction access mode on
   * server side when Connection.setReadOnly() is called? Setting this property to 'true' enables
   * InnoDB read-only potential optimizations but also requires an extra roundtrip to set the right
   * transaction state. Even if this property is set to 'false', the driver will do its best effort
   * to prevent the execution of database-state-changing queries. Requires minimum of MySQL 5.6.
   *
   * @since MySQL 5.1.35
   */
  readOnlyPropagatesToServer ("readOnlyPropagatesToServer", "true", new Version (5, 1, 35)),
  /**
   * Should the driver use multiqueries (irregardless of the setting of "allowMultiQueries") as well
   * as rewriting of prepared statements for INSERT into multi-value inserts when executeBatch() is
   * called? Notice that this has the potential for SQL injection if using plain java.sql.Statements
   * and your code doesn't sanitize input correctly. Notice that for prepared statements,
   * server-side prepared statements can not currently take advantage of this rewrite option, and
   * that if you don't specify stream lengths when using PreparedStatement.set*Stream(), the driver
   * won't be able to determine the optimum number of parameters per batch and you might receive an
   * error from the driver that the resultant packet is too large. Statement.getGeneratedKeys() for
   * these rewritten statements only works when the entire batch includes INSERT statements. Please
   * be aware using rewriteBatchedStatements=true with INSERT .. ON DUPLICATE KEY UPDATE that for
   * rewritten statement server returns only one value as sum of all affected (or found) rows in
   * batch and it isn't possible to map it correctly to initial statements; in this case driver
   * returns 0 as a result of each batch statement if total count was 0, and the
   * Statement.SUCCESS_NO_INFO as a result of each batch statement if total count was &gt; 0.
   *
   * @since MySQL 3.1.13
   */
  rewriteBatchedStatements ("rewriteBatchedStatements", "false", new Version (3, 1, 13)),
  /**
   * Use newer result set row unpacking code that skips a copy from network buffers to a MySQL
   * packet instance and instead reads directly into the result set row data buffers.
   *
   * @since MySQL 5.1.1
   */
  useDirectRowUnpack ("useDirectRowUnpack", "true", new Version (5, 1, 1)),
  /**
   * Use newer, optimized non-blocking, buffered input stream when reading from the server?
   *
   * @since MySQL 3.1.5
   */
  useReadAheadInput ("useReadAheadInput", "true", new Version (3, 1, 5)),
  /**
   * The name of a class that implements "com.mysql.cj.api.log.Log" that will be used to log
   * messages to. (default is "com.mysql.cj.core.log.StandardLogger", which logs to STDERR)
   *
   * @since MySQL 3.1.1
   */
  logger ("logger", "com.mysql.cj.core.log.StandardLogger", new Version (3, 1, 1)),
  /**
   * Should the driver gather performance metrics, and report them via the configured logger every
   * 'reportMetricsIntervalMillis' milliseconds?
   *
   * @since MySQL 3.1.2
   */
  gatherPerfMetrics ("gatherPerfMetrics", "false", new Version (3, 1, 2)),
  /**
   * Trace queries and their execution/fetch times to the configured logger (true/false) defaults to
   * 'false'
   *
   * @since MySQL 3.1.0
   */
  profileSQL ("profileSQL", "false", new Version (3, 1, 0)),
  /**
   * If 'gatherPerfMetrics' is enabled, how often should they be logged (in ms)?
   *
   * @since MySQL 3.1.2
   */
  reportMetricsIntervalMillis ("reportMetricsIntervalMillis", "30000", new Version (3, 1, 2)),
  /**
   * Controls the maximum length/size of a query that will get logged when profiling or tracing
   *
   * @since MySQL 3.1.3
   */
  maxQuerySizeToLog ("maxQuerySizeToLog", "2048", new Version (3, 1, 3)),
  /**
   * The maximum number of packets to retain when 'enablePacketDebug' is true
   *
   * @since MySQL 3.1.3
   */
  packetDebugBufferSize ("packetDebugBufferSize", "20", new Version (3, 1, 3)),
  /**
   * If 'logSlowQueries' is enabled, how long should a query (in ms) before it is logged as 'slow'?
   *
   * @since MySQL 3.1.2
   */
  slowQueryThresholdMillis ("slowQueryThresholdMillis", "2000", new Version (3, 1, 2)),
  /**
   * If 'useNanosForElapsedTime' is set to true, and this property is set to a non-zero value, the
   * driver will use this threshold (in nanosecond units) to determine if a query was slow.
   *
   * @since MySQL 5.0.7
   */
  slowQueryThresholdNanos ("slowQueryThresholdNanos", "0", new Version (5, 0, 7)),
  /**
   * Should the driver issue 'usage' warnings advising proper and efficient usage of JDBC and MySQL
   * Connector/J to the log (true/false, defaults to 'false')?
   *
   * @since MySQL 3.1.1
   */
  useUsageAdvisor ("useUsageAdvisor", "false", new Version (3, 1, 1)),
  /**
   * Should the driver dump the SQL it is executing, including server-side prepared statements to
   * STDERR?
   *
   * @since MySQL 3.1.9
   */
  autoGenerateTestcaseScript ("autoGenerateTestcaseScript", "false", new Version (3, 1, 9)),
  /**
   * Instead of using slowQueryThreshold* to determine if a query is slow enough to be logged,
   * maintain statistics that allow the driver to determine queries that are outside the 99th
   * percentile?
   *
   * @since MySQL 5.1.4
   */
  autoSlowLog ("autoSlowLog", "true", new Version (5, 1, 4)),
  /**
   * The name of a class that implements the com.mysql.cj.api.jdbc.ClientInfoProvider interface in
   * order to support JDBC-4.0's Connection.get/setClientInfo() methods
   *
   * @since MySQL 5.1.0
   */
  clientInfoProvider ("clientInfoProvider", "com.mysql.cj.jdbc.CommentClientInfoProvider", new Version (5, 1, 0)),
  /**
   * When enabled, a ring-buffer of 'packetDebugBufferSize' packets will be kept, and dumped when
   * exceptions are thrown in key areas in the driver's code
   *
   * @since MySQL 3.1.3
   */
  enablePacketDebug ("enablePacketDebug", "false", new Version (3, 1, 3)),
  /**
   * If 'logSlowQueries' is enabled, should the driver automatically issue an 'EXPLAIN' on the
   * server and send the results to the configured log at a WARN level?
   *
   * @since MySQL 3.1.2
   */
  explainSlowQueries ("explainSlowQueries", "false", new Version (3, 1, 2)),
  /**
   * Should queries that take longer than 'slowQueryThresholdMillis' be logged?
   *
   * @since MySQL 3.1.2
   */
  logSlowQueries ("logSlowQueries", "false", new Version (3, 1, 2)),
  /**
   * Should the driver log XA commands sent by MysqlXaConnection to the server, at the DEBUG level
   * of logging?
   *
   * @since MySQL 5.0.5
   */
  logXaCommands ("logXaCommands", "false", new Version (5, 0, 5)),
  /**
   * Name of a class that implements the interface com.mysql.cj.api.ProfilerEventHandler that will
   * be used to handle profiling/tracing events.
   *
   * @since MySQL 5.1.6
   */
  profilerEventHandler ("profilerEventHandler",
                        "com.mysql.cj.core.profiler.LoggingProfilerEventHandler",
                        new Version (5, 1, 6)),
  /**
   * If the usage advisor is enabled, how many rows should a result set contain before the driver
   * warns that it is suspiciously large?
   *
   * @since MySQL 5.0.5
   */
  resultSetSizeThreshold ("resultSetSizeThreshold", "100", new Version (5, 0, 5)),
  /**
   * Should trace-level network protocol be logged?
   *
   * @since MySQL 3.1.2
   */
  traceProtocol ("traceProtocol", "false", new Version (3, 1, 2)),
  /**
   * For profiling/debugging functionality that measures elapsed time, should the driver try to use
   * nanoseconds resolution if available (JDK &ge; 1.5)?
   *
   * @since MySQL 5.0.7
   */
  useNanosForElapsedTime ("useNanosForElapsedTime", "false", new Version (5, 0, 7)),
  /**
   * Don't prepend 'standard' SQLState error messages to error messages returned by the server.
   *
   * @since MySQL 3.0.15
   */
  useOnlyServerErrorMessages ("useOnlyServerErrorMessages", "true", new Version (3, 0, 15)),
  /**
   * Should the driver dump the contents of the query sent to the server in the message for
   * SQLExceptions?
   *
   * @since MySQL 3.1.3
   */
  dumpQueriesOnException ("dumpQueriesOnException", "false", new Version (3, 1, 3)),
  /**
   * Comma-delimited list of classes that implement
   * com.mysql.cj.api.exceptions.ExceptionInterceptor. These classes will be instantiated one per
   * Connection instance, and all SQLExceptions thrown by the driver will be allowed to be
   * intercepted by these interceptors, in a chained fashion, with the first class listed as the
   * head of the chain.
   *
   * @since MySQL 5.1.8
   */
  exceptionInterceptors ("exceptionInterceptors", null, new Version (5, 1, 8)),
  /**
   * Ignore non-transactional table warning for rollback? (defaults to 'false').
   *
   * @since MySQL 3.0.9
   */
  ignoreNonTxTables ("ignoreNonTxTables", "false", new Version (3, 0, 9)),
  /**
   * Include the output of "SHOW ENGINE INNODB STATUS" in exception messages when deadlock
   * exceptions are detected?
   *
   * @since MySQL 5.0.7
   */
  includeInnodbStatusInDeadlockExceptions ("includeInnodbStatusInDeadlockExceptions", "false", new Version (5, 0, 7)),
  /**
   * Include a current Java thread dump in exception messages when deadlock exceptions are detected?
   *
   * @since MySQL 5.1.15
   */
  includeThreadDumpInDeadlockExceptions ("includeThreadDumpInDeadlockExceptions", "false", new Version (5, 1, 15)),
  /**
   * Include the name of the current thread as a comment visible in "SHOW PROCESSLIST", or in Innodb
   * deadlock dumps, useful in correlation with "includeInnodbStatusInDeadlockExceptions=true" and
   * "includeThreadDumpInDeadlockExceptions=true".
   *
   * @since MySQL 5.1.15
   */
  includeThreadNamesAsStatementComment ("includeThreadNamesAsStatementComment", "false", new Version (5, 1, 15)),
  /**
   * Should the driver return "true" for DatabaseMetaData.supportsIntegrityEnhancementFacility()
   * even if the database doesn't support it to workaround applications that require this method to
   * return "true" to signal support of foreign keys, even though the SQL specification states that
   * this facility contains much more than just foreign key support (one such application being
   * OpenOffice)?
   *
   * @since MySQL 3.1.12
   */
  overrideSupportsIntegrityEnhancementFacility ("overrideSupportsIntegrityEnhancementFacility",
                                                "false",
                                                new Version (3, 1, 12)),
  /**
   * Create PreparedStatements for prepareCall() when required, because UltraDev is broken and
   * issues a prepareCall() for _all_ statements? (true/false, defaults to 'false')
   *
   * @since MySQL 2.0.3
   */
  ultraDevHack ("ultraDevHack", "false", new Version (2, 0, 3)),
  /**
   * Prior to JDBC-4.0, the JDBC specification had a bug related to what could be given as a "column
   * name" to ResultSet methods like findColumn(), or getters that took a String property. JDBC-4.0
   * clarified "column name " to mean the label, as given in an "AS" clause and returned by
   * ResultSetMetaData.getColumnLabel(), and if no AS clause, the column name. Setting this property
   * to "true" will give behavior that is congruent to JDBC-3.0 and earlier versions of the JDBC
   * specification, but which because of the specification bug could give unexpected results. This
   * property is preferred over "useOldAliasMetadataBehavior" unless you need the specific behavior
   * that it provides with respect to ResultSetMetadata.
   *
   * @since MySQL 5.1.7
   */
  useColumnNamesInFindColumn ("useColumnNamesInFindColumn", "false", new Version (5, 1, 7)),
  /**
   * Follow the JDBC spec to the letter.
   *
   * @since MySQL 3.0.0
   */
  pedantic ("pedantic", "false", new Version (3, 0, 0)),
  /**
   * Should the driver use the legacy behavior for "AS" clauses on columns and tables, and only
   * return aliases (if any) for ResultSetMetaData.getColumnName() or
   * ResultSetMetaData.getTableName() rather than the original column/table name? In 5.0.x, the
   * default value was true.
   *
   * @since MySQL 5.0.4
   */
  useOldAliasMetadataBehavior ("useOldAliasMetadataBehavior", "false", new Version (5, 0, 4)),
  /**
   * Fabric password
   *
   * @since MySQL 5.1.30
   */
  fabricPassword ("fabricPassword", null, new Version (5, 1, 30)),
  /**
   * Fabric protocol
   *
   * @since MySQL 5.1.30
   */
  fabricProtocol ("fabricProtocol", "http", new Version (5, 1, 30)),
  /**
   * Fabric report errors
   *
   * @since MySQL 5.1.30
   */
  fabricReportErrors ("fabricReportErrors", "false", new Version (5, 1, 30)),
  /**
   * Fabric server group
   *
   * @since MySQL 5.1.30
   */
  fabricServerGroup ("fabricServerGroup", null, new Version (5, 1, 30)),
  /**
   * Fabric shard key
   *
   * @since MySQL 5.1.30
   */
  fabricShardKey ("fabricShardKey", null, new Version (5, 1, 30)),
  /**
   * Fabric shard table
   *
   * @since MySQL 5.1.30
   */
  fabricShardTable ("fabricShardTable", null, new Version (5, 1, 30)),
  /**
   * Fabric username
   *
   * @since MySQL 5.1.30
   */
  fabricUsername ("fabricUsername", null, new Version (5, 1, 30)),
  /**
   * Use asynchronous variant of X Protocol
   *
   * @since MySQL 6.0.0
   */
  mysqlx_useAsyncProtocol ("mysqlx.useAsyncProtocol", "true", new Version (6, 0, 0));

  private final String m_sName;
  private final String m_sDefaultValue;
  private final Version m_aMinVersion;

  EMySQLConnectionProperty (@NonNull @Nonempty final String sName,
                            @Nullable final String sDefaultValue,
                            @Nullable final Version aMinVersion)
  {
    m_sName = sName;
    m_sDefaultValue = sDefaultValue;
    m_aMinVersion = aMinVersion;
  }

  @NonNull
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
