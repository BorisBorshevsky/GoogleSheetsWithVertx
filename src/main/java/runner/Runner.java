package runner;

import io.vertx.core.Vertx;
import service.GracefulShutdownVerticle;
import service.SimpleGoogleDataSheetRetrievalVerticle;
import service.SimpleHttpVerticle;

/**
 * Created by Alyx on 29.06.2016.
 */
public class Runner {
    public static void main(String[] args) {
        Vertx vertx = Vertx.factory.vertx();
        vertx.deployVerticle(new SimpleHttpVerticle());
        vertx.deployVerticle(new SimpleGoogleDataSheetRetrievalVerticle());
        vertx.deployVerticle(new GracefulShutdownVerticle());
    }

}
