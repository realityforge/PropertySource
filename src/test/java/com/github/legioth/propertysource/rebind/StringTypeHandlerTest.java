package com.github.legioth.propertysource.rebind;

import com.google.gwt.user.rebind.StringSourceWriter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
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
}
