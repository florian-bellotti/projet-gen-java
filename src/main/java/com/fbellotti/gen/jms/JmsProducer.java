package com.fbellotti.gen.jms;

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
public class JmsProducer {

  private static final Logger LOG = LoggerFactory.getLogger(JmsProducer.class);
  private Session session;
  private MessageProducer producer;

  @Autowired
  public JmsProducer(String url, String producerQueue) {
    ConnectionFactory factory = new ActiveMQConnectionFactory(url);
    try {
      Connection connection = factory.createConnection();
      this.session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
      Queue queue = session.createQueue(producerQueue);
      this.producer = session.createProducer(queue);
    } catch(JMSException e) {
      LOG.error("Error during JMSProducer initialisation " + e);
    }
  }

  /**
   * This method allow to send a message in ActiveMQ
   * @param message The message to send
   * @param fileName The fileName to send
   * @param key The key to send
   * @param secret The secret to send
   * @param ratio The ratio to send
   */
  void produce(String message, String fileName, String key, String secret, float ratio) {
    try {
      TextMessage msg = session.createTextMessage();
      msg.setStringProperty("fileName", fileName);
      msg.setStringProperty("key", key);
      msg.setStringProperty("secret", secret);
      msg.setFloatProperty("ratio", ratio);
      msg.setText(message);
      producer.send(msg);
    } catch (JMSException e) {
      LOG.error("Error when send message " + message + e);
    }
  }

}
