package jcrazy.config.spring;

import jcrazy.config.ServiceConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import sun.reflect.misc.ReflectUtil;

import java.util.List;

public class LightningBeanDefinitionParser implements BeanDefinitionParser {
    private Class<?> beanClass;

    public LightningBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();

        beanDefinition.setBeanClass(beanClass);

        String ref = element.getAttribute("ref");
        String id = element.getAttribute("id");

        String className;
        int len$;
        if((id == null || id.length() == 0)) {
            //以服务接口全名作为ServiceBean的实例id
            className = element.getAttribute("interface");
            id = className;

            //若容器中已存在相同服务接口的ServiceBean,则在其后加上序号作为其id
            for(len$ = 2; parserContext.getRegistry().containsBeanDefinition(id); id=className + len$++) {
                ;
            }
        }

        if(id != null && id.length() > 0) {
            if(parserContext.getRegistry().containsBeanDefinition(id)) {
                throw new IllegalStateException("Duplicate spring bean id" + id);
            }
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
        }

        className = element.getAttribute("class");
        if(className != null && className.length() > 0) {
            GenericBeanDefinition classDefinition = new GenericBeanDefinition();
            try {
                classDefinition.setBeanClass(ReflectUtil.forName(className));
                beanDefinition.getPropertyValues().addPropertyValue("ref",
                        new BeanDefinitionHolder(classDefinition, id+ "Impl"));
            }catch (ClassNotFoundException var1) {
                throw new IllegalStateException("Not found class " + className + ", cause: " + var1.getMessage(), var1);
            }
        }

        if(parserContext.getRegistry().containsBeanDefinition(ref)) {
            BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(ref);
            if(!refBean.isSingleton()) {
                throw new IllegalStateException("The exported service ref " + ref + " must be singleton.");
            }
        }

        beanDefinition.getPropertyValues().addPropertyValue("ref", new RuntimeBeanReference(ref));

        return beanDefinition;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parse(element, parserContext, beanClass);
    }
}
