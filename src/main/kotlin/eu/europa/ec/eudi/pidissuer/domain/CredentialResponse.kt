/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.pidissuer.domain

/**
 * The identifier of a deferred issuance transaction.
 */
@JvmInline
value class TransactionId(val value: String)

/**
 * The response to a Credential Request.
 */
sealed interface CredentialResponse<out T> {
    val format: Format

    /**
     * An unencrypted Credential has been issued.
     */
    data class Issued<T>(override val format: Format, val credential: T) : CredentialResponse<T>

    /**
     * The issuance of the requested Credential has been deferred.
     * The deferred transaction can be identified by [transactionId].
     */
    data class Deferred(
        override val format: Format,
        val transactionId: TransactionId,
    ) : CredentialResponse<Nothing>
}
