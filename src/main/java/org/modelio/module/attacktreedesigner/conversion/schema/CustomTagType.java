package org.modelio.module.attacktreedesigner.conversion.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>Java class for customTagType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="customTagType">
 * &lt;simpleContent>
 * &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 * &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 * &lt;/extension>
 * &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "customTagType", propOrder = {
    "value"
})
public class CustomTagType {
    @XmlValue
    protected String value;

    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the value property.
     * @return
     * possible object is
     * {@link String }
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value allowed object is
     * {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the name property.
     * @return
     * possible object is
     * {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value allowed object is
     * {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

}
