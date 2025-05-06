package org.idea.irpc.framework.common;

/**
 * @author cyang
 */
public class UserBootstrap {
    public static User buildUser() {
        return new User("100", "cyan", "111@111.com", "11111",
                "12", "134971947", "1", "深圳南山区",
                "一些备注", "369733200008236423");
    }
}
