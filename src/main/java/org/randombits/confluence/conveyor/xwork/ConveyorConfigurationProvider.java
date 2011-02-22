package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.opensymphony.util.ClassLoaderUtil;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionSupport;
import com.opensymphony.xwork.ObjectFactory;
import com.opensymphony.xwork.config.*;
import com.opensymphony.xwork.config.entities.*;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;
import com.opensymphony.xwork.config.providers.XmlHelper;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.apache.commons.lang.StringUtils;
import org.jfree.base.modules.PackageManager.PackageConfiguration;
import org.randombits.confluence.conveyor.ConveyorException;
import org.randombits.confluence.conveyor.OverrideManager;
import org.randombits.confluence.conveyor.Receipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ConveyorConfigurationProvider extends XmlConfigurationProvider {

    private static final Logger LOG = LoggerFactory.getLogger( ConveyorConfigurationProvider.class );

    private static final String DEFAULT_RESOURCE_NAME = "conveyor-context.xml";

    private final OverrideManager overrideManager;

    private final Plugin plugin;

    private String resourceName;

    private Configuration configuration;

    private Set<String> includedFileNames = new java.util.TreeSet<String>();

    private Exception failureException;

    private final ConditionElementParser conditionElementParser;

    private List<Receipt> receipts;

    public ConveyorConfigurationProvider( OverrideManager overrideManager, Plugin plugin, String resourceName ) {
        super( resourceName );
        this.overrideManager = overrideManager;
        this.resourceName = resourceName;
        this.plugin = plugin;

        conditionElementParser = new ConditionElementParser( plugin );
        receipts = new ArrayList<Receipt>();
    }

    public ConveyorConfigurationProvider( OverrideManager overrideManager, Plugin plugin ) {
        this( overrideManager, plugin, DEFAULT_RESOURCE_NAME );
    }

    public Plugin getPlugin() {
        return plugin;
    }

    // DAN COPIED
    @Override
    protected void addResultTypes( PackageConfig packageContext, Element element ) {
        NodeList resultTypeList = element.getElementsByTagName( "result-type" );

        for ( int i = 0; i < resultTypeList.getLength(); i++ ) {
            Element resultTypeElement = (Element) resultTypeList.item( i );
            String name = resultTypeElement.getAttribute( "name" );
            String className = resultTypeElement.getAttribute( "class" );
            String def = resultTypeElement.getAttribute( "default" );

            try {
                Class<?> clazz = ClassLoaderUtil.loadClass( className, getClass() );
                ResultTypeConfig resultType = new ResultTypeConfig( name, clazz );
                packageContext.addResultTypeConfig( resultType );

                // set the default result type
                if ( "true".equals( def ) ) {
                    packageContext.setDefaultResultType( name );
                }
            } catch ( ClassNotFoundException e ) {
                LOG.error( "Result class [" + className + "] doesn't exist, ignoring" );
            }
        }
    }

    private void checkElementName( Element element, String name ) throws ConveyorException {
        if ( !name.equals( element.getNodeName() ) )
            throw new ConveyorException( "Expected element named '" + name + "' but got '" + element.getNodeName()
                    + "'." );
    }

    public static Map<String, String> copyParams( final Map<String, String> params ) {
        if ( params != null ) {
            final Map<String, String> copy = new java.util.HashMap<String, String>();
            copy.putAll( params );
            return copy;
        } else {
            return null;
        }
    }

    /**
     * Copies the specified result config.
     *
     * @param config The config to copy.
     * @return the copy.
     */
    public static ResultConfig copyResultConfig( final ResultConfig config ) {
        return new ResultConfig( config.getName(), config.getClassName(), copyParams( config.getParams() ) );
    }

    public static Map<String, ResultConfig> copyResults( final Map<String, ResultConfig> results ) {
        if ( results != null ) {
            Map<String, ResultConfig> copy = new java.util.HashMap<String, ResultConfig>();

            for ( Map.Entry<String, ResultConfig> e : results.entrySet() ) {
                copy.put( e.getKey(), copyResultConfig( e.getValue() ) );
            }

            return copy;
        } else {
            return null;
        }
    }

    public static List<InterceptorConfig> copyInterceptors( final List<InterceptorConfig> interceptors ) {
        if ( interceptors != null ) {
            // Note: Copies the list, but not the actual interceptors.
            return new java.util.ArrayList<InterceptorConfig>( interceptors );
        }
        return null;
    }

    public static ExternalReference copyExternalRef( final ExternalReference reference ) {
        return new ExternalReference( reference.getName(), reference.getExternalRef(), reference.isRequired() );
    }

    public static List<ExternalReference> copyExternalRefs( final List<ExternalReference> externalRefs ) {
        if ( externalRefs != null ) {
            final List<ExternalReference> copy = new java.util.ArrayList<ExternalReference>( externalRefs.size() );
            final Iterator<ExternalReference> i = externalRefs.iterator();
            while ( i.hasNext() ) {
                copy.add( copyExternalRef( i.next() ) );
            }
            return copy;
        }
        return null;
    }

    // // ConfigurationProvider methods ////

    @Override
    public void destroy() {
        overrideManager.returnReceipts( receipts );
        receipts.clear();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }

        if ( !( o instanceof ConveyorConfigurationProvider ) ) {
            return false;
        }

        final ConveyorConfigurationProvider configProvider = (ConveyorConfigurationProvider) o;

        if ( ( resourceName != null ) ? ( !resourceName.equals( configProvider.resourceName ) )
                : ( configProvider.resourceName != null ) ) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return ( ( resourceName != null ) ? resourceName.hashCode() : 0 );
    }

    @Override
    public void init( Configuration configuration ) {
        this.configuration = configuration;

        // Destroy any lingering references. Plugin XWork actions don't always
        // clean up after themselves.
        destroy();

        DocumentBuilder db;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating( false );
            dbf.setNamespaceAware( true );

            db = dbf.newDocumentBuilder();

            // TODO: Define an actual DTD
            /*
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
            */

            db.setErrorHandler( new ErrorHandler() {
                public void warning( SAXParseException exception ) throws SAXException {
                }

                public void error( SAXParseException exception ) throws SAXException {
                    LOG.error( exception.getMessage() + " at (" + exception.getLineNumber() + ":"
                            + exception.getColumnNumber() + ")" );
                    throw exception;
                }

                public void fatalError( SAXParseException exception ) throws SAXException {
                    LOG.error( exception.getMessage() + " at (" + exception.getLineNumber() + ":"
                            + exception.getColumnNumber() + ")" );
                    throw exception;
                }
            } );
            // Clear any old inclusions.
            includedFileNames.clear();
            // Load the file.
            loadConfigurationFile( resourceName, db );
        } catch ( RuntimeException e ) {
            fail( e );
        } catch ( ConveyorException e ) {
            fail( e );
        } catch ( ParserConfigurationException e ) {
            fail( e );
        }
    }

    private void fail( Exception e ) {
        LOG.error( e.getMessage(), e );
        this.failureException = e;
    }

    public Exception getFailureException() {
        return failureException;
    }

    public boolean isFailed() {
        return failureException != null;
    }

    private void loadConfigurationFile( String fileName, DocumentBuilder db ) throws ConveyorException {
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
                    throw new ConveyorException( "Could not open file " + fileName );
                }

                doc = db.parse( is );
            } catch ( Exception e ) {
                final String s = "Caught exception while loading file " + fileName;
                throw new ConveyorException( s, e );
            } finally {
                if ( is != null ) {
                    try {
                        is.close();
                    } catch ( IOException e ) {
                        throw new ConveyorException( "Unable to close input stream", e );
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
                    Element child = (Element) childNode;

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

    @Override
    protected InputStream getInputStream( String fileName ) {
        return plugin.getResourceAsStream( fileName );
    }

    /**
     * Tells whether the ConfigurationProvider should reload its configuration.
     * This method should only be called if
     * ConfigurationManager.isReloadingConfigs() is true.
     *
     * @return true if the file has been changed since the last time we read it
     */
    @Override
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
     *
     * @throws ConveyorException
     */
    protected void addPackageOverride( Element packageOverrideElement ) throws ConveyorException {

        OverridingPackageConfig packageConfig = new OverridingPackageConfig( plugin );

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Overridden: " + packageConfig );
        }

        packageConfig.setName( packageOverrideElement.getAttribute( "name" ) );
        packageConfig.setNamespace( packageOverrideElement.getAttribute( "namespace" ) );

        // even though this package will not be added directly to the XWork configuration,
        // we need to set the parent as the 'overridden' package so that result configs, etc can
        // be built correctly.
        packageConfig.addParent( findPackageConfig( packageConfig.getName(), packageConfig.getNamespace() ) );

        // add result types (and default result) to this package
        addResultTypes( packageConfig, packageOverrideElement );

        // load the interceptors and interceptor stacks for this package
        loadInterceptors( packageConfig, packageOverrideElement );

        // load the global result list for this package
        loadGlobalResults( packageConfig, packageOverrideElement );

        // get action overrides
        NodeList actionOverrideList = packageOverrideElement.getElementsByTagName( "action-override" );

        for ( int i = 0; i < actionOverrideList.getLength(); i++ ) {
            Element actionOverrideElement = (Element) actionOverrideList.item( i );
            addActionOverride( actionOverrideElement, packageConfig );
        }

        // get actions
        NodeList actionList = packageOverrideElement.getElementsByTagName( "action" );

        for ( int i = 0; i < actionList.getLength(); i++ ) {
            Element actionElement = (Element) actionList.item( i );
            // Check the action doesn't already exist.
            String name = actionElement.getAttribute( "name" );

            ActionConfig existing = (ActionConfig) packageConfig.getAllActionConfigs().get( name );
            if ( existing != null )
                LOG.error( "An action with the specified name already exists in the '" + packageConfig.getName()
                        + "' package: " + name + "; " + existing.getClassName() );
            else
                addAction( actionElement, packageConfig );
        }


        // Reset the parent list so we don't get infinite recursion...
        packageConfig.getParents().clear();

        keepReceipt( overrideManager.overridePackage( packageConfig ) );
    }

    private PackageConfig findPackageConfig( String name, String namespace ) throws ConveyorException {
        PackageConfig packageConfig = ConfigurationManager.getConfiguration().getPackageConfig( name );
        if ( packageConfig == null ) {
            throw new ConveyorException( "Unable to find the package being overridden named '" + name + "'." );
        }
        if ( !StringUtils.equals( namespace, packageConfig.getNamespace() ) ) {
            throw new ConveyorException( "The namespace for the '" + name + "' package is '" + packageConfig.getNamespace() + "' but the override specified '" + namespace + "'" );
        }
        return packageConfig;
    }

    protected void addActionOverride( Element actionOverrideElement, OverridingPackageConfig packageConfig )
            throws ConveyorException {

        String name = actionOverrideElement.getAttribute( "name" );
        String className = actionOverrideElement.getAttribute( "class" );
        String methodName = actionOverrideElement.getAttribute( "method" );

        // Extra elements for override
        String inheritAttr = actionOverrideElement.getAttribute( "inherit" );
        boolean inherit = "true".equals( inheritAttr );
        String key = actionOverrideElement.getAttribute( "key" );
        int weight = getIntValue( actionOverrideElement.getAttribute( "weight" ), 0 );

        // classname/methodName should be null if not set
        className = ( className.trim().length() > 0 ) ? className.trim() : null;
        methodName = ( methodName.trim().length() > 0 ) ? methodName.trim() : null;

        if ( TextUtils.stringSet( className ) ) {
            try {
                ObjectFactory.getObjectFactory().getClassInstance( className );
            } catch ( Exception e ) {
                fail( "Action class [" + className + "] not found, skipping action [" + name + "]", e );
                return;
            }
        } else if ( !inherit ) {
            throw new ConveyorException( "No class specified for action override: " + name );
        }

        Map<String, String> actionParams = XmlHelper.getParams( actionOverrideElement );

        Map<String, ResultConfig> results;

        try {
            results = buildResults( actionOverrideElement, packageConfig );
        } catch ( ConfigurationException e ) {
            throw new ConveyorException( "Error building results for action " + name + " in '"
                    + packageConfig.getName() + "' package.", e );
        }

        List<Interceptor> interceptorList = buildInterceptorList( actionOverrideElement, packageConfig );

        List<ExternalReference> externalRefs = buildExternalRefs( actionOverrideElement, packageConfig );

        Condition condition = makeConditions( actionOverrideElement );

        OverridingActionConfig actionConfig = new OverridingActionConfig( methodName, className,
                actionParams, results, interceptorList, externalRefs, packageConfig.getName(), plugin, inherit, key, weight, condition );

        // Do the override and add it to the cache of actions created by this provider.
        packageConfig.addOverridingAction( name, actionConfig );

        if ( LOG.isDebugEnabled() ) {
            LOG
                    .debug( "Loaded "
                            + ( TextUtils.stringSet( packageConfig.getNamespace() ) ? ( packageConfig
                            .getNamespace() + "/" ) : "" ) + name + " in '" + packageConfig.getName()
                            + "' package:" + actionConfig );
        }
    }

    /**
     * Create a condition for when this web fragment should be displayed
     *
     * @param element Element containing condition definitions.
     * @throws com.atlassian.plugin.PluginParseException
     *
     */
    protected Condition makeConditions( final Element element ) throws PluginParseException {
        return conditionElementParser.makeConditions( plugin, element, ConditionElementParser.CompositeType.AND );
    }

    private int getIntValue( String stringValue, int defaultValue ) {
        int value = defaultValue;
        if ( StringUtils.isNotBlank( stringValue ) ) {
            try {
                value = Integer.parseInt( stringValue );
            } catch ( NumberFormatException e ) {
                // Do nothing.
            }
        }
        return value;
    }

    /**
     * Create a PackageConfig from an XML element representing it.
     * <p/>
     * Note: Copied verbatim from the XmlConfigurationProvider class so that the
     * configuration object will be populated.
     */
    @Override
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

        try {
            // get actions
            NodeList actionList = packageElement.getElementsByTagName( "action" );

            for ( int i = 0; i < actionList.getLength(); i++ ) {
                Element actionElement = (Element) actionList.item( i );
                addAction( actionElement, newPackage );
            }

            keepReceipt( overrideManager.createPackage( newPackage ) );

        } catch ( ConveyorException e ) {
            throw new ConfigurationException( e );
        }
    }

    private void keepReceipt( Receipt receipt ) {
        receipts.add( receipt );
    }

    /**
     * This method builds a package context by looking for the parents of this
     * new package.
     * <p/>
     * If no parents are found, it will return a root package.
     * <p/>
     * Note: Copied verbatim from the XmlConfigurationProvider class so that the
     * configuration object will be populated.
     */
    @Override
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
                Class<?> erResolverClazz = ClassLoaderUtil.loadClass( externalReferenceResolver,
                        ExternalReferenceResolver.class );

                erResolver = (ExternalReferenceResolver) erResolverClazz.newInstance();
            } catch ( ClassNotFoundException e ) {
                // TODO this should be localized
                String msg = "Could not find External Reference Resolver: " + externalReferenceResolver + ". "
                        + e.getMessage();
                fail( msg, e );
                return null;
            } catch ( Exception e ) {
                // TODO this should be localized
                String msg = "Could not create External Reference Resolver: " + externalReferenceResolver + ". "
                        + e.getMessage();
                fail( msg, e );
                return null;
            }
        }

        if ( !TextUtils.stringSet( TextUtils.noNull( parent ) ) ) { // no
            // parents

            return new PluginAwarePackageConfig( name, namespace, isAbstract, erResolver, plugin );
        } else { // has parents, let's look it up

            List<PackageConfiguration> parents = ConfigurationUtil.buildParentsFromString( configuration, parent );

            if ( parents.size() <= 0 ) {
                LOG.error( "Unable to find parent packages " + parent );

                return new PluginAwarePackageConfig( name, namespace, isAbstract, erResolver, plugin );
            } else {
                return new PluginAwarePackageConfig( name, namespace, isAbstract, erResolver, parents, plugin );
            }
        }
    }

    private void fail( String message, Exception e ) {
        fail( new ConveyorException( message, e ) );
    }

    @Override
    protected void addAction( Element actionElement, PackageConfig packageContext ) throws ConfigurationException {

        String name = actionElement.getAttribute( "name" );
        String className = actionElement.getAttribute( "class" );
        String methodName = actionElement.getAttribute( "method" );

        // CONF-11593 According to the XWork people, missing attribute should default to the ActionSupport class
        if ( StringUtils.isBlank( className ) ) {
            className = ActionSupport.class.getName();
        }

        methodName = ( methodName == null || methodName.trim().length() <= 0 ) ? null : methodName.trim();
        try {
            PluginAwareActionConfig actionConfig = new PluginAwareActionConfig( null, className, null, null, null, plugin );
            ObjectFactory.getObjectFactory().buildAction( actionConfig );
        } catch ( Exception e ) {
            throw new ConfigurationException( "Action class [" + className + "] not found, skipping action [" + name + "]", e );
        } catch ( NoClassDefFoundError e ) {
            throw new ConfigurationException( "Unable to load Action class [" + className + "], skipping action [" + name + "]", e );
        }

        HashMap actionParams = getParams( actionElement );
        Map results;
        try {
            results = buildResults( actionElement, packageContext );
        } catch ( ConfigurationException e ) {
            throw new ConfigurationException( "Error building results for action " + name + " in namespace " + packageContext.getNamespace(), e );
        }
        List interceptorList = buildInterceptorList( actionElement, packageContext );
        List externalrefs = buildExternalRefs( actionElement, packageContext );

        // Create the action
        PluginAwareActionConfig actionConfig = new PluginAwareActionConfig( methodName, className, actionParams, results, interceptorList, externalrefs, packageContext.getName(), plugin );

        packageContext.addActionConfig( name, actionConfig );

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Loaded " + ( TextUtils.stringSet(
                    packageContext.getNamespace() ) ? packageContext.getNamespace() + "/" : "" ) + name + " in '" + packageContext.getName() + "' package:" + actionConfig );
        }
    }

    public static HashMap getParams( Element paramsElement ) {
        HashMap params = new HashMap();
        if ( paramsElement == null ) {
            return params;
        }
        NodeList childNodes = paramsElement.getElementsByTagName( "param" );
        for ( int i = 0; i < childNodes.getLength(); i++ ) {
            Element childNode = (Element) childNodes.item( i );

            String paramName = childNode.getAttribute( "name" );
            if ( childNode.getNodeValue() != null ) {
                String paramValue = childNode.getNodeValue();
                params.put( paramName, paramValue );
            }
        }
        return params;
    }


}
