package com.ryonday.test;

import com.ryonday.avro.sux.ChildGameState;
import com.ryonday.avro.sux.EnumTest;
import com.ryonday.avro.sux.PiggyType;
import com.ryonday.test.util.EncodeDecodeHelper;
import org.apache.avro.AvroTypeException;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.slf4j.LoggerFactory.getLogger;

public class Avro180EnumFail {

    private static final Logger logger = getLogger(Avro180EnumFail.class);

    @Test
    public void genericDatumWriter_failsForSpecificRecord() throws Exception {
        EnumTest record = EnumTest.newBuilder()
            .setGameState(ChildGameState.Eeny)
            .setPiggyType(PiggyType.The_Piggy_Who_Stayed_Home)
            .build();

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToJsonGeneric(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToByteArrayGeneric(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");

        assertThat(EncodeDecodeHelper.encodeToJsonSpecific(record)).isNotNull();

        assertThat(EncodeDecodeHelper.encodeToByteArraySpecific(record)).isNotNull();
    }

    @Test
    public void genericDatumWriter_failsForGenericRecord_populatedWithRawEnum() throws Exception {
        GenericRecord record = new GenericData.Record(EnumTest.getClassSchema());
        record.put("game_state", ChildGameState.Eeny);
        record.put("piggy_type", PiggyType.The_Piggy_Who_Stayed_Home);

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToJsonGeneric(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToByteArrayGeneric(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");

        assertThat(EncodeDecodeHelper.encodeToJsonSpecific(record)).isNotNull();

        assertThat(EncodeDecodeHelper.encodeToByteArraySpecific(record)).isNotNull();
    }

    @Test
    public void genericDatumWriter_andSpecificDatumWriter_failForGenericRecord_populatedWithTextualEnum() throws Exception {
        GenericRecord record = new GenericData.Record(EnumTest.getClassSchema());
        record.put("game_state", "Eeny");
        record.put("piggy_type", "The_Piggy_Who_Stayed_Home");

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToJsonGeneric(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToByteArrayGeneric(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToByteArraySpecific(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");

        assertThatThrownBy(() -> EncodeDecodeHelper.encodeToJsonSpecific(record))
            .isInstanceOf(AvroTypeException.class)
            .hasMessageStartingWith("Not an enum:");
    }

}
