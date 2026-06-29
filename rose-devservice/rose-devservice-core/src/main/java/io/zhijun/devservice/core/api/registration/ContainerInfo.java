package io.zhijun.devservice.core.api.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import org.apiguardian.api.API;

/**
 * Holds information about a container.
 */
@API(status = API.Status.EXPERIMENTAL)
public final class ContainerInfo {

    private final String id;
    private final String imageName;
    private final List<String> names;
    private final List<ContainerPort> exposedPorts;
    private final Map<String, String> labels;
    private final String status;

    public ContainerInfo(
            String id,
            String imageName,
            List<String> names,
            List<ContainerPort> exposedPorts,
            Map<String, String> labels,
            String status) {
        Validate.notBlank(id, "id cannot be null or empty");
        Validate.notBlank(imageName, "imageName cannot be null or empty");
        Validate.notNull(names, "names cannot be null");
        Validate.notNull(exposedPorts, "exposedPorts cannot be null");
        Validate.notNull(labels, "labels cannot be null");
        Validate.notBlank(status, "status cannot be null or empty");

        this.id = id;
        this.imageName = imageName;
        this.names = Collections.unmodifiableList(new ArrayList<String>(names));
        this.exposedPorts = Collections.unmodifiableList(new ArrayList<ContainerPort>(exposedPorts));
        this.labels = Collections.unmodifiableMap(new HashMap<String, String>(labels));
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getImageName() {
        return imageName;
    }

    public List<String> getNames() {
        return names;
    }

    public List<ContainerPort> getExposedPorts() {
        return exposedPorts;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Holds information about a container port.
     */
    public static final class ContainerPort {

        private final String ip;

        private final Integer privatePort;

        private final Integer publicPort;

        private final String type;

        public ContainerPort(String ip, Integer privatePort, Integer publicPort, String type) {
            this.ip = ip;
            this.privatePort = privatePort;
            this.publicPort = publicPort;
            this.type = type;
        }

        public String getIp() {
            return ip;
        }

        public Integer getPrivatePort() {
            return privatePort;
        }

        public Integer getPublicPort() {
            return publicPort;
        }

        public String getType() {
            return type;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private String imageName;
        private List<String> names = Collections.emptyList();
        private List<ContainerPort> exposedPorts = Collections.emptyList();
        private Map<String, String> labels = Collections.emptyMap();
        private String status;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder imageName(String imageName) {
            this.imageName = imageName;
            return this;
        }

        public Builder names(List<String> names) {
            this.names = names;
            return this;
        }

        public Builder exposedPorts(List<ContainerPort> exposedPorts) {
            this.exposedPorts = exposedPorts;
            return this;
        }

        public Builder labels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public ContainerInfo build() {
            return new ContainerInfo(id, imageName, names, exposedPorts, labels, status);
        }
    }
}
