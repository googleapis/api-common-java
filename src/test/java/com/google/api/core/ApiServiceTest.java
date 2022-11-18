/*
 * Copyright 2017 Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.google.api.core;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;

public class ApiServiceTest {
  @Test
  public void testNoopService() {
    ApiService service =
        new AbstractApiService() {
          @Override
          protected void doStop() {
            notifyStopped();
          }

          @Override
          protected void doStart() {
            notifyStarted();
          }
        };
    service.startAsync().awaitRunning();
    Assert.assertTrue(service.isRunning());
    service.stopAsync().awaitTerminated();
  }

  @Test
  public void testFailingService() {
    final AtomicReference<Throwable> savedFailure = new AtomicReference<>();
    ApiService service =
        new AbstractApiService() {
          @Override
          protected void doStop() {
            // This should never be called.
            throw new Error();
          }

          @Override
          protected void doStart() {
            notifyFailed(new IllegalStateException("this service always fails"));
          }
        };
    service.addListener(
        new ApiService.Listener() {
          @Override
          public void failed(ApiService.State from, Throwable failure) {
            savedFailure.set(failure);
          }
        },
        MoreExecutors.directExecutor());

    try {
      service.startAsync().awaitRunning();
      throw new RuntimeException("unreachable");
    } catch (IllegalStateException e) {
      // Expected
    }

    Assert.assertEquals(service.state(), ApiService.State.FAILED);
    Assert.assertEquals(savedFailure.get().getMessage(), "this service always fails");
    Assert.assertEquals(service.failureCause().getMessage(), "this service always fails");
  }
}
