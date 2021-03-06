/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.controller.client;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

import org.jboss.as.protocol.StreamUtils;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class __ThrowawayClientTest {

    public static void main(String[] args) throws Exception {
        ModelControllerClient client = null;
        try {
            System.out.println("Connecting");
            client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999);
            System.out.println("Connected");

            {
                ModelNode request = new ModelNode();
                request.get("operation").set("read-resource");
                request.get("address").setEmptyList();
                // request.get("address").set("subsystem", "threads");
                request.get("recursive").set(true);
                ModelNode r = client.execute(request);
                System.out.println(r);
            }
            {
                ModelNode request = new ModelNode();
                request.get("operation").set("read-operation-description");
                request.get("address").setEmptyList();
                request.get("address").set("subsystem", "logging");
                request.get("recursive").set(true);
                request.get("name").set("add");
                ModelNode r = client.execute(request);
                System.out.println(r);
            }
            {
                // Read the resource including runtime stats
                ModelNode request = new ModelNode();
                request.get("operation").set("read-resource");
                request.get("address").add("subsystem", "web").add("connector", "http");
                request.get("include-runtime").set(true);
                ModelNode r = client.execute(request);
                System.out.println(r);
            }
            {
                ModelNode request = new ModelNode();
                request.get("operation").set("read-attribute");
                request.get("address").setEmptyList();
                request.get("address").add("subsystem", "web").add("connector", "http");
                request.get("name").set("requestCount");
                ModelNode r = client.execute(request);
                System.out.println(r);
            }

//            System.out.println("--- Synchronous simple operation");
//            ModelNode result = client.execute(createOperation(0));
//            System.out.println("Received synchronous result " + result);
//
//            System.out.println("--- Synchronous operation");
//            client.execute(createOperation(0), new TestResultHandler());
//
//
//            System.out.println("--- Asynchronous operation");
//            TestResultHandler handler = new TestResultHandler();
//            Cancellable operation = client.execute(createOperation(1000), handler);
//            System.out.println("Control returned to client");
//            handler.waitForFragment();
//            System.out.println("Fragment signalled");
//            handler.waitForComplete();
//
//            System.out.println("--- Asynchronous cancelled operation");
//            TestResultHandler handler2 = new TestResultHandler();
//            Cancellable operation2 = client.execute(createOperation(1000), handler2);
//            handler2.waitForFragment();
//            operation2.cancel();
//            handler2.waitForComplete();

        } finally {
            StreamUtils.safeClose(client);
            System.out.println("Closed");
        }
    }

    static ModelNode createOperation(long sleep) {
        ModelNode node = new ModelNode();
        node.get("operation").set("read-resource");
        node.get("address").add("subsystem", "threads");
        if (sleep > 0) {
            node.get("sleep").set(sleep);
        }
        System.out.println(node);
        return node;
    }

    static class TestResultHandler implements ResultHandler {

        CountDownLatch fragmentLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(1);
        boolean success;

        @Override
        public void handleResultFragment(String[] location, ModelNode result) {
            System.out.println("Handle fragment " + result);
            fragmentLatch.countDown();
        }

        @Override
        public void handleResultComplete(ModelNode compensatingOperation) {
            System.out.println("Handle complete " + compensatingOperation);
            success = true;
            completeLatch.countDown();
        }

        @Override
        public void handleCancellation() {
            System.out.println("Handle cancellation");
            completeLatch.countDown();
        }

        @Override
        public void handleException(Exception e) {
            System.out.println("Handle exception");
            completeLatch.countDown();
        }

        void waitForFragment() throws InterruptedException {
            fragmentLatch.await();
        }

        void waitForComplete() throws InterruptedException {
            completeLatch.await();
        }

    }
}
