package com.devlambda.proto.modbus;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.devlambda.meta.modbus.Slave;
import com.devlambda.meta.modbus.ap.MBAP;
import com.devlambda.meta.protocol.Connection;
import com.devlambda.meta.protocol.Stream;


/**
 * 
 * SlaveTest
 *
 * @author Dewald Pretorius
 */
@RunWith(MockitoJUnitRunner.class)
public class SlaveTest {

   @Test
   @SuppressWarnings("unchecked")
   public void testSlave() {
      Connection connection = mock(Connection.class);
      Stream<MBAP> stream = mock(Stream.class);
      long deviceAddress = 3;
      Slave.ResponseFactory<MBAP> responseFactory = Slave.FACTORY_MBAP;
      Slave<MBAP> slave = new Slave<>(connection, stream, deviceAddress, responseFactory);

      assertNotNull(slave);
   }
}
