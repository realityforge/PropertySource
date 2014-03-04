package com.github.legioth.propertysource.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.user.rebind.SourceWriter;
import java.util.Arrays;
import java.util.List;

final class StringListTypeHandler
  implements TypeHandler<List<String>>
{
  static final StringListTypeHandler INSTANCE = new StringListTypeHandler();

  private StringListTypeHandler()
  {
  }

  @Override
  public List<String> getStaticReturnValue( final TreeLogger logger,
                                            final List<String> propertyValue,
                                            final JMethod method )
    throws UnableToCompleteException
  {
    final JType type = method.getReturnType();
    final JParameterizedType parameterizedType = type.isParameterized();
    if ( null == parameterizedType ||
         !parameterizedType.getTypeArgs()[ 0 ].getQualifiedSourceName().equals( String.class.getName() ) )
    {
      final String message =
        type.getParameterizedQualifiedSourceName() + " is not supported. List<String> is the only supported List type.";
      logger.log( Type.ERROR, message );
      throw new UnableToCompleteException();
    }

    return propertyValue;
  }

  @Override
  public void writeValue( final TreeLogger logger,
                          final SourceWriter writer,
                          final List<String> value )
  {
    // Arrays.asList(value1, value2);
    writer.print( Arrays.class.getName() );
    writer.print( ".asList(" );
    for ( int i = 0; i < value.size(); i++ )
    {
      if ( i != 0 )
      {
        writer.print( "," );
      }
      writer.print( "\"" );
      writer.print( Generator.escape( value.get( i ) ) );
      writer.print( "\"" );
    }
    writer.print( ")" );
  }
}
