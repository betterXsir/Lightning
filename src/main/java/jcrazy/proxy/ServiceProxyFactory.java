package jcrazy.proxy;

import jcrazy.communication.CommunicationClient;
import jcrazy.communication.MessageClientHandler;
import jcrazy.entity.Request;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class ServiceProxyFactory implements ApplicationContextAware{
    private ServiceProxyFactory instance;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ServiceProxyFactory getInstance() {
        if(instance != null) {
            return instance;
        }
        else {
            synchronized (this) {
                if(instance != null) {
                    return instance;
                }
                else {
                    instance = new ServiceProxyFactory();
                    return instance;
                }
            }
        }
    }

    public Object createProxyObject(Class<?> interfaceClass) {
        Class[] interfaces = new Class[] {interfaceClass};
        Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, new ProxyHandler(interfaceClass));
        return proxy;
    }

    private class ProxyHandler implements InvocationHandler {
        private Class<?> serviceInterface;

        public ProxyHandler(Class<?> serviceInterface) {
            this.serviceInterface = serviceInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            CommunicationClient client = (CommunicationClient) applicationContext.getBean("communicationClient");

            Request request = new Request();
            request.setId(UUID.randomUUID().toString());
            request.setClassName(serviceInterface.getName());
            request.setMethodName(method.getName());
            Class<?>[] parameterTypes = new Class[args.length];
            for(int i = 0 ; i < args.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
            request.setParameterTypes(parameterTypes);
            request.setParameters(args);

            client.startUpClient();
            MessageClientHandler messageClientHandler = client.getMessageClientHandler();
            if(messageClientHandler == null) {
                System.out.println("连接失败");
            }
            messageClientHandler.sendMessage(request);
            return null;
        }

    }
}
