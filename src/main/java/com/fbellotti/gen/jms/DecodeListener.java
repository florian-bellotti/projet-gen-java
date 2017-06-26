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
  private float minFiability;
  private JmsProducer jmsProducer;
  private Words words;
  private Map<String, Integer> maxLoopByFileName;


  public DecodeListener(DecodedFileDao daoDecodedFile, float minFiability,
                        JmsProducer jmsProducer, Words words) {
    super();
    this.daoDecodedFile = daoDecodedFile;
    this.minFiability = minFiability;
    this.jmsProducer = jmsProducer;
    this.words = words;
    this.maxLoopByFileName = new HashMap<>();
  }

  @Override
  public void onMessage(Message msg) {
    try {
      TextMessage textMessage = (TextMessage) msg;
      if (textMessage == null) return;
      if (textMessage.getText() == null) return;

      String key = msg.getStringProperty("key");
      String fileName = msg.getStringProperty("fileName");
      String md5 = msg.getStringProperty("md5");
      int maxLoop = msg.getIntProperty("maxLoop");
      int contains = 0;

      if (key == null || fileName == null || md5 == null || maxLoop == 0) {
        LOG.error("key, fileName, md5 or maxLoop are null");
        return;
      }

      if (maxLoopByFileName.get(fileName) == null) {
        maxLoopByFileName.put(fileName, 0);
      }

      int count = maxLoopByFileName.get(fileName);
      maxLoopByFileName.put(fileName, count + 1);

      String[] wordsTab = textMessage.getText().split(" ");
      System.out.print(words.getWords().size());

      List<String> sendedWords = Arrays.asList(wordsTab);
      Set<String> wordsDico = words.getWords();

      // find matching words
      for (String word :  sendedWords) {
        if (wordsDico.contains(word)) {
          contains++;
        }
      }
      float ratio = (float) contains / sendedWords.size();

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

        if (!daoDecodedFile.isAlreadyExist(fileName, wordsTab[0])) {
          LOG.info("File " + fileName + " was decoded with key " + key);
          daoDecodedFile.create(decodedFile);
        }
        System.out.println("Send " + fileName );
        jmsProducer.produce(key, fileName, key, secret);
      }

      if (count == maxLoop) {
        System.out.println("send null");
        jmsProducer.produce("", fileName, "", "");
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
