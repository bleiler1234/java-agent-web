//unsupport slf4j,because it is not in webappclassloader env
//if you modify this file, it works only when the server restarted!!!
includes = new HashSet()
excludes = new HashSet()
matchReg = /org.wt.*(Mapper|Service|Component|Controller).*/

def classes = """
org.wt.util.CryptUtils
org.wt.model.User
org.wt.aop.AspectTest
"""
includes = classes.trim().split(/\s+/) as HashSet
includes << "com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler" //通过代理dubbo的InvokerInvocationHandler，实现对远程dubbo服务的代理

boolean match(String className){
    if( className in excludes){
        return false
    }
    //对java-agent项目中的类直接放行，不代理
    /*if (className.startsWith("org.sirenia")) {
        return false
    }*/
    if (className.contains '$'){
        return false
    }
    //这里配置class name regexp to proxy的正则表达式，这样在mock时，不需要重启应用。
    def matchRes = className ==~ matchReg
    //不需要代理的类，放行
    if (!matchRes && !includes.contains(className)) {
        return false
    }
    return true
}
return this

