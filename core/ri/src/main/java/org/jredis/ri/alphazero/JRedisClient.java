/*
 *   Copyright 2009 Joubin Houshyar
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *    
 *   http://www.apache.org/licenses/LICENSE-2.0
 *    
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.jredis.ri.alphazero;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.jredis.ClientRuntimeException;
import org.jredis.Command;
import org.jredis.JRedis;
import org.jredis.ProviderException;
import org.jredis.Redis;
import org.jredis.RedisException;
import org.jredis.connector.Connection;
import org.jredis.connector.ConnectionSpec;
import org.jredis.connector.Protocol;
import org.jredis.connector.Response;
import org.jredis.ri.alphazero.connection.SynchConnection;
import org.jredis.ri.alphazero.support.Assert;
import org.jredis.ri.alphazero.support.Convert;


/**
 * [TODO: check documentation and make necessary changes made during refactoring]
 * 
 * A basic client, using {@link SocketConnection} and handler delegate 
 * <p>
 * This class is simply an assembly of various other components that address distinct
 * concerns of a JRedis connection, and effectively defines the connection policy
 * and use-case patterns by selecting this set of cooperating elements.
 * <p>
 * This is a <i>simple client</i> object suitable for long-held open connections
 * to a redis server by single threaded applications.  
 * <p>
 * <b>The components that are used ARE NOT thread-safe</b> and assume 
 * synchronised/sequential access to the api defined by {@link JRedis}.
 * Both the connection and protocol handler delegates of this class are intended for use
 * by a <b>single</b> thread, or strictly sequential access by a pool of threads.  You can
 * create multiple instances of this class and use dedicated threads for each to create a
 * service (if you wish).
 * <p>
 * Redis protocol is handled by a {@link Protocol} instance obtained from the {@link ProtocolManager}.
 * This class will by default specify the {@link RedisVersion#current_revision}.  
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * 
 */
/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Aug 13, 2009
 * @since   alpha.0
 * 
 */
/**
 * [TODO: document me!]
 *
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Aug 13, 2009
 * @since   alpha.0
 * 
 */
@Redis(versions={"0.09"})
public class JRedisClient extends SynchJRedisBase  {
	
	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	private Connection	connection;

	// ------------------------------------------------------------------------
	// Construct and initialize
	// ------------------------------------------------------------------------

