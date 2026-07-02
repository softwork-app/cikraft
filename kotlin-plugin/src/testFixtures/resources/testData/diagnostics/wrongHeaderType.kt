// RUN_PIPELINE_TILL: FRONTEND
import app.softwork.cikraft.Header

class Foo(
  <!CIKRAFT_HEADER_IS_NOT_PRIMITIVE, CIKRAFT_HEADER_IS_NOT_PRIMITIVE!>@Header
  val foo: kotlin.time.Duration<!>,

  @Header
  val s: kotlin.String,

  @Header
  val i: kotlin.Int,
)

/* GENERATED_FIR_TAGS: classDeclaration, primaryConstructor, propertyDeclaration */
