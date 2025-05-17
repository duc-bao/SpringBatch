package com.ducbao.demotest.config;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PersonPartition implements Partitioner {

    private final int TOTAL_RECORDS = 10000;
    private final int GRID_SIZE = 4;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();
        int range = TOTAL_RECORDS / GRID_SIZE;
        int fromId = 0;
        int toId = range;

        for (int i = 1; i <= GRID_SIZE; i++) {
            ExecutionContext executionContext = new ExecutionContext();

            // Last partition may have extra records
            if (i == GRID_SIZE) {
                toId = TOTAL_RECORDS;
            }

            executionContext.putInt("fromId", fromId);
            executionContext.putInt("toId", toId);
            executionContext.putInt("partition", i);

            // Add execution context with unique name
            result.put("partition" + i, executionContext);

            // Update range for next iteration
            fromId = toId;
            toId += range;
        }

        return result;
    }
}
