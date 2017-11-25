package garden.bots;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import java.util.Optional;

class Parameters {
  
  static ServiceDiscovery getServiceDiscovery(io.vertx.core.Vertx vertx) {
    // Discovery settings
    ServiceDiscoveryOptions serviceDiscoveryOptions = new ServiceDiscoveryOptions();
    
    // Redis settings with the standard Redis Backend
    Integer redisPort = Integer.parseInt(Optional.ofNullable(System.getenv("REDIS_PORT")).orElse("6379"));
    String redisHost = Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("127.0.0.1");
    String redisAuth = Optional.ofNullable(System.getenv("REDIS_PASSWORD")).orElse(null);
    String redisRecordsKey = Optional.ofNullable(System.getenv("REDIS_RECORDS_KEY")).orElse("vert.x.ms");    // the redis hash
    
    return ServiceDiscovery.create(
      vertx,
      serviceDiscoveryOptions.setBackendConfiguration(
        new JsonObject()
          .put("host", redisHost)
          .put("port", redisPort)
          .put("auth", redisAuth)
          .put("key", redisRecordsKey)
      ));
  }
  
  /**
   * Define microservice options
   * servicePort: this is the visible port from outside
   * for example you run your service with 8080 on a platform (Clever Cloud, Docker, ...)
   * and the visible port is 80
   */
  static Record getMicroServiceRecord() {
    
    String serviceName = Optional.ofNullable(System.getenv("SERVICE_NAME")).orElse("John Doe");
    String serviceHost = Optional.ofNullable(System.getenv("SERVICE_HOST")).orElse("localhost"); // domain name
    Integer servicePort = Integer.parseInt(Optional.ofNullable(System.getenv("SERVICE_PORT")).orElse("80")); // set to 80 on Clever Cloud
    String serviceRoot = Optional.ofNullable(System.getenv("SERVICE_ROOT")).orElse("/api");
    
    return HttpEndpoint.createRecord(
      serviceName,
      serviceHost,
      servicePort,
      serviceRoot
    );
  }
  
  static Integer getHttpPort() {
    return Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8080"));
  }
}
