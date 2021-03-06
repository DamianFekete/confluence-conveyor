package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.plugin.ConfluencePluginUtils;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.util.Assertions;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AbstractCompositeCondition;
import com.atlassian.plugin.web.conditions.AndCompositeCondition;
import com.atlassian.plugin.web.conditions.InvertedCondition;
import com.atlassian.plugin.web.conditions.OrCompositeCondition;
import com.opensymphony.xwork.config.providers.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the logic for constructing
 * {@link com.atlassian.plugin.web.Condition} objects from an XML element.
 */
public class ConditionElementParser {

    static class CompositeType {

        static final int OR = 0;

        static final int AND = 1;

        static int parse( final String type ) throws PluginParseException {
            if ( "or".equalsIgnoreCase( type ) ) {
                return CompositeType.OR;
            } else if ( "and".equalsIgnoreCase( type ) ) {
                return CompositeType.AND;
            } else {
                throw new PluginParseException( "Invalid condition type specified. type = " + type );
            }
        }

    }

    private final Plugin plugin;

    public ConditionElementParser( final Plugin plugin ) {
        this.plugin = plugin;
    }

    private List<Element> getChildElements( Element element, String name ) {
        List<Element> matches = new ArrayList<Element>();
        NodeList children = element.getChildNodes();

        for ( int i = 0; i < children.getLength(); i++ ) {
            Node child = children.item( i );
            if ( child instanceof Element && name.equals( child.getNodeName() ) )
                matches.add( (Element) child );
        }

        return matches;
    }

    /**
     * Create a condition for when this web fragment should be displayed.
     *
     * @param element Element of web-section, web-item, or web-panel.
     * @param type    logical operator type
     * @throws com.atlassian.plugin.PluginParseException
     *
     */
    public Condition makeConditions( final Plugin plugin, final Element element, final int type ) throws PluginParseException {
        Assertions.notNull( "plugin == null", plugin );

        // make single conditions (all Anded together)
        final List<Element> singleConditionElements = getChildElements( element, "condition" );
        Condition singleConditions = null;
        if ( ( singleConditionElements != null ) && singleConditionElements.size() > 0 ) {
            singleConditions = makeConditions( plugin, singleConditionElements, type );
        }

        // make composite conditions (logical operator can be specified by
        // "type")
        final List<Element> nestedConditionsElements = getChildElements( element, "conditions" );
        AbstractCompositeCondition nestedConditions = null;
        if ( ( nestedConditionsElements != null ) && nestedConditionsElements.size() > 0 ) {
            nestedConditions = getCompositeCondition( type );
            for ( Element nestedElement : nestedConditionsElements ) {
                nestedConditions.addCondition( makeConditions( plugin, nestedElement, CompositeType.parse( nestedElement.getAttribute( "type" ) ) ) );
            }
        }

        if ( ( singleConditions != null ) && ( nestedConditions != null ) ) {
            // Join together the single and composite conditions by this type
            final AbstractCompositeCondition compositeCondition = getCompositeCondition( type );
            compositeCondition.addCondition( singleConditions );
            compositeCondition.addCondition( nestedConditions );
            return compositeCondition;
        } else if ( singleConditions != null ) {
            return singleConditions;
        } else if ( nestedConditions != null ) {
            return nestedConditions;
        }

        return null;
    }

    public Condition makeConditions( final Plugin plugin, final List<Element> elements, final int type ) throws PluginParseException {
        int size = elements.size();
        if ( size == 0 ) {
            return null;
        } else if ( size == 1 ) {
            return makeCondition( (Element) elements.get( 0 ) );
        } else {
            final AbstractCompositeCondition compositeCondition = getCompositeCondition( type );
            for ( Element element : elements ) {
                compositeCondition.addCondition( makeCondition( element ) );
            }

            return compositeCondition;
        }
    }

    public Condition makeCondition( final Element element ) throws PluginParseException {
        try {
            final Condition condition = loadClass( element.getAttribute( "class" ), Condition.class );
            condition.init( XmlHelper.getParams( element ) );

            if ( ( element.getAttributeNode( "invert" ) != null ) && "true".equals( element.getAttribute( "invert" ) ) ) {
                return new InvertedCondition( condition );
            }

            return condition;
        } catch ( final ClassCastException e ) {
            throw new PluginParseException( "Configured condition class does not implement the Condition interface", e );
        }
    }

    private <T> T loadClass( String className, Class<T> type ) {
        Class cls = null;
        try {
            cls = plugin.loadClass( className, getClass() );
            return type.cast( ConfluencePluginUtils.instantiatePluginModule( plugin, cls ) );
        } catch ( ClassNotFoundException e ) {
            throw new PluginParseException( "Unable to load the condition class: " + className );
        }
    }

    private AbstractCompositeCondition getCompositeCondition( final int type ) throws PluginParseException {
        switch ( type ) {
            case CompositeType.OR:
                return new OrCompositeCondition();
            case CompositeType.AND:
                return new AndCompositeCondition();
            default:
                throw new PluginParseException( "Invalid condition type specified. type = " + type );
        }
    }
}