package com.fbellotti.gen.jms;

import com.fbellotti.gen.dao.DecodedFileDao;
import com.fbellotti.gen.model.DecodedFile;
import com.fbellotti.gen.model.Words;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
public class DecodeListener implements MessageListener {

  private static final Logger LOG = LoggerFactory.getLogger(DecodeListener.class);
  private DecodedFileDao daoDecodedFile;
  private float minReliability;
  private JmsProducer jmsProducer;
  private Words words;
  private Map<String, Integer> maxLoopByFileName;

  DecodeListener(DecodedFileDao daoDecodedFile, float minReliability, JmsProducer jmsProducer, Words words) {
    super();
    this.daoDecodedFile = daoDecodedFile;
    this.minReliability = minReliability;
    this.jmsProducer = jmsProducer;
    this.words = words;
    this.maxLoopByFileName = new HashMap<>();
  }

  @Override
  public void onMessage(Message msg) {
    try {
      // get the message and stop if is null
      TextMessage textMessage = (TextMessage) msg;
      if (textMessage == null) return;
      if (textMessage.getText() == null) return;

      // get message's properties
      String key = msg.getStringProperty("key");
      String fileName = msg.getStringProperty("fileName");
      String md5 = msg.getStringProperty("md5");
      int maxLoop = msg.getIntProperty("maxLoop");
      int contains = 0;

      // stop if one property is null
      if (key == null || fileName == null || md5 == null || maxLoop == 0) {
        LOG.error("key, fileName, md5 or maxLoop are null");
        return;
      }

      // if the filename was never received, add in the map
      // then increase the count
      maxLoopByFileName.putIfAbsent(fileName, 0);
      int count = maxLoopByFileName.get(fileName);
      maxLoopByFileName.put(fileName, count + 1);

      // split the message in words
      String[] wordsTab = textMessage.getText().split(" ");
      List<String> sendedWords = Arrays.asList(wordsTab);
      Set<String> wordsDico = words.getWords();

      // find matching words
      for (String word :  sendedWords) {
        if (wordsDico.contains(word)) {
          contains++;
        }
      }

      // calculate the ratio
      float ratio = (float) contains / sendedWords.size();

      // if the ratio is higher than minReliability
      if (ratio >= minReliability) {
        // find the secret
        String secret = findEmail(textMessage.getText());

        // check if the file already exist in the database
        if (!daoDecodedFile.isAlreadyExist(fileName, md5)) {
          // init a new decoded file to add this one in the database
          DecodedFile decodedFile = new DecodedFile();
          decodedFile.setDecodeKey(key);
          decodedFile.setFileName(fileName);
          decodedFile.setFirstWorld(wordsTab[0]);
          decodedFile.setMd5(md5);
          if (secret != null) {
            decodedFile.setSecret(secret);
          }
          daoDecodedFile.create(decodedFile);
        }

        LOG.info("The decoded file " + fileName + " with key " + key + " is valid");
        jmsProducer.produce(textMessage.getText(), fileName, key, secret, ratio);
      }

      if (count == maxLoop) {
        LOG.info("The listening process if finished, but no message was valid");
        jmsProducer.produce("", fileName, "", "", 0);
      }
    } catch (JMSException e) {
      LOG.error("Error when try to listen messages from ActiveMQ" + e);
    }
  }

  /**
   * This method allow to return an email contained in a String
   * @param text The text to analyse
   * @return Return the email
   */
  private String findEmail(String text) {
    Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    Matcher matcher = pattern.matcher(text);
    return matcher.find() ? matcher.group() : null;
  }
}
