package net.morimekta.providence.thrift;

import org.apache.thrift.protocol.TSimpleJSONProtocol;

/**
 * @author Stein Eldar Johnsen
 * @since 24.10.15.
 */
public class TSimpleJsonProtocolSerializer extends TProtocolSerializer {
    public TSimpleJsonProtocolSerializer() {
        super(false, new TSimpleJSONProtocol.Factory());
    }
}
