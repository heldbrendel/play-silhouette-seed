package models

import java.sql.Timestamp
import java.util.UUID

/**
 * A token to authenticate a user against an endpoint for a short time period.
 *
 * @param authTokenId The unique token ID.
 * @param userId      The unique ID of the user the token is associated with.
 * @param expiry      The date-time the token expires.
 */
case class AuthToken(authTokenId: UUID,
                     userId: Long,
                     expiry: Timestamp)
