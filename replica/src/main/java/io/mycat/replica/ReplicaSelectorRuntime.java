/**
 * Copyright (C) <2019>  <chen junwen>
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */
package io.mycat.replica;

import io.mycat.MycatConfig;
import io.mycat.config.ClusterRootConfig;
import io.mycat.config.DatasourceRootConfig;
import io.mycat.plug.PlugRuntime;
import io.mycat.plug.loadBalance.LoadBalanceElement;
import io.mycat.plug.loadBalance.LoadBalanceStrategy;
import io.mycat.plug.loadBalance.SessionCounter;
import io.mycat.replica.heartbeat.DefaultHeartbeatFlow;
import io.mycat.replica.heartbeat.HeartBeatStrategy;
import io.mycat.replica.heartbeat.HeartbeatFlow;
import io.mycat.replica.heartbeat.strategy.MySQLGaleraHeartBeatStrategy;
import io.mycat.replica.heartbeat.strategy.MySQLMasterSlaveBeatStrategy;
import io.mycat.replica.heartbeat.strategy.MySQLSingleHeartBeatStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ReplicaSelectorRuntime {
    INSTANCE;
    final ConcurrentMap<String, ReplicaDataSourceSelector> map = new ConcurrentHashMap<>();
    final ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
    volatile ScheduledFuture<?> schedule;
    volatile MycatConfig config;

    public synchronized void load(MycatConfig config) {
        if (this.config == config) {
            return;
        }
        innerThis(config);
        this.config = config;
    }

    private void innerThis(MycatConfig config) {
        PlugRuntime.INSTCANE.load(config);

        ClusterRootConfig replicasRootConfig = config.getReplicas();
        Objects.requireNonNull(replicasRootConfig, "replica config can not found");

        List<ClusterRootConfig.ClusterConfig> replicaConfigList = replicasRootConfig.getReplicas();
        ClusterRootConfig.TimerConfig timerConfig = replicasRootConfig.getTimer();

        List<DatasourceRootConfig.DatasourceConfig> datasources = config.getDatasource().getDatasources();
        Map<String, DatasourceRootConfig.DatasourceConfig> datasourceConfigMap = datasources.stream().collect(Collectors.toMap(k -> k.getName(), v -> v));
        ////////////////////////////////////check/////////////////////////////////////////////////
        Objects.requireNonNull(replicaConfigList, "replica config can not be empty");
        ////////////////////////////////////check/////////////////////////////////////////////////

        for (ClusterRootConfig.ClusterConfig replicaConfig : replicaConfigList) {
            addCluster(datasourceConfigMap, replicaConfig);
        }
        updateTimer(config);
    }

    public synchronized void updateTimer(MycatConfig config) {
        /////////////////////////////////////////////////////////////////////////////////////////
        if (this.schedule != null) {
            schedule.cancel(false);
            schedule = null;
        }
        ClusterRootConfig.TimerConfig timerConfig = config.getReplicas().getTimer();
        this.schedule = this.timer.scheduleAtFixedRate(() -> {
            Stream<PhysicsInstanceImpl> stream = map.values().stream().flatMap(i -> i.datasourceMap.values().stream());
            stream.forEach(c -> {
                HeartbeatFlow heartbeatFlow = heartbeatDetectorMap.get(c.getName());
                if (heartbeatFlow == null) {
                    c.notifyChangeSelectRead(false);
                    c.notifyChangeAlive(false);
                } else {
                    heartbeatFlow.heartbeat();
                }
            });
        }, timerConfig.getInitialDelay(), timerConfig.getPeriod(), TimeUnit.valueOf(timerConfig.getTimeUnit()));
    }


    /////////////////////////////////////////public manager/////////////////////////////////////////////////////////////

    public void addCluster(MycatConfig config, ClusterRootConfig.ClusterConfig replicaConfig) {
        addCluster(config.getDatasource().getDatasources().stream().collect(Collectors.toMap(k -> k.getName(), v -> v)), replicaConfig);
    }

    public void removeCluster(String name) {
        map.remove(name);
    }

    public void addDatasource(ClusterRootConfig.ClusterConfig replicaConfig, String clusterName, DatasourceRootConfig.DatasourceConfig datasource) {
        boolean master = replicaConfig.getMasters().contains(datasource.getName());
        ReplicaDataSourceSelector replicaDataSourceSelector = map.get(clusterName);
        registerDatasource(master, replicaDataSourceSelector, datasource, null);
    }

    public void removeDatasource(String clusterName, String datasourceName) {
        ReplicaDataSourceSelector selector = map.get(clusterName);
        if (selector != null) {
            selector.unregister(datasourceName);
        }
    }

    public boolean notifySwitchReplicaDataSource(String replicaName) {
        ReplicaDataSourceSelector selector = map.get(replicaName);
        Objects.requireNonNull(selector);
        return selector.switchDataSourceIfNeed();
    }

    public void updateInstanceStatus(String replicaName, String dataSource, boolean alive,
                                     boolean selectAsRead) {
        ReplicaDataSourceSelector selector = map.get(replicaName);
        if (selector != null) {
            PhysicsInstanceImpl physicsInstance = selector.datasourceMap.get(dataSource);
            if (physicsInstance != null) {
                physicsInstance.notifyChangeAlive(alive);
                physicsInstance.notifyChangeSelectRead(selectAsRead);
            }
        }
    }

    public void updateInstanceStatus(String dataSource, boolean alive,
                                     boolean selectAsRead) {
        map.values().stream().flatMap(i -> i.datasourceMap.values().stream()).filter(i -> i.getName().equals(dataSource)).findFirst().ifPresent(physicsInstance -> {
            physicsInstance.notifyChangeAlive(alive);
            physicsInstance.notifyChangeSelectRead(selectAsRead);
        });
    }


///////////////////////////////////////private manager//////////////////////////////////////////////////////////////////////////

    private PhysicsInstance registerDatasource(boolean master, ReplicaDataSourceSelector selector,
                                               DatasourceRootConfig.DatasourceConfig datasourceConfig,
                                               SessionCounter sessionCounter) {
        Objects.requireNonNull(selector);
        InstanceType instanceType = InstanceType.READ;
        switch (selector.type) {
            case SINGLE_NODE:
            case MASTER_SLAVE:
                instanceType = master ? InstanceType.READ_WRITE : InstanceType.READ;
                break;
            case GARELA_CLUSTER:
                instanceType = master ? InstanceType.READ_WRITE : InstanceType.READ;
            case NONE:
                break;
        }
        if (datasourceConfig.getInstanceType() != null) {
            instanceType = InstanceType.valueOf(datasourceConfig.getInstanceType());
        }
        return registerDatasource(selector.name, datasourceConfig.getName(), instanceType,
                datasourceConfig.getWeight(), sessionCounter);
    }

    private void addCluster(Map<String, DatasourceRootConfig.DatasourceConfig> datasourceConfigMap, ClusterRootConfig.ClusterConfig replicaConfig) {
        String name = replicaConfig.getName();
        ReplicaType replicaType = ReplicaType.valueOf(replicaConfig.getReplicaType());
        BalanceType balanceType = BalanceType.valueOf(replicaConfig.getReadBalanceType());
        ReplicaSwitchType switchType = ReplicaSwitchType.valueOf(replicaConfig.getSwitchType());

        LoadBalanceStrategy readLB = PlugRuntime.INSTCANE
                .getLoadBalanceByBalanceName(replicaConfig.getReadBalanceName());
        LoadBalanceStrategy writeLB
                = PlugRuntime.INSTCANE
                .getLoadBalanceByBalanceName(replicaConfig.getWriteBalanceName());

        ReplicaDataSourceSelector selector = registerCluster(name, balanceType,
                replicaType, switchType, readLB, writeLB);

        registerDatasource(datasourceConfigMap, selector, replicaConfig.getMasters(), true);
        registerDatasource(datasourceConfigMap, selector, replicaConfig.getReplicas(), false);
    }

    private void registerDatasource(Map<String, DatasourceRootConfig.DatasourceConfig> datasourceConfigMap, ReplicaDataSourceSelector selector, List<String> datasourceNameList, boolean master) {
        if (datasourceNameList == null) {
            datasourceNameList = Collections.emptyList();
        }
        for (String datasourceName : datasourceNameList) {
            DatasourceRootConfig.DatasourceConfig datasource = datasourceConfigMap.get(datasourceName);
            registerDatasource(master, selector, datasource, null);
        }
    }

    private PhysicsInstance registerDatasource(String replicaName, String dataSourceName,
                                               InstanceType type,
                                               int weight, SessionCounter sessionCounter) {
        ReplicaDataSourceSelector sourceSelector = map.get(replicaName);
        Objects.requireNonNull(sourceSelector);
        PhysicsInstanceImpl instance = sourceSelector.register(dataSourceName, type, weight);
        if (sessionCounter != null) {
            instance.sessionCounters.add(sessionCounter);
        }
        return instance;
    }

    private ReplicaDataSourceSelector registerCluster(String replicaName, BalanceType balanceType,
                                                      ReplicaType type,
                                                      ReplicaSwitchType switchType, LoadBalanceStrategy readLB,
                                                      LoadBalanceStrategy writeLB) {
        return map.computeIfAbsent(replicaName,
                s -> new ReplicaDataSourceSelector(replicaName, balanceType, type, switchType, readLB,
                        writeLB));
    }
//////////////////////////////////////////public read///////////////////////////////////////////////////////////////////

    public PhysicsInstanceImpl getWriteDatasourceByReplicaName(String replicaName) {
        return getWriteDatasourceByReplicaName(replicaName, null);
    }

    public PhysicsInstanceImpl getDatasourceByReplicaName(String replicaName) {
        return getDatasourceByReplicaName(replicaName, null);
    }

    public PhysicsInstanceImpl getWriteDatasourceByReplicaName(String replicaName,
                                                               LoadBalanceStrategy balanceStrategy) {
        ReplicaDataSourceSelector selector = map.get(replicaName);
        return getDatasource(balanceStrategy, selector,
                selector.defaultWriteLoadBalanceStrategy, selector.getWriteDataSource());
    }

    public PhysicsInstanceImpl getWriteDatasource(LoadBalanceStrategy balanceStrategy,
                                                  ReplicaDataSourceSelector selector) {
        return getDatasource(balanceStrategy, selector, selector.defaultWriteLoadBalanceStrategy,
                selector.getWriteDataSource());
    }

    public PhysicsInstanceImpl getDatasource(LoadBalanceStrategy balanceStrategy,
                                             ReplicaDataSourceSelector selector) {
        return getDatasource(balanceStrategy, selector, selector.defaultReadLoadBalanceStrategy,
                selector.getDataSourceByLoadBalacneType());
    }

    public PhysicsInstanceImpl getDatasource(LoadBalanceStrategy balanceStrategy,
                                             ReplicaDataSourceSelector selector, LoadBalanceStrategy defaultWriteLoadBalanceStrategy,
                                             List writeDataSource) {
        Objects.requireNonNull(writeDataSource);
        Objects.requireNonNull(selector);
        if (balanceStrategy == null) {
            balanceStrategy = defaultWriteLoadBalanceStrategy;
        }
        LoadBalanceElement select = balanceStrategy.select(selector, writeDataSource);
        Objects.requireNonNull(select);
        return (PhysicsInstanceImpl) select;
    }

    public PhysicsInstanceImpl getDatasourceByReplicaName(String replicaName,
                                                          LoadBalanceStrategy balanceStrategy) {
        ReplicaDataSourceSelector selector = map.get(replicaName);
        return getDatasource(balanceStrategy, selector,
                selector.defaultReadLoadBalanceStrategy, selector.getDataSourceByLoadBalacneType());
    }

    public ReplicaDataSourceSelector getDataSourceSelector(String replicaName) {
        return map.get(replicaName);
    }
    ////////////////////////////////////////heartbeat///////////////////////////////////////////////////////////////////

    final ConcurrentMap<String, HeartbeatFlow> heartbeatDetectorMap = new ConcurrentHashMap<>();

    public synchronized void putHeartFlow(String replicaName, String datasourceName, Consumer<HeartBeatStrategy> executer) {
        MycatConfig config = this.config;
        Objects.requireNonNull(config);

        config.getReplicas().getReplicas().stream().filter(i -> replicaName.equals(i.getName())).findFirst().ifPresent(c -> {
            ClusterRootConfig.HeartbeatConfig heartbeat = c.getHeartbeat();
            ReplicaDataSourceSelector selector = map.get(replicaName);
            PhysicsInstanceImpl physicsInstance = selector.datasourceMap.get(datasourceName);
            DefaultHeartbeatFlow heartbeatFlow = new DefaultHeartbeatFlow(physicsInstance, replicaName, datasourceName,
                    heartbeat.getMaxRetry(), heartbeat.getMinSwitchTimeInterval(), heartbeat.getHeartbeatTimeout(),
                    ReplicaSwitchType.valueOf(c.getSwitchType()),
                    heartbeat.getSlaveThreshold(), getStrategyByReplicaType(c.getReplicaType()),
                    executer);

            heartbeatDetectorMap.put(replicaName + "." + datasourceName, heartbeatFlow);
        });
    }

    public void removeHeartFlow(String replicaName, String datasourceName) {
        heartbeatDetectorMap.remove(replicaName + "." + datasourceName);
    }

    private Function<HeartbeatFlow, HeartBeatStrategy> getStrategyByReplicaType(String replicaType) {
        Function<HeartbeatFlow, HeartBeatStrategy> strategyProvider;
        switch (ReplicaType.valueOf(replicaType)) {
            case MASTER_SLAVE:
                strategyProvider = MySQLMasterSlaveBeatStrategy::new;
                break;
            case GARELA_CLUSTER:
                strategyProvider = MySQLGaleraHeartBeatStrategy::new;
                break;
            case NONE:
            case SINGLE_NODE:
            default:
                strategyProvider = MySQLSingleHeartBeatStrategy::new;
                break;
        }
        return strategyProvider;
    }
}