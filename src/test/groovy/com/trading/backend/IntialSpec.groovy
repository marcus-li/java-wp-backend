package com.trading.backend

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class IntialSpec extends Specification{

    def "test"(){
        expect:
        1 == 1
        log.info( "helo")
    }
}
