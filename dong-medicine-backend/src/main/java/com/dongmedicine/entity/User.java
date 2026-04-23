package com.dongmedicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_BANNED = "banned";

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    @TableField("password_hash")
    private String passwordHash;
    private String role;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public boolean isBanned() {
        return STATUS_BANNED.equals(this.status);
    }
}
