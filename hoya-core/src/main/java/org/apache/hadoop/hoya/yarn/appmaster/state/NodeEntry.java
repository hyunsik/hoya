/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hoya.yarn.appmaster.state;

/**
 * Information about the state of a role on a specific node instance.
 * No fields are synchronized; sync on the instance to work with it
 *
 The two fields `releasing` and `requested` are used to track the ongoing
 state of YARN requests; they do not need to be persisted across freeze/thaw
 cycles. They may be relevant across AM restart, but without other data
 structures in the AM, not enough to track what the AM was up to before
 it was restarted. The strategy will be to ignore unexpected allocation
 responses (which may come from pre-restart) requests, while treating
 unexpected container release responses as failures.

 The `active` counter is only decremented after a container release response
 has been received.
 */
public class NodeEntry {

  /**
   * Number of active nodes. Active includes starting as well as live
   */
  public int active;
  /**
   * no of requests made of this role of this node. If it goes above
   * 1 there's a problem
   */
  public int requested;
  /**
   * No of instances in release state
   */
  public int releasing;

  /**
   * Time last used.
   */
  public long last_used;

  /**
   * Is the node available for assignments.
   * @return true if there are no outstanding requests or role instances here
   * other than some being released.
   */
  public boolean available() {
    return (active - releasing) == 0 && (requested == 0);
  }
}
