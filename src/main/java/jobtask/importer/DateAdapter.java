package jobtask.importer;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

class DateAdapter extends XmlAdapter<String, Date> {
    
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Date unmarshal(String v) throws Exception {
        return format.parse(v);
    }

    @Override
    public String marshal(Date v) throws Exception {
        return format.format(v);
    }
}
