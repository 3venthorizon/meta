package com.devlambda.meta.persistence;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MpaTest {

   MPA metaRepo = spy(new MPA(null));

   @Test
   public void testCreateBlob() throws SQLException {
      Connection connection = mock(Connection.class);
      Blob blob = mock(Blob.class);
      byte[] binary = new byte[8];

      doReturn(connection).when(metaRepo).connection();
      when(connection.createBlob()).thenReturn(blob);

      Blob result = metaRepo.createBlob(binary);

      assertEquals(blob, result);

      verify(connection).createBlob();
      verify(blob).setBytes(1, binary);
   }

   @Test
   public void testExtractBinary() throws SQLException {
      Blob blob = mock(Blob.class);
      byte[] binary = new byte[8];

      when(blob.length()).thenReturn((long) binary.length);
      when(blob.getBytes(1, binary.length)).thenReturn(binary);

      byte[] result = metaRepo.extractBinary(blob);

      assertEquals(binary, result);
   }

   @Test
   public void testConnection() throws SQLException {
      Connection connection = mock(Connection.class);
      metaRepo.connector = () -> connection;

      Connection result = metaRepo.connection(); //test
      assertEquals(connection, result);

      verifyZeroInteractions(connection);
   }
}
