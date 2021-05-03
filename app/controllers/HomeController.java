package controllers;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import play.libs.EventSource;
import play.mvc.*;
import scala.concurrent.ExecutionContext;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
  private final Materializer _materializer;

  private final ActorSystem _actorSystem;

  private final ExecutionContext _executionContext;

  private final ConcurrentHashMap<String, ActorRef> _myActors;

  @Inject
  public HomeController(final Materializer materializer, final ActorSystem actorSystem,
      final ExecutionContext executionContext) {
    _materializer = materializer;
    _actorSystem = actorSystem;
    _executionContext = executionContext;
    _myActors = new ConcurrentHashMap<>();
  }

  /**
   * An action that renders an HTML page with a welcome message.
   * The configuration in the <code>routes</code> file means that
   * this method will be called when the application receives a
   * <code>GET</code> request with a path of <code>/</code>.
   */
  public Result index(final String key) {
    Source<EventSource.Event, ActorRef> actorRefPoweredEventSource = Source.actorRef(100, OverflowStrategy.dropNew());
    Pair<ActorRef, Source<EventSource.Event, NotUsed>> actorRefEventSourcePair =
        actorRefPoweredEventSource.preMaterialize(_materializer);
    ActorRef eventSourceActor = actorRefEventSourcePair.first();
    Source<EventSource.Event, ?> eventSource = actorRefEventSourcePair.second();
    ActorRef myActor = _actorSystem.actorOf(MyActor.getProps(eventSourceActor), key);
    _myActors.put(key, myActor);
    _actorSystem.scheduler()
        .schedule(Duration.ofMinutes(0), Duration.ofSeconds(10), myActor, "Hello", _executionContext,
            ActorRef.noSender());
    return ok().chunked(eventSource.via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
  }
}
