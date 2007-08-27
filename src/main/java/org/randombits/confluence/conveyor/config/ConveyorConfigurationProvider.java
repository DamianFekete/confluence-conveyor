package org.randombits.confluence.conveyor.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.opensymphony.util.ClassLoaderUtil;
import com.opensymphony.util.FileManager;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ObjectFactory;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.ConfigurationException;
import com.opensymphony.xwork.config.ConfigurationUtil;
import com.opensymphony.xwork.config.ExternalReferenceResolver;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.ExternalReference;
import com.opensymphony.xwork.config.entities.PackageConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.opensymphony.xwork.config.providers.XmlHelper;

public class ConveyorConfigurationProvider extends XmlConfigurationProvider {

    private static final Logger LOG = Logger.getLogger( ConveyorConfigurationProvider.class );

    private String resourceName = "conveyor-config.xml";

    private Configuration configuration;

    private Set includedFileNames = new java.util.TreeSet();

    public ConveyorConfigurationProvider( String resourceName ) {
        super( resourceName );
        this.resourceName = resourceName;
    }

    public ConveyorConfigurationProvider() {
    }

    private void checkElementName( Element element, String name ) {
        if ( !name.equals( element.getNodeName() ) )
            throw new ConfigurationException( "Expected element named '" + name + "' but got '"
                    + element.getNodeName() + "'." );
    }

    public static Map copyParams( final Map params ) {
        if ( params != null ) {
            final Map copy = new java.util.HashMap();
            copy.putAll( params );
            return copy;
        } else {
            return null;
        }
    }

    /**
     * Copies the specified result config.
     * 
     * @param config
     *            The config to copy.
     * @return the copy.
     */
    public static ResultConfig copyResultConfig( final ResultConfig config ) {
        return new ResultConfig( config.getName(), config.getClassName(), copyParams( config.getParams() ) );
    }

