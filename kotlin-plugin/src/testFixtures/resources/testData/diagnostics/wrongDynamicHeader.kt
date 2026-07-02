// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.DynamicHeaders

class Foo(
  <!CIKRAFT_DYNAMIC_HEADER_IS_NOT_MAP_STRING!>@DynamicHeaders
  val foo: Map<String, Int><!>
)

/* GENERATED_FIR_TAGS: classDeclaration, primaryConstructor, propertyDeclaration */
