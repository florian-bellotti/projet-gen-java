package com.fbellotti.gen.jms;

import com.fbellotti.gen.dao.DecodedFileDao;
import com.fbellotti.gen.dao.DictionaryDao;
import com.fbellotti.gen.model.Words;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Component
public class JmsConsumer {

  private static final Logger LOG = LoggerFactory.getLogger(JmsConsumer.class);

  // url should be
  @Autowired
  public JmsConsumer(String url, String listenerQueue, int listenerNb, DictionaryDao daoDictionary,
                     DecodedFileDao daoDecodedFile, float minFiability, JmsProducer jmsProducer, Words words) {
    ConnectionFactory factory = new ActiveMQConnectionFactory(url);
    try {
      Connection connection = factory.createConnection();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = session.createQueue(listenerQueue);

      // init all listeners
      for (int i = 0;  i < listenerNb ; i++) {
        MessageConsumer consumer = session.createConsumer(queue);
        DecodeListener listener = new DecodeListener(daoDictionary, daoDecodedFile, minFiability, jmsProducer, words);
        consumer.setMessageListener(listener);
      }
      LOG.info(listenerNb + " jms listeners created");

      // start the connection
      connection.start();
    } catch(JMSException e) {
      LOG.error("Error during JmsConsumer initialisation " + e);
    }
  }
}
