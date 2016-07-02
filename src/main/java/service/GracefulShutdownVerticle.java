package service;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Alyx on 02.07.2016.
 */
public class GracefulShutdownVerticle implements Verticle {

    private Vertx vertx;
    private EventBus eventBus;
    private MessageConsumer<JsonObject> consumer;

    public Vertx getVertx() {
        return vertx;
    }

    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        eventBus = this.vertx.eventBus();
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        consumer = eventBus.consumer("shutdown",
                (Message<JsonObject> event) -> vertx.executeBlocking(
                        (Future<JsonObject> event1) -> arrangeGracefulShutdown(),
                        (AsyncResult<JsonObject> event1) -> {}
                )
        );
        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        System.out.println("stopping shutdown verticle");
        consumer.unregister();
        stopFuture.complete();
    }

    private void arrangeGracefulShutdown() {
        Set<String> deploymentIDs = vertx.deploymentIDs();

        CountDownLatch countDownLatch = new CountDownLatch(deploymentIDs.size());

        for (String deployment : deploymentIDs) {
            vertx.undeploy(deployment, (AsyncResult<Void> event) -> countDownLatch.countDown());
        }
        System.out.println("waiting");
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("undeployed all verticles");

        System.out.println("shutting down vertx");
        vertx.close((AsyncResult<Void> event) -> System.out.println("vertx is shut down"));
    }
}
