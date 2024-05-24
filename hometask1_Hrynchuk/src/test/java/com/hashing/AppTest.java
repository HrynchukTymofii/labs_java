package com.hashing;

import org.junit.Test;
import javax.crypto.SecretKey;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import static org.junit.Assert.*;


public class AppTest
{
    @Test
    //Check the sending and receiving the message
    public void testSendAndReceivePacket() throws GeneralSecurityException {
        SecretKey key = App.generateKey();
        byte[] sendedMessage = Sender.sendPacket(1, 444, "This is a secret!", 2, 124, key);
        assertNotNull(sendedMessage);
        assertTrue(sendedMessage.length > 17);

        Message receivedMessage = Receiver.receivePacket(sendedMessage, key);
        assertNotNull(receivedMessage);
        assertEquals(1, receivedMessage.getCType());
        assertEquals(444, receivedMessage.getBUserId());
        assertEquals("This is a secret!", new String(receivedMessage.getMessage()));
    }

    @Test
    //Check the sending and receiving the empty message
    public void testEmptyMessage() throws GeneralSecurityException {
        SecretKey key = App.generateKey();
        byte[] sendedMessage = Sender.sendPacket(1, 444, "", 2, 124, key);
        assertNotNull(sendedMessage);
        assertTrue(sendedMessage.length > 17);

        Message receivedMessage = Receiver.receivePacket(sendedMessage, key);
        assertNotNull(receivedMessage);
        assertEquals(1, receivedMessage.getCType());
        assertEquals(444, receivedMessage.getBUserId());
        assertEquals("", new String(receivedMessage.getMessage()));
    }


    @Test
    //Change some data in packet head and check if we get an error that says: Packet is damaged or changed
    public void testInvalidCRC() throws GeneralSecurityException {
        SecretKey key = App.generateKey();
        byte[] sendedMessage = Sender.sendPacket(1, 444, "This is a secret!", 2, 124, key);
        assertNotNull(sendedMessage);

        sendedMessage[1] = (byte) (sendedMessage[1] + 1);

        try {
            Receiver.receivePacket(sendedMessage, key);
            fail("Expected IllegalArgumentException due to invalid CRC");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid header CRC16", e.getMessage());
        }
    }

    @Test
    //Check if parser and serializer works fine  (because of some methods are private
    // and we have access to them only from parent class i have used reflection to call the necessary private methods)
    public void testPacketSerialization() throws GeneralSecurityException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SecretKey key = App.generateKey();
        byte[] sendedPacket = Sender.sendPacket(1, 444, "This is a secret!", 2, 124, key);

        Method parsePacketMethod = Receiver.class.getDeclaredMethod("parsePacket", byte[].class);
        parsePacketMethod.setAccessible(true);
        Packet originalPacket = (Packet) parsePacketMethod.invoke(null, sendedPacket);

        Method serializePacketMethod = Sender.class.getDeclaredMethod("serializePacket", Packet.class);
        serializePacketMethod.setAccessible(true);
        byte[] serializedPacket = (byte[]) serializePacketMethod.invoke(null, originalPacket);