	public JRedisClient (ConnectionSpec connectionSpec){
		Connection synchConnection = createSynchConnection (connectionSpec, RedisVersion.current_revision);
		setConnection (synchConnection);
		
		try {
			if(null != connectionSpec.getCredentials())
				this.serviceRequest(Command.AUTH, connectionSpec.getCredentials());
			
			this.serviceRequest(Command.SELECT, Convert.toBytes(connectionSpec.getDatabase()));
		} 
		catch (RedisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	/**
	 * Connects to the localhost:6379 redis server using the password.
	 * Will select db 0.
     * @param password used for AUTH
     */
    public JRedisClient (String password) {
		this ("localhost", 6379, password, 0);
    }

	/**
	 * New RedisClient for the default protocol version {@link RedisVersion} 
	 * obtained from the {@link ProtocolManager}
	 * and using localhost:6379 as its network addressing parameters. 
	 * Database will be selected to db 0
	 * Assumes no password required.
	 */
	public JRedisClient ( ){
		this ("localhost", 6379, null, 0, RedisVersion.current_revision);
	}
	/**
	 * New RedisClient for the default protocol version {@link RedisVersion} 
	 * obtained from the {@link ProtocolManager}
	 * 
	 * @param host
	 * @param port
	 */
	public JRedisClient(String host, int port) {
		this(host, port, null, 0, RedisVersion.current_revision);
	}
	
	
	/**
	 * New JRedisClient for the default protocol version {@link RedisVersion}
	 * @param host redis server's host
	 * @param port redis server's port
	 * @param password to use for AUTHentication (can be null)
	 * @param database database to select on connect
	 * @throws ClientRuntimeException
	 */
	public JRedisClient (String host, int port, String password, int database) 
	throws ClientRuntimeException
	{
//		this(host, port, (null != password ? password.getBytes() : null), database, RedisVersion.current_revision);
		this(host, port, getCredentialBytes(password), database, RedisVersion.current_revision);
	}

	/**
	 * Creates a new instance of RedisClient, using the information provided.  
	 * <p>
	 * This constructor will delegate all {@link Protocol} issues to a
	 * {@link SocketConnection} instance, for which it will obtain a {@link Protocol}
	 * handler delegate from {@link ProtocolManager} for the user specified redis version 
	 * <p>
	 * All specifics regarding the implementation of the {@link JRedis} contract are handled by
	 * the superclass  and you should consult that class's documentation for the detailss.
	 *   
	 * @param host
	 * @param port
	 * @param redisVersion
	 * @throws ClientRuntimeException
	 */
	protected JRedisClient (String host, int port, byte[] credentials, int database, RedisVersion redisVersion) 
	throws ClientRuntimeException
	{
		Assert.notNull(host, "host parameter", IllegalArgumentException.class);
		Assert.notNull(redisVersion, "redisVersion paramter", IllegalArgumentException.class);
		
		Connection synchConnection = createSynchConnection (host, port, database, credentials, redisVersion);
		setConnection (synchConnection);
		
		try {
			if(null != credentials)
				this.serviceRequest(Command.AUTH, credentials);
			
			this.serviceRequest(Command.SELECT, Convert.toBytes(database));
		} 
		catch (RedisException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	// ------------------------------------------------------------------------
	// Super overrides
	// ------------------------------------------------------------------------
	@Override
	protected Response serviceRequest(Command cmd, byte[]... args)
			throws RedisException, ClientRuntimeException, ProviderException 
	{
		Response response = connection.serviceRequest(cmd, args);
		// temp bench
//		reqCnt ++;
//		if(reqCnt == benchCnt) {
//			if(start == -1) {
//				start = System.currentTimeMillis();
//			}
//				else {
//				long delta = System.currentTimeMillis() - start;
//				float rate = (benchCnt * 1000)/delta;
//				Log.log("JRedisService: served %d at %9.2f /sec in %d msecs\n", benchCnt, rate, delta);
//				start = System.currentTimeMillis();
//			}
//			reqCnt = 0;
//		}
		return response;
	}
//	long reqCnt = 0;
//	long start = -1;
//	int benchCnt = 1000*1;
	
	
	@Override
	protected final void setConnection (Connection connection)  {
		this.connection = Assert.notNull(connection, "connection on setConnection()", ClientRuntimeException.class);
	}
	
	// ------------------------------------------------------------------------
	// Interface
	// =========================================================== Resource<T>
	/*
	 * Provides basic Resource support without any state management.  Extensions
	 * that use context in a simply manner can rely on these methods.  Others may
	 * wish to override.
	 */
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.jredis.resource.Resource#getInterface()
	 */
	@Override
	public JRedis getInterface() {
		return this;
	}
	// ------------------------------------------------------------------------
	// Static Utils
	// ------------------------------------------------------------------------
	/**
	 * Internal use only - 
	 * @param password
	 * @return the bytes of password or null if none spec'd
	 */
	private static byte[] getCredentialBytes (String password){
		return (null != password ? password.getBytes() : null);
	}
	
	/**
	 * @return the {@link ConnectionSpec} used by default by this {@link JRedisClient}
	 */
	ConnectionSpec getDefaultConnectionSpec () {
        ConnectionSpec defaultConnectionSpec = null;
		try {
			defaultConnectionSpec = getDefaultConnectionSpec("localhost", 6379, null, 0);
        }
        catch (UnknownHostException e) {
	        e.printStackTrace(); // since when is localhost unknown?  never ..
        }
        return defaultConnectionSpec;
	}
    /**
     * @param host redis server host name 
     * @param port redis server port
     * @param password for the Redis server per redis.conf
     * @param database the selected databse
	 * @return the {@link ConnectionSpec} used by default by this {@link JRedisClient} for the given params.
     * @throws UnknownHostException
     */
    private ConnectionSpec getDefaultConnectionSpec (String host, int port, String password, int database) throws UnknownHostException {
    	InetAddress address = InetAddress.getByName(host);
	    return SynchConnection.getDefaultConnectionSpec(address, port, database, getCredentialBytes(password));
    }
}
