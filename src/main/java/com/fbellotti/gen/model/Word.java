package com.fbellotti.gen.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@XmlRootElement(name="word")
@XmlAccessorType(XmlAccessType.FIELD)
public class Word {

  private int id;
  private String label;

  public Word() {
    // empty
  }

  public Word(int id, String label) {
    this.id = id;
    this.label = label;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String toString() {
    return "Word[id=" + id + ", label=" + label + "]";
  }
}
