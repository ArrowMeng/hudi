/*
 * Copyright (c) 2016 Uber Technologies, Inc. (hoodie-dev-group@uber.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uber.hoodie.common.model;

import com.google.common.base.Objects;
import java.io.Serializable;

/**
 * Location of a HoodieRecord within the parition it belongs to. Ultimately, this points to an
 * actual file on disk
 */
public class HoodieRecordLocation implements Serializable {

  private final String commitTime;
  private final String fileId;

  public HoodieRecordLocation(String commitTime, String fileId) {
    this.commitTime = commitTime;
    this.fileId = fileId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HoodieRecordLocation otherLoc = (HoodieRecordLocation) o;
    return Objects.equal(commitTime, otherLoc.commitTime) &&
        Objects.equal(fileId, otherLoc.fileId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(commitTime, fileId);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("HoodieRecordLocation {");
    sb.append("commitTime=").append(commitTime).append(", ");
    sb.append("fileId=").append(fileId);
    sb.append('}');
    return sb.toString();
  }

  public String getCommitTime() {
    return commitTime;
  }

  public String getFileId() {
    return fileId;
  }
}
