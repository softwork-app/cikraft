package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("VerifyJWT", namespace = "http://www.sap.com/apimgmt")
public data class VerifyJWT(
    override val async: Boolean,
    override val continueOnError: Boolean,
    override val enabled: Boolean,
    val algorithm: Algorithm,
    @XmlSerialName("Issuer")
    val issuer: RefOrValue?,
    @XmlSerialName("Subject")
    val subject: RefOrValue? = null,
    @XmlSerialName("Audience")
    val audience: RefOrValue? = null,
    @XmlSerialName("PublicKey")
    val publicKey: PublicKey,
    val additionalClaims: AdditionalClaims,
) : Policy {
    @Serializable
    public enum class Algorithm {
        RS256,
    }

    @Serializable
    public data class PublicKey(
        @XmlSerialName("JWKS")
        val jwks: RefOrValue,
    )

    @Serializable
    public data class AdditionalClaims(val claims: List<Claim>) {
        @Serializable
        public data class Claim(
            val name: String,
            @XmlValue
            val value: String,
        )
    }
}
