package jcrazy.protocol.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import jcrazy.entity.Request;
import jcrazy.entity.Response;

import java.util.List;

public class ProtobufNettyDecoder extends ByteToMessageDecoder{
    private Class<?> genericClass;

    public ProtobufNettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes() < 4) {
            System.out.println("可读字节少于4个");
            return;
        }

        /**将当前的读位置指针备份到mark变量中，当调用reset操作之后，
         * 重新将读位置指针恢复为备份值
         */
        byteBuf.markReaderIndex();
        int length = byteBuf.readInt();
        if(length < 0) {
            System.out.println("数据长度少于0");
            channelHandlerContext.close();
        }

        if(byteBuf.readableBytes() < length) {
            System.out.println("可读字节少于总长度");
            byteBuf.resetReaderIndex();
            return;
        }
        if(byteBuf.readableBytes() > length) {
            System.out.println("可读字节大于总长度");
        }
        byte[] data = new byte[length];
        byteBuf.readBytes(data);
        byteBuf.discardReadBytes();

        Object obj = ProtoStuffUtil.deserialize(data, genericClass);
        list.add(obj);
    }
}
