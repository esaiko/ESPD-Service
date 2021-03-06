/*
 *
 * Copyright 2016 EUROPEAN COMMISSION
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 *
 */

package eu.europa.ec.grow.espd.xml.response.importing

import eu.europa.ec.grow.espd.domain.EspdDocument
import eu.europa.ec.grow.espd.domain.enums.other.Country
import eu.europa.ec.grow.espd.xml.LocalDateAdapter
import eu.europa.ec.grow.espd.xml.LocalTimeAdapter
import eu.europa.ec.grow.espd.xml.base.AbstractXmlFileImport
import org.apache.commons.io.IOUtils
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import spock.lang.Shared

/**
 * Created by ratoico on 3/8/16 at 4:11 PM.
 */
class EspdRequestResponseMergeTest extends AbstractXmlFileImport {

    @Shared
    static EspdDocument espd

    void setupSpec() {
        // init objects run before the first feature method
        def espdRequestXml = importXmlRequestFile("../response/merging/request_to_merge.xml")
        def espdResponseXml = importXmlResponseFile("merging/response_to_merge.xml")
        espd = marshaller.mergeEspdRequestAndResponse(IOUtils.toInputStream(espdRequestXml), IOUtils.toInputStream(espdResponseXml)).get()
    }

    void cleanupSpec() {
        espd = null
    }

    def "a merge between Request and Response should contain the criteria from the Request'"() {
        expect: "the Response contains three criteria but the Request only one so only the criterion from the Request should be present"
        espd.criminalConvictions.exists == true
        espd.criminalConvictions.answer == true
        espd.criminalConvictions.reason == "Participation in a criminal organisation reason"

        and: "payment of taxes"
        espd.paymentTaxes.exists == true
        espd.paymentTaxes.answer == false

        and: "enrolment professional register"
        espd.enrolmentProfessionalRegister.exists == true
        espd.enrolmentProfessionalRegister.answer == true

        and: "the other criteria from the response should not be present because they were not required in the request"
        espd.corruption == null
        espd.generalYearlyTurnover == null
    }

    def "should contain the economic operator criteria set in the response"() {
        expect:
        espd.meetsObjective.exists == true

        and: "there is no answer"
        espd.meetsObjective.answer == false
        espd.meetsObjective.description1 == "please describe"

        and: "info electronically"
        espd.meetsObjective.availableElectronically.answer == true
        espd.meetsObjective.availableElectronically.url == "www.hodor.com"
        espd.meetsObjective.availableElectronically.code == "MEETS"
    }

    def "a merge between Request and Response should get the EO information from the Response"() {
        expect:
        espd.economicOperator.name == "ACME Corp."
        espd.economicOperator.website == "www.hodor.com"
        espd.economicOperator.vatNumber == "B207781243"
        espd.economicOperator.anotherNationalId == "eo another national identification number"
        espd.economicOperator.street == "Vitruvio"
        espd.economicOperator.postalCode == "28006"
        espd.economicOperator.city == "Madrid"
        espd.economicOperator.country == Country.ES
        espd.economicOperator.contactName == "hodor"
        espd.economicOperator.contactPhone == "+666"
        espd.economicOperator.contactEmail == "hodor@hodor.com"
        espd.economicOperator.isSmallSizedEnterprise == false
        espd.lotConcerned == "hodor lot"
    }

    def "should import economic operator representative full information"() {
        given:
        def representative = espd.economicOperator.representatives[0]

        expect:
        representative.firstName == "Emilio"
        representative.lastName == "García De Tres Torres"
        representative.dateOfBirth == LocalDateAdapter.unmarshal("1960-01-19").toDate()
        representative.placeOfBirth == "València, Spain"
        representative.street == "Vitruvio"
        representative.postalCode == "28006"
        representative.city == "Madrid"
        representative.country == Country.ES
        representative.email == "emilio.garcia3torres@acme.com"
        representative.phone == "+34 96 123 456"
        representative.position == "Empowered to represent the Consortium"
        representative.additionalInfo == "Can represent ACME, Corp. and the Consortia to which ACME, Corp"
    }

    def "should import espd request full information from the request used in the merge"() {
        expect:
        espd.requestMetadata.id == "3d36dc60-a03f-4294-99fd-54bfe3dc793b"
        espd.requestMetadata.url == null
        espd.requestMetadata.description == "ESPDRequest SMART 2015/0075"
        LocalDateAdapter.marshal(new LocalDate(espd.requestMetadata.issueDate)) == "2016-04-08"
        LocalTimeAdapter.marshal(new LocalTime(espd.requestMetadata.issueDate)) == "13:28:57"
    }

    def "should parse TED procurement procedure information"() {
        expect:
        //fields taken from request
        espd.fileRefByCA == "SMART 2015/0075"
        espd.ojsNumber == "2e556f14-c643-4abc-9177-2f4dycdfh411"
        espd.procedureTitle == "Poland-Kalisz: Stadium construction work"
        espd.procedureShortDesc == "2015/S 206-373046"
        espd.tedUrl == "http://ted.europa.eu/udl?uri=TED:NOTICE:373046-2015:TEXT:EN:HTML"
    }

}