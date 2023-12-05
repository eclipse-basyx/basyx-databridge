package org.eclipse.digitaltwin.basyx.databridge.httppolling.configuration.api;

/**
 * An Enum to define supported API types
 * 
 * @author danish
 *
 */
public enum ApiType {
	BASYX("BaSyx"),
	DOT_AAS_V3("DotAAS-V3");
	
    private final String name;

    ApiType(String name) {
        this.name = name;
    }

    /**
     * Convenient method to get the name of the Api type
     * 
     * @return the name of the ApiType
     */
    public String getName() {
        return name;
    }

}
