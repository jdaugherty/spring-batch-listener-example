package com.example.demo

import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext

class FixedRangePartitioner implements Partitioner {
    /**
     * Return a fixed set of partitions with values 00, 01, 02, etc.
     * @param the gridSize should always equal 100 since this is a fixed range and always returns 100 partitions
     * @return
     */
    @Override
    Map<String, ExecutionContext> partition(int gridSize) {
        assert gridSize == 100 || gridSize == 1000 || gridSize == 10000, "The grid size for this partitioner must always be 100 (thread pool can be smaller)"

        Map<String, ExecutionContext> map = [:]
        gridSize.times {
            String prefix = it.toString().padLeft(gridSize.toString().length()-1, '0')
            String partitionName = "chunk:${prefix}"
            map[partitionName] = new ExecutionContext([partitionValue: prefix])
        }

        map
    }
}
