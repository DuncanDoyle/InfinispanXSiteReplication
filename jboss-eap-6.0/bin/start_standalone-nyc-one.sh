#!/bin/sh 

./standalone.sh -b 127.0.0.3 -Djboss.bind.address.management=127.0.0.3 -u 234.8.8.8 -Djboss.server.base.dir=../standalone-nyc-one -c standalone-ha.xml -Djboss.node.name=standalone-nyc-one -Djgroups.bind.address.infinispan=127.0.0.3 -Djboss.mcast.address.infinispan=234.8.8.8.10 -Djgroups.relay2.site=NYC -Djgroups.relay2.backupSites=LON
