package dev.xf3d3.ultimatereports;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class UltimateReportsLoader implements PluginLoader {


    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        try {
            resolver.addRepository(new RemoteRepository.Builder("maven", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        }
        catch (NoSuchFieldError error) {
            resolver.addRepository(new RemoteRepository.Builder("maven", "default", "https://maven-central.storage-download.googleapis.com/maven2").build());
        }

        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:6.0.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("net.dv8tion:JDA:6.1.1"), null));

        resolver.addDependency(new Dependency(new DefaultArtifact("com.mysql:mysql-connector-j:9.2.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:3.4.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.xerial:sqlite-jdbc:3.49.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.h2database:h2:2.3.232"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.postgresql:postgresql:42.7.3"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("redis.clients:jedis:5.2.0"), null));

        classpathBuilder.addLibrary(resolver);
    }

}