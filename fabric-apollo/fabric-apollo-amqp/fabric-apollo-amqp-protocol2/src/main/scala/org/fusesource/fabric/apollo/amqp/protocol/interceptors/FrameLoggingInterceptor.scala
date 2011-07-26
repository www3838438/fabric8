/*
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved
 *
 *    http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license, a copy of which has been included with this distribution
 * in the license.txt file
 */

package org.fusesource.fabric.apollo.amqp.protocol.interceptors

import org.apache.activemq.apollo.util.Logging
import org.fusesource.fabric.apollo.amqp.codec.interfaces.AMQPFrame
import org.fusesource.fabric.apollo.amqp.protocol.interfaces.Interceptor
import scala.collection.mutable.Queue

/**
 * 
 */
class FrameLoggingInterceptor(prefix:String = "") extends Interceptor with Logging {  
  
  def send(frame:AMQPFrame, tasks:Queue[() => Unit]) = {
    info("%s:send{frame=%s tasks=%s", prefix, frame, tasks)
    outgoing.send(frame, tasks)
  }
  
  def receive(frame:AMQPFrame, tasks:Queue[() => Unit]) = {
    info("%s:receive{frame=%s tasks=%s", prefix, frame, tasks)
    incoming.receive(frame, tasks)
  }

}
