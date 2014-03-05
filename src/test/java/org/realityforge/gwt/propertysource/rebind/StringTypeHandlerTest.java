package org.realityforge.gwt.propertysource.rebind;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.user.rebind.StringSourceWriter;
import java.util.Arrays;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class StringTypeHandlerTest
{
  @DataProvider( name = "writeValues" )
  public Object[][] actionDescriptions()
  {
    return new Object[][]{
      new Object[]{ "hi", "\"hi\"" },
      new Object[]{ "1\n2", "\"1\\n2\"" },
      new Object[]{ "&", "\"&\"" },
      new Object[]{ "Å", "\"Å\"" },
    };
  }

  @Test( dataProvider = "writeValues" )
  public void writeValues( final String input, final String output )
  {
    final StringSourceWriter writer = new StringSourceWriter();
    StringTypeHandler.INSTANCE.writeValue( null, writer, input );
    assertEquals( writer.toString(), output );
  }

  @Test
  public void getStaticReturnValue()
    throws Exception
  {
    final String result =
      StringTypeHandler.INSTANCE.getStaticReturnValue( mock( TreeLogger.class ),
                                                       Arrays.asList( "A" ),
                                                       mock( JMethod.class ) );

    assertEquals( result, "A" );
  }

  @Test
  public void getStaticReturnValue_whenMultipleValues()
    throws Exception
  {
      final TreeLogger logger = mock( TreeLogger.class );
    try
    {
      StringTypeHandler.INSTANCE.getStaticReturnValue( logger,
                                                       Arrays.asList( "A", "B" ),
                                                       mock( JMethod.class ) );
      fail( "Should have raised an exception" );
    }
    catch ( final UnableToCompleteException utce )
    {
      verify( logger ).log( Type.ERROR, "String only supported for properties with only one value" );
    }
  }
}
