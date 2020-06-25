require "ApscliWrapper/version"
require 'java'
require "apg-patch-cli-fat.jar"
module ApscliWrapper
  class Error < StandardError; end

  def ApscliWrapper.run(args)
    args.each do|a|
      puts "Argument: #{a}"
    end
    java_import java.lang.System
    System.setProperty("spring.profiles.active","less,live,remotecvs,groovyactions")
    System.setProperty("logback.statusListenerClass","ch.qos.logback.core.status.NopStatusListener")
    defaultPropPath = File.join( File.expand_path(File.dirname(__FILE__)), '../conf/application.properties' )
    puts "Relative path expanded : #{defaultPropPath}"
    System.setProperty("appPropertiesFile","file://#{defaultPropPath}")
    cli = com.apgsga.patch.service.client.PatchCli.create("pliLess")
    cli.process(args)
  end
end
