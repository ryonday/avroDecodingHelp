package com.ryonday.avro.test.v180;

import com.google.common.collect.ImmutableList;
import com.ryonday.avro.test.deepcopy.CinderellaStatus;
import com.ryonday.avro.test.deepcopy.DeepCopyTest;
import com.ryonday.avro.test.deepcopy.MouseStatus;
import com.ryonday.avro.test.deepcopy.PumpkinStatus;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumWriter;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

public class AvroDeepCopyTest {

    private static final Logger logger = getLogger(AvroDeepCopyTest.class);

    private DeepCopyTest dct;

    @Before
    public void setUp() throws Exception {
        dct = DeepCopyTest.newBuilder()
            .setToldBy("dad")
            .setToldTo("son")
            .setTimesTold(0) // first time, how precious! Honey, get the camera!
            .setCinderellaStatus(CinderellaStatus.Scullery_Maid)
            .setPumpkinStatus(PumpkinStatus.Plain_Old_Gourd)
            .setMouseStatus(MouseStatus.Plain_Old_Vermin)
            .build();
    }

    @Test
    public void itCant_useDeepCopy_withDomainObjects_unlessSchemasAre_completelyIdentical() throws Exception {

        Schema fromParser = null;
        Schema.Parser p = new Schema.Parser();

        for (File f : ImmutableList.of(
            new File("../schema/src/main/avro/deepCopyTest/cinderella_status.avsc"),
            new File("../schema/src/main/avro/deepCopyTest/mouse_status.avsc"),
            new File("../schema/src/main/avro/deepCopyTest/pumpkin_status.avsc"),
            new File("../schema/src/main/avro/deepCopyTest/deep_copy_test.avsc"))) {
            fromParser = p.parse(f);
        }

        logger.info("Schemas:\n" +
            "From Schema Parser:  {}\n" +
            "From Domain Objects: {}", fromParser, DeepCopyTest.getClassSchema());

        String base64Encoded = "AAICBnNvbgIGZGFkAA==";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        DatumWriter<DeepCopyTest> writer = new SpecificDatumWriter<>(DeepCopyTest.getClassSchema());
        writer.write(dct, encoder);
        encoder.flush();
        byte[] encoded = out.toByteArray();

        Decoder decoder = DecoderFactory.get().binaryDecoder(encoded, null);

        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<>(fromParser);

        GenericRecord record = datumReader.read(null, decoder);

        Assertions.assertThatThrownBy(() -> {
            DeepCopyTest dctFromDeepCopy = (DeepCopyTest) SpecificData.get().deepCopy(DeepCopyTest.getClassSchema(), record);
        })
            .isInstanceOf(ClassCastException.class)
            .hasMessageStartingWith("org.apache.avro.util.Utf8 cannot be cast to java.lang.String");

        final Schema finalFromParser = fromParser;

        Assertions.assertThatThrownBy(() -> {
            DeepCopyTest dctFromDeepCopy = (DeepCopyTest) SpecificData.get().deepCopy(finalFromParser, record);
        })
            .isInstanceOf(ClassCastException.class)
            .hasMessageStartingWith("org.apache.avro.util.Utf8 cannot be cast to java.lang.String");

    }
}
