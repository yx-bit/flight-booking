//package com.bit.flightbooking.playground.config;
//
//
//import org.springframework.beans.factory.FactoryBean;
//
//// FactoryBean实现
//public class ToolCallbackProviderFactoryBean implements FactoryBean<ToolCallbackProvider> {
//
//    private final Object toolObject;
//
//    public ToolCallbackProviderFactoryBean(Object toolObject) {
//        this.toolObject = toolObject;
//    }
//
//    @Override
//    public ToolCallbackProvider getObject() {
//        return MethodToolCallbackProvider.builder()
//                .toolObjects(toolObject)
//                .build();
//    }
//
//    @Override
//    public Class<?> getObjectType() {
//        return ToolCallbackProvider.class;
//    }
//
//    @Override
//    public boolean isSingleton() {
//        return true;
//    }
//}
