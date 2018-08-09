package scripts
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.util.filter.OrDependencyFilter
import org.eclipse.aether.util.filter.PatternInclusionsDependencyFilter
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import com.apgsga.artifact.query.impl.RepositorySystemFactory
import com.apgsga.artifact.query.util.ConsoleDependencyGraphDumper

def RepositorySystem repoSystem = RepositorySystemFactory.newRepositorySystem();
def RepositorySystemSession session = RepositorySystemFactory.newRepositorySystemSession(repoSystem, "/Users/chhex/apg/test/mavenrepo");
def repos = RepositorySystemFactory.newRepositories(repoSystem, session)

def Dependency dependency = new Dependency( new DefaultArtifact( "com.affichage.it21.gp", "gp-ui", "", "jar", "9.0.6.ADMIN-UIMIG-SNAPSHOT" ), "compile" );

CollectRequest collectRequest = new CollectRequest();
collectRequest.setRoot( dependency );
collectRequest.setRepositories(repos)
PatternInclusionsDependencyFilter filter1 = new PatternInclusionsDependencyFilter("com.apgsga.*")
PatternInclusionsDependencyFilter filter2 = new PatternInclusionsDependencyFilter("com.affichage.*")
OrDependencyFilter depFilter = new OrDependencyFilter(filter1,filter2)
DependencyRequest dependencyRequest = new DependencyRequest();
dependencyRequest.setCollectRequest( collectRequest );
dependencyRequest.setFilter(depFilter)

DependencyNode rootNode = repoSystem.resolveDependencies( session, dependencyRequest ).getRoot();

StringBuilder dump = new StringBuilder();
ByteArrayOutputStream os = new ByteArrayOutputStream( 1024 );
rootNode.accept( new ConsoleDependencyGraphDumper( new PrintStream( os ) ) );
dump.append( os.toString() );
PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
rootNode.accept( nlg );
println "Root :" + rootNode.toString()
nlg.getArtifacts(false).forEach( { a -> println a.toString() })

