/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.hadoop.hoya.yarn;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
import org.apache.hadoop.hoya.tools.HoyaUtils;
import org.apache.hadoop.hoya.tools.PathArgumentConverter;
import org.apache.hadoop.hoya.tools.URIArgumentConverter;
import org.apache.hadoop.hoya.yarn.appmaster.EnvMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the common argument set for all tne entry points,
 * and the core parsing logic to verify that the action is on the list
 * of allowed actions -and that the remaining number of arguments is
 * in the range allowed
 */

public class CommonArgs {

  public static final String ARG_ACTION = "--action";
  public static final String ARG_CONFDIR = "--confdir";
  public static final String ARG_DEBUG = "--debug";
  public static final String ARG_FILESYSTEM = "--fs";
  public static final String ARG_GENERATED_CONFDIR = "--generated_confdir";
  public static final String ARG_HBASE_HOME = "--hbasehome";
  public static final String ARG_HBASE_ZKPATH = "--hbasezkpath";
  public static final String ARG_HELP = "--help";
  public static final String ARG_IMAGE = "--image";
  public static final String ARG_MANAGER = "--manager";
  public static final String ARG_MASTERS = "--masters";
  public static final String ARG_MASTER_HEAP = "--masterheap";
  public static final String ARG_NAME = "--name";
  public static final String ARG_OUTPUT = "--output";
  public static final String ARG_PATH = "--path";
  public static final String ARG_USER = "--user";
  public static final String ARG_WORKERS = "--min";
  public static final String ARG_WORKER_HEAP = "--workerheap";

  public static final String ARG_ZKPORT = "--zkport";
  public static final String ARG_ZKQUORUM = "--zkhosts";

  public static final String ARG_X_TEST = "--Xtest";
  /** for testing only: {@value} */
  public static final String ARG_X_HBASE_MASTER_COMMAND = "--Xhbase-master-command";



  public static final String ERROR_NO_ACTION = "No action specified";
  public static final String ERROR_UNKNOWN_ACTION = "Unknown command: ";
  public static final String ERROR_NOT_ENOUGH_ARGUMENTS = "Not enough arguments for action: ";
  /**
   * All the remaining values after argument processing
   */
  public static final String ERROR_TOO_MANY_ARGUMENTS = "Too many arguments for action: ";

  /**
   * Actions.
   * Only some of these are supported by specific Hoya Services; they
   * are listed in the common args to ensure the names are consistent
   */
  public static final String ACTION_ADDNODE = "addnode";
  public static final String ACTION_CREATE = "create";
  public static final String ACTION_DESTROY = "destroy";
  public static final String ACTION_GETSIZE = "getsize";
  public static final String ACTION_FLEX = "flex";
  public static final String ACTION_GETCONF = "getconf";
  public static final String ACTION_HELP = "help";
  public static final String ACTION_EXISTS = "exists";
  public static final String ACTION_LIST = "list";

  public static final String ACTION_MIGRATE = "migrate";
  public static final String ACTION_PREFLIGHT = "preflight";
  public static final String ACTION_RECONFIGURE = "reconfigure";
  public static final String ACTION_REIMAGE = "reimage";
  public static final String ACTION_RMNODE = "rmnode";
  public static final String ACTION_START = "start";
  public static final String ACTION_STATUS = "status";
  public static final String ACTION_STOP = "stop";

  protected static final Logger log = LoggerFactory.getLogger(CommonArgs.class);

  @Parameter
  public List<String> parameters = new ArrayList<String>();

  @Parameter(names = "--debug", description = "Debug mode")
  public boolean debug = false;

  /**
   *    Declare the image configuration directory to use when creating or reconfiguring a hoya cluster. The path must be on a filesystem visible to all nodes in the YARN cluster.
   Only one configuration directory can be specified.
   */
  @Parameter(names = "--confdir",
      description = "path cluster configuration directory in HDFS",
      converter = PathArgumentConverter.class)
  Path confdir;

  @Parameter(names = "--fs", description = "filesystem URI",
      converter = URIArgumentConverter.class)
  URI filesystemURL;

  @Parameter(names = "--hbasehome",
      description = "HBase home dir for starting pre-installed binaries")
  public String hbasehome;

  @Parameter(names = "--hbasezkpath",
      description = "HBase Zookeeper path")
  public String hbasezkpath;

  @Parameter(names = "--help", help = true)
  public boolean help;

  //TODO: do we need this?
  @Parameter(names = "--rm",
      description = "Resource manager hostname:port ",
      required = false)
  public String rmAddress;

  @Parameter(names = "--Xtest", description = "Test mode")
  public boolean xTest = false;

  @Parameter(names = "--user",
      description = "username if not self")
  public String user = System.getProperty("user.name");

  @Parameter(names = "--zkhosts",
      description = "Zookeeper connection string")
  public String zkhosts;

  @Parameter(names = "--zkport",
      description = "Zookeeper port")
  public int zkport = EnvMappings.HBASE_ZK_PORT;

  /*
   -D name=value

   Define an HBase configuration option which overrides any options in
    the configuration XML files of the image or in the image configuration
     directory. The values will be persisted.
      Configuration options are only passed to the cluster when creating or reconfiguring a cluster.

   */

  @Parameter(names = "-D", description = "Definitions")
  public List<String> definitions = new ArrayList<String>();
  public Map<String, String> definitionMap = new HashMap<String, String>();


  @Parameter(names = {"--m", "--manager"},
      description = "hostname:port of the YARN resource manager")
  String manager;

  @Parameter(names = {"--workers", "--min"}, description = "number of worker nodes")
  public int workers = 0;

