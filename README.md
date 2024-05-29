# Javino

[![](https://jitpack.io/v/chon-group/Javino.svg)](https://jitpack.io/#chon-group/Javino)

Javino is a serial message's error check protocol for communication over a serial channel. This repository provides the Javino protocol for Java in High level side. Other options of javino implementations can see below: 

|   _Side_            |     Java      |Arduino|Python|C|
|:-------------:|:-------------:|:-------------:|:-------------:|:-------------:|
|High level|This Repository|-|-|-|
|Low level | - |[javino2Arduino](https://github.com/chon-group/javino2arduino)|[javino2Python](https://github.com/chon-group/javino2python)|[javinoCLibrary](https://github.com/bptfreitas/JavinoCLibrary)|


There are some libraries that use the serial port to deal with one-sided messages. However, these libraries just provide message treatment for one platform side, leaving the other side to the programmer.
The Javino aims to fill this gap because it offers a double-sided communication that provides a higher level of correctness in message exchange.

For this reason, every message is composed of a preamble, a field size and the message content. The preamble is a field composed of four hexadecimal characters that are used to identify the beginning of a message sent by an agent. The field size is composed of two hexadecimal characters that are used to calculate the message extension. Finally, the last field is the message content, up to 255 bytes. The preamble and the field size are used together to avoid errors in the event of a loss of information during the message transmission. For the sake of practice, Javino automatically mounts the message.

![Javino Message Format](https://www.researchgate.net/profile/Carlos-Pantoja-3/publication/277403727/figure/fig1/The-Javino-message-format.png)

When a message is sent, the Javino library starts to listen on the serial port for arriving char-to-char messages. If there is any information arriving, the Javino stores this character, analyzing if it is part of the expected preamble. So, this process is repeated until the message has been completely received. 

Once the preamble is not confirmed, the Javino discards all information received until it finds a valid preamble. Otherwise, the Javino verifies the field size value to identify the message length. 
This process avoids error insertions and defines where a message starts
and ends. 

## How to import?

#### Using Maven:
  - Add it in your pom.xml at the end of repositories and add the dependency.
    <details>
    <summary>pom.xml</summary>

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
        <version>1.6.3</version>
      </dependency>
    </dependencies>
    ```
    </details>

#### Using Gradle:
  - Add it in your root build.gradle at the end of repositories and add the dependency.
    <details>
    <summary>build.gradle</summary>

    ```xml
    allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
    }

    dependencies {
      implementation 'com.github.chon-group:javino:1.6.3'
    }

    ```
    </details>

## COPYRIGHT
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />Javino is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>. The licensor cannot revoke these freedoms as long as you follow the license terms:

* __Attribution__ — You must give __appropriate credit__ like below:

N. M. Lazarin e C. E. Pantoja, “A robotic-agent platform for embedding software agents using raspberry pi and arduino boards”, _in_ __Proceedings of 9th Software Agents, Environments and Applications School (WESAAC 2015)__, Niteroi: UFF, 2015, p. 13–20. Available at: [http://www2.ic.uff.br/~wesaac2015/Proceedings-WESAAC-2015.pdf](https://www.researchgate.net/publication/277403727_A_Robotic-agent_Platform_for_Embedding_Software_Agents_Using_Raspberry_Pi_and_Arduino_Boards)

<details>
<summary> Bibtex citation format</summary>

```
@inproceedings{javino,
	address = {Niteroi - RJ},
	title = {A robotic-agent platform for embedding software agents using raspberry pi and arduino boards},
	url = {http://www2.ic.uff.br/~wesaac2015/Proceedings-WESAAC-2015.pdf},
	booktitle = {Proceedings of 9th Software Agents, Environments and Applications School (WESAAC 2015)},
	publisher = {UFF},
	author = {Lazarin, Nilson Mori and Pantoja, Carlos Eduardo},
	year = {2015},
	pages = {13--20},
	note = {ISSN: 2177-2096},
}
```
</details>