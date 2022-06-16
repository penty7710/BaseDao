package com.pty.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : pety
 * @date : 2022/6/15 20:42
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private Integer stuId;
    private String stuName;
    private String major;
    private String sex;
    private Integer age;
    private String remark;
}
