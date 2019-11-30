package Message;

import java.util.Date;

/**
 * Idővel eláátott üzenetet reprezentál
 */
public class MessageTimeStamp extends Message
{
    private Date timestamp;
//    private static DateFormat todayDateFormat = new SimpleDateFormat("HH:mm:ss");      //Kiírásokhoz
//    private static DateFormat thisYearFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
//    private static DateFormat longAgoFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Konstruktor, ezt használjuk mindig
     * @param msg Az átalakítandó üzenet, ezt látjuk el a jelenlegi idővel
     */
    public MessageTimeStamp(Message msg)
    {
        super(msg.getType(), msg.getText(), msg.getSender(), msg.getReceiver());
        this.timestamp = new Date();
    }

//    @Override
//    public String toString()
//    {
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE, -1);     //Tegnapi nap
//        if(timestamp.after(cal.getTime()))  //Ha mai az üzenet
//        {
//            return(todayDateFormat.format(timestamp)) + "  " + super.toString();
//        }
//        cal.add(Calendar.YEAR, -1);     //Ha idei az üzenet
//        if(timestamp.after(cal.getTime()))
//        {
//            return thisYearFormat.format(timestamp) + super.toString();
//        }
//        else return longAgoFormat.format(timestamp) + super.toString();
//    }
}
