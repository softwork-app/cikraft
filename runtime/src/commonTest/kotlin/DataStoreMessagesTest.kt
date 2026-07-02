import app.softwork.cikraft.*
import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*
import kotlin.test.*

class DataStoreMessagesTest {
    @Serializable
    data class Products(
        @XmlSerialName("Product") val product: List<Product>,
    )

    @Serializable
    data class Product(
        @XmlElement
        val WeightUnit: String,
    )

    @Test
    fun selectXml() {
        val input = """<?xml version="1.0" encoding="UTF-8"?>
<messages>
<message id="9197e678-345e-4e3b-ae51-3bfaa8990918">
<Products>
    <Product>
      <WeightUnit>KG</WeightUnit>
    </Product>
  </Products>
</message>
<message id="b4c0f332-ec8e-4edf-81d8-3a64f374123c">
<Products>
    <Product>
      <WeightUnit>KG</WeightUnit>
    </Product>
  </Products>
</message>
</messages>"""

        assertEquals(
            DataStoreMessages(
                listOf(
                    DataStoreMessage(
                        id = "9197e678-345e-4e3b-ae51-3bfaa8990918",
                        content = Products(listOf(Product("KG"))),
                    ),
                    DataStoreMessage(
                        id = "b4c0f332-ec8e-4edf-81d8-3a64f374123c",
                        content = Products(listOf(Product("KG"))),
                    ),
                ),
            ),
            XML.v1.decodeFromString(DataStoreMessages.serializer(Products.serializer()), input),
        )
    }
}
