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

package eu.europa.ec.grow.espd.xml.response.importing.selection

import eu.europa.ec.grow.espd.domain.AvailableElectronically
import eu.europa.ec.grow.espd.domain.EspdDocument
import eu.europa.ec.grow.espd.domain.TechnicalProfessionalCriterion
import eu.europa.ec.grow.espd.xml.base.AbstractXmlFileImport
import org.apache.commons.io.IOUtils

/**
 * Created by ratoico on 1/8/16 at 1:49 PM.
 */
class TechnicalFacilitiesQualityImportTest extends AbstractXmlFileImport {

    def "19. should import all fields of 'Technical facilities and measures for ensuring quality'"() {
        given:
        def espdResponseXml = importXmlResponseFile("selection/technical_facilities_quality_import.xml")

        when:
        EspdDocument espd = marshaller.importEspdResponse(IOUtils.toInputStream(espdResponseXml)).get()

        then:
        espd.technicalFacilitiesMeasures.exists == true

        then: "answer is null and it is a selection criterion so the default value should be true"
        espd.technicalFacilitiesMeasures.answer == true
        espd.technicalFacilitiesMeasures.description == "Another description"

        then: "info electronically"
        espd.technicalFacilitiesMeasures.availableElectronically.answer == true
        espd.technicalFacilitiesMeasures.availableElectronically.url == "www.hodor.com"
        espd.technicalFacilitiesMeasures.availableElectronically.code == "TECHNICAL_QUALITY"
        espd.technicalFacilitiesMeasures.availableElectronically.issuer == "HODOR"
    }

    def "all fields needed to generate a XML sample"() {
        given:
        def espd = new EspdDocument(technicalFacilitiesMeasures: new TechnicalProfessionalCriterion(exists: true, answer: true,
                description: "Another description",
                availableElectronically: new AvailableElectronically(answer: true, url: "www.hodor.com", code: "TECHNICAL_QUALITY", issuer: "HODOR")))
//                saveEspdAsXmlResponse(espd, "/home/ratoico/Downloads/espd-response.xml")

        expect:
        1 == 1
    }

}