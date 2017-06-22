package com.fbellotti.gen.model;

import com.fbellotti.gen.dao.DictionaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Component
public class Words {

  private DictionaryDao daoDictionary;
  private Set<String> words;

  @Autowired
  public Words(DictionaryDao daoDictionary) {
    this.words = daoDictionary.findAll();
  }

  public Set<String> getWords() {
    return words;
  }

  public void setWords(Set<String> words) {
    this.words = words;
  }
}
