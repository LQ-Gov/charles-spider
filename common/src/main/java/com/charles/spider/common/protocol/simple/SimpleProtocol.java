package com.charles.spider.common.protocol.simple;

import com.charles.spider.common.protocol.*;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LQ on 2015/10/20.
 */
public final class SimpleProtocol extends ProtocolBase {
    private final static int MAX_LEN = (Integer.MAX_VALUE - 5);

    @Override
    public byte[] pack(int data) {
        System.out.println("pack int");
        byte[] result = new byte[5];
        result[0] = DataTypes.INT.value();
        result[1] = (byte) (data & 0xff);
        result[2] = (byte) ((data >> 8) & 0xff);
        result[3] = (byte) ((data >> 16) & 0xff);
        result[4] = (byte) ((data >> 24) & 0xff);
        return result;
    }

    @Override
    public byte[] pack(boolean data) {

        byte[] result = new byte[2];
        result[0] = DataTypes.BOOL.value();
        result[1] = (byte) (data ? 1 : 0);
        return result;
    }

    @Override
    public byte[] pack(float data) {
        byte[] result = pack(Float.floatToIntBits(data));
        result[0] = DataTypes.FLOAT.value();
        return result;
    }

    @Override
    public byte[] pack(double data) {
        byte[] result = pack(Double.doubleToLongBits(data));
        result[0] = DataTypes.DOUBLE.value();
        return result;
    }

    @Override
    public byte[] pack(long data) {
        byte[] result = new byte[9];
        result[0] = DataTypes.LONG.value();

        for (int i = 0; i < 8; i++)
            result[i + 1] = (byte) ((data >> (i * 8)) & 0xff);

        return result;
    }

    @Override
    public byte[] pack(char data) {
        byte[] result = new byte[3];
        result[0] = DataTypes.CHAR.value();
        result[1] = (byte) (data & 0xff);
        result[2] = (byte) ((data >> 8) & 0xff);
        return result;

    }

    @Override
    public byte[] pack(byte data) {
        byte[] result = new byte[2];
        result[0] = DataTypes.BYTE.value();
        result[1] = data;
        return result;
    }

    //数据最大长度为 65531
    @Override
    public byte[] pack(String input, Charset charset) throws Exception {
        if (input!=null&& input.length() > MAX_LEN)
            throw new Exception("the string length must between 0 and " + MAX_LEN);


        if(input==null)
            return new byte[]{DataTypes.STRING.value(),1,0,0,0,0};

        byte[] result = new byte[input.length() + 6];

        ByteBuffer buffer = ByteBuffer.allocate(input.length()+5).put(DataTypes.STRING.value())
                .putInt(input.length())
                .put(input.getBytes(charset));

        return buffer.array();
    }

    /**
     * @param o
     * @param <T>
     * @return
     * @throws Exception
     */
    @Override
    public <T> byte[] pack(T o) throws Exception {
        if(o==null) return null;
        if (o instanceof ProtocolObject) {

            ProtocolObject base = (ProtocolObject) o;
            byte[] data = base.toBytes();
            if (base.isAutoPrefix()) {
                data = ByteBuffer.allocate(5 + data.length)
                        .put(DataTypes.CLASS.value())
                        .putInt(data.length)
                        .put(data).array();

            }
            return data;
        } else {
            if (o instanceof Integer) //Integer
                return pack(((Integer) o).intValue());
            else if (o instanceof Boolean) //Boolean
                return pack(((Boolean) o).booleanValue());
            else if (o instanceof Long) //Long
                return pack(((Long) o).longValue());
            else if (o instanceof Float) //Float
                return pack(((Float) o).floatValue());
            else if (o instanceof Double) //Double
                return pack(((Double) o).doubleValue());
            else if (o instanceof Byte) //Byte
                return pack(((Byte) o).byteValue());
            else if (o instanceof Character)//Char
                return pack(((Character) o).charValue());
            else {
                Field[] fields = o.getClass().getDeclaredFields();
                int len = 0;
                List<byte[]> list = new ArrayList<>();
                for (Field field : fields) {
                    byte[] name = pack(field.getName());
                    byte[] value = pack(field.get(o));
                    len +=name.length+value.length;
                    if(len>MAX_LEN)
                        throw new Exception("the struct size must less "+MAX_LEN+" bytes");
                    list.add(name);
                    list.add(value);
                }

                ByteBuffer buffer = ByteBuffer.allocate(5+len).put(DataTypes.CLASS.value())
                        .putInt(len);

                list.forEach(buffer::put);
                return buffer.array();
            }
        }
    }

    /**
     * @param data
     * @param <T>
     * @return
     * @throws Exception
     */
    @Override
    public <T> byte[] pack(T[] data) throws Exception {

        List<byte[]> list = new ArrayList<>();
        int len = 0;
        for (T i : data) {
            byte[] t = pack(i);
            len += t.length;
            list.add(t);
        }
        if (len > MAX_LEN)
            throw new Exception("数组长度不能超过" + (MAX_LEN));

        ByteBuffer buffer = ByteBuffer.allocate(5 + len).put(DataTypes.ARRAY.value());
        for (byte[] it : list)
            buffer.put(it);

        return buffer.array();
    }

    private static String getMethodName(String filedName) throws Exception {
        byte[] items = filedName.getBytes();
        items[0] = (byte) ((char) items[0] - 'a' + 'A');
        return new String(items);
    }


    @Override
    public Assemble assemble(byte[] data, int pos, int len) throws Exception {
        return new SimpleAssemble(data, pos, len);
    }
}
