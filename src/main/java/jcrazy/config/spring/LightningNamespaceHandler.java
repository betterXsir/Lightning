package jcrazy.config.spring;

import jcrazy.config.ServiceConfig;
import jcrazy.config.spring.LightningBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class LightningNamespaceHandler extends NamespaceHandlerSupport{
    public LightningNamespaceHandler() {
    }

    @Override
    public void init() {
        this.registerBeanDefinitionParser("service", new LightningBeanDefinitionParser(ServiceConfig.class));
    }
}
