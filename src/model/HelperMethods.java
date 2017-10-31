package model;

import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class HelperMethods{
    public static String receiveFixedLengthMessage( SocketChannel socketChannel ){
        String message = "";
        try {
            ByteBuffer buf = ByteBuffer.allocate(64);
            socketChannel.read( buf );
            buf.flip();
            while( buf.hasRemaining()){
                message += (char) buf.get();
            }
        } catch( IOException ex ){
            // handle the exception
        }
        return message;
    }

    public static void sendFixedLengthMessage( SocketChannel socketChannel, String message ){
        try {
            ByteBuffer buf = ByteBuffer.allocate(64);
            buf.put(message.getBytes());
            buf.flip();
            while( buf.hasRemaining()){
                socketChannel.write(buf);
            }
        }  catch( IOException ex ){
            // handle the exception
        }
    }

    public static void sendMessage( SocketChannel socketChannel, String message ){
        try{
            ByteBuffer buf = ByteBuffer.allocate(message.length()+1);
            buf.put( message.getBytes());
            buf.put((byte) 0x00 );
            buf.flip();
            while( buf.hasRemaining()) {
                socketChannel.write(buf);
            }
            System.out.println("Sent: "+message);
        } catch( IOException ex ){
            // handle exception here
        }
    }

    public static String receiveMessage( SocketChannel socketChannel ){
        String message = "";
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            while( socketChannel.read(byteBuffer) > 0 ) {
                char byteRead = 0x00;
                byteBuffer.flip();
                while( byteBuffer.hasRemaining()) {
                    byteRead = (char) byteBuffer.get();
                    if(byteRead == 0x00 )
                        break;
                    message += byteRead;
                }
                if( byteRead == 0x00)
                    break;
                byteBuffer.clear();
            }
            return message;
        } catch (IOException ex ){
            ex.printStackTrace();
        }
        return "";
    }
}

