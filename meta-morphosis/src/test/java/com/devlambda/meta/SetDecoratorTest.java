package com.devlambda.meta;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.Set;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SetDecoratorTest extends CollectionDecoratorTest {
   protected @Mock Set<Integer> set;
   
   private SetDecorator<Integer> decorator;

   @Override
   public void setUp() throws Exception { 
      super.collection = set;
      super.iterable = set;
      decorator = spy(new SetDecorator<>(set));
      assertEquals(set, decorator.decorated);
      
      super.setUp();
   }
}
