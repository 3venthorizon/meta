package com.devlambda.meta.protocol;


import java.util.HashSet;
import java.util.Set;

import com.devlambda.eventhorizon.Observer;


/**
 * Slave {@link Protocol}.
 */
@SuppressWarnings("hiding")
public class Slave<Packet extends com.devlambda.meta.protocol.Packet> extends Protocol<Packet> {

   public interface Command<Packet> {
      boolean isSupported(Packet command);

      Packet execute(Packet command);
   }

   protected Set<Command<Packet>> commands;

   public Slave(Connection connection, Stream<Packet> stream) {
      super(connection, stream);

      commands = new HashSet<>();
      Observer<Protocol<Packet>, Packet> masterRequest = (master, request) -> processCommand(request);

      readEvent.add(masterRequest);
   }

   public void registerCommand(Command<Packet> command) {
      commands.add(command);
   }

   protected void processCommand(Packet request) {
      Command<Packet> processor = commands.stream().filter(command -> command.isSupported(request)).findFirst()
            .orElseThrow(() -> new ProtocolException("Slave Command not found for Request"));
      Packet response = processor.execute(request);

      send(response);
   }
}
