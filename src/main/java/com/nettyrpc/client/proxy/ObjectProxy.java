package com.nettyrpc.client.proxy;

import com.nettyrpc.client.ConnectManage;
import com.nettyrpc.client.RPCFuture;
import com.nettyrpc.client.RpcClientHandler;
import com.nettyrpc.protocol.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by luxiaoxun on 2016-03-16.
 */
public class ObjectProxy<T> implements InvocationHandler, IAsyncObjectProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        // Debug
        LOGGER.debug(method.getDeclaringClass().getName());
        LOGGER.debug(method.getName());
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            LOGGER.debug(method.getParameterTypes()[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            LOGGER.debug(args[i].toString());
        }

        RpcClientHandler handler = ConnectManage.getInstance().chooseHandler();
        RPCFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture.get();
    }

    public RPCFuture call(String funcName, Object... args) {
        RpcClientHandler handler = ConnectManage.getInstance().chooseHandler();
        RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
        RPCFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture;
    }

    private RpcRequest createRequest(String className, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);

        Class[] parameterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);
//        Method[] methods = clazz.getDeclaredMethods();
//        for (int i = 0; i < methods.length; ++i) {
//            // Bug: if there are 2 methods have the same name
//            if (methods[i].getName().equals(methodName)) {
//                parameterTypes = methods[i].getParameterTypes();
//                request.setParameterTypes(parameterTypes); // get parameter types
//                break;
//            }
//        }

        LOGGER.debug(className);
        LOGGER.debug(methodName);
        for (int i = 0; i < parameterTypes.length; ++i) {
            LOGGER.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            LOGGER.debug(args[i].toString());
        }

        return request;
    }

    private Class<?> getClassType(Object obj){
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        if("java.lang.Integer".equals(typeName)){
        	return Integer.TYPE;	
        }
        if("java.lang.Long".equals(typeName)){
        	return Long.TYPE;	
        }
        if("java.lang.Float".equals(typeName)){
        	return Float.TYPE;	
        }
        if("java.lang.Double".equals(typeName)){
        	return Double.TYPE;	
        }
        if("java.lang.Character".equals(typeName)){
        	return Character.TYPE;	
        }
        if("java.lang.Boolean".equals(typeName)){
        	return Boolean.TYPE;	
        }
        
        if("java.lang.Short".equals(typeName)){
        	return Short.TYPE;	
        }
        if("java.lang.Byte".equals(typeName)){
        	return Byte.TYPE;	
        }

        return classType;
    }

}
