package org.idea.irpc.framework.core.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;

@Data
@AllArgsConstructor
public class ServiceBeanWrapper {
    private Object serviceBean;
    //    private Class<?> beanClass;
    private String group;
    private String token;
    private Integer limit;

    public ServiceBeanWrapper(Object serviceBean) {
        this(serviceBean, DEFAULT_GROUP, DEFAULT_TOKEN, DEFAULT_LIMIT);
    }

    public ServiceBeanWrapper(Object serviceBean, String group) {
        this(serviceBean, group, DEFAULT_TOKEN, DEFAULT_LIMIT);
    }

    public ServiceBeanWrapper(Object serviceBean, String group, String token) {
        this(serviceBean, group, token, DEFAULT_LIMIT);
    }
}
