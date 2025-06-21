package com.bit.ffmpeg;


import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class FfmpegTool {

    private static final Runtime runtime = Runtime.getRuntime();

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(getVersion());
    }
    public static String getVersion() throws IOException, InterruptedException {
//        使用本地运行命令获取ffmpeg版本
        Process process = runtime.exec("ffmpeg -version");
        // 读取标准输出
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // 读取错误输出（可选）
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder output = new StringBuilder();
        String line;

        // 读取版本信息（第一行即为版本）
        if ((line = stdInput.readLine()) != null) {
            output.append(line).append("\n");
        }
        // 关闭流
        stdInput.close();
        stdError.close();
        return output.toString().trim();
    }
    @Tool(description = """
            专门使用ffmpeg来处理音视频文件
            - 音频/视频格式转换
            - 音频/视频剪辑
            """)
    public void execute(McpSyncServerExchange mcpSyncServerExchange,@ToolParam(description = "待处理的文件")MultipartFile file,@ToolParam(description = "待执行的ffmpeg命令",required = false) String command) throws IOException, InterruptedException {

        McpSchema.ClientCapabilities clientCapabilities = mcpSyncServerExchange.getClientCapabilities();
        McpSchema.ClientCapabilities.Sampling sampling = clientCapabilities.sampling();
        System.out.println(sampling);
        Process process = runtime.exec(command);
        process.waitFor();
    }
}
