# ESUP DESFire APDU Library

A lightweight Java library for generating DESFire EV1 APDU commands. This library extracts the core APDU functionality from the ESUP NFC Tag Server project without the web application dependencies.

## Features

- ✅ **Core APDU Commands**: All essential DESFire EV1 commands
- ✅ **Lightweight**: Minimal dependencies (no Spring, Hibernate, etc.)
- ✅ **Easy Integration**: Simple JAR dependency
- ✅ **Cryptographic Support**: AES, DES, 3DES encryption utilities
- ✅ **Smart Card I/O**: Direct card communication via javax.smartcardio

## Dependencies

- Java 8+
- `io.github.jnasmartcardio:jnasmartcardio:0.2.7` (Smart Card I/O)
- `org.slf4j:slf4j-api:2.0.11` (Logging)
- `org.apache.commons:commons-lang3:3.12.0` (Utilities)

## Installation

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.esupportail.desfire</groupId>
    <artifactId>esup-desfire-apdu-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Basic APDU Command Generation

```java
import org.esupportail.desfire.service.DESFireApduService;
import org.esupportail.desfire.model.KeyType;

DESFireApduService service = new DESFireApduService();

// Get card version
String versionCommand = service.getVersion();
System.out.println("Version APDU: " + versionCommand);
// Output: 9060000000

// Select root application
byte[] rootAid = {0x00, 0x00, 0x00};
String selectCommand = service.selectApplication(rootAid);
System.out.println("Select APDU: " + selectCommand);
// Output: 905A000003000000

// Get free memory
String memoryCommand = service.getFreeMemory();
System.out.println("Memory APDU: " + memoryCommand);
```

### 2. Application Management

```java
// Create application
byte[] appId = {0x12, 0x34, 0x56};
byte keySettings = 0x0F;  // All keys changeable
byte numberOfKeys = 0x01; // 1 key
String createAppCommand = service.createApplication(appId, keySettings, numberOfKeys);

// Select the new application
String selectAppCommand = service.selectApplication(appId);

// Get application list
String getAppsCommand = service.getApplicationIds();
```

### 3. File Operations

```java
// Create standard data file
byte fileNo = 0x01;
byte commSettings = 0x00; // Plain communication
byte[] accessRights = {0x00, 0x00}; // Key 0 for all operations
int fileSize = 32; // 32 bytes

String createFileCommand = service.createStdDataFile(
    fileNo, commSettings, accessRights, fileSize);

// Write data to file
byte[] data = "Hello DESFire!".getBytes();
String writeCommand = service.writeData(fileNo, 0, data);

// Read data from file
String readCommand = service.readData(fileNo, 0, data.length);
```

### 4. Complete 3-Step Authentication

```java
import org.esupportail.desfire.service.DESFireAuthService;

DESFireAuthService authService = new DESFireAuthService();

// Method 1: Simple complete authentication
byte[] aesKey = new byte[16]; // Your 16-byte AES key
byte keyNo = 0x00;
boolean success = authService.authenticateComplete(keyNo, KeyType.AES, aesKey);

if (success) {
    System.out.println("Authentication successful!");
    
    // Now you can encrypt/decrypt data
    byte[] data = "Secret message".getBytes();
    byte[] encrypted = authService.encryptData(data);
    byte[] decrypted = authService.decryptData(encrypted);
    
    // Or generate CMAC for integrity
    byte[] cmac = authService.generateCmac(data);
}

// Method 2: Step-by-step authentication (for advanced users)
// Step 1: Initialize
String step1Apdu = authService.authenticate1(keyNo, KeyType.AES);
// Send step1Apdu to card, get response...

// Step 2: Process challenge
byte[] cardResponse1 = {/* response from card */};
String step2Apdu = authService.authenticate2(aesKey, keyNo, KeyType.AES, cardResponse1);
// Send step2Apdu to card, get response...

// Step 3: Verify final response
byte[] cardResponse2 = {/* final response from card */};
boolean authenticated = authService.authenticate3(aesKey, keyNo, KeyType.AES, cardResponse2);
```

### 5. Using Crypto Utilities

```java
import org.esupportail.desfire.core.util.AES;
import org.esupportail.desfire.service.DesfireUtils;

// Convert hex string to bytes
String hexData = "00112233445566778899AABBCCDDEEFF";
byte[] byteData = DesfireUtils.hexStringToByteArray(hexData);

// Convert bytes to hex string
String hexString = DesfireUtils.byteArrayToHexString(byteData);

// AES encryption
byte[] key = new byte[16]; // 16-byte AES key
byte[] iv = new byte[16];  // 16-byte IV
byte[] plaintext = "Secret data".getBytes();
byte[] ciphertext = AES.encrypt(iv, key, plaintext);
```

### 6. Complete Service with Card Communication

