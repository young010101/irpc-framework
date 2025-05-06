package org.idea.irpc.framework.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cyang
 * @implNote kryo 需要 no args constructor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String age;
    private String bankNo;
    private String sex;
    private String address;
    private String remark;
    private String idCardNo;
}
