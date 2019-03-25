package xyz.eginez.yacta.data

@Target(AnnotationTarget.CLASS)
annotation class YactaResource

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class ResourceId

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class ResourceProperty
