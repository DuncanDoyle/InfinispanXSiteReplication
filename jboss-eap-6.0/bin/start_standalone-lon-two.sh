#!/bin/sh

./standalone.sh -b 127.0.0.2 -Djboss.bind.address.management=127.0.0.2 -u 233.8.8.8 -Djboss.server.base.dir=../standalone-lon-two -c standalone-ha.xml -Djboss.node.name=standalone-lon-two -Djgroups.bind.address.infinispan=127.0.0.2 -Djboss.mcast.address.infinispan=233.8.8.8.10 -Djgroups.relay2.site=LON -Djgroups.relay2.backupSites=NYC