    public static Map copyResults( final Map results ) {
        if ( results != null ) {
            final Map copy = new java.util.HashMap();

            final Iterator i = results.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry e = ( Entry ) i.next();
                copy.put( e.getKey(), copyResultConfig( ( ResultConfig ) e.getValue() ) );
            }

            return copy;
        } else {
            return null;
        }
    }

    public static List copyInterceptors( final List interceptors ) {
        if ( interceptors != null ) {
            // Note: Copies the list, but not the actual interceptors.
            return new java.util.ArrayList( interceptors );
        }
        return null;
    }

    public static Object copyExternalRef( final ExternalReference reference ) {
        return new ExternalReference( reference.getName(), reference.getExternalRef(), reference.isRequired() );
    }

    public static List copyExternalRefs( final List externalRefs ) {
        if ( externalRefs != null ) {
            final List copy = new java.util.ArrayList( externalRefs.size() );
            final Iterator i = externalRefs.iterator();
            while ( i.hasNext() ) {
                copy.add( copyExternalRef( ( ExternalReference ) i.next() ) );
            }
        }
        return null;
    }

    // // ConfigurationProvider methods ////

    public void destroy() {
    }

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( !( o instanceof ConveyorConfigurationProvider ) ) {
            return false;
        }

        final ConveyorConfigurationProvider configProvider = ( ConveyorConfigurationProvider ) o;

        if ( ( resourceName != null ) ? ( !resourceName.equals( configProvider.resourceName ) )
                : ( configProvider.resourceName != null ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return ( ( resourceName != null ) ? resourceName.hashCode() : 0 );
    }

    public void init( Configuration configuration ) {
        this.configuration = configuration;
        DocumentBuilder db;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating( false );
            dbf.setNamespaceAware( true );

            db = dbf.newDocumentBuilder();
            db.setEntityResolver( new EntityResolver() {
                public InputSource resolveEntity( String publicId, String systemId ) throws SAXException,
                        IOException {
                    if ( "-//randombits.org//Confluence Conveyor 0.2//EN".equals( publicId ) ) {
                        return new InputSource( ClassLoaderUtil.getResourceAsStream(
                                "confluence-conveyor-0.2.dtd", ConveyorConfigurationProvider.class ) );
                    }

                    return null;
                }
            } );
            db.setErrorHandler( new ErrorHandler() {
                public void warning( SAXParseException exception ) throws SAXException {
                }

                public void error( SAXParseException exception ) throws SAXException {
                    LOG.error( exception.getMessage() + " at (" + exception.getLineNumber() + ":"
                            + exception.getColumnNumber() + ")" );
                    throw exception;
                }

                public void fatalError( SAXParseException exception ) throws SAXException {
                    LOG.fatal( exception.getMessage() + " at (" + exception.getLineNumber() + ":"
                            + exception.getColumnNumber() + ")" );
                    throw exception;
                }
            } );
            loadConfigurationFile( resourceName, db );
        } catch ( Exception e ) {
            LOG.fatal( "Could not load XWork configuration file, failing", e );
            throw new ConfigurationException( "Error loading configuration file " + resourceName, e );
        }
    }

    private void loadConfigurationFile( String fileName, DocumentBuilder db ) {
        if ( !includedFileNames.contains( fileName ) ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Loading xwork configuration from: " + fileName );
            }

            includedFileNames.add( fileName );

            Document doc = null;
            InputStream is = null;

            try {
                is = getInputStream( fileName );

                if ( is == null ) {
                    throw new Exception( "Could not open file " + fileName );
                }

                doc = db.parse( is );
            } catch ( Exception e ) {
                final String s = "Caught exception while loading file " + fileName;
                LOG.error( s, e );
                throw new ConfigurationException( s, e );
            } finally {
                if ( is != null ) {
                    try {
                        is.close();
                    } catch ( IOException e ) {
                        LOG.error( "Unable to close input stream", e );
                    }
                }
            }

            Element rootElement = doc.getDocumentElement();
            checkElementName( rootElement, "conveyor-config" );

            NodeList children = rootElement.getChildNodes();
            int childSize = children.getLength();

            for ( int i = 0; i < childSize; i++ ) {
                Node childNode = children.item( i );

                if ( childNode instanceof Element ) {
                    Element child = ( Element ) childNode;

                    final String nodeName = child.getNodeName();

                    if ( nodeName.equals( "package-override" ) ) {
                        addPackageOverride( child );
                    }
                    if ( nodeName.equals( "package" ) ) {
                        addPackage( child );
                    } else if ( nodeName.equals( "include" ) ) {
                        String includeFileName = child.getAttribute( "file" );
                        loadConfigurationFile( includeFileName, db );
                    }
                }
            }

            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Loaded xwork configuration from: " + fileName );
            }
        }
    }

    protected InputStream getInputStream( String fileName ) {
        return FileManager.loadFile( fileName, this.getClass() );
    }

    /**
     * Tells whether the ConfigurationProvider should reload its configuration.
     * This method should only be called if
     * ConfigurationManager.isReloadingConfigs() is true.
     * 
     * @return true if the file has been changed since the last time we read it
     */
    public boolean needsReload() {
        return true;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName( String resourceName ) {
        this.resourceName = resourceName;
    }

    /**
     * Create a PackageConfig from an XML element representing it.
     */
    protected void addPackageOverride( Element packageOverrideElement ) {
        PackageConfig overridePackage = findPackageConfig( packageOverrideElement );

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Overridden: " + overridePackage );
        }

        // add result types (and default result) to this package
        // Note: Only classes which are available from the same classloader as
        // the XWork library will be available.
        addResultTypes( overridePackage, packageOverrideElement );

        // load the interceptors and interceptor stacks for this package
        // Note: Only classes which are available from the same classloader as
        // the XWork library will be available.
        loadInterceptors( overridePackage, packageOverrideElement );

        // load the default interceptor reference for this package
        loadDefaultInterceptorRef( overridePackage, packageOverrideElement );

        // load the global result list for this package
        loadGlobalResults( overridePackage, packageOverrideElement );

        // get action overrides
        NodeList actionOverrideList = packageOverrideElement.getElementsByTagName( "action-override" );

        for ( int i = 0; i < actionOverrideList.getLength(); i++ ) {
            Element actionOverrideElement = ( Element ) actionOverrideList.item( i );
            overrideAction( actionOverrideElement, overridePackage );
        }

        // get actions
        NodeList actionList = packageOverrideElement.getElementsByTagName( "action" );

        for ( int i = 0; i < actionList.getLength(); i++ ) {
            Element actionElement = ( Element ) actionList.item( i );
            // Check the action doesn't already exist.
            String name = actionElement.getAttribute( "name" );

            if ( overridePackage.getActionConfigs().get( name ) != null )
                LOG.error( "An action with the specified name already exists in the '" + overridePackage.getName()
                        + "' package: " + name );
            else
                addAction( actionElement, overridePackage );
        }

        configuration.addPackageConfig( overridePackage.getName(), overridePackage );
    }

    protected void overrideAction( Element actionOverrideElement, PackageConfig packageContext ) {
        String name = actionOverrideElement.getAttribute( "name" );
        String className = actionOverrideElement.getAttribute( "class" );
        String methodName = actionOverrideElement.getAttribute( "method" );
        String inheritAttr = actionOverrideElement.getAttribute( "inherit" );
        boolean inherit = "true".equals( inheritAttr );

        // methodName should be null if it's not set
        methodName = ( methodName.trim().length() > 0 ) ? methodName.trim() : null;

        if ( !TextUtils.stringSet( className ) ) {
            throw new ConfigurationException( "No class specified for action override: " + name );
        }

        ActionConfig oldAction = ( ActionConfig ) packageContext.getActionConfigs().get( name );
        if ( oldAction == null ) {
            throw new ConfigurationException( "No existing action was found to override: " + name );
        }

        try {
            ObjectFactory.getObjectFactory().getClassInstance( className );
        } catch ( Exception e ) {
            LOG.error( "Action class [" + className + "] not found, skipping action [" + name + "]", e );
            return;
        }

        Map actionParams = XmlHelper.getParams( actionOverrideElement );

        Map results;

        try {
            results = buildResults( actionOverrideElement, packageContext );
        } catch ( ConfigurationException e ) {
            throw new ConfigurationException( "Error building results for action " + name + " in namespace "
                    + packageContext.getNamespace(), e );
        }

        List interceptorList = buildInterceptorList( actionOverrideElement, packageContext );

        List externalrefs = buildExternalRefs( actionOverrideElement, packageContext );

        ActionOverrideConfig actionConfig = new ActionOverrideConfig( oldAction, inherit, methodName, className,
                actionParams, results, interceptorList, externalrefs, packageContext.getName() );
        packageContext.addActionConfig( name, actionConfig );

        if ( LOG.isDebugEnabled() ) {
            LOG
                    .debug( "Loaded "
                            + ( TextUtils.stringSet( packageContext.getNamespace() ) ? ( packageContext
                                    .getNamespace() + "/" ) : "" ) + name + " in '" + packageContext.getName()
                            + "' package:" + actionConfig );
        }
    }

    /**
     * This method finds the package config specified by the package override
     * element. If the package does not match the name and namespace a
     * ConfigurationException will be thrown.
     * 
     * If no parents are found, it will return a root package.
     */
    protected PackageConfig findPackageConfig( Element packageOverrideElement ) {
        String name = TextUtils.noNull( packageOverrideElement.getAttribute( "name" ) );
        String namespace = TextUtils.noNull( packageOverrideElement.getAttribute( "namespace" ) );

        PackageConfig config = configuration.getPackageConfig( name );
        if ( config == null )
            throw new ConfigurationException( "Unable to locate package to override: " + name );
        if ( !StringUtils.equals( namespace, config.getNamespace() ) )
            throw new ConfigurationException( "The '" + name + "' package is is not specified to be in the '"
                    + namespace + "' namepace." );

        return config;
    }

    /**
     * Create a PackageConfig from an XML element representing it.
     * 
     * Note: Copied verbatim from the XmlConfigurationProvider class so that the
     * configuration object will be populated.
     */
    protected void addPackage( Element packageElement ) {
        PackageConfig newPackage = buildPackageContext( packageElement );

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Loaded " + newPackage );
        }

        // add result types (and default result) to this package
        addResultTypes( newPackage, packageElement );

        // load the interceptors and interceptor stacks for this package
        loadInterceptors( newPackage, packageElement );

        // load the default interceptor reference for this package
        loadDefaultInterceptorRef( newPackage, packageElement );

        // load the global result list for this package
        loadGlobalResults( newPackage, packageElement );

        // get actions
        NodeList actionList = packageElement.getElementsByTagName( "action" );

        for ( int i = 0; i < actionList.getLength(); i++ ) {
            Element actionElement = ( Element ) actionList.item( i );
            addAction( actionElement, newPackage );
        }

        configuration.addPackageConfig( newPackage.getName(), newPackage );
    }

    /**
     * This method builds a package context by looking for the parents of this
     * new package.
     * 
     * If no parents are found, it will return a root package.
     * 
     * Note: Copied verbatim from the XmlConfigurationProvider class so that the
     * configuration object will be populated.
     */
    protected PackageConfig buildPackageContext( Element packageElement ) {
        String parent = packageElement.getAttribute( "extends" );
        String abstractVal = packageElement.getAttribute( "abstract" );
        boolean isAbstract = Boolean.valueOf( abstractVal ).booleanValue();
        String name = TextUtils.noNull( packageElement.getAttribute( "name" ) );
        String namespace = TextUtils.noNull( packageElement.getAttribute( "namespace" ) );

        // RM* Load the ExternalReferenceResolver if one has been set
        ExternalReferenceResolver erResolver = null;

        String externalReferenceResolver = TextUtils.noNull( packageElement
                .getAttribute( "externalReferenceResolver" ) );

        if ( !( "".equals( externalReferenceResolver ) ) ) {
            try {
                Class erResolverClazz = ClassLoaderUtil.loadClass( externalReferenceResolver,
                        ExternalReferenceResolver.class );

                erResolver = ( ExternalReferenceResolver ) erResolverClazz.newInstance();
            } catch ( ClassNotFoundException e ) {
                // TODO this should be localized
                String msg = "Could not find External Reference Resolver: " + externalReferenceResolver + ". "
                        + e.getMessage();
                LOG.error( msg );
                throw new ConfigurationException( msg, e );
            } catch ( Exception e ) {
                // TODO this should be localized
                String msg = "Could not create External Reference Resolver: " + externalReferenceResolver + ". "
                        + e.getMessage();
                LOG.error( msg );
                throw new ConfigurationException( msg, e );
            }
        }

        if ( !TextUtils.stringSet( TextUtils.noNull( parent ) ) ) { // no
            // parents

            return new PackageConfig( name, namespace, isAbstract, erResolver );
        } else { // has parents, let's look it up

            List parents = ConfigurationUtil.buildParentsFromString( configuration, parent );

            if ( parents.size() <= 0 ) {
                LOG.error( "Unable to find parent packages " + parent );

                return new PackageConfig( name, namespace, isAbstract, erResolver );
            } else {
                return new PackageConfig( name, namespace, isAbstract, erResolver, parents );
            }
        }
    }

}
