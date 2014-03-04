package com.github.legioth.propertysource.rebind;

import com.github.legioth.propertysource.client.DynamicPropertySource;
import com.github.legioth.propertysource.client.annotations.Namespace;
import com.github.legioth.propertysource.client.annotations.Property;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropertySourceGenerator
  extends Generator
{
  private static final Map<String, TypeHandler<?>> _typeHandlers = new HashMap<String, TypeHandler<?>>();
  static
  {
    _typeHandlers.put( String.class.getName(), StringTypeHandler.INSTANCE );
    _typeHandlers.put( "boolean", BooleanTypeHandler.INSTANCE );
    _typeHandlers.put( List.class.getName(), StringListTypeHandler.INSTANCE );
  }

  @Override
  public String generate( final TreeLogger logger,
                          final GeneratorContext context,
                          final String typeName )
    throws UnableToCompleteException
  {
    final TypeOracle typeOracle = context.getTypeOracle();
    final JClassType type = typeOracle.findType( typeName );

    if ( type.isAbstract() && null == type.isInterface() )
    {
      logger.log( Type.ERROR, "Target type should either be an interface or a non-abstract class" );
      throw new UnableToCompleteException();
    }

    /*
     * Class name depends on the used properties -> must evaluate the type
     * before we know whether to generate a new class. Most of the effort
     * does currently go to the evaluation, so we might as well keep the
     * design simple by directly producing the class body at the same time
     * instead of first populating a data model and then using that data
     * model to produce the source only when needed.
     */
    final StringSourceWriter sourceWriter = new StringSourceWriter();
    final Set<String> usedSelectionProperties =
      writeMethods( logger.branch( Type.DEBUG, "Processing methods in " + typeName ),
                    context,
                    type,
                    sourceWriter );

    // Properties in deterministic order
    final ArrayList<String> sortedProperties = new ArrayList<String>( usedSelectionProperties );
    Collections.sort( sortedProperties );

    final StringBuilder prefixes = new StringBuilder( "Impl" );
    for ( final String propertyName : sortedProperties )
    {
      try
      {
        final SelectionProperty property = context.getPropertyOracle().getSelectionProperty( logger, propertyName );
        if ( property.getPossibleValues().size() > 1 )
        {
          /*
           * Separating with not 100% safe as a_b + c and a + b_c
           * would both give a_b_c, but the collision risk is quite
           * minimal. AbstractClientBundleGenerator seems to use the
           * same strategy in generateSimpleSourceName.
           */
          prefixes.append( '_' ).append( property.getCurrentValue() );
        }
      }
      catch ( final BadPropertyValueException bpve )
      {
        logger.log( Type.ERROR, "Could not recheck property", bpve );
        throw new UnableToCompleteException();
      }
    }

    final String packageName = type.getPackage().getName();
    final String className = type.getSimpleSourceName() + prefixes.toString();

    final PrintWriter writer = context.tryCreate( logger, packageName, className );
    if ( null != writer )
    {
      final ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory( packageName, className );
      logger.log( Type.DEBUG, "Assembling " + factory.getCreatedClassName() );
      if ( null != type.isInterface() )
      {
        factory.addImplementedInterface( type.getQualifiedSourceName() );
      }
      else
      {
        factory.setSuperclass( type.getQualifiedSourceName() );
      }

      final SourceWriter realSourceWriter = factory.createSourceWriter( context, writer );
      realSourceWriter.print( sourceWriter.toString() );
      realSourceWriter.commit( logger );
    }
    return packageName + "." + className;
  }

  private static Set<String> writeMethods( final TreeLogger logger,
                                           final GeneratorContext context,
                                           final JClassType type,
                                           final SourceWriter writer )
    throws UnableToCompleteException
  {
    final Set<String> allUsedSelectionProperties = new HashSet<String>();
    final JMethod[] methods = type.getMethods();
    for ( final JMethod method : methods )
    {
      if ( method.isStatic() )
      {
        logger.log( Type.DEBUG, "Ignoring static method " + method.getName() );
        continue;
      }

      if ( !method.isPublic() )
      {
        logger.log( Type.DEBUG, "Ignoring non-public method " + method.getName() );
        continue;
      }

      final Set<String> usedSelectionProperties =
        writeMethod( logger.branch( Type.DEBUG, "Processing method " + method.getReadableDeclaration() ),
                     context,
                     method,
                     writer );
      if ( null != usedSelectionProperties )
      {
        allUsedSelectionProperties.addAll( usedSelectionProperties );
      }
    }

    return allUsedSelectionProperties;
  }

  @SuppressWarnings( "unchecked" )
  private static Set<String> writeMethod( final TreeLogger logger,
                                          final GeneratorContext context,
                                          final JMethod method,
                                          final SourceWriter writer )
    throws UnableToCompleteException
  {
    if ( 0 != method.getParameters().length )
    {
      logger.log( Type.ERROR, "Only supporting methods with no arguments" );
      throw new UnableToCompleteException();
    }

    final JType returnType = method.getReturnType();
    final TypeHandler<Object> typeHandler =
      (TypeHandler<Object>) _typeHandlers.get( returnType.getQualifiedSourceName() );
    if ( null == typeHandler )
    {
      logger.log( Type.ERROR, returnType.getQualifiedSourceName() + " is not supported" );
      throw new UnableToCompleteException();
    }

    final Set<String> usedSelectionProperties;
    final Object returnValue;
    if ( method.isAbstract() )
    {
      final String propertyName = getPropertyName( method );
      final String selectionPropertyValue = getStaticSelectionPropertyValue( logger, context, propertyName );
      final List<String> configurationPropertyValues = getStaticConfigurationPropertyValues( context, propertyName );
      if ( null == selectionPropertyValue && null == configurationPropertyValues )
      {
        logger.log( Type.ERROR, "Property " + propertyName + " not found" );
        throw new UnableToCompleteException();
      }

      if ( null != selectionPropertyValue )
      {
        final List<String> propertyValues = Collections.singletonList( selectionPropertyValue );
        returnValue = typeHandler.getStaticReturnValue( logger, propertyValues, method );
        usedSelectionProperties = Collections.singleton( propertyName );
      }
      else
      {
        returnValue = typeHandler.getStaticReturnValue( logger, configurationPropertyValues, method );
        usedSelectionProperties = null;
      }
    }
    else
    {
      usedSelectionProperties = new HashSet<String>();
      returnValue = getDynamicPropertyValue( logger.branch( Type.DEBUG, "Evaluating method" ),
                                             context,
                                             method,
                                             usedSelectionProperties );
    }

    writer.println( "%s {", method.getReadableDeclaration( false, false, false, false, true ) );
    writer.indent();

    writer.print( "return " );
    typeHandler.writeValue( logger, writer, returnValue );
    writer.println( ";" );

    writer.outdent();
    writer.println( "}" );
    writer.println();

    return usedSelectionProperties;
  }

  private static Object getDynamicPropertyValue( final TreeLogger logger,
                                                 final GeneratorContext context,
                                                 final JMethod method,
                                                 final Set<String> usedSelectionProperties )
    throws UnableToCompleteException
  {
    final JClassType enclosingType = method.getEnclosingType();
    if ( enclosingType.isAbstract() )
    {
      logger.log( Type.ERROR, "Can not evaluate method in abstract class" );
      throw new UnableToCompleteException();
    }

    try
    {
      // TODO Load class using a new class loader to pick up changes
      final Class<? extends DynamicPropertySource> targetClass =
        Class.forName( enclosingType.getQualifiedSourceName() ).asSubclass( DynamicPropertySource.class );
      final DynamicPropertySource source = targetClass.newInstance();

      final Field proxyField = DynamicPropertySource.class.getDeclaredField( "proxy" );
      proxyField.setAccessible( true );

      final PropertyProxyImpl proxy =
        new PropertyProxyImpl( logger, context.getPropertyOracle(), usedSelectionProperties );
      proxyField.set( source, proxy );

      final Method instanceMethod = targetClass.getMethod( method.getName() );
      return instanceMethod.invoke( source );
    }
    catch ( final Exception e )
    {
      logger.log( Type.ERROR, "Could not get dynamic type", e );
      throw new UnableToCompleteException();
    }
  }

  private static String getStaticSelectionPropertyValue( final TreeLogger logger,
                                                         final GeneratorContext context,
                                                         final String propertyName )
  {
    try
    {
      final SelectionProperty property = context.getPropertyOracle().getSelectionProperty( logger, propertyName );
      return property.getCurrentValue();
    }
    catch ( final BadPropertyValueException bpve )
    {
      return null;
    }
  }

  private static List<String> getStaticConfigurationPropertyValues( final GeneratorContext context,
                                                                    final String propertyName )
  {
    try
    {
      final ConfigurationProperty property = context.getPropertyOracle().getConfigurationProperty( propertyName );
      return property.getValues();
    }
    catch ( final BadPropertyValueException bpve )
    {
      return null;
    }
  }

  private static String getPropertyName( final JMethod method )
  {
    // Use @Property on method if defined
    final Property methodPropertyAnnotation = method.getAnnotation( Property.class );
    if ( null != methodPropertyAnnotation )
    {
      return methodPropertyAnnotation.value();
    }

    final JClassType enclosingType = method.getEnclosingType();

    // Use @Property on type if defined
    Property typePropertyAnnotation = enclosingType.getAnnotation( Property.class );
    if ( null != typePropertyAnnotation )
    {
      return typePropertyAnnotation.value();
    }

    // Default to using method name
    final String propertyName = method.getName();

    // Supplement with @Namespace on type if defined
    final Namespace namespace = enclosingType.getAnnotation( Namespace.class );
    if ( null != namespace )
    {
      return namespace.value() + "." + propertyName;
    }
    else
    {
      return propertyName;
    }
  }
}