        assertArrayEquals(sendedPacket, serializedPacket);
    }

    @Test
    //Encrypt and decrypt the message and check if it is the same one (because of some methods are private
    // and we have access to them only from parent class i have used reflection to call the necessary private methods)
    public void testEncryptionDecryption() throws GeneralSecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InvocationTargetException {
        SecretKey key = App.generateKey();
        Message originalMessage = new Message();
        originalMessage.setCType(1);
        originalMessage.setBUserId(444);
        originalMessage.setMessage("This is a secret!".getBytes());

        Method serializeMessageMethod = Sender.class.getDeclaredMethod("serializeMessage", Message.class);
        serializeMessageMethod.setAccessible(true);
        byte[] serializedMessage = (byte[]) serializeMessageMethod.invoke(null, originalMessage);

        Method encryptMethod = Sender.class.getDeclaredMethod("encrypt", byte[].class, SecretKey.class);
        encryptMethod.setAccessible(true);
        byte[] encryptedMessage = (byte[]) encryptMethod.invoke(null, serializedMessage, key);
        assertNotNull(encryptedMessage);

        Method decryptMethod = Receiver.class.getDeclaredMethod("decrypt", byte[].class, SecretKey.class);
        decryptMethod.setAccessible(true);
        byte[] decryptedMessage = (byte[]) decryptMethod.invoke(null, encryptedMessage, key);
        assertNotNull(decryptedMessage);

        Method parseMessageMethod = Receiver.class.getDeclaredMethod("parseMessage", byte[].class);
        parseMessageMethod.setAccessible(true);
        Message parsedMessage = (Message) parseMessageMethod.invoke(null, decryptedMessage);

        assertEquals(originalMessage.getCType(), parsedMessage.getCType());
        assertEquals(originalMessage.getBUserId(), parsedMessage.getBUserId());
        assertArrayEquals(originalMessage.getMessage(), parsedMessage.getMessage());
    }

    @Test
    //Check the sending and receiving the big message (LAST TEST:)
    public void testBigMessage() throws GeneralSecurityException {
        SecretKey key = App.generateKey();
        byte[] sendedMessage = Sender.sendPacket(1, 444, "The Buffer classes are the foundation upon which Java NIO is built. However, in these classes, the ByteBuffer class is most preferred. That’s because the byte type is the most versatile one. For example, we can use bytes to compose other non-boolean primitive types in JVM. Also, we can use bytes to transfer data between JVM and external I/O devices.\n" +
                "\n" +
                "In this tutorial, we’ll inspect different aspects of the ByteBuffer class.\n" +
                "\n" +
                "2. ByteBuffer Creation\n" +
                "The ByteBuffer is an abstract class, so we can’t construct a new instance directly. However, it provides static factory methods to facilitate instance creation. Briefly, there are two ways to create a ByteBuffer instance, either by allocation or wrapping:\n" +
                "\n" +
                "ByteBuffer Creation 1\n" +
                "2.1. Allocation\n" +
                "Allocation will create an instance and allocate private space with a specific capacity. To be precise, the ByteBuffer class has two allocation methods: allocate and allocateDirect.\n" +
                "\n" +
                "Using the allocate method, we’ll get a non-direct buffer – that is, a buffer instance with an underlying byte array:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10);\n" +
                "Copy\n" +
                "When we use the allocateDirect method, it’ll generate a direct buffer:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocateDirect(10);\n" +
                "Copy\n" +
                "For simplicity, let’s focus on the non-direct buffer and leave the direct buffer discussion for later.\n" +
                "\n" +
                "2.2. Wrapping\n" +
                "Wrapping allows an instance to reuse an existing byte array:\n" +
                "\n" +
                "byte[] bytes = new byte[10];\n" +
                "ByteBuffer buffer = ByteBuffer.wrap(bytes);\n" +
                "Copy\n" +
                "And the above code is equivalent to:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);\n" +
                "Copy\n" +
                "Any changes made to the data elements in the existing byte array will be reflected in the buffer instance, and vice versa.\n" +
                "\n" +
                "\n" +
                "freestar\n" +
                "2.3. Onion Model\n" +
                "Now, we know how to get a ByteBuffer instance. Next, let’s treat the ByteBuffer class as a three-layer onion model and understand it layer by layer from the inside out:\n" +
                "\n" +
                "Data and Indices Layer\n" +
                "Transferring Data Layer\n" +
                "View Layer\n" +
                "ByteBuffer Onion\n" +
                "At the innermost layer, we regard the ByteBuffer class as a container for a byte array with extra indices. In the middle layer, we focus on using a ByteBuffer instance to transfer data from/to other data types. We inspect the same underlying data with different buffer-based views at the outermost layer.\n" +
                "\n" +
                "3. ByteBuffer Indices\n" +
                "Conceptually, the ByteBuffer class is a byte array wrapped inside an object. It provides lots of convenient methods to facilitate reading or writing operations from/to underlying data. And, these methods are highly dependent on the indices maintained.\n" +
                "\n" +
                "Now, let’s deliberately simplify the ByteBuffer class into a container of byte array with extra indices:\n" +
                "\n" +
                "ByteBuffer = byte array + index\n" +
                "Copy\n" +
                "With this concept in mind, we can classify index-related methods into four categories:\n" +
                "\n" +
                "\n" +
                "Basic\n" +
                "Mark and Reset\n" +
                "Clear, Flip, Rewind, and Compact\n" +
                "Remain\n" +
                "ByteBuffer Indices 2\n" +
                "3.1. Four Basic Indices\n" +
                "There are four indices defined in the Buffer class. These indices record the state of the underlying data elements:\n" +
                "\n" +
                "Capacity: the maximum number of data elements the buffer can hold\n" +
                "Limit: an index to stop read or write\n" +
                "Position: the current index to read or write\n" +
                "Mark: a remembered position\n" +
                "Also, there is an invariant relationship between these indices:\n" +
                "\n" +
                "0 <= mark <= position <= limit <= capacity\n" +
                "Copy\n" +
                "And, we should note that all index-related methods revolve around these four indices.\n" +
                "\n" +
                "When we create a new ByteBuffer instance, the mark is undefined, the position holds 0, and the limit is equal to the capacity. For example, let’s allocate a ByteBuffer with 10 data elements:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10);\n" +
                "Copy\n" +
                "Or, let’s wrap an existing byte array with 10 data elements:\n" +
                "\n" +
                "byte[] bytes = new byte[10];\n" +
                "ByteBuffer buffer = ByteBuffer.wrap(bytes);\n" +
                "Copy\n" +
                "As a result, the mark will be -1, the position will be 0, and both the limit and capacity will be 10:\n" +
                "\n" +
                "int position = buffer.position(); // 0\n" +
                "int limit = buffer.limit();       // 10\n" +
                "int capacity = buffer.capacity(); // 10\n" +
                "Copy\n" +
                "The capacity is read-only and can’t be changed. But, we can use the position(int) and limit(int) methods to change the corresponding position and limit:\n" +
                "\n" +
                "buffer.position(2);\n" +
                "buffer.limit(5);\n" +
                "Copy\n" +
                "Then, the position will be 2, and the limit will be 5.\n" +
                "\n" +
                "\n" +
                "freestar\n" +
                "3.2. Mark and Reset\n" +
                "The mark() and reset() methods allow us to remember a particular position and return to it later.\n" +
                "\n" +
                "When we first create a ByteBuffer instance, the mark is undefined. Then, we can call the mark() method, and the mark is set to the current position. After some operations, calling the reset() method will change the position back to the mark.\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10); // mark = -1, position = 0\n" +
                "buffer.position(2);                          // mark = -1, position = 2\n" +
                "buffer.mark();                               // mark = 2,  position = 2\n" +
                "buffer.position(5);                          // mark = 2,  position = 5\n" +
                "buffer.reset();                              // mark = 2,  position = 2\n" +
                "Copy\n" +
                "One thing to note: If the mark is undefined, calling the reset() method will lead to InvalidMarkException.\n" +
                "\n" +
                "3.3. Clear, Flip, Rewind, and Compact\n" +
                "The clear(), flip(), rewind(), and compact() methods have some common parts and slight differences:\n" +
                "\n" +
                "ByteBuffer clear clip rewind compact\n" +
                "To compare these methods, let’s prepare a code snippet:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10); // mark = -1, position = 0, limit = 10\n" +
                "buffer.position(2);                          // mark = -1, position = 2, limit = 10\n" +
                "buffer.mark();                               // mark = 2,  position = 2, limit = 10\n" +
                "buffer.position(5);                          // mark = 2,  position = 5, limit = 10\n" +
                "buffer.limit(8);                             // mark = 2,  position = 5, limit = 8\n" +
                "Copy\n" +
                "The clear() method will change the limit to the capacity, the position to 0, and the mark to -1:\n" +
                "\n" +
                "buffer.clear();                              // mark = -1, position = 0, limit = 10\n" +
                "Copy\n" +
                "The flip() method will change the limit to the position, the position to 0, and the mark to -1:\n" +
                "\n" +
                "buffer.flip();                               // mark = -1, position = 0, limit = 5\n" +
                "Copy\n" +
                "The rewind() method keeps the limit unchanged and changes the position to 0, and the mark to -1:\n" +
                "\n" +
                "\n" +
                "freestar\n" +
                "buffer.rewind();                             // mark = -1, position = 0, limit = 8\n" +
                "Copy\n" +
                "The compact() method will change the limit to the capacity, the position to remaining (limit – position), and the mark to -1:\n" +
                "\n" +
                "buffer.compact();                            // mark = -1, position = 3, limit = 10\n" +
                "Copy\n" +
                "The above four methods have their own use cases:\n" +
                "\n" +
                "To reuse a buffer, the clear() method is handy. It will set the indices to the initial state and be ready for new writing operations.\n" +
                "After calling the flip() method, the buffer instance switches from write-mode to read-mode. But, we should avoid calling the flip() method twice. That’s because a second call will set the limit to 0, and no data elements can be read.\n" +
                "If we want to read the underlying data more than once, the rewind() method comes in handy.\n" +
                "The compact() method is suited for partial reuse of a buffer. For example, suppose we want to read some, but not all, of the underlying data, and then we want to write data to the buffer. The compact() method will copy the unread data to the beginning of the buffer and change the buffer indices to be ready for writing operations.\n" +
                "3.4. Remain\n" +
                "The hasRemaining() and remaining() methods calculate the relationship of the limit and the position:\n" +
                "\n" +
                "ByteBuffer remain\n" +
                "When the limit is greater than the position, hasRemaining() will return true. Also, the remaining() method returns the difference between the limit and the position.\n" +
                "\n" +
                "For example, if a buffer has a position of 2 and a limit of 8, then its remaining will be 6:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10); // mark = -1, position = 0, limit = 10\n" +
                "buffer.position(2);                          // mark = -1, position = 2, limit = 10\n" +
                "buffer.limit(8);                             // mark = -1, position = 2, limit = 8\n" +
                "boolean flag = buffer.hasRemaining();        // true\n" +
                "int remaining = buffer.remaining();          // 6\n" +
                "Copy\n" +
                "4. Transfer Data\n" +
                "The second layer of the Onion Model is concerned with transferring data. Specifically, the ByteBuffer class provides methods to transfer data from/to other data types (byte, char, short, int, long, float, and double):\n" +
                "\n" +
                "ByteBuffer transfer data\n" +
                "4.1. Transfer byte Data\n" +
                "To transfer byte data, the ByteBuffer class provides single and bulk operations.\n" +
                "\n" +
                "We can read or write a single byte from/to the buffer’s underlying data in single operations. These operations include:\n" +
                "\n" +
                "\n" +
                "public abstract byte get();\n" +
                "public abstract ByteBuffer put(byte b);\n" +
                "public abstract byte get(int index);\n" +
                "public abstract ByteBuffer put(int index, byte b);\n" +
                "Copy\n" +
                "We may notice two versions of the get()/put() methods from the above methods: One has no parameters, and the other accepts an index. So, what’s the difference?\n" +
                "\n" +
                "The one with no index is a relative operation, which operates on the data element in the current position and later increments the position by 1. However, the one with an index is a whole operation, which operates on the data elements at the index and won’t change the position.", 2, 124, key);
        assertNotNull(sendedMessage);

        Message receivedMessage = Receiver.receivePacket(sendedMessage, key);
        assertNotNull(receivedMessage);
        assertEquals(1, receivedMessage.getCType());
        assertEquals(444, receivedMessage.getBUserId());
        assertEquals("The Buffer classes are the foundation upon which Java NIO is built. However, in these classes, the ByteBuffer class is most preferred. That’s because the byte type is the most versatile one. For example, we can use bytes to compose other non-boolean primitive types in JVM. Also, we can use bytes to transfer data between JVM and external I/O devices.\n" +
                "\n" +
                "In this tutorial, we’ll inspect different aspects of the ByteBuffer class.\n" +
                "\n" +
                "2. ByteBuffer Creation\n" +
                "The ByteBuffer is an abstract class, so we can’t construct a new instance directly. However, it provides static factory methods to facilitate instance creation. Briefly, there are two ways to create a ByteBuffer instance, either by allocation or wrapping:\n" +
                "\n" +
                "ByteBuffer Creation 1\n" +
                "2.1. Allocation\n" +
                "Allocation will create an instance and allocate private space with a specific capacity. To be precise, the ByteBuffer class has two allocation methods: allocate and allocateDirect.\n" +
                "\n" +
                "Using the allocate method, we’ll get a non-direct buffer – that is, a buffer instance with an underlying byte array:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10);\n" +
                "Copy\n" +
                "When we use the allocateDirect method, it’ll generate a direct buffer:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocateDirect(10);\n" +
                "Copy\n" +
                "For simplicity, let’s focus on the non-direct buffer and leave the direct buffer discussion for later.\n" +
                "\n" +
                "2.2. Wrapping\n" +
                "Wrapping allows an instance to reuse an existing byte array:\n" +
                "\n" +
                "byte[] bytes = new byte[10];\n" +
                "ByteBuffer buffer = ByteBuffer.wrap(bytes);\n" +
                "Copy\n" +
                "And the above code is equivalent to:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, bytes.length);\n" +
                "Copy\n" +
                "Any changes made to the data elements in the existing byte array will be reflected in the buffer instance, and vice versa.\n" +
                "\n" +
                "\n" +
                "freestar\n" +
                "2.3. Onion Model\n" +
                "Now, we know how to get a ByteBuffer instance. Next, let’s treat the ByteBuffer class as a three-layer onion model and understand it layer by layer from the inside out:\n" +
                "\n" +
                "Data and Indices Layer\n" +
                "Transferring Data Layer\n" +
                "View Layer\n" +
                "ByteBuffer Onion\n" +
                "At the innermost layer, we regard the ByteBuffer class as a container for a byte array with extra indices. In the middle layer, we focus on using a ByteBuffer instance to transfer data from/to other data types. We inspect the same underlying data with different buffer-based views at the outermost layer.\n" +
                "\n" +
                "3. ByteBuffer Indices\n" +
                "Conceptually, the ByteBuffer class is a byte array wrapped inside an object. It provides lots of convenient methods to facilitate reading or writing operations from/to underlying data. And, these methods are highly dependent on the indices maintained.\n" +
                "\n" +
                "Now, let’s deliberately simplify the ByteBuffer class into a container of byte array with extra indices:\n" +
                "\n" +
                "ByteBuffer = byte array + index\n" +
                "Copy\n" +
                "With this concept in mind, we can classify index-related methods into four categories:\n" +
                "\n" +
                "\n" +
                "Basic\n" +
                "Mark and Reset\n" +
                "Clear, Flip, Rewind, and Compact\n" +
                "Remain\n" +
                "ByteBuffer Indices 2\n" +
                "3.1. Four Basic Indices\n" +
                "There are four indices defined in the Buffer class. These indices record the state of the underlying data elements:\n" +
                "\n" +
                "Capacity: the maximum number of data elements the buffer can hold\n" +
                "Limit: an index to stop read or write\n" +
                "Position: the current index to read or write\n" +
                "Mark: a remembered position\n" +
                "Also, there is an invariant relationship between these indices:\n" +
                "\n" +
                "0 <= mark <= position <= limit <= capacity\n" +
                "Copy\n" +
                "And, we should note that all index-related methods revolve around these four indices.\n" +
                "\n" +
                "When we create a new ByteBuffer instance, the mark is undefined, the position holds 0, and the limit is equal to the capacity. For example, let’s allocate a ByteBuffer with 10 data elements:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10);\n" +
                "Copy\n" +
                "Or, let’s wrap an existing byte array with 10 data elements:\n" +
                "\n" +
                "byte[] bytes = new byte[10];\n" +
                "ByteBuffer buffer = ByteBuffer.wrap(bytes);\n" +
                "Copy\n" +
                "As a result, the mark will be -1, the position will be 0, and both the limit and capacity will be 10:\n" +
                "\n" +
                "int position = buffer.position(); // 0\n" +
                "int limit = buffer.limit();       // 10\n" +
                "int capacity = buffer.capacity(); // 10\n" +
                "Copy\n" +
                "The capacity is read-only and can’t be changed. But, we can use the position(int) and limit(int) methods to change the corresponding position and limit:\n" +
                "\n" +
                "buffer.position(2);\n" +
                "buffer.limit(5);\n" +
                "Copy\n" +
                "Then, the position will be 2, and the limit will be 5.\n" +
                "\n" +
                "\n" +
                "freestar\n" +
                "3.2. Mark and Reset\n" +
                "The mark() and reset() methods allow us to remember a particular position and return to it later.\n" +
                "\n" +
                "When we first create a ByteBuffer instance, the mark is undefined. Then, we can call the mark() method, and the mark is set to the current position. After some operations, calling the reset() method will change the position back to the mark.\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10); // mark = -1, position = 0\n" +
                "buffer.position(2);                          // mark = -1, position = 2\n" +
                "buffer.mark();                               // mark = 2,  position = 2\n" +
                "buffer.position(5);                          // mark = 2,  position = 5\n" +
                "buffer.reset();                              // mark = 2,  position = 2\n" +
                "Copy\n" +
                "One thing to note: If the mark is undefined, calling the reset() method will lead to InvalidMarkException.\n" +
                "\n" +
                "3.3. Clear, Flip, Rewind, and Compact\n" +
                "The clear(), flip(), rewind(), and compact() methods have some common parts and slight differences:\n" +
                "\n" +
                "ByteBuffer clear clip rewind compact\n" +
                "To compare these methods, let’s prepare a code snippet:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10); // mark = -1, position = 0, limit = 10\n" +
                "buffer.position(2);                          // mark = -1, position = 2, limit = 10\n" +
                "buffer.mark();                               // mark = 2,  position = 2, limit = 10\n" +
                "buffer.position(5);                          // mark = 2,  position = 5, limit = 10\n" +
                "buffer.limit(8);                             // mark = 2,  position = 5, limit = 8\n" +
                "Copy\n" +
                "The clear() method will change the limit to the capacity, the position to 0, and the mark to -1:\n" +
                "\n" +
                "buffer.clear();                              // mark = -1, position = 0, limit = 10\n" +
                "Copy\n" +
                "The flip() method will change the limit to the position, the position to 0, and the mark to -1:\n" +
                "\n" +
                "buffer.flip();                               // mark = -1, position = 0, limit = 5\n" +
                "Copy\n" +
                "The rewind() method keeps the limit unchanged and changes the position to 0, and the mark to -1:\n" +
                "\n" +
                "\n" +
                "freestar\n" +
                "buffer.rewind();                             // mark = -1, position = 0, limit = 8\n" +
                "Copy\n" +
                "The compact() method will change the limit to the capacity, the position to remaining (limit – position), and the mark to -1:\n" +
                "\n" +
                "buffer.compact();                            // mark = -1, position = 3, limit = 10\n" +
                "Copy\n" +
                "The above four methods have their own use cases:\n" +
                "\n" +
                "To reuse a buffer, the clear() method is handy. It will set the indices to the initial state and be ready for new writing operations.\n" +
                "After calling the flip() method, the buffer instance switches from write-mode to read-mode. But, we should avoid calling the flip() method twice. That’s because a second call will set the limit to 0, and no data elements can be read.\n" +
                "If we want to read the underlying data more than once, the rewind() method comes in handy.\n" +
                "The compact() method is suited for partial reuse of a buffer. For example, suppose we want to read some, but not all, of the underlying data, and then we want to write data to the buffer. The compact() method will copy the unread data to the beginning of the buffer and change the buffer indices to be ready for writing operations.\n" +
                "3.4. Remain\n" +
                "The hasRemaining() and remaining() methods calculate the relationship of the limit and the position:\n" +
                "\n" +
                "ByteBuffer remain\n" +
                "When the limit is greater than the position, hasRemaining() will return true. Also, the remaining() method returns the difference between the limit and the position.\n" +
                "\n" +
                "For example, if a buffer has a position of 2 and a limit of 8, then its remaining will be 6:\n" +
                "\n" +
                "ByteBuffer buffer = ByteBuffer.allocate(10); // mark = -1, position = 0, limit = 10\n" +
                "buffer.position(2);                          // mark = -1, position = 2, limit = 10\n" +
                "buffer.limit(8);                             // mark = -1, position = 2, limit = 8\n" +
                "boolean flag = buffer.hasRemaining();        // true\n" +
                "int remaining = buffer.remaining();          // 6\n" +
                "Copy\n" +
                "4. Transfer Data\n" +
                "The second layer of the Onion Model is concerned with transferring data. Specifically, the ByteBuffer class provides methods to transfer data from/to other data types (byte, char, short, int, long, float, and double):\n" +
                "\n" +
                "ByteBuffer transfer data\n" +
                "4.1. Transfer byte Data\n" +
                "To transfer byte data, the ByteBuffer class provides single and bulk operations.\n" +
                "\n" +
                "We can read or write a single byte from/to the buffer’s underlying data in single operations. These operations include:\n" +
                "\n" +
                "\n" +
                "public abstract byte get();\n" +
                "public abstract ByteBuffer put(byte b);\n" +
                "public abstract byte get(int index);\n" +
                "public abstract ByteBuffer put(int index, byte b);\n" +
                "Copy\n" +
                "We may notice two versions of the get()/put() methods from the above methods: One has no parameters, and the other accepts an index. So, what’s the difference?\n" +
                "\n" +
                "The one with no index is a relative operation, which operates on the data element in the current position and later increments the position by 1. However, the one with an index is a whole operation, which operates on the data elements at the index and won’t change the position.", new String(receivedMessage.getMessage()));
    }
}
