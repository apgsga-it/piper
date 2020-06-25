VERSION = IO.read("version")
Gem::Specification.new do |spec|
  spec.name = 'apscli'
  spec.version = VERSION
  spec.authors = ['che,jhe']
  spec.summary = 'A Jruby wrapper for apscli et all'
  spec.platform = 'java'
  spec.require_paths = %w[lib vendor/jars]
  spec.files = Dir[ 'lib/**/*', 'Gemfile', 'conf/**', '*.gemspec']
  spec.test_files = Dir[ 'gem/test/*.rb' ]
  spec.executables   = ["apscli"]
  spec.bindir  = "bin"
  spec.require_paths = %w[lib conf]
end