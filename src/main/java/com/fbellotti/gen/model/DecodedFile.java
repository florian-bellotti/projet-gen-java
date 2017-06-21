package com.fbellotti.gen.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@XmlRootElement(name="decodedFile")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecodedFile {

  private int id;
  private String fileName;
  private String decodeKey;
  private String md5;
  private String firstWord;
  private String secret;

  public DecodedFile() {
    // empty
  }

  public DecodedFile(int id, String fileName, String decodeKey, String md5, String firstWord, String secret) {
    this.id = id;
    this.fileName = fileName;
    this.decodeKey = decodeKey;
    this.md5 = md5;
    this.firstWord = firstWord;
    this.secret = secret;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDecodeKey() {
    return decodeKey;
  }

  public void setDecodeKey(String decodeKey) {
    this.decodeKey = decodeKey;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public String getFirstWorld() {
    return firstWord;
  }

  public void setFirstWorld(String firstWord) {
    this.firstWord = firstWord;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }
}
