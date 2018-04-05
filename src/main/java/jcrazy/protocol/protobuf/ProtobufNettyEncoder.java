package jcrazy.protocol.protobuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.protostuff.ProtostuffIOUtil;

public class ProtobufNettyEncoder extends MessageToByteEncoder{
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        byte[] byteData = ProtoStuffUtil.serialize(o);
        byteBuf.writeInt(byteData.length);
        byteBuf.writeBytes(byteData);
    }
}
