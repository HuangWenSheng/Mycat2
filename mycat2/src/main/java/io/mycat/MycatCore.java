/**
 * Copyright (C) <2019>  <chen junwen>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */
package io.mycat;

import io.mycat.proxy.AsyncTaskCallBack;
import io.mycat.proxy.AsyncTaskCallBackCounter;
import io.mycat.proxy.ProxyRuntime;
import io.mycat.proxy.session.MySQLClientSession;
import io.mycat.proxy.session.Session;
import io.mycat.proxy.task.client.MySQLTaskUtil;
import io.mycat.proxy.task.client.QueryUtil;
import io.mycat.proxy.task.client.resultset.QueryResultSetCollector;
import io.mycat.replica.DefaultMySQLReplicaFactory;
import io.mycat.replica.MySQLDataSourceEx;
import io.mycat.replica.MySQLDatasource;
import io.mycat.replica.MySQLReplica;
import io.mycat.router.MycatRouter;
import io.mycat.router.MycatRouterConfig;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author cjw
 **/
public class MycatCore {


  public static void main(String[] args) throws Exception {
    ProxyRuntime runtime = ProxyRuntime.INSTANCE;
    runtime.loadMycat();
    runtime.loadProxy();

    MycatRouterConfig routerConfig = new MycatRouterConfig(ProxyRuntime.getResourcesPath());
    MycatRouter router = new MycatRouter(routerConfig);
    runtime.initReactor(() -> new MycatCommandHandler(router), new AsyncTaskCallBack() {
      @Override
      public void finished(Session session, Object sender, boolean success, Object result,
          Object attr) {
        runtime.initRepliac(new DefaultMySQLReplicaFactory(), new AsyncTaskCallBack() {
          @Override
          public void finished(Session session, Object sender, boolean success, Object result,
              Object attr) {
            try {
              runtime.initDataNode();

              getReplicaMetaData(runtime, new AsyncTaskCallBack() {
                @Override
                public void finished(Session session, Object sender, boolean success, Object result,
                    Object attr) {
                  if (success) {
                    for (MySQLReplica mySQLReplica : ProxyRuntime.INSTANCE.getMySQLReplicaList()) {
                      Map<String, Map<String, Set<String>>> metaData = mySQLReplica.getMetaData();

                    }

                    try {
                      runtime.initAcceptor();
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  } else {
                    System.out.println("fail");
                  }
                }
              });

              ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
              service.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                  Collection<MySQLDataSourceEx> datasourceList = runtime.getMySQLDatasourceList();
                  for (MySQLDataSourceEx datasource : datasourceList) {
                    datasource.heartBeat();
                  }
                }
              }, 0, 10, TimeUnit.SECONDS);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
      }
    });

  }

  public static void getReplicaMetaData(ProxyRuntime runtime, AsyncTaskCallBack asyncTaskCallBack) {
    Collection<MySQLReplica> mySQLReplicaList = runtime.getMySQLReplicaList();
    AsyncTaskCallBackCounter counter = new AsyncTaskCallBackCounter(mySQLReplicaList.size(),
        asyncTaskCallBack);
    for (MySQLReplica mySQLReplica : mySQLReplicaList) {
      MySQLDatasource master = mySQLReplica.getMaster();
      MySQLTaskUtil.getMySQLSessionForHeatbeat(master,
          new AsyncTaskCallBack<MySQLClientSession>() {
            @Override
            public void finished(MySQLClientSession session, Object sender,
                boolean success,
                Object result, Object attr) {
              QueryUtil.showInformationSchemaColumns(session,
                  new AsyncTaskCallBack<MySQLClientSession>() {
                    @Override
                    public void finished(MySQLClientSession session, Object sender,
                        boolean success, Object result, Object attr) {
                      if (success) {
                        session.getSessionManager().addIdleSession(session);
                        QueryResultSetCollector collector = (QueryResultSetCollector) result;
                        for (Object[] objects : collector) {
                          String TABLE_SCHEMA = (String) objects[1];
                          String TABLE_NAME = (String) objects[2];
                          String COLUMN_NAME = (String) objects[3];
                          Object CHARACTER_OCTET_LENGTH = objects[9];
                          Object COLUMN_TYPE = objects[15];
                          mySQLReplica
                              .addMetaData(TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME);
                        }
                        counter.finished(session, sender, success, result, attr);
                      } else {
                        counter.finished(session, sender, success, result, attr);
                      }
                    }
                  });
            }
          });
    }
  }

}
