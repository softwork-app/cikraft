package app.softwork.cikraft.core

import app.softwork.validation.MaxLength
import app.softwork.validation.MinLength
import io.github.hfhbd.kfx.codegen.CodeGenTree
import io.github.hfhbd.kfx.codegen.CodeGenTree.Member
import kotlinx.serialization.SerialName
import kotlin.test.Test
import kotlin.test.assertEquals

class AnnotationsTest {
    @Test
    fun validationNames() {
        val (maxLengthAnnoPackageName, maxLengthName) = MaxLength::class.qualifiedName!!.asPackageNameToName()
        val (minLengthAnnoPackageName, minLengthName) = MinLength::class.qualifiedName!!.asPackageNameToName()

        val member = Member(
            name = "foo",
            type = CodeGenTree.Type.Builtin.INT,
            annotations = listOf(
                CodeGenTree.Annotation(
                    packageName = maxLengthAnnoPackageName,
                    names = listOf(maxLengthName),
                    values = mapOf(
                        MaxLength::inclusive.name to CodeGenTree.Expression.IntLiteral(0),
                    ),
                ),
                CodeGenTree.Annotation(
                    packageName = minLengthAnnoPackageName,
                    names = listOf(minLengthName),
                    values = mapOf(
                        MinLength::inclusive.name to CodeGenTree.Expression.IntLiteral(1),
                    ),
                ),
            ),
        )

        assertEquals(0, member.maxLength)
        assertEquals(1, member.minLength)
    }

    @Test
    fun serialName() {
        val (packageName, annoName) = SerialName::class.qualifiedName!!.asPackageNameToName()

        val member = Member(
            name = "foo",
            type = CodeGenTree.Type.Builtin.INT,
            annotations = listOf(
                CodeGenTree.Annotation(
                    packageName = packageName,
                    names = listOf(annoName),
                    values = mapOf(
                        SerialName::value.name to CodeGenTree.Expression.StringLiteral("FOO"),
                    ),
                ),
            ),
        )

        assertEquals("FOO", member.serialName)
    }

    private fun String.asPackageNameToName(): Pair<String, String> {
        val splitted = split(".")
        return splitted.dropLast(1).joinToString(".") to splitted.last()
    }
}
