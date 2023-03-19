package com.oramind.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.DefaultKubernetesResourceList;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Singular;
import io.fabric8.kubernetes.model.annotation.Version;


@Group("oramind.com")
@Version("v1")
@Singular("application")
@Plural("applications")
public class Application extends CustomResource<Application.ApplicationSpec, Void> {

    public static class ApplicationSpec {
        @JsonProperty("application-name")
        private String applicationName;

        private String description;

        private String image;

        @JsonProperty("secret-name")
        private String secretName;

        @JsonProperty("secret-env-var")
        private String secretEnvVar;

        public String getApplicationName() {
            return applicationName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getSecretName() {
            return secretName;
        }

        public void setSecretName(String secretName) {
            this.secretName = secretName;
        }

        public String getSecretEnvVar() {
            return secretEnvVar;
        }

        public void setSecretEnvVar(String secretEnvVar) {
            this.secretEnvVar = secretEnvVar;
        }
    }

    public static class ApplicationsList extends DefaultKubernetesResourceList<Application> {
    }
}
