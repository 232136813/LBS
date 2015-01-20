package com.lenovo.vctl.common.lbs.cluster;

/**
 * lbsServer  zookeeper集群 最终规划
 * 
 * 
 * 
 *   clientA ....    clientN 
 * 		|              |
 *   |router A ---- routerB ----... routerN      |
 *   |-------------------------------------------|  ==> zookeeper 管理者两个集群
 *   |LbsServerA ----LbsServerB ---... LbsServerN|  
 *   
 *   clientN 从zookeeper获取 router 集群信息   写到routerX  routerX 根据 规则写到 对应的LbsServerX 和 slave 
 * 
 * @author songkun1
 *
 */
public class LbsZookeeperRegister {

}
