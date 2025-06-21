package com.bit.mcpserver;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestToolService {

    @Autowired
    private OpenMeteoService openMeteoService;

    TestToolService(){
        System.out.println("初始化");
    }
    @Tool(description = "两数之和")
    public String testTool(int a,int b)
    {
        return a+b+"";
    }
    @Tool(description = """
            支持音频/视频格式转换
            支持音频/视频剪辑
            """)
    public void audioVideoConvert(String inputFilePath, String outputFilePath)  {


        System.out.println("sampling");
    }
    @Tool(description = "获取指定经纬度的天气预报")
    public String getWeather(double latitude, double longitude)
    {
        return openMeteoService.getWeatherForecastByLocation(latitude,longitude);
    }
}
