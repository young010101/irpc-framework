package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.registy.URL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cyang
 */
public class CommonServerCache {
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();
    public static final Map<String, Object> PROVIDED_CLASSES_MAP = new HashMap<>();
    /**
     * 服务的URL
     */
    public static final Set<URL> PROVIDED_URL_SET = new HashSet<>();
}
