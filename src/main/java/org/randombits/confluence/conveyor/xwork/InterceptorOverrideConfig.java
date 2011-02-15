package org.randombits.confluence.conveyor.xwork;

import java.util.Map;

import com.opensymphony.xwork.config.entities.InterceptorConfig;

public class InterceptorOverrideConfig extends InterceptorConfig {

    private InterceptorConfig overriddenInterceptor;

    public InterceptorOverrideConfig() {
        super();
    }

    public InterceptorOverrideConfig( InterceptorConfig overriddenInterceptor, boolean copySettings, String name,
            Class clazz, Map params ) {
        super( name, clazz, params );
        setOverriddenInterceptor( overriddenInterceptor, copySettings );
    }

    public InterceptorOverrideConfig( InterceptorConfig overriddenInterceptor, boolean copySettings, String name,
            String className, Map params ) {
        super( name, className, params );
        setOverriddenInterceptor( overriddenInterceptor, copySettings );
    }

    public InterceptorConfig getOverriddenInterceptor() {
        return overriddenInterceptor;
    }

    private void setOverriddenInterceptor( InterceptorConfig overriddenInterceptor, boolean copySettings ) {
        this.overriddenInterceptor = overriddenInterceptor;
        if ( copySettings ) {
            if ( this.getClassName() == null ) {
                setClassName( overriddenInterceptor.getClassName() );
            }

            // Copy the new params over the old params.
            Map<String, String> oldParams = ConveyorConfigurationProvider.copyParams( overriddenInterceptor
                    .getParams() );
            if ( oldParams != null ) {
                Map<String, String> params = getParams();
                if ( params != null )
                    oldParams.putAll( params );
                setParams( oldParams );
            }
        }
    }

}