  @Parameter(names = {"--masters"}, description = "number of master nodes")
  public int masters = 1;

  @Parameter(names = {"--masterheap"},
      description = "Master heap size in MB")
  public int masterHeap = 128;

  //--masterinfoport [port]
  @Parameter(names = "--masterinfoport",
      description = "The web UI port that the Master should bind to")
  public int masterInfoPort = 0;

  //--workerinfoport [port]
  @Parameter(names = "--workerinfoport",
      description = "The web UI port that the Workers should bind to")
  public int workerInfoPort = 0;

  @Parameter(names = "--max",
      description = "(ignored argument)")
  public int max = -1;

  @Parameter(names = {"-o", "--output"},
      description = "output file for the configuration data")
  public String output;

  @Parameter(names = {"--workerheap", "--regionserverheap"},
      description = "Worker heap size in MB")
  public int workerHeap = 256;

  @Parameter(names = "--Xhbase-master-command",
      description = "testing only: hbase command to exec on the master")
  public String xHBaseMasterCommand = null;

  /**
   * fields
   */
  public JCommander commander;
  public String action;
  //action arguments; 
  public List<String> actionArgs;
  public final String[] args;

  /**
   * create a 3-tuple
   * @param msg
   * @param min
   * @param max
   * @return
   */
  protected static List<Object> t(String msg, int min, int max) {
    List<Object> l = new ArrayList<Object>(3);
    l.add(msg);
    l.add(min);
    l.add(max);
    return l;
  }

  /**
   * Create a tuple
   * @param msg
   * @param min
   * @return
   */
  protected static List<Object> t(String msg, int min) {
    return t(msg, min , min );
  }
  
  /**
   * get the name: relies on arg 1 being the cluster name in all operations 
   * @return the name argument, null if there is none
   */
  public String getClusterName() {
    return (actionArgs == null || actionArgs.isEmpty() || args.length < 2) ?
      null 
    : args[1];
  }

  public CommonArgs(String[] args) {
    this.args = args;
    commander = new JCommander(this);
  }

  public CommonArgs(Collection args) {
    List<String> argsAsStrings = HoyaUtils.collectionToStringList(args);
    this.args = argsAsStrings.toArray(new String[argsAsStrings.size()]);
    commander = new JCommander(this);
  }


  public String usage() {
    StringBuilder builder = new StringBuilder("\n");
    commander.usage(builder, "  ");
    builder.append("\nactions: ");
    Map<String, List<Object>> actions = getActions();
    for (String key:actions.keySet()) {
      builder.append(key).append(" ");
    }
    return builder.toString();
  }

  public void parse() throws BadCommandArgumentsException {
    try {
      commander.parse(args);
    } catch (ParameterException e) {
      throw new BadCommandArgumentsException(e.toString() +
                                             " with " + HoyaUtils.join(actionArgs, " "),
                                             e);
    }
  }

  /**
   * Map of supported actions to (description, #of args following)
   * format is of style:
   * <pre>
   *   (ACTION_CREATE): ["create cluster", 1],
   * </pre>
   * @return
   */
  public Map<String, List<Object>> getActions() {
    return Collections.emptyMap();
  }

/**
 * validate args via {@link #validate()}
 * then postprocess the arguments
 */
  public void postProcess() throws BadCommandArgumentsException {
    validate();
    for (String prop:definitions) {
      String[] keyval = prop.split("=", 2);
      if (keyval.length == 2) {
        definitionMap.put(keyval[0],keyval[1]);
      }
    }
  }

  /**
   * Validate the arguments against the action requested
   */
  public void validate() throws BadCommandArgumentsException {
    if (parameters.isEmpty()) {
      throw new BadCommandArgumentsException(ERROR_NO_ACTION
                                                 + " in " + HoyaUtils.join(actionArgs, " ")
                                                 + usage());
    }
    action = parameters.get(0);
    log.debug("action={}",action);
    Map<String, List<Object>> actionMap = getActions();
    List<Object> actionOpts = actionMap.get(action);
    if (null == actionOpts) {
      throw new BadCommandArgumentsException(ERROR_UNKNOWN_ACTION
                                                 + action
                                                 + " in " + HoyaUtils.join(actionArgs, " ")
                                                 + usage());
    }
    actionArgs = parameters.subList(1, parameters.size());

    int minArgs = (Integer) actionOpts.get(1);
    int actionArgSize = actionArgs.size();
    log.debug("Action {} expected #args={} actual #args={}", action, minArgs, actionArgSize);
    if (minArgs > actionArgSize) {
      throw new BadCommandArgumentsException(ERROR_NOT_ENOUGH_ARGUMENTS + action
                                                 + " in " + HoyaUtils.join(actionArgs, " "));
    }
    int maxArgs = (actionOpts.size() == 3) ? ((Integer) actionOpts.get(2)) : minArgs;
    if (actionArgSize > maxArgs) {
      throw new BadCommandArgumentsException(ERROR_TOO_MANY_ARGUMENTS + action
                                                 + " in " + HoyaUtils.join(actionArgs, " "));
    }
  }

  /**
   * Apply all the definitions on the command line to the configuration
   * @param conf config
   */
  public void applyDefinitions(Configuration conf) {
    for (String key : definitionMap.keySet()) {
      String val = definitionMap.get(key);
      conf.set(key, val, "command line");
    }
  }

  /**
   * If the Filesystem URL was provided, it overrides anything in
   * the configuration
   * @param conf configuration
   */
  public void applyFileSystemURL(Configuration conf) {
    if (filesystemURL != null) {
      //filesystem argument was set -this overwrites any defaults in the
      //configuration
      FileSystem.setDefaultUri(conf, filesystemURL);
    }
  }
}