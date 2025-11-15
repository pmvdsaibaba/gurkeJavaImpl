# gurkeJavaImpl

## Overview
gurkeJavaImpl implements a Key Encapsulation Mechanism (KEM) called [GURKE (Group Unidirectional Ratcheted Key Exchange) published by Daniel Collins and Paul Rösler](https://eprint.iacr.org/2025/1437).

## Programming Language
Project is configured to use Java 21 for source and target compilation. Development environment is setup with Java version 23.0.2 running on Windows 11

## Build Tool
Apache Maven 3.9.9 is used for project building, dependency resolution, and running tests. The project follows Maven conventions with a pom.xml file.

## Project Structure
The project structure follows standard Maven conventions, with source code organized in a Java package structure. Build output in target directory.

Here is the sample structure of the project:
```
pom.xml
README.md
src/
├── main/
│   └── java/
│       └── com/
│           └── gurkePrj/
│               ├── signatureScheme/
│               │   └── SignatureScheme.java
│               └── s_MSMR/
│                   └── S_MSMR.java
└── test/
    └── java/
        └── com/
            └── gurkePrj/
                └── TestSignature/
                    └── TestSignatureScheme.java
                └── TestS_MSMR/
                    └── TestS_MSMR_init.java
```

## Testing
Testing uses JUnit Jupiter 5.8.2 (modern) and for deprecated format it seems to use JUnit 3.8.1 (legacy).

### Code Coverage
Jacoco Maven Plugin provides a code coverage automatically if the build is executed successfully. Coverage reports will be generated in \target\site\jacoco

## Setup Instructions

1. **Clone the Repository**
   ```sh
   git clone https://github.com/pmvdsaibaba/gurkeJavaImpl.git
   ```

2. **Install Dependencies**
   - Make sure you have [Maven](https://maven.apache.org/) and Java installed.
   - The project uses BouncyCastle for cryptography (see `pom.xml`).

3. **Build the Project**
   To build this project execute the following command in the repository root folder.
   ```sh
   mvn clean install -U
   ```

4. **Run Tests**
   ```sh
   mvn test
   ```
   **Run Specific Test**
   ```sh
   mvn -Dtest=TestClassName test
   ```
   Example: mvn -Dtest=TestTreeAddEk test

## Dependencies

- Java JDK 21+
- Maven
- BouncyCastle (for cryptographic operations)

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Contact

For questions, contact [saibaba.pothula@gmail.com].