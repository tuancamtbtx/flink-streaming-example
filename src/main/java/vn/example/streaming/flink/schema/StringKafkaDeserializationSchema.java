package vn.example.streaming.flink.schema;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class StringKafkaDeserializationSchema implements KafkaDeserializationSchema<String> {

    public TypeInformation<String> getProducedType() {
        // TODO Auto-generated method stub
        return TypeExtractor.getForClass(String.class);
    }



    public String deserialize(ConsumerRecord<byte[], byte[]> record) throws Exception {
        // TODO Auto-generated method stub

        String message = new String(record.value());
        String meta = record.partition() + "_"+ record.offset();
        System.out.println(message);
        return message + "&&" + meta;
    }


    public boolean isEndOfStream(String nextElement) {
        // TODO Auto-generated method stub
        return false;
    }
}
