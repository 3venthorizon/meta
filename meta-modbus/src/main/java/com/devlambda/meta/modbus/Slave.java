package com.devlambda.meta.modbus;


import java.util.HashMap;
import java.util.Map;

import com.devlambda.eventhorizon.Observer;
import com.devlambda.meta.modbus.ap.MBAP;
import com.devlambda.meta.modbus.serial.rtu.RTU;
import com.devlambda.meta.protocol.Connection;
import com.devlambda.meta.protocol.Protocol;
import com.devlambda.meta.protocol.Stream;


/**
 * Slave
 *
 * @author Dewald Pretorius
 */
@SuppressWarnings("hiding")
public class Slave<ADU extends com.devlambda.meta.modbus.ADU> extends Protocol<ADU> {

   public static final int CODE_UNSUPPORTED = 0x01;
   public static final int CODE_ERROR = 0x04;

   public interface Command {
      int getFunctionCode();

      PDU execute(PDU command) throws Exception;
   }

   public interface ResponseFactory<ADU extends com.devlambda.meta.modbus.ADU> {
      ADU createResponse(ADU request, PDU response);
   }

   public static final ResponseFactory<MBAP> FACTORY_MBAP = (request, result) -> {
      return new MBAP(request.getDeviceAddress(), request.getTransactionId(), request.getProtocolId(), result);
   };

   public static final ResponseFactory<RTU> FACTORY_RTU = (request, result) -> {
      return new RTU(request.getDeviceAddress(), result);
   };

   protected final long deviceAddress;
   protected final Map<Integer, Command> commands;
   protected final PDUFactory.Slave pduFactory;
   protected final ResponseFactory<ADU> responseFactory;

   public Slave(Connection connection, Stream<ADU> stream, long deviceAddress, ResponseFactory<ADU> responseFactory) {
      super(connection, stream);

      this.deviceAddress = deviceAddress;
      this.responseFactory = responseFactory;
      pduFactory = new PDUFactory.Slave();
      commands = new HashMap<>();
      Observer<Protocol<ADU>, ADU> masterRequest = (slave, request) -> processCommand(request);

      readEvent.add(masterRequest);
   }

   public void registerCommand(Command slaveCommand) {
      commands.put(slaveCommand.getFunctionCode(), slaveCommand);
   }

   protected void processCommand(ADU request) {
      if (this.deviceAddress != request.getDeviceAddress()) return;

      PDU command = request.getPDU();
      Integer function = command.getFunctionCode();
      Command slaveCommand = commands.get(function);

      if (slaveCommand == null) {
         PDU unsupportedCommand = pduFactory.error(function, CODE_UNSUPPORTED);
         ADU response = responseFactory.createResponse(request, unsupportedCommand);
         send(response);
         return;
      }

      try {
         PDU result = slaveCommand.execute(command);
         ADU response = responseFactory.createResponse(request, result);
         send(response);
      } catch (Exception e) {
         PDU commandError = pduFactory.error(function, CODE_ERROR);
         ADU response = responseFactory.createResponse(request, commandError);
         send(response);
      }
   }
}
