package com.airboard.core.model.system;

import java.io.Serializable;
import com.airboard.core.base.BaseObject;
import javax.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description:
 * @author Wangshuo123
 * @since 2018-08-01
 */
@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseObject<SysUser> {

    private static final long serialVersionUID = 1L;

    @Column(name = "user_name")
    private String userName;
    @Column(name = "pass_word")
    private String passWord;


    @Override
    protected Serializable pkVal() {
       return this.id;
    }

}