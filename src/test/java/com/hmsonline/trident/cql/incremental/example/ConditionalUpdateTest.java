package com.hmsonline.trident.cql.incremental.example;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.hmsonline.trident.cql.CassandraCqlStateFactory;
import com.hmsonline.trident.cql.CqlClientFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test that demonstrates how to construct and use conditional updates.
 */
@RunWith(JUnit4.class)
public class ConditionalUpdateTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConditionalUpdateTest.class);

    private CqlClientFactory clientFactory;

    public ConditionalUpdateTest() {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put(CassandraCqlStateFactory.TRIDENT_CASSANDRA_CQL_HOSTS, "localhost");
        clientFactory = new CqlClientFactory(configuration);
    }

    public void executeAndAssert(Statement statement, String k, Integer expectedValue) {
        LOG.debug("EXECUTING [{}]", statement.toString());
        clientFactory.getSession().execute(statement);
        Select.Selection selection = QueryBuilder.select();
        selection.column("v");
        Select selectStatement = selection.from(SalesAnalyticsMapper.KEYSPACE_NAME, SalesAnalyticsMapper.TABLE_NAME);
        Clause clause = QueryBuilder.eq("k", k);
        selectStatement.where(clause);
        ResultSet results = clientFactory.getSession().execute(selectStatement);
        Integer actualValue = results.one().getInt("v");
        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConditionalUpdates() throws Exception {
        Update initialStatement = QueryBuilder.update(SalesAnalyticsMapper.KEYSPACE_NAME,
                SalesAnalyticsMapper.TABLE_NAME);
        initialStatement.with(QueryBuilder.set("v", 10));
        Clause clause = QueryBuilder.eq("k", "DE");
        initialStatement.where(clause);
        this.executeAndAssert(initialStatement, "DE", 10);

        // Now let's conditionally update where it is true
        Update updateStatement = QueryBuilder.update(SalesAnalyticsMapper.KEYSPACE_NAME,
                SalesAnalyticsMapper.TABLE_NAME);
        updateStatement.with(QueryBuilder.set("v", 15));
        updateStatement.where(clause);
        Clause conditionalClause = QueryBuilder.eq("v", 10);
        updateStatement.onlyIf(conditionalClause);
        this.executeAndAssert(updateStatement, "DE", 15);

        // Now let's conditionally update where it is false
        Update conditionalStatement = QueryBuilder.update(SalesAnalyticsMapper.KEYSPACE_NAME,
                SalesAnalyticsMapper.TABLE_NAME);
        conditionalStatement.with(QueryBuilder.set("v", 20));
        conditionalStatement.where(clause);
        conditionalStatement.onlyIf(conditionalClause);
        this.executeAndAssert(conditionalStatement, "DE", 15);
    }
}
