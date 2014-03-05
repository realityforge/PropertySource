package org.realityforge.gwt.propertysource.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.user.rebind.SourceWriter;
import java.util.List;

class StringTypeHandler
  implements TypeHandler<String>
{
  static final StringTypeHandler INSTANCE = new StringTypeHandler();

  private StringTypeHandler()
  {
  }

  @Override
  public String getStaticReturnValue( final TreeLogger logger,
                                      final List<String> propertyValue,
                                      final JMethod method )
    throws UnableToCompleteException
  {
    if ( 1 != propertyValue.size() )
    {
      logger.log( Type.ERROR, "String only supported for properties with only one value" );
      throw new UnableToCompleteException();
    }
    else
    {
      return propertyValue.get( 0 );
    }
  }

  @Override
  public void writeValue( final TreeLogger logger,
                          final SourceWriter writer,
                          final String value )
  {
    writer.print( "\"" );
    writer.print( Generator.escape( value ) );
    writer.print( "\"" );
  }
}
