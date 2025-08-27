package com.grensil.network.container

/**
 * Manual DI를 위한 의존성 컨테이너
 * Android 기본 API만 사용한 간단한 DI 구현
 */
object DependencyContainer {

    private val dependencies = mutableMapOf<Class<*>, Any>()

    // 기본 메서드들 (inline 없음)
    fun <T> register(clazz: Class<T>, instance: T) {
        dependencies[clazz] = instance as Any
    }

    fun <T> get(clazz: Class<T>): T {
        return dependencies[clazz] as? T
            ?: throw IllegalStateException("${clazz.simpleName} not registered in DI container")
    }

    fun <T> contains(clazz: Class<T>): Boolean {
        return dependencies.containsKey(clazz)
    }

    fun <T> remove(clazz: Class<T>) {
        dependencies.remove(clazz)
    }

    fun clear() {
        dependencies.clear()
    }

    // 편의 메서드들 (reified + inline) - private 접근 안 함
    inline fun <reified T> register(instance: T) {
        register(T::class.java, instance)
    }

    inline fun <reified T> get(): T {
        return get(T::class.java)
    }

    inline fun <reified T> contains(): Boolean {
        return contains(T::class.java)
    }

    inline fun <reified T> remove() {
        remove(T::class.java)
    }
}