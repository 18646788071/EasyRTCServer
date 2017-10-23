package com.lyl.WebSocketServer.model;

import lombok.Data;
import lombok.ToString;

/**
 * Created by david on 2017/10/11.
 */
@Data
@ToString
public class User {
//    public Long userId;
    private String userName;
    private String password;
}
