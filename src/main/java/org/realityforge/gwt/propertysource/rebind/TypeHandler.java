package org.realityforge.gwt.propertysource.rebind;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.user.rebind.SourceWriter;
import java.util.List;

interface TypeHandler<T>
{
  T getStaticReturnValue( TreeLogger logger, List<String> propertyValue, JMethod method )
    throws UnableToCompleteException;

  void writeValue( TreeLogger logger, SourceWriter writer, T value );
}
