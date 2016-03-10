package com.ryonday.test;

import com.ryonday.avro.sux.ThisShouldWork;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

public class AvroDecodingHelp {

    private static final Logger logger = getLogger(AvroDecodingHelp.class);

    private static Schema schema;
    private static Schema thisShouldworkSchema;

    @BeforeClass
    public static void setUp() throws Exception {

        File schemaFile = new File("src/main/avro/avrodecodinghelp.avsc");
        logger.info("Schema File: {}", schemaFile.getAbsolutePath());
        schema = new Schema.Parser().parse(schemaFile);
        logger.info("Schema: {}", schema);
        thisShouldworkSchema = ThisShouldWork.getClassSchema();
        logger.info("ThisShouldWorkSchema: {}", thisShouldworkSchema);
    }


    @Test(expected = ClassCastException.class)
    public void testEncodeGenericRecordUsingGenericEncoderAndDecodeToSepcificRecordUsingSpecificDecoderAndSameSchemaForReadAndWrite() throws Exception {

        GenericRecord thisShouldWork = new GenericData.Record(thisShouldworkSchema);

        thisShouldWork.put("age", "999");
        thisShouldWork.put("gender", "F");
        thisShouldWork.put("location", "work");

        logger.debug("This is the GenericRecord: {}", thisShouldWork);
        ThisShouldWork outRecord = encodeUsingGenericDecodeUsingSpecific(thisShouldWork, schema, schema);

        logger.debug("OutRecord: {}", outRecord);

    }

    @Test(expected = ClassCastException.class)
    public void testEncodeSpecificRecordUsingGenericEncoderAndDecodeToSpecifiRecordUsingSpecificDecoderAndSameSchemaForReadAndWrite() throws Exception {

        ThisShouldWork thisShouldWork = ThisShouldWork
            .newBuilder()
            .setAge("999")
            .setGender("F")
            .setLocation("work")
            .build();

        logger.debug("This is the SpecificRecord: {}", thisShouldWork);
        ThisShouldWork outRecord = encodeUsingGenericDecodeUsingSpecific(thisShouldWork, schema, schema);

        logger.debug("OutRecord: {}", outRecord);

    }

    @Test
    public void testEncodeSpecificRecordUsingSpecificEncoderAndDecodeUsingSpecificEncoderAndSameSchemaForReadAndWrite() throws Exception {

        ThisShouldWork thisShouldWork = ThisShouldWork
            .newBuilder()
            .setAge("999")
            .setGender("F")
            .setLocation("work")
            .build();

        logger.debug("This is the SpecificRecord: {}", thisShouldWork);
        ThisShouldWork outRecord = encodeUsingSpecificDecodeUsingSpecific(thisShouldWork,
            ThisShouldWork.getClassSchema(),
            ThisShouldWork.getClassSchema());

        logger.debug("OutRecord: {}", outRecord);

    }

    @Test
    public void testEncodeGenericRecordUsingGenericEncoderAndDecodeToSepcificRecordUsingSpecificDecoderAndDifferentSchemasForReadAndWrite() throws Exception {

        GenericRecord thisShouldWork = new GenericData.Record(thisShouldworkSchema);

        thisShouldWork.put("age", "999");
        thisShouldWork.put("gender", "F");
        thisShouldWork.put("location", "work");

        logger.debug("This is the GenericRecord: {}", thisShouldWork);
        ThisShouldWork outRecord = encodeUsingGenericDecodeUsingSpecific(thisShouldWork, schema,
            ThisShouldWork.getClassSchema());

        logger.debug("OutRecord: {}", outRecord);

    }

    @Test
    public void testEncodeSpecificRecordUsingGenericEncoderAndDecodeToSpecifiRecordUsingSpecificDecoderAndDifferentSchemaForReadAndWrite() throws Exception {

        ThisShouldWork thisShouldWork = ThisShouldWork
            .newBuilder()
            .setAge("999")
            .setGender("F")
            .setLocation("work")
            .build();

        logger.debug("This is the SpecificRecord: {}", thisShouldWork);
        ThisShouldWork outRecord = encodeUsingGenericDecodeUsingSpecific(thisShouldWork, schema,
            ThisShouldWork.getClassSchema());

        logger.debug("OutRecord: {}", outRecord);

    }

    @Test
    public void testEncodeSpecificRecordUsingSpecificEncoderAndDecodeUsingSpecificEncoderAndDifferentSchemaForReadAndWrite() throws Exception {

        ThisShouldWork thisShouldWork = ThisShouldWork
            .newBuilder()
            .setAge("999")
            .setGender("F")
            .setLocation("work")
            .build();

        logger.debug("This is the SpecificRecord: {}", thisShouldWork);
        ThisShouldWork outRecord = encodeUsingSpecificDecodeUsingSpecific(thisShouldWork,
            schema,
            ThisShouldWork.getClassSchema());

        logger.debug("OutRecord: {}", outRecord);

    }

    public static <T extends SpecificRecord> T encodeUsingGenericDecodeUsingSpecific(GenericRecord in,
                                                                                     Schema writerSchema,
                                                                                     Schema readerSchema) throws Exception {

        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(writerSchema);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        datumWriter.write(in, encoder);
        encoder.flush();
        byte[] inBytes = out.toByteArray();
        out.close();
        logger.debug("Wrote {} bytes.", inBytes);
        logger.debug("Bytes to String: {}", Arrays.toString(inBytes));
        logger.debug("String from Bytes: {}", new String(inBytes));

        SpecificDatumReader<T> reader = new SpecificDatumReader<>(writerSchema, readerSchema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(inBytes, null);
        T result = reader.read(null, decoder);
        logger.debug("Read record {}", result.toString());

        return result;
    }

    public static <T extends SpecificRecord> T encodeUsingSpecificDecodeUsingSpecific(T in, Schema writerSchema,
                                                                                      Schema readerSchema) throws Exception {

        DatumWriter<T> datumWriter = new SpecificDatumWriter<>(writerSchema);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        datumWriter.write(in, encoder);
        encoder.flush();
        byte[] inBytes = out.toByteArray();
        out.close();
        logger.debug("Wrote {} bytes.", inBytes);
        logger.debug("Bytes to String: {}", Arrays.toString(inBytes));
        logger.debug("String from Bytes: {}", new String(inBytes));
        SpecificDatumReader<T> reader = new SpecificDatumReader<>(writerSchema, readerSchema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(inBytes, null);
        T result = reader.read(null, decoder);
        logger.debug("Read record {}", result.toString());

        return result;
    }
}
