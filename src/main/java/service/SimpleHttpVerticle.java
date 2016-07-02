package service;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by Alyx on 29.06.2016.
 */
public class SimpleHttpVerticle implements Verticle {

    public static final String SHEET_ADDRESS = "sheetAddress";
    public static final String SHEET_NAME = "sheetName";
    public static final String ROW = "row";
    public static final String COLUMN = "column";
    private Vertx vertx;
    private EventBus eventBus;
    private HttpServer httpServer;

    public Vertx getVertx() {
        return vertx;
    }

    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        eventBus = this.vertx.eventBus();
    }

    public void start(Future<Void> startFuture) throws Exception {
        httpServer = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.route(HttpMethod.GET, "/googleSheet").handler((RoutingContext routingContext) -> {
            HttpServerRequest request = routingContext.request();
            MultiMap headers = request.params();

            JsonObject jsonObject = new JsonObject()
                    .put(SHEET_ADDRESS, headers.get(SHEET_ADDRESS))
                    .put(SHEET_NAME, headers.get(SHEET_NAME))
                    .put(ROW, headers.get(ROW))
                    .put(COLUMN, headers.get(COLUMN));

            eventBus.send("googleSheet", jsonObject, (AsyncResult<Message<JsonObject>> messageAsyncResult) -> {
                JsonObject body = messageAsyncResult.result().body();


                HttpServerResponse response = routingContext.response();
                response.putHeader("content-type", "text/plain");
                response.end("content of the cell is: " + body.getString("cell"));
            });
        });

        router.route(HttpMethod.GET, "/shutdown").handler((RoutingContext event) -> eventBus.send("shutdown", new JsonObject()));

        httpServer.requestHandler(router::accept).listen(8080);
        startFuture.complete();
    }

    public void stop(Future<Void> stopFuture) throws Exception {
        System.out.println("stopping http verticle");
        httpServer.close((AsyncResult<Void> event) -> stopFuture.complete());
    }
}
