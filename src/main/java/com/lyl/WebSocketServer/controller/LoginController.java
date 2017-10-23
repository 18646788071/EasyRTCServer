package com.lyl.WebSocketServer.controller;

import com.lyl.WebSocketServer.model.User;
import org.springframework.web.bind.annotation.*;

/**
 * Created by david on 2017/10/13.
 */
@RestController
public class LoginController {

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    void userLogin(@RequestBody User user){
        System.out.println("用户登录 userName:"+"  password:"+user);
    }
}
