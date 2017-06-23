CREATE TABLE `decodedFile` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fileName` varchar(255) NOT NULL,
  `decodeKey` varchar(255) NOT NULL,
  `md5` varchar(255) DEFAULT NULL,
  `firstWord` varchar(255) NOT NULL,
  `secret` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51708 DEFAULT CHARSET=utf8;


CREATE TABLE `dictionary` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `label` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
