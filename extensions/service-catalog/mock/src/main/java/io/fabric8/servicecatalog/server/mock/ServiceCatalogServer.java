/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.servicecatalog.server.mock;

import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.ServerRequest;
import io.fabric8.mockwebserver.ServerResponse;
import io.fabric8.mockwebserver.dsl.MockServerExpectation;
import io.fabric8.servicecatalog.client.ServiceCatalogClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.rules.ExternalResource;

import java.util.HashMap;
import java.util.Queue;

public class ServiceCatalogServer extends ExternalResource {

  protected ServiceCatalogMockServer mock;
  private ServiceCatalogClient client;

  private boolean https;
  // In this mode the mock web server will store, read, update and delete
  // kubernetes resources using an in memory map and will appear as a real api
  // server.
  private boolean crudMode;

  public ServiceCatalogServer() {
    this(true, false);
  }

  public ServiceCatalogServer(boolean https) {
    this(https, false);
  }

  public ServiceCatalogServer(boolean https, boolean crudMode) {
    this.https = https;
    this.crudMode = crudMode;
  }

  public void before() {
    mock = crudMode
      ? new ServiceCatalogMockServer(new Context(), new MockWebServer(), new HashMap<ServerRequest, Queue<ServerResponse>>(), new KubernetesCrudDispatcher(), true)
      : new ServiceCatalogMockServer(https);
    mock.init();
    client = mock.createServiceCatalog();
  }

  public void after() {
    mock.destroy();
    client.close();
  }


  public ServiceCatalogClient getServiceCatalogClient() {
    return client;
  }


  public MockServerExpectation expect() {
    return mock.expect();
  }

  @Deprecated
  public <T> void expectAndReturnAsJson(String path, int code, T body) {
    expect().withPath(path).andReturn(code, body).always();
  }

  @Deprecated
  public void expectAndReturnAsString(String path, int code, String body) {
    expect().withPath(path).andReturn(code, body).always();
  }

  public MockWebServer getMockServer() {
    return mock.getServer();
  }
}
