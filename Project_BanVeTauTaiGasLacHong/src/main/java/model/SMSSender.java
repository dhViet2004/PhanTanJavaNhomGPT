//package model;
//
//import com.twilio.Twilio;
//import com.twilio.rest.api.v2010.account.Message;
//import com.twilio.type.PhoneNumber;
//
//public class SMSSender {
//
//    // Thay thế bằng thông tin thật của bạn từ Twilio Console
//    public static final String ACCOUNT_SID = "AC84ea01987a4d4ad78e298310d85aedf8";
//    public static final String AUTH_TOKEN = "ef785954400cc0ebf05f88e504592c5b";
//    public static final String TWILIO_PHONE_NUMBER = "+84356307125";
//
//    public static void sendSMS(String toPhoneNumber, String messageBody) {
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//
//        Message message = Message.creator(
//                new PhoneNumber(toPhoneNumber),      // Số nhận
//                new PhoneNumber(TWILIO_PHONE_NUMBER),// Số gửi (Twilio cung cấp)
//                messageBody                          // Nội dung tin nhắn
//        ).create();
//
//        System.out.println("Tin nhắn đã gửi thành công. SID: " + message.getSid());
//    }
//
//    public static void main(String[] args) {
//        // Thay bằng số điện thoại của bạn (phải đã verify với Twilio nếu đang dùng trial)
//        sendSMS("+849xxxxxxxx", "Chào bạn! Đây là tin nhắn từ ứng dụng Java sử dụng Twilio.");
//    }
//}
//
