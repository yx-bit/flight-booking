package com.bit.mcpserver;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class TestToolService {

    TestToolService(){
        System.out.println("初始化");
    }
    @Tool(description = "两数之和")
    public String testTool(int a,int b)
    {
        return a+b+"";
    }
}
