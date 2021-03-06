/*
 *  Copyright (c) 2016 Uber Technologies, Inc. (hoodie-dev-group@uber.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.uber.hoodie.io.compact.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uber.hoodie.common.model.HoodieDataFile;
import com.uber.hoodie.common.model.HoodieLogFile;
import com.uber.hoodie.common.util.FSUtils;
import com.uber.hoodie.config.HoodieWriteConfig;
import com.uber.hoodie.io.compact.CompactionOperation;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CompactionStrategy which looks at total IO to be done for the compaction (read + write) and
 * limits the list of compactions to be under a configured limit on the IO
 *
 * @see CompactionStrategy
 */
public class BoundedIOCompactionStrategy implements CompactionStrategy {

  public static final String TOTAL_IO_READ_MB = "TOTAL_IO_READ_MB";
  public static final String TOTAL_IO_WRITE_MB = "TOTAL_IO_WRITE_MB";
  public static final String TOTAL_IO_MB = "TOTAL_IO_MB";

  @Override
  public Map<String, Object> captureMetrics(HoodieDataFile dataFile, String partitionPath,
      List<HoodieLogFile> logFiles) {
    Map<String, Object> metrics = Maps.newHashMap();
    // Total size of all the log files
    Long totalLogFileSize = logFiles.stream().map(HoodieLogFile::getFileSize).filter(
        Optional::isPresent).map(Optional::get).reduce(
        (size1, size2) -> size1 + size2).orElse(0L);
    // Total read will be the base file + all the log files
    Long totalIORead = FSUtils.getSizeInMB(dataFile.getFileSize() + totalLogFileSize);
    // Total write will be similar to the size of the base file
    Long totalIOWrite = FSUtils.getSizeInMB(dataFile.getFileSize());
    // Total IO will the the IO for read + write
    Long totalIO = totalIORead + totalIOWrite;
    // Save these metrics and we will use during the filter
    metrics.put(TOTAL_IO_READ_MB, totalIORead);
    metrics.put(TOTAL_IO_WRITE_MB, totalIOWrite);
    metrics.put(TOTAL_IO_MB, totalIO);
    return metrics;

  }

  @Override
  public List<CompactionOperation> orderAndFilter(HoodieWriteConfig writeConfig,
      List<CompactionOperation> operations) {
    // Iterate through the operations in order and accept operations as long as we are within the IO limit
    // Preserves the original ordering of compactions
    List<CompactionOperation> finalOperations = Lists.newArrayList();
    long targetIORemaining = writeConfig.getTargetIOPerCompactionInMB();
    for (CompactionOperation op : operations) {
      long opIo = (Long) op.getMetrics().get(TOTAL_IO_MB);
      targetIORemaining -= opIo;
      finalOperations.add(op);
      if (targetIORemaining <= 0) {
        return finalOperations;
      }
    }
    return finalOperations;
  }
}
