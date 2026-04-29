package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_BANNED = "banned";

    private String username;
    @TableField("password_hash")
    private String passwordHash;
    private String role;
    private String status;

    public boolean isBanned() {
        return STATUS_BANNED.equals(this.status);
    }
}
