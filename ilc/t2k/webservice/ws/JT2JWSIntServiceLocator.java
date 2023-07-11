/**
 * JT2JWSIntServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package ilc.t2k.webservice.ws;

public class JT2JWSIntServiceLocator extends org.apache.axis.client.Service implements ilc.t2k.webservice.ws.JT2JWSIntService {

	public JT2JWSIntServiceLocator() {
	}


	public JT2JWSIntServiceLocator(org.apache.axis.EngineConfiguration config) {
		super(config);
	}

	public JT2JWSIntServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
		super(wsdlLoc, sName);
	}

	// Use to get a proxy class for jt2k
//	private java.lang.String jt2k_address = "http://localhost:8080/axis/services/jt2k";
	private java.lang.String jt2k_address = "http://vikef:8080/axis/services/jt2k";
//	private java.lang.String jt2k_address = "http://195.96.216.156:8080/axis/services/jt2k";

	public java.lang.String getjt2kAddress() {
		return jt2k_address;
	}

	// The WSDD service name defaults to the port name.
	private java.lang.String jt2kWSDDServiceName = "jt2k";

	public java.lang.String getjt2kWSDDServiceName() {
		return jt2kWSDDServiceName;
	}

	public void setjt2kWSDDServiceName(java.lang.String name) {
		jt2kWSDDServiceName = name;
	}

	public ilc.t2k.webservice.ws.JT2JWSInt getjt2k() throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(jt2k_address);
		}
		catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getjt2k(endpoint);
	}

	public ilc.t2k.webservice.ws.JT2JWSInt getjt2k(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
		try {
			ilc.t2k.webservice.ws.Jt2KSoapBindingStub _stub = new ilc.t2k.webservice.ws.Jt2KSoapBindingStub(portAddress, this);
			_stub.setPortName(getjt2kWSDDServiceName());
			return _stub;
		}
		catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	public void setjt2kEndpointAddress(java.lang.String address) {
		jt2k_address = address;
	}

	/**
	 * For the given interface, get the stub implementation.
	 * If this service has no port for the given interface,
	 * then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
		try {
			if (ilc.t2k.webservice.ws.JT2JWSInt.class.isAssignableFrom(serviceEndpointInterface)) {
				ilc.t2k.webservice.ws.Jt2KSoapBindingStub _stub = new ilc.t2k.webservice.ws.Jt2KSoapBindingStub(new java.net.URL(jt2k_address), this);
				_stub.setPortName(getjt2kWSDDServiceName());
				return _stub;
			}
		}
		catch (java.lang.Throwable t) {
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation.
	 * If this service has no port for the given interface,
	 * then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
		if (portName == null) {
			return getPort(serviceEndpointInterface);
		}
		java.lang.String inputPortName = portName.getLocalPart();
		if ("jt2k".equals(inputPortName)) {
			return getjt2k();
		}
		else  {
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public javax.xml.namespace.QName getServiceName() {
		return new javax.xml.namespace.QName("urn:JT2JWSInt", "JT2JWSIntService");
	}

	private java.util.HashSet ports = null;

	public java.util.Iterator getPorts() {
		if (ports == null) {
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("urn:JT2JWSInt", "jt2k"));
		}
		return ports.iterator();
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

		if ("jt2k".equals(portName)) {
			setjt2kEndpointAddress(address);
		}
		else 
		{ // Unknown Port Name
			throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		}
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
		setEndpointAddress(portName.getLocalPart(), address);
	}

}
