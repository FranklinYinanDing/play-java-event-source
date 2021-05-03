package controllers;

import akka.actor.*;
import java.util.concurrent.atomic.AtomicInteger;
import play.libs.EventSource;


public class MyActor extends AbstractActor {

  public static Props getProps(final ActorRef eventSourceActor) {
    return Props.create(MyActor.class, eventSourceActor);
  }

  private final ActorRef _eventSourceActor;

  private final AtomicInteger _atomicInteger;

  public MyActor(final ActorRef eventSourceActor) {
    _eventSourceActor = eventSourceActor;
    getContext().watch(_eventSourceActor);
    _atomicInteger = new AtomicInteger(0);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(String.class, hello -> {
      if (_atomicInteger.incrementAndGet() < 5) {
        _eventSourceActor.tell(EventSource.Event.event(hello), self());
      } else {
        System.out.println("No more");
      }
    }).match(Terminated.class, t -> t.actor().equals(_eventSourceActor), t -> System.out.println("Terminated")).build();
  }
}
