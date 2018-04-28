package com.nettyrpc.test.server;

import com.nettyrpc.test.client.HelloService;
import com.nettyrpc.test.client.Person;
import com.nettyrpc.server.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    public String hello(String name) {
        return "Hello! " + name;
    }

    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
