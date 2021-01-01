package com.github.ruslanyussupov.androidmvi.core.middleware

interface WrappingCondition {

    fun shouldWrap(target: Any, name: String?, standalone: Boolean) : Boolean

    object Always : WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return true
        }
    }

    object Never : WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return false
        }
    }

    object IsStandalone: WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return standalone
        }
    }

    object IsNamed: WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return !name.isNullOrBlank()
        }
    }

    sealed class Name {
        class SimpleMatcher(private val pattern: String) : WrappingCondition {

            override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
                return name != null && name.contains(pattern, ignoreCase = true)
            }
        }

        class RegexMatcher(private val pattern: String) : WrappingCondition {

            override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
                return name != null && Regex(pattern).containsMatchIn(name)
            }
        }
    }

    class InstanceOf(private val clz: Class<*>) : WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return clz.isInstance(target)
        }
    }

    class Not(private val wrapped: WrappingCondition) : WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return !wrapped.shouldWrap(target, name, standalone)
        }
    }

    class AnyOf(private vararg val wrapped: WrappingCondition) : WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return wrapped.any { it.shouldWrap(target, name, standalone) }
        }
    }

    class AllOf(private vararg val wrapped: WrappingCondition) : WrappingCondition {

        override fun shouldWrap(target: Any, name: String?, standalone: Boolean): Boolean {
            return wrapped.all { it.shouldWrap(target, name, standalone) }
        }
    }
}
