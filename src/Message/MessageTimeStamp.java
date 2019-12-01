package Message;

import Cryptography.Cryptography;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Idővel eláátott üzenetet reprezentál, ezt használjuk a chat ablakba való kiíráshoz
 */
public class MessageTimeStamp extends Message
{
    private Date timestamp;
    private String decodedText; //Ez az objektum csak kliensoldali, nem küldjük el, benne lehet dekódolva
    private static DateFormat todayDateFormat = new SimpleDateFormat("HH:mm:ss");      //Kiírásokhoz
    private static DateFormat thisYearFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
    private static DateFormat longAgoFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Konstruktor, ezt használjuk mindig
     * @param msg Az átalakítandó üzenet, ezt látjuk el a jelenlegi idővel
     */
    public MessageTimeStamp(Message msg)
    {
        super(msg.getType(), msg.getText(), msg.getSender(), msg.getReceiver());
        this.timestamp = new Date();
        this.decodedText = Cryptography.decryptToString(this.getText());
    }

    @Override
    public String toString()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);     //Tegnapi nap
        if(timestamp.after(cal.getTime()))  //Ha mai az üzenet
        {
            return(todayDateFormat.format(timestamp)) + "  " + getSender() + ": " + decodedText;
        }
        cal.add(Calendar.YEAR, -1);     //Előző év
        if(timestamp.after(cal.getTime())) //Ha idei az üzenet
        {
            return thisYearFormat.format(timestamp) + "  " + getSender() + ": " + decodedText;
        }
        else return longAgoFormat.format(timestamp) + "  " + getSender() + ": " + decodedText;
    }
}
