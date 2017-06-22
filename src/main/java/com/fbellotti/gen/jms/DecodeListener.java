package com.fbellotti.gen.jms;

import com.fbellotti.gen.dao.DecodedFileDao;
import com.fbellotti.gen.dao.DictionaryDao;
import com.fbellotti.gen.model.DecodedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
public class DecodeListener implements MessageListener {

  private static final Logger LOG = LoggerFactory.getLogger(DecodeListener.class);

  private DictionaryDao daoDictionary;
  private DecodedFileDao daoDecodedFile;
  private float minFiability;
  private JmsProducer jmsProducer;

  public DecodeListener(DictionaryDao daoDictionary, DecodedFileDao daoDecodedFile,
                        float minFiability, JmsProducer jmsProducer) {
    super();
    this.daoDictionary = daoDictionary;
    this.daoDecodedFile = daoDecodedFile;
    this.minFiability = minFiability;
    this.jmsProducer = jmsProducer;
  }

  @Override
  public void onMessage(Message msg) {
    try {
      TextMessage textMessage = (TextMessage) msg;

      String key = msg.getStringProperty("key");
      String fileName = msg.getStringProperty("fileName");
      String md5 = msg.getStringProperty("md5");

      if (key == null || fileName == null || md5 == null) {
        LOG.error("key, fileName or md5 are null");
        return;
      }

      String[] wordsTab = textMessage.getText().split(" ");
      List<String> words = Arrays.asList(wordsTab);

      // find matching words
      List<String> findedWords = daoDictionary.findWords(words);
      float ratio = (float) findedWords.size() / words.size();

      // if the fiability is higher than minFiability
      if (ratio >= minFiability) {
        DecodedFile decodedFile = new DecodedFile();
        decodedFile.setDecodeKey(key);
        decodedFile.setFileName(fileName);
        decodedFile.setFirstWorld(wordsTab[0]);
        decodedFile.setMd5(md5);

        // find the secret
        String secret = findEmail(textMessage.getText());
        if (secret != null) {
          decodedFile.setSecret(secret);
        }

        LOG.info("File " + fileName + " was decoded with key " + key);
        jmsProducer.produce(key, fileName);
        daoDecodedFile.create(decodedFile);
      }
      System.out.println(Thread.currentThread().getName() + " ratio : " + ratio);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  private String findEmail(String text) {
    Pattern patt = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    Matcher matcher = patt.matcher(text);
    if (matcher.find()) {
      return matcher.group();
    } else {
      return null;
    }
  }
}
