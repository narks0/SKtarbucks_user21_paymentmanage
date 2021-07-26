package local;

import local.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyHandler{

    @Autowired
    PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    // Menu 삭제로 인한 강제 결제취소
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMenuDeleted_ForcedPaymentCancel(@Payload MenuDeleted menuDeleted){

        if(menuDeleted.isMe()){
            System.out.println("##### listener ForcedPaymentCanceled : " + menuDeleted.toJson());

            List<Payment> list = paymentRepository.findByMenuId(menuDeleted.getId());
            for(Payment temp : list){
                if(!"CANCELED".equals(temp.getStatus())) {
                    temp.setStatus("PAYMENT_FORCE_CANCELED");
                    paymentRepository.save(temp);
                }
            }
        }
    }

    // Order 취소로 인한 결제취소
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_PaymentCancel(@Payload OrderCanceled orderCanceled){

        if(orderCanceled.isMe()){
            Payment temp = paymentRepository.findByOrderId(orderCanceled.getId());
            temp.setStatus("PAYMENT_CANCELED");
            paymentRepository.save(temp);

        }
    }

}
