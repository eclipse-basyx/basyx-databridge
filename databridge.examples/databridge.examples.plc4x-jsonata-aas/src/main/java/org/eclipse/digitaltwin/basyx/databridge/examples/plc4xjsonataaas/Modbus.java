/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package org.eclipse.digitaltwin.basyx.databridge.examples.plc4xjsonataaas;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.ProcessImage;

/**
 * A basic Modbus TCP listener
 * 
 * @author danish
 *
 */
public class Modbus {

	private int threadPoolSize;
	private String host;
	private int port;
	private ModbusTCPListener modbusTCPListener;

	public Modbus(int threadPoolSize, String host, int port) {
		super();
		this.threadPoolSize = threadPoolSize;
		this.host = host;
		this.port = port;
	}

	/**
	 * Configure the Singleton pattern {@link ModbusCoupler}, to couple the slave
	 * side with a master side or with a device.
	 * 
	 * @param image the ProcessImage
	 */
	public void configureDefaultModbusCoupler(ProcessImage image) {
		ModbusCoupler.getReference().setProcessImage(image);
		ModbusCoupler.getReference().setMaster(false);
		ModbusCoupler.getReference().setUnitID(15);
	}

	/**
	 * Starts the modbus listener
	 * 
	 * @throws UnknownHostException
	 */
	public void start() throws UnknownHostException {
		configureModbus();
		
		modbusTCPListener.start();
	}
	
	/**
	 * Stops the modbus listener
	 * 
	 */
	public void stop() {
		if (modbusTCPListener == null || !modbusTCPListener.isListening())
			return;

		modbusTCPListener.stop();
	}

	private void configureModbus() throws UnknownHostException {
		modbusTCPListener = new ModbusTCPListener(threadPoolSize);
		modbusTCPListener.setAddress(InetAddress.getByName(this.host));
		modbusTCPListener.setPort(this.port);
	}

}
