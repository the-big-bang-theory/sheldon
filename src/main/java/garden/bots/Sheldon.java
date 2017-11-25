package garden.bots;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceReference;

import java.util.Optional;

public class Sheldon extends AbstractVerticle {
  
  private ServiceDiscovery discovery;
  private Record record;
  
  public void start() {
    
    discovery = Parameters.getServiceDiscovery(vertx);
    record = Parameters.getMicroServiceRecord();
    Integer httpPort = Parameters.getHttpPort();
    
    System.out.println("🎃  " + record.toJson().encodePrettily());
    
    /* add some metadata to the record */
    record.setMetadata(new JsonObject()
      .put("description", "Hello 🌍 I'm Sheldon")
    );
    
    /* Define routes */
    Router router = Router.router(vertx);
    
    router.route().handler(BodyHandler.create());
    
    router.get("/go").handler(context -> {
      // knock knock knock Penny
      discovery.getRecord(rec -> rec.getName().equals("penny"), asyncRecord -> {
        if(asyncRecord.succeeded()) {
  
          ServiceReference reference = discovery.getReference(asyncRecord.result());
          WebClient pennyClient = reference.getAs(WebClient.class);
  
          pennyClient.get(asyncRecord.result().getLocation().getString("endpoint")).send(asyncGetResult -> {
            //TODO: manage errors
            String pennysResponse = asyncGetResult.result().bodyAsJsonObject().encodePrettily();
            
            context.response()
              .putHeader("content-type", "application/json;charset=UTF-8")
              .end(pennysResponse);
          });
          
          
        } else {
          System.out.println("😡 Unable to get a WebClient for the service: " + asyncRecord.cause().getMessage());
          System.out.println("😡, Houston, we have a problem with this raider");
          context.response()
            .putHeader("content-type", "application/json;charset=UTF-8")
            .end(new JsonObject().put("error", "ouch").encodePrettily());
        }
        
      });
      
      
    });
    
    
    /* serve static assets, see /resources/webroot directory */
    router.route("/*").handler(StaticHandler.create());
    
    /* Start the server */
    HttpServer server = vertx.createHttpServer();
  
    server
      .requestHandler(router::accept).listen(httpPort, result -> {
    
      if(result.succeeded()) {
        System.out.println("🌍 Listening on " + httpPort);
          
        /* then publish the microservice to the discovery backend */
        discovery.publish(record, asyncResult -> {
          if(asyncResult.succeeded()) {
            System.out.println("😃 Microservice is published! " + record.getRegistration());
          } else {
            System.out.println("😡 Not able to publish the microservice: " + asyncResult.cause().getMessage());
          }
        });
      
      } else {
        System.out.println("😡 Houston, we have a problem: " + result.cause().getMessage());
      }
    
    });
  }
  
  public void stop(Future<Void> stopFuture) {
  
    discovery.unpublish(record.getRegistration(), asyncResult -> {
      if(asyncResult.succeeded()) {
        System.out.println("👋 bye bye " + record.getRegistration());
      } else {
        System.out.println("😡 Not able to unpublish the microservice: " + asyncResult.cause().getMessage());
      }
      stopFuture.complete();
    });
  }
}
