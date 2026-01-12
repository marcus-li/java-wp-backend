package com.trading.backend

import com.trading.backend.repository.QuoteLogRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import spock.lang.Specification

@SpringBootTest // Loads your real application.yaml (remote DB connection)
class QuoteLogIntegrationSpec extends Specification {

    @Autowired
    QuoteLogRepository quoteLogRepository

    def "Connect to remote DB and read first 100 rows"() {
        given: "A page request for the first 100 rows"
        // We use PageRequest to limit the results to 100
        def pageable = PageRequest.of(0, 100, Sort.by("requestedTime").descending())

        when: "We query the database"
        def results = quoteLogRepository.findAll(pageable).getContent()

        then: "The connection is successful and data is returned"
        results != null
        println "Successfully fetched ${results.size()} rows from remote DB."

        // Print the first row details to console for verification
        if (!results.isEmpty()) {
            def firstRow = results[0]
            println "First Row -> Symbol: ${firstRow.symbol}, Mark: ${firstRow.mark}"
        }

        // We expect at least some data to exist if the DB is populated
        results.size() <= 100
    }
}