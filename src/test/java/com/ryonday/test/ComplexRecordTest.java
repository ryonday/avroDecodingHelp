package com.ryonday.test;

import com.google.common.collect.ImmutableList;
import com.ryonday.avro.sux.Genders;
import com.ryonday.avro.sux.Job;
import com.ryonday.avro.sux.Recreation;
import com.ryonday.avro.sux.Relationship;
import com.ryonday.avro.sux.SampleComplexRecord;
import com.ryonday.test.util.EncodeDecodeHelper;
import org.apache.avro.specific.SpecificRecord;
import org.junit.Test;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import static com.ryonday.test.util.EncodeDecodeHelper.readRecordsFromFile;
import static com.ryonday.test.util.EncodeDecodeHelper.writeRecordsToFile;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

public class ComplexRecordTest {

    private static final Logger logger = getLogger(ComplexRecordTest.class);

    private static SampleComplexRecord mkComplexRecord() {
        Random r = new Random();
        return SampleComplexRecord.newBuilder()
            .setAge("Ten dog years" + r.nextInt())
            .setGender( r.nextBoolean() ? Genders.F : Genders.M)
            .setLocation("Mom's basement" + r.nextInt())
            .setRelationship(Relationship.newBuilder()
                .setName("sally" + r.nextInt() )
                .setDuration(r.nextInt())
                .build()
            )
            .setJob(Job.newBuilder()
                .setDuties(ImmutableList.of("Pressing a button",
                    "pressing another button",
                    "lunch" + r.nextInt()))
                .setEmotions(ImmutableList.of(
                    "Anguish",
                    "Boredom",
                    "Anger when I deal with Avro",
                    "Fury when I see a UTF8 object in an Avro domain object"))
                .setEmployer("Dad" + r.nextInt())
                .setHappy(r.nextBoolean())
                .setSalary(r.nextFloat())
                .setStartDate(r.nextLong())
                .setYearsWorked(r.nextInt())
                .setBytes(ByteBuffer.allocate(2).put((byte)r.nextInt()).put((byte) r.nextInt()))
                .build()
            ).setActivities(ImmutableList.of(
                Recreation.newBuilder()
                    .setActivity("Darts" + r.nextInt())
                    .setTimeSpent(r.nextFloat())
                    .build(),
                Recreation.newBuilder()
                    .setActivity("Tiddlywinks"+ r.nextInt())
                    .setTimeSpent(r.nextFloat())
                    .build(),
                Recreation.newBuilder()
                    .setActivity("CandyLand" + r.nextInt())
                    .setTimeSpent(r.nextFloat())
                    .build()
            ))
            .build();
    }

    @Test
    public void testSerializingAndDeserializingIt() throws Exception {

        SampleComplexRecord complexRecord = mkComplexRecord();

        String asJson = EncodeDecodeHelper.encodeToJson(complexRecord);
        byte[] asBytes = EncodeDecodeHelper.encodeToByteArray(complexRecord);
        String asByteString = EncodeDecodeHelper.encodeToByteArrayString(complexRecord);

        logger.info("Some decoding:\n\t" +
                "Original Record:    {}\n\t" +
                "As JSON:            {}\n\t" +
                "As byte array:      {}\n\t" +
                "As String of bytes: {}",
            complexRecord,
            asJson, asBytes, asByteString
        );

        SampleComplexRecord decodedFromJson = EncodeDecodeHelper.decodeFromJson(asJson, SampleComplexRecord.getClassSchema());
        SampleComplexRecord decodedFromBytes = EncodeDecodeHelper.decodeFromBytes(asBytes, SampleComplexRecord.getClassSchema());

        assertEquals(complexRecord, decodedFromJson);
        assertEquals(complexRecord, decodedFromBytes);

    }

    @Test
    public void testItCanWriteRecordsToAFileAndWriteThemBack() throws Exception {

        writeRecordsToFile(SampleComplexRecord.getClassSchema(), "Test.avro", 100, ComplexRecordTest::mkComplexRecord);
        List<SpecificRecord> records = readRecordsFromFile( SampleComplexRecord.getClassSchema(), "Test.avro");

        logger.info("Read {} records from file.", records.size());
    }


}
