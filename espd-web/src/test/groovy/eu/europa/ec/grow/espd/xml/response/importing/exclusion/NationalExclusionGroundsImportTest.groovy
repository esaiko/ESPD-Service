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

package eu.europa.ec.grow.espd.xml.response.importing.exclusion
import eu.europa.ec.grow.espd.domain.AvailableElectronically
import eu.europa.ec.grow.espd.domain.EspdDocument
import eu.europa.ec.grow.espd.domain.PurelyNationalGrounds
import eu.europa.ec.grow.espd.domain.SelfCleaning
import eu.europa.ec.grow.espd.xml.base.AbstractXmlFileImport
import org.apache.commons.io.IOUtils
/**
 * Created by ratoico on 1/8/16 at 10:59 AM.
 */
class NationalExclusionGroundsImportTest extends AbstractXmlFileImport {

    def "21. should import all fields of 'Purely national exclusion grounds'"() {
        given:
        def espdResponseXml = importXmlResponseFile("exclusion/purely_national_grounds_import.xml")

        when:
        EspdDocument espd = marshaller.importEspdResponse(IOUtils.toInputStream(espdResponseXml)).get()

        then:
        espd.purelyNationalGrounds.exists == true
        espd.purelyNationalGrounds.answer == true
        espd.purelyNationalGrounds.description == "Hodor is national"

        then: "self cleaning"
        espd.purelyNationalGrounds.selfCleaning.answer == true
        espd.purelyNationalGrounds.selfCleaning.description == "Hodor24 is clean"

        then: "info electronically"
        espd.purelyNationalGrounds.availableElectronically.answer == true
        espd.purelyNationalGrounds.availableElectronically.url == "www.hodor.com"
        espd.purelyNationalGrounds.availableElectronically.code == "NATIONAL"
        espd.purelyNationalGrounds.availableElectronically.issuer == "HODOR"
    }

    def "all fields needed to generate a XML sample"() {
        given:
        def espd = new EspdDocument(purelyNationalGrounds: new PurelyNationalGrounds(exists: true,  answer: true,
                description: "Hodor is national",
                selfCleaning: new SelfCleaning(answer: true, description: "Hodor24 is clean"),
                availableElectronically: new AvailableElectronically(answer: true, url: "www.hodor.com", code: "NATIONAL")))
//        saveEspdAsXmlResponse(espd, "/home/ratoico/Downloads/espd-response.xml")

        expect:
        1 == 1
    }

}