```java
import org.esupportail.desfire.service.DESFireCompleteService;

DESFireCompleteService service = new DESFireCompleteService();

try {
    // Connect to card reader
    if (!service.connect()) {
        System.err.println("Failed to connect to card reader");
        return;
    }
    
    // Initialize card with new master key
    byte[] newMasterKey = "MySecretAESKey16".getBytes(); // 16 bytes for AES
    boolean initialized = service.initializeCard(newMasterKey, KeyType.AES);
    
    if (initialized) {
        System.out.println("Card initialized successfully");
        
        // Create application with authentication
        byte[] appId = {0x12, 0x34, 0x56};
        byte[] appKey = "AppMasterKey1234".getBytes();
        boolean appCreated = service.createApplicationComplete(
            appId, (byte)0x0F, (byte)0x01, appKey, KeyType.AES);
        
        if (appCreated) {
            // Write data to file
            byte[] data = "Hello DESFire World!".getBytes();
            boolean written = service.writeFileComplete(
                appId, (byte)0x00, KeyType.AES, appKey, 
                (byte)0x01, 0, data);
            
            if (written) {
                // Read data back
                byte[] readData = service.readFileComplete(
                    appId, (byte)0x00, KeyType.AES, appKey,
                    (byte)0x01, 0, data.length);
                
                System.out.println("Read data: " + new String(readData));
            }
        }
    }
    
} finally {
    service.disconnect();
}
```

### 7. APDU Generation Only (No Card)

```java
// If you only need APDU commands without card communication
DESFireApduService apduService = new DESFireApduService();

String versionApdu = apduService.getVersion();
System.out.println("Get Version APDU: " + versionApdu);

// Or use the complete service for APDU generation
DESFireCompleteService completeService = new DESFireCompleteService();
String selectApdu = completeService.getApduString("SELECT_APPLICATION", new byte[]{0x00, 0x00, 0x00});
System.out.println("Select Root APDU: " + selectApdu);
```

### 8. Complete Example

```java
public class DESFireExample {
    public static void main(String[] args) {
        DESFireApduService service = new DESFireApduService();
        
        try {
            // 1. Get card info
            System.out.println("Getting version: " + service.getVersion());
            
            // 2. Select root application
            byte[] rootAid = {0x00, 0x00, 0x00};
            System.out.println("Selecting root: " + service.selectApplication(rootAid));
            
            // 3. Create new application  
            byte[] newAppId = {0x12, 0x34, 0x56};
            System.out.println("Creating app: " + 
                service.createApplication(newAppId, (byte)0x0F, (byte)0x01));
            
            // 4. Select new application
            System.out.println("Selecting app: " + service.selectApplication(newAppId));
            
            // 5. Create file in application
            System.out.println("Creating file: " + 
                service.createStdDataFile((byte)0x01, (byte)0x00, 
                new byte[]{0x00, 0x00}, 32));
            
            // 6. Write data to file
            byte[] data = "Hello DESFire!".getBytes();
            System.out.println("Writing data: " + 
                service.writeData((byte)0x01, 0, data));
                
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## API Reference

### Service Classes

#### DESFireApduService
Basic APDU command generation service (no card communication).

#### DESFireAuthService  
Complete 3-step authentication service with encryption/decryption capabilities.

#### DESFireCompleteService
Full-featured service combining APDU generation, authentication, and direct card communication.

#### DESFireEV1 (Core)
Low-level core service for direct card communication and response handling.

#### PICC Level Commands
- `getVersion()` - Get card version info
- `getFreeMemory()` - Get available memory
- `formatPicc()` - Format the card (destroys all data)
- `getApplicationIds()` - List all applications
- `createApplication(aid, keySettings, numberOfKeys)` - Create new application
- `deleteApplication(aid)` - Delete application

#### Application Level Commands  
- `selectApplication(aid)` - Select application
- `getFileIds()` - List files in current application
- `getFileSettings(fileNo)` - Get file configuration
- `createStdDataFile(...)` - Create standard data file

#### Data Commands
- `readData(fileNo, offset, length)` - Read from file
- `writeData(fileNo, offset, data)` - Write to file

#### Authentication Commands
- `authenticate(keyNo, keyType)` - Start authentication
- `changeKey(...)` - Change key (simplified)

### Utility Classes

#### DesfireUtils
- `hexStringToByteArray(hex)` - Convert hex to bytes
- `byteArrayToHexString(bytes)` - Convert bytes to hex
- `swapPairs(bytes)` - Endianness conversion

#### Crypto Classes (org.esupportail.desfire.core.util)
- `AES` - AES encryption/decryption
- `DES` - DES encryption/decryption  
- `TripleDES` - 3DES encryption/decryption
- `CMAC` - CMAC calculation

## Model Classes

### DesfireTag
Represents a complete DESFire tag configuration with applications, keys, and files.

### DesfireApplication  
Represents an application with its keys and files.

### DesfireKey / DesfireFile
Represent individual keys and files within applications.

## Response Codes

The library includes `ApduResponse` class with all DESFire response codes:

```java
// Check if operation succeeded
if (ApduResponse.isSuccess(responseCode)) {
    // Success
}

// Get human-readable description
String description = ApduResponse.getDescription(responseCode);
```

## Building

```bash
git clone https://github.com/your-repo/esup-desfire-apdu-lib
cd esup-desfire-apdu-lib
mvn clean package
```

## License

This library is based on code from the ESUP-Portail project and maintains the same Apache 2.0 license.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## Support

- [Issues](https://github.com/your-repo/esup-desfire-apdu-lib/issues)
- [Documentation](https://your-docs-site.com)
- [ESUP-Portail](https://www.esup-portail.org/)