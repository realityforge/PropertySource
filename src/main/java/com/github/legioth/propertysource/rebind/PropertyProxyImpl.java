package com.github.legioth.propertysource.rebind;

import com.github.legioth.propertysource.client.DynamicPropertySource.PropertyProxy;
import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import java.util.List;
import java.util.Set;

class PropertyProxyImpl
  implements PropertyProxy
{
  private final TreeLogger logger;
  private final PropertyOracle oracle;
  private final Set<String> usedSelectionProperties;

  public PropertyProxyImpl( final TreeLogger logger,
                            final PropertyOracle oracle,
                            final Set<String> usedSelectionProperties )
  {
    this.logger = logger;
    this.oracle = oracle;
    this.usedSelectionProperties = usedSelectionProperties;
  }

  @Override
  public String getSelectionPropertyValue( final String name )
  {
    try
    {
      usedSelectionProperties.add( name );
      return oracle.getSelectionProperty( logger, name ).getCurrentValue();
    }
    catch ( BadPropertyValueException e )
    {
      throw new RuntimeException( e );
    }
  }

  @Override
  public String getSelectionPropertyFallback( final String name )
  {
    try
    {
      usedSelectionProperties.add( name );
      return oracle.getSelectionProperty( logger, name ).getFallbackValue();
    }
    catch ( BadPropertyValueException e )
    {
      throw new RuntimeException( e );
    }
  }

  @Override
  public List<String> getConfigurationPropertyValues( final String name )
  {
    try
    {
      return oracle.getConfigurationProperty( name ).getValues();
    }
    catch ( BadPropertyValueException e )
    {
      throw new RuntimeException( e );
    }
  }
}
