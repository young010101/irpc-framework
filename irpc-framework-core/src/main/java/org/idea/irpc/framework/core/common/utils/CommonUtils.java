package org.idea.irpc.framework.core.common.utils;

import org.idea.irpc.framework.core.common.event.IRpcListener;

import java.util.List;

public class CommonUtils {
    public static boolean isEmptyList(List objects) {
        return objects == null || objects.size() == 0;
    }

    public static boolean isNotEmptyList(List objects) {
        return !isEmptyList(objects);
    }
}
