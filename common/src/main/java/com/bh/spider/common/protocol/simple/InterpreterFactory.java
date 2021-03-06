package com.bh.spider.common.protocol.simple;

import com.bh.spider.common.protocol.DataTypes;
import com.bh.spider.common.protocol.Protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lq on 17-5-6.
 */
public class InterpreterFactory {
    private final Map<DataTypes,Interpreter> INTERPRETERS = new HashMap<>();

    public InterpreterFactory(Protocol protocol) {
        INTERPRETERS.put(DataTypes.NULL,new NullInterpreter());
        INTERPRETERS.put(DataTypes.INT, new IntInterpreter());
        INTERPRETERS.put(DataTypes.BYTE, new ByteInterpreter());
        INTERPRETERS.put(DataTypes.BOOL, new BoolInterpreter());
        INTERPRETERS.put(DataTypes.CHAR, new CharInterpreter());
        INTERPRETERS.put(DataTypes.LONG, new LongInterpreter());
        INTERPRETERS.put(DataTypes.FLOAT, new FloatInterpreter());
        INTERPRETERS.put(DataTypes.DOUBLE, new DoubleInterpreter());
        INTERPRETERS.put(DataTypes.STRING, new StringInterpreter());
        INTERPRETERS.put(DataTypes.ENUM, new EnumInterpreter());
        INTERPRETERS.put(DataTypes.OBJECT, new ObjectInterpreter(protocol,this));
        INTERPRETERS.put(DataTypes.ARRAY, new ArrayInterpreter(protocol,this));
        INTERPRETERS.put(DataTypes.COLLECTION,new CollectionInterpreter(protocol,this));
    }

    public Interpreter get(DataTypes type){
        return INTERPRETERS.get(type);
    }

}
