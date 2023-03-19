package com.oramind;

import com.oramind.model.Application;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

import java.util.Map;

public class ApplicationWatcher implements Watcher<Application> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApplicationWatcher.class);


    KubernetesClient client;

    public ApplicationWatcher(KubernetesClient client) {
        log.info("watching applications...");
        this.client = client;
    }

    @Override
    public void eventReceived(Action action, Application resource) {
        switch (action.name()) {
            case "ADDED", "MODIFIED" -> onModifyOrAddApp(resource);
            case "DELETED" -> onDeleteApp(resource);
            case "ERROR" -> log.error("Error while watching OramindApplication: " + resource.getMetadata().getName());
        }
    }

    public void onModifyOrAddApp(Application resource){
        log.info("Oramind Application: " + resource.getMetadata().getName() + " has been added or modified");

        var spec = resource.getSpec();
        var secretName = spec.getSecretName();

        //creating the secret
        var newSecret = new SecretBuilder()
                .withNewMetadata()
                .withName(secretName)
                .endMetadata()
                .addToStringData("secret", generateHash())
                .build();
        client.resource(newSecret).inNamespace(resource.getMetadata().getNamespace()).createOrReplace();

        //creating the deployment
        var deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(resource.getMetadata().getName())
                .withLabels(Map.of("oramind-app", resource.getMetadata().getName()))
                .endMetadata()

                .withNewSpec()
                .withReplicas(1)
                .withNewSelector()
                .addToMatchLabels("oramind-app", resource.getMetadata().getName())
                .endSelector()

                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("oramind-app", resource.getMetadata().getName())
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                        .withName("application")
                        .withImage(spec.getImage())
                        .addNewEnv()
                            .withName(spec.getSecretEnvVar())
                            .withNewValueFrom()
                                .withNewSecretKeyRef()
                                    .withName(spec.getSecretName())
                                    .withKey("secret")
                                .endSecretKeyRef()
                            .endValueFrom()
                        .endEnv()
                        .endContainer()
                    .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        client.resource(deployment).inNamespace(resource.getMetadata().getNamespace()).createOrReplace();


    }

    private void onDeleteApp(Application resource) {
        log.info("OramindApplication: " + resource.getMetadata().getName() + " has been deleted");
        var name = resource.getMetadata().getName();
        var namespace = resource.getMetadata().getNamespace();
        var app = client.apps().deployments().inNamespace(namespace).withName(name).get();
        var secret = client.secrets().inNamespace(namespace).withName(resource.getSpec().getSecretName()).get();
        if (app != null) {
            client.apps().deployments().inNamespace(namespace).withName(name).delete();
        }
        if (secret != null) {
            client.secrets().inNamespace(namespace).withName(resource.getSpec().getSecretName()).delete();
        }
    }

    @Override
    public void onClose(WatcherException cause) {

    }

    //generate random 32-bit hash
    private String generateHash() {
        return String.format("%032x", new java.util.Random().nextLong());
    }
}
