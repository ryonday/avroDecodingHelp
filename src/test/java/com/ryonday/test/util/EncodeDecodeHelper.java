package com.ryonday.test.util;


import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class EncodeDecodeHelper {

    private static final Logger logger = getLogger(EncodeDecodeHelper.class);

    public static String encodeToJson(SpecificRecord record) throws Exception {
        return new String(encode(record, jsonEncoder(record.getSchema())), Charsets.UTF_8);

    }

    public static byte[] encodeToByteArray(SpecificRecord record) throws Exception {
        return encode(record, binaryEncoder);
    }

    public static String encodeToByteArrayString(SpecificRecord record) throws Exception {
        return Arrays.toString(encodeToByteArray(record));
    }

    private static byte[] encode(SpecificRecord record, Function<OutputStream, Encoder> encoderFn) throws Exception {
        checkNotNull(record, "Cannot encode null record.");
        logger.info("Received Record: {}", record);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SpecificDatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(record.getSchema());
        Encoder encoder = encoderFn.apply(baos);
        writer.write(record, encoder);
        encoder.flush();
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    private static Function<OutputStream, Encoder> binaryEncoder = o -> EncoderFactory.get().binaryEncoder(o, null);

    private static Function<OutputStream, Encoder> jsonEncoder(Schema s) {
        checkNotNull(s, "Cannot encode with null schema.");
        return o -> {
            try {
                return EncoderFactory.get().jsonEncoder(s, o);
            } catch (Exception e) {
                logger.error("Received error created JSON encoder: {}", e);
                throw new RuntimeException(e);
            }
        };

    }

    public static <T extends SpecificRecord> T decodeFromJson(String json, Schema schema) throws Exception {
        checkNotNull(json, "Cannot decode null JSON.");
        checkNotNull(schema, "Cannot decode using null schema.");

        Decoder decoder = DecoderFactory.get().jsonDecoder(schema, json);

        return decode( decoder, schema);
    }

    public static <T extends SpecificRecord> T decodeFromBytes(byte[] bytes, Schema schema) throws Exception {
        checkNotNull(bytes, "Cannot decode null bytes.");

        Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);

        return decode( decoder, schema);
    }

    private static <T extends SpecificRecord> T decode(Decoder decoder, Schema schema) throws Exception {

        SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);

        return reader.read(null, decoder);

    }

    public static void writeRecordsToFile(Schema s, String fileName, int amount, Supplier<SpecificRecord> supplier) throws Exception {
        DataFileWriter<SpecificRecord> writer = new DataFileWriter<>(new SpecificDatumWriter<>(s));

        writer.create(s, new File(fileName));

        for( int i = 0 ; i < amount ; ++i ) {
            SpecificRecord x = supplier.get();
            logger.info("Writing record to file '{}': {}", fileName, x );
            writer.append( x );
        }

        writer.close();
    }

    public static List<SpecificRecord> readRecordsFromFile(Schema expectedSchema, String fileName) throws Exception {
        DataFileStream<SpecificRecord> dataFileStream;
        DatumReader<SpecificRecord> datumReader = new SpecificDatumReader<>(expectedSchema);

        ImmutableList.Builder<SpecificRecord> bld = ImmutableList.builder();

            dataFileStream = new DataFileStream<>(new FileInputStream(fileName), datumReader);
            Schema actualSchema = dataFileStream.getSchema();

            int recordNo = 0;

            SpecificRecord record;

            while (dataFileStream.hasNext()) {
                ++recordNo;
                record = dataFileStream.next();
                logger.debug("Received message {} from Avro datafile: {}", recordNo, record);
                try {
                    bld.add( record );
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }

            dataFileStream.close();

        return bld.build();
    }


}