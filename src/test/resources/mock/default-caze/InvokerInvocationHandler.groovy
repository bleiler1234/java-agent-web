package mock.agent

/**
实现远程dubbo服务的代理。远程服务即使没有注册到注册中心也可以。
*/
import org.slf4j.LoggerFactory

/**
 * 尽量不要给metaClass添加字段来保存数据，容易出bug（比如重新解析类后，即使重新给metaClass.proxys赋值，也会出现proxys还是旧值的情况）。
 */
class InvokerInvocationHandler{
	def logger = LoggerFactory.getLogger("InvokerInvocationHandlerLogger")
	def timestamp = System.currentTimeMillis()
	def includes = """
org.wt.service.HelloService
org.wt.service.RemoteUserService
""".trim().split(/\s+/) as HashSet
	def methods
	def init(methods){
		this.methods = methods
	}
	/**
	 params[] => target,method,args
	 */
	def "invoke-invoke"(ivkSelf ,ivkThisMethod,ivkProceed,ivkArgs){
		def serviceTarget = ivkArgs[0]
		def serviceMethod = ivkArgs[1]
		def serviceArgs = ivkArgs[2]
		def matchedInterface = serviceTarget.getClass().getInterfaces().find{
			includes.contains(it.getName())
		}
		if(matchedInterface){
			def className = matchedInterface.getName()
			logger.info "transformer(dubbo)=> $className"

			def sn = className.split(/\./)[-1]
			def proxys = methods.proxys
			if(!proxys[sn]){
				return ivkProceed.invoke(ivkSelf,ivkArgs)
			}
			def proxy = proxys[sn]
			def methodName = serviceMethod.getName()
			if (proxy.metaClass.respondsTo(proxy, methodName, *serviceArgs)) {
				logger.info("ivk proxy(dubbo)=====> ${className}#$methodName ${ivkArgs}")
				return proxy."$methodName"(*serviceArgs)
			} else if (proxy.metaClass.respondsTo(proxy, "${methodName}-invoke", *ivkArgs)) {
				logger.info("ivk proxy(dubbo)=====> ${className}#$methodName-invoke ${ivkArgs}")
				return proxy."${methodName}-invoke"(*ivkArgs)
			}else {
				return ivkProceed.invoke(ivkSelf, ivkArgs)
			}

		}else{
			ivkProceed.invoke(ivkSelf,ivkArgs)
		}
	}
}

