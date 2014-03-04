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
  private final TreeLogger _logger;
  private final PropertyOracle _propertyOracle;
  private final Set<String> _usedSelectionProperties;

  public PropertyProxyImpl( final TreeLogger logger,
                            final PropertyOracle propertyOracle,
                            final Set<String> usedSelectionProperties )
  {
    _logger = logger;
    _propertyOracle = propertyOracle;
    _usedSelectionProperties = usedSelectionProperties;
  }

  @Override
  public String getSelectionPropertyValue( final String name )
  {
    try
    {
      _usedSelectionProperties.add( name );
      return _propertyOracle.getSelectionProperty( _logger, name ).getCurrentValue();
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
      _usedSelectionProperties.add( name );
      return _propertyOracle.getSelectionProperty( _logger, name ).getFallbackValue();
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
      return _propertyOracle.getConfigurationProperty( name ).getValues();
    }
    catch ( BadPropertyValueException e )
    {
      throw new RuntimeException( e );
    }
  }
}
