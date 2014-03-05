package org.realityforge.gwt.propertysource.rebind;

import org.realityforge.gwt.propertysource.client.annotations.BooleanConversion;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.user.rebind.SourceWriter;
import java.util.List;

final class BooleanTypeHandler
  implements TypeHandler<Boolean>
{
  static final BooleanTypeHandler INSTANCE = new BooleanTypeHandler();

  private BooleanTypeHandler()
  {
  }

  @Override
  public Boolean getStaticReturnValue( final TreeLogger logger,
                                       final List<String> propertyValues,
                                       final JMethod method )
    throws UnableToCompleteException
  {
    final String truePattern;
    final String[] trueValues;
    final boolean matchAll;
    final BooleanConversion conversion = method.getAnnotation( BooleanConversion.class );
    if ( null != conversion )
    {
      truePattern = conversion.truePattern();
      trueValues = conversion.trueValues();
      matchAll = conversion.matchAll();
    }
    else
    {
      truePattern = "";
      trueValues = new String[ 0 ];
      matchAll = false;
    }

    for ( final String propertyValue : propertyValues )
    {
      boolean match = true;
      boolean useDefaultLogic = true;
      if ( !truePattern.isEmpty() )
      {
        match = propertyValue.matches( truePattern );
        useDefaultLogic = false;
      }
      if ( 0 != trueValues.length )
      {
        match &= isTrueValue( propertyValue, trueValues );
        useDefaultLogic = false;
      }

      if ( useDefaultLogic )
      {
        match = isTrueValue( logger, propertyValue );
      }

      if ( matchAll && !match )
      {
        // Found one that didn't match
        return Boolean.FALSE;
      }
      else if ( match && !matchAll )
      {
        // Found one that did match
        return Boolean.TRUE;
      }
    }

    // Found all without terminating
    return matchAll ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public void writeValue( final TreeLogger logger, final SourceWriter writer, final Boolean value )
  {
    writer.print( value.toString() );
  }

  private boolean isTrueValue( final String propertyValue, final String[] trueValues )
  {
    for ( final String trueValue : trueValues )
    {
      if ( trueValue.equals( propertyValue ) )
      {
        return true;
      }
    }
    return false;
  }

  private boolean isTrueValue( final TreeLogger logger, final String propertyValue )
    throws UnableToCompleteException
  {
    if ( "true".equalsIgnoreCase( propertyValue ) || "1".equals( propertyValue ) )
    {
      return true;
    }
    else if ( "false".equalsIgnoreCase( propertyValue ) || "0".equals( propertyValue ) )
    {
      return false;
    }
    else
    {
      logger.log( Type.ERROR, "Can not interpret " + propertyValue + " as a boolean" );
      throw new UnableToCompleteException();
    }
  }
}
