import com.google.common.io.BaseEncoding;
import com.ryonday.avro.test.deepcopy.CinderellaStatus;
import com.ryonday.avro.test.deepcopy.DeepCopyTest;
import com.ryonday.avro.test.deepcopy.MouseStatus;
import com.ryonday.avro.test.deepcopy.PumpkinStatus;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;

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
    public void genericData_deepCopy_cantMake_specificRecord() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        DatumWriter<IndexedRecord> writer = new GenericDatumWriter<>(dct.getSchema());
        writer.write(dct, encoder);
        encoder.flush();
        byte[] encoded = out.toByteArray();

//        BaseEncoding.base64().encode(encoded);

        Decoder decoder = DecoderFactory.get().binaryDecoder(encoded, null);
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<>(dct.getSchema());
        GenericRecord record = datumReader.read(null, decoder);

        Assertions.assertThatThrownBy( () -> { DeepCopyTest dctFromDeepCopy = (DeepCopyTest) GenericData.get().deepCopy(dct.getSchema(), record); } )
            .isInstanceOf(ClassCastException.class)
            .hasMessageStartingWith("org.apache.avro.generic.GenericData$Record cannot be cast to ");
    }

    @Test
    public void specificData_deepCopy_cantMake_specificRecord_fromGenericRecord() throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        DatumWriter<IndexedRecord> writer = new GenericDatumWriter<>(dct.getSchema());
        writer.write(dct, encoder);
        encoder.flush();
        byte[] encoded = out.toByteArray();

        String base64 = BaseEncoding.base64().encode(encoded);

        logger.info("Base64: {}", base64);

        Decoder decoder = DecoderFactory.get().binaryDecoder(encoded, null);
        GenericDatumReader<GenericRecord> datumReader = new GenericDatumReader<>(dct.getSchema());
        GenericRecord record = datumReader.read(null, decoder);

        Assertions.assertThatThrownBy( () -> { DeepCopyTest dctFromDeepCopy = (DeepCopyTest) SpecificData.get().deepCopy(dct.getSchema(), record); } )
            .isInstanceOf(ClassCastException.class)
            .hasMessageStartingWith("org.apache.avro.generic.GenericData$EnumSymbol cannot be cast to");
    }




}
