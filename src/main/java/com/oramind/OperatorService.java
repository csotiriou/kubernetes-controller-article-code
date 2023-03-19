package com.oramind;

import com.oramind.model.Application;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class OperatorService {
    @Inject
    KubernetesClient client;


    public void onStartup(@Observes StartupEvent event) {
        client.v1()
                .resources(Application.class, Application.ApplicationsList.class)
                .watch(new ApplicationWatcher(client));

        var list = client.v1().resources(Application.class, Application.ApplicationsList.class).list();
        System.out.println("Applications: " + list.getItems());
    }
}
