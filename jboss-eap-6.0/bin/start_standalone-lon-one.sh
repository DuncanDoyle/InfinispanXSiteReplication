#!/bin/sh

./standalone.sh -b 127.0.0.1 -Djboss.bind.address.management=127.0.0.1 -u 233.8.8.8 -Djboss.server.base.dir=../standalone-lon-one -c standalone-ha.xml -Djboss.node.name=standalone-lon-one -Djgroups.bind.address.infinispan=127.0.0.1 -Djboss.mcast.address.infinispan=233.8.8.8.10 -Djgroups.relay2.site=LON -Djgroups.relay2.backupSites=NYC
