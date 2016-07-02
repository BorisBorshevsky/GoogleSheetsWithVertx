package service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alyx on 30.06.2016.
 */
public class SimpleGoogleDataSheetRetrievalVerticle implements Verticle {

    private static final String APPLICATION_NAME = "GoogleSheetsWithVertx";
    public static final String KEY_JSON = "D:\\scala_training\\GoogleSheetsWithVertx-c9b9087c52e2.json";
    private Vertx vertx;
    private EventBus eventBus;
    private MessageConsumer<JsonObject> consumer;
    private Sheets sheets;

    public static final String SHEET_ADDRESS = "sheetAddress";
    public static final String SHEET_NAME = "sheetName";
    public static final String ROW = "row";
    public static final String COLUMN = "column";

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        eventBus = this.vertx.eventBus();
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(KEY_JSON))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

        sheets = new Sheets.Builder(httpTransport, jsonFactory, credential).setApplicationName(APPLICATION_NAME)
                .build();

        consumer = eventBus.consumer("googleSheet", (Message<JsonObject> message) -> {
            JsonObject body = message.body();

            String spreadSheet = body.getString(SHEET_ADDRESS);
            String sheetName = body.getString(SHEET_NAME);
            String row = body.getString(ROW);
            String column = body.getString(COLUMN);

            vertx.executeBlocking((Future<ValueRange> valueRangeFuture) -> {
                try {
                    long time = System.currentTimeMillis();
                    ValueRange execute = sheets.spreadsheets().values().get(spreadSheet, sheetName + "!" + column + row).execute();
                    System.out.println(System.currentTimeMillis() - time);
                    valueRangeFuture.complete(execute);
                } catch (IOException e) {
                    valueRangeFuture.fail(e);
                }
            }, (AsyncResult<ValueRange> valueRangeAsyncResult) -> {
                if (valueRangeAsyncResult.succeeded()) {
                    List<List<Object>> values = valueRangeAsyncResult.result().getValues();
                    message.reply(new JsonObject().put("cell", values.get(0).get(0)));
                } else {
                    valueRangeAsyncResult.cause().printStackTrace();
                    message.reply(new JsonObject().put("cell", "content"));
                }
            });
        });

        startFuture.complete();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        System.out.println("stopping google sheet verticle");
        consumer.unregister();
        stopFuture.complete();
    }

}
