package com.ryonday.test;

import com.google.common.collect.ImmutableList;
import com.ryonday.avro.sux.Genders;
import com.ryonday.avro.sux.Job;
import com.ryonday.avro.sux.Recreation;
import com.ryonday.avro.sux.Relationship;
import com.ryonday.avro.sux.SampleComplexRecord;
import com.ryonday.test.util.EncodeDecodeHelper;
import org.junit.Test;
import org.slf4j.Logger;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

public class ComplexRecordTest {

    private static final Logger logger = getLogger(ComplexRecordTest.class);

    private final static SampleComplexRecord complexRecord = SampleComplexRecord.newBuilder()
        .setAge("Ten dog years")
        .setGender(Genders.M)
        .setLocation("Mom's basement")
        .setRelationship(Relationship.newBuilder()
            .setName("sally")
            .setDuration(60)
            .build()
        )
        .setJob(Job.newBuilder()
            .setDuties(ImmutableList.of("Pressing a button",
                "pressing another button",
                "lunch"))
            .setEmotions(ImmutableList.of(
                "Anguish",
                "Boredom",
                "Anger when I deal with Avro",
                "Fury when I see a UTF8 object in an Avro domain object"))
            .setEmployer("Dad")
            .setHappy(false)
            .setSalary(100.0F)
            .setStartDate(9837495082374509L)
            .setYearsWorked(1)
            .setBytes(ByteBuffer.allocate(2).put((byte)255).put((byte)0))
            .build()
        ).setActivities(ImmutableList.of(
            Recreation.newBuilder()
                .setActivity("Darts")
                .setTimeSpent(100.0)
                .build(),
            Recreation.newBuilder()
                .setActivity("Tiddlywinks")
                .setTimeSpent(11000239023.0)
                .build(),
            Recreation.newBuilder()
                .setActivity("CandyLand")
                .setTimeSpent(9384593749853.0)
                .build()
        ))
        .build();

    @Test
    public void testSerializingAndDeserializingIt() throws Exception {

        String asJson = EncodeDecodeHelper.encodeToJson(complexRecord);
        byte[] asBytes = EncodeDecodeHelper.encodeToByteArray(complexRecord);
        String asByteString = EncodeDecodeHelper.encodeToByteArrayString( complexRecord );

        logger.info("Some decoding:\n\t" +
            "Original Record:    {}\n\t" +
            "As JSON:            {}\n\t" +
            "As byte array:      {}\n\t" +
            "As String of bytes: {}",
            complexRecord,
            asJson, asBytes, asByteString
        );

        SampleComplexRecord decodedFromJson = EncodeDecodeHelper.decodeFromJson( asJson, SampleComplexRecord.getClassSchema() );
        SampleComplexRecord decodedFromBytes = EncodeDecodeHelper.decodeFromBytes( asBytes, SampleComplexRecord.getClassSchema() );

        assertEquals( complexRecord, decodedFromJson );
        assertEquals( complexRecord, decodedFromBytes );

    }
}
