package com.bit.mcpserver;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ToolCallbackProviderAutoConfiguration {

    @Bean
    public static BeanDefinitionRegistryPostProcessor toolCallbackProviderRegistrar() {
        return new ToolCallbackProviderRegistrar();
    }

    private static class ToolCallbackProviderRegistrar implements BeanDefinitionRegistryPostProcessor {

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            String[] beanNames = registry.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                BeanDefinition beanDef = registry.getBeanDefinition(beanName);
                String className = beanDef.getBeanClassName();
                if (className == null) continue; // 忽略无法获取类名的Bean

                try {
                    Class<?> clazz = Class.forName(className);
                    if (hasToolMethod(clazz)) {
                        registerToolCallbackProvider(registry, beanName);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        private boolean hasToolMethod(Class<?> clazz) {
            return Arrays.stream(clazz.getDeclaredMethods())
                    .anyMatch(method -> method.isAnnotationPresent(Tool.class));
        }

        private void registerToolCallbackProvider(BeanDefinitionRegistry registry, String sourceBeanName) {
            BeanDefinitionBuilder factoryBeanBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(ToolCallbackProviderFactoryBean.class);
            factoryBeanBuilder.addConstructorArgReference(sourceBeanName);

            String providerBeanName = sourceBeanName + "ToolCallbackProvider";
            registry.registerBeanDefinition(providerBeanName, factoryBeanBuilder.getBeanDefinition());
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        }
    }
}

