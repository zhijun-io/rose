package io.zhijun.boot.diagnostics.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.util.StringUtils;

/**
 * Scans the classpath for duplicate Maven coordinates using META-INF/maven pom.properties entries.
 */
public final class ClasspathMavenArtifactScanner {

    private static final String POM_PROPERTIES_SUFFIX = "/pom.properties";

    public Set<String> findCollidingCoordinates(ClassLoader classLoader) throws IOException {
        Set<String> seen = new LinkedHashSet<String>();
        Set<String> collisions = new LinkedHashSet<String>();
        for (MavenCoordinate coordinate : scan(classLoader)) {
            String id = coordinate.getGroupId() + ":" + coordinate.getArtifactId();
            if (!seen.add(id)) {
                collisions.add(id);
            }
        }
        return collisions;
    }

    public List<MavenCoordinate> scan(ClassLoader classLoader) throws IOException {
        if (classLoader == null) {
            return Collections.emptyList();
        }
        List<MavenCoordinate> coordinates = new ArrayList<MavenCoordinate>();
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

    private static List<MavenCoordinate> scanJar(URL manifestUrl) throws IOException {
        JarURLConnection connection = (JarURLConnection) manifestUrl.openConnection();
        connection.setUseCaches(false);
        try (JarFile jarFile = connection.getJarFile()) {
            List<MavenCoordinate> coordinates = new ArrayList<MavenCoordinate>();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith("META-INF/maven/") || !name.endsWith(POM_PROPERTIES_SUFFIX)) {
                    continue;
                }
                MavenCoordinate coordinate = readCoordinate(jarFile, entry, name);
                if (coordinate != null) {
                    coordinates.add(coordinate);
                }
            }
            return coordinates;
        }
    }

    private static MavenCoordinate readCoordinate(JarFile jarFile, JarEntry entry, String entryName)
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
        if (!StringUtils.hasText(groupId) || !StringUtils.hasText(artifactId)) {
            return null;
        }
        return new MavenCoordinate(groupId, artifactId, version, jarFile.getName());
    }

    public static final class MavenCoordinate {

        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String location;

        MavenCoordinate(String groupId, String artifactId, String version, String location) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.location = location;
        }

        String getGroupId() {
            return groupId;
        }

        String getArtifactId() {
            return artifactId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MavenCoordinate)) {
                return false;
            }
            MavenCoordinate that = (MavenCoordinate) other;
            return groupId.equals(that.groupId)
                    && artifactId.equals(that.artifactId)
                    && nullSafeEquals(version, that.version);
        }

        @Override
        public int hashCode() {
            int result = groupId.hashCode();
            result = 31 * result + artifactId.hashCode();
            result = 31 * result + (version != null ? version.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return groupId + ":" + artifactId + (StringUtils.hasText(version) ? ":" + version : "") + " (" + location
                    + ")";
        }

        private static boolean nullSafeEquals(String left, String right) {
            if (left == null) {
                return right == null;
            }
            return left.equals(right);
        }
    }
}
