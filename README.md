# javino

### Maven
Add it in your pom.xml at the end of repositories and add the dependency.
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.chon-group</groupId>
    <artifactId>javino</artifactId>
    <version>1.6</version>
  </dependency>
</dependencies>
```
### Gradle
Add it in your root build.gradle at the end of repositories and add the dependency.
```xml
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
  implementation 'com.github.chon-group:javino:1.6'
}

```
