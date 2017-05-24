require 'buildr/git_auto_version'
require 'buildr/gpg'

desc 'GWT Property Source Library: Programmatic access to compile time configuration values'
define 'gwt-property-source' do
  project.group = 'org.realityforge.gwt.property-source'
  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project("realityforge/gwt-property-source")

  pom.add_developer('Legioth', 'Leif Astrand')
  pom.add_developer('realityforge', 'Peter Donald')
  pom.provided_dependencies.concat [:javax_annotation, :gwt_user, :gwt_dev]

  compile.with :javax_annotation, :gwt_user, :gwt_dev

  test.using :testng
  test.with :mockito

  package(:jar).include("#{_(:source, :main, :java)}/*")
  package(:sources)
  package(:javadoc)
end
