package jobtask.importer;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "entry")
@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.NONE)
public class Model {
    
    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;
    
    @XmlElement
    @NotNull
    @Size(max = 1024)
    @Column(name = "content")
    private String content;
    
    @XmlElement
    @NotNull
    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creatonDate; 
}
