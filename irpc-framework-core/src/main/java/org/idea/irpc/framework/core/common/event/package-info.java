/**
 * <li>event: 里面有回调函数 在loader send的时候开启线程池异步执行 listener的callback
 * <ul>
 *   <li> update event: 在注册中心层实现 </li>
 *   <li> node data changed event: 在路由层实现, 主要是权重信息更新 </li>
 * </ul>
 * </li>
 * <li>listener: </li>
 */
package org.idea.irpc.framework.core.common.event;