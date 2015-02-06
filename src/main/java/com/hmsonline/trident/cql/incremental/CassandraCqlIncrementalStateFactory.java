package com.hmsonline.trident.cql.incremental;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.CombinerAggregator;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import backtype.storm.task.IMetricsContext;

import com.hmsonline.trident.cql.CqlClientFactory;

@SuppressWarnings("rawtypes")
// TODO: Is it worth subclassing from CassandraCqlStateFactory?
public class CassandraCqlIncrementalStateFactory<K, V> implements StateFactory {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CassandraCqlIncrementalStateFactory.class);
    private CqlClientFactory clientFactory;
    private CombinerAggregator<V> aggregator;
    private CqlIncrementMapper<K, V> mapper;

    public CassandraCqlIncrementalStateFactory(CombinerAggregator<V> aggregator, CqlIncrementMapper<K, V> mapper, CqlClientFactory clientFactory) {
        this.aggregator = aggregator;
        this.mapper = mapper;
        this.clientFactory = clientFactory;
    }

    @Override
    public State makeState(Map configuration, IMetricsContext metrics, int partitionIndex, int numPartitions) {
        LOG.debug("Creating State for partition [{}] of [{}]", new Object[]{partitionIndex, numPartitions});
        return new CassandraCqlIncrementalState<K, V>(clientFactory, aggregator, mapper, partitionIndex);
    }
}
