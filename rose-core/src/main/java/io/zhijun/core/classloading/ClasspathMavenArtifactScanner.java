package io.zhijun.core.classloading;

import io.zhijun.core.annotation.Internal;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Internal scanner for duplicate Maven coordinates using Maven {@code pom.properties} metadata entries.
 */
@Internal
public final class ClasspathMavenArtifactScanner {

    private static final String POM_PROPERTIES_SUFFIX = "/pom.properties";

    public Set<String> findCollidingCoordinates(ClassLoader classLoader) throws IOException {
        Set<String> seen = new LinkedHashSet<String>();
        Set<String> collisions = new LinkedHashSet<String>();
        for (Artifact coordinate : scan(classLoader)) {
            String id = coordinate.getGroupId() + ":" + coordinate.getArtifactId();
            if (!seen.add(id)) {
                collisions.add(id);
            }
        }
        return collisions;
    }

    public List<Artifact> scan(ClassLoader classLoader) throws IOException {
        if (classLoader == null) {
            return Collections.emptyList();
        }
        List<Artifact> coordinates = new ArrayList<>();
        Enumeration<URL> manifests = classLoader.getResources("META-INF/MANIFEST.MF");
        while (manifests.hasMoreElements()) {
            URL manifestUrl = manifests.nextElement();
            if (!"jar".equals(manifestUrl.getProtocol())) {
                continue;
            }
            coordinates.addAll(scanJar(manifestUrl));
        }
        return coordinates;
    }

    private static List<Artifact> scanJar(URL manifestUrl) throws IOException {
        JarURLConnection connection = (JarURLConnection) manifestUrl.openConnection();
        connection.setUseCaches(false);
        try (JarFile jarFile = connection.getJarFile()) {
            List<Artifact> coordinates = new ArrayList<>();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith("META-INF/maven/") || !name.endsWith(POM_PROPERTIES_SUFFIX)) {
                    continue;
                }
                Artifact coordinate = readCoordinate(jarFile, entry, name);
                if (coordinate != null) {
                    coordinates.add(coordinate);
                }
            }
            return coordinates;
        }
    }

    private static Artifact readCoordinate(JarFile jarFile, JarEntry entry, String entryName)
        throws IOException {
        String relative =
            entryName.substring("META-INF/maven/".length(), entryName.length() - POM_PROPERTIES_SUFFIX.length());
        int separator = relative.indexOf('/');
        if (separator <= 0 || separator >= relative.length() - 1) {
            return null;
        }
        String groupIdFromPath = relative.substring(0, separator);
        String artifactIdFromPath = relative.substring(separator + 1);
        Properties properties = new Properties();
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            properties.load(inputStream);
        }
        String groupId = properties.getProperty("groupId", groupIdFromPath);
        String artifactId = properties.getProperty("artifactId", artifactIdFromPath);
        String version = properties.getProperty("version");
        if (StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId)) {
            return null;
        }
        return new Artifact(groupId, artifactId, version, jarFile.getName());
    }
}
