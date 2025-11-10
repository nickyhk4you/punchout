# Mock Service Token Endpoint - UUID Format

## Updated Token Format

The Mock Service token endpoint (`http://localhost:8082/api/v1/token`) now returns a full UUID-based one-time token.

### Token Format

**Pattern**: `OTT-{UUID}`

**Example**: `OTT-51709106-b64e-4dd8-9767-550a47edff9e`

Where:
- `OTT` = One-Time Token prefix
- `-` = Separator
- `{UUID}` = Full UUID (36 characters with dashes)

### Before vs After

| Before | After |
|--------|-------|
| `OTT_A1B2C3D4E5F6G7H8` | `OTT-51709106-b64e-4dd8-9767-550a47edff9e` |
| 16-char shortened | Full UUID format |
| Uppercase hex | Standard UUID |

## API Usage

### Generate Token

**Endpoint**: `POST http://localhost:8082/api/v1/token`

**Request**:
```json
{
  "sessionKey": "SESSION_TEST_123",
  "operation": "create"
}
```

**Response**:
```
OTT-51709106-b64e-4dd8-9767-550a47edff9e
```

### Validate Token

**Endpoint**: `POST http://localhost:8082/api/v1/validate`

**Request**:
```json
{
  "token": "OTT-51709106-b64e-4dd8-9767-550a47edff9e"
}
```

**Response**:
```json
{
  "valid": true,
  "message": "Token is valid"
}
```

## Token Features

### 1. **One-Time Use**
- Token can only be used once
- After validation, token is marked as "used"
- Second validation attempt returns `{"valid": false, "message": "Token is invalid or already used"}`

### 2. **Expiration**
- Tokens expire after 30 minutes
- Expired tokens are automatically removed
- Validation of expired token returns `false`

### 3. **In-Memory Storage**
- Tokens stored in ConcurrentHashMap
- Thread-safe for concurrent requests
- Tokens cleared on service restart

### 4. **UUID-Based**
- Uses Java's `UUID.randomUUID()`
- Statistically unique (collision extremely unlikely)
- Industry-standard format

## Example Flow

### Normal PunchOut Flow

```
1. Gateway requests token
   POST /api/v1/token
   → OTT-51709106-b64e-4dd8-9767-550a47edff9e

2. Gateway validates token
   POST /api/v1/validate
   → {"valid": true, "message": "Token is valid"}

3. Gateway uses token for catalog request
   Authorization: Bearer OTT-51709106-b64e-4dd8-9767-550a47edff9e

4. Token is now consumed (cannot be reused)
```

## Testing

### Using cURL

**Generate Token**:
```bash
curl -X POST http://localhost:8082/api/v1/token \
  -H "Content-Type: application/json" \
  -d '{"sessionKey": "SESSION_TEST_123", "operation": "create"}'
```

**Validate Token**:
```bash
curl -X POST http://localhost:8082/api/v1/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "OTT-51709106-b64e-4dd8-9767-550a47edff9e"}'
```

### Expected Results

**First Request**: 
```
OTT-51709106-b64e-4dd8-9767-550a47edff9e
```

**Second Request (different UUID)**:
```
OTT-a3f8d912-4c5e-4a2b-9d8c-1e7f6b3a8c4d
```

## Benefits

✅ **Standard Format** - Uses industry-standard UUID format
✅ **Unique** - Extremely low collision probability
✅ **Readable** - Full UUID is easier to read and debug
✅ **Recognizable** - `OTT-` prefix clearly indicates one-time token
✅ **Compatible** - Works with standard UUID parsers and validators
✅ **Secure** - Unpredictable, hard to guess

## Integration

The Gateway automatically:
1. Requests token from Mock Service
2. Receives UUID-based token
3. Uses token in Authorization header for catalog request
4. Token is validated and consumed by Mock Service

All of this is logged in the network requests for visibility in the UI Dashboard.

## Logs

Check Mock Service logs to see token generation:
```bash
tail -f /tmp/punchout-mock-service.log | grep "Generated one-time token"
```

Example output:
```
Generated one-time token: OTT-51709106-b64e-4dd8-9767-550a47edff9e for sessionKey: SESSION_TEST_123
```

## Summary

The token endpoint now returns full UUID-based tokens in the format:
```
OTT-{uuid}
```

This provides better readability, debugging, and follows industry standards for one-time tokens! 🔐
