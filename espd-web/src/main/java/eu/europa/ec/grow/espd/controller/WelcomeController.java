package eu.europa.ec.grow.espd.controller;

import eu.europa.ec.grow.espd.business.EspdExchangeMarshaller;
import eu.europa.ec.grow.espd.constants.enums.Country;
import eu.europa.ec.grow.espd.domain.EconomicOperatorImpl;
import eu.europa.ec.grow.espd.domain.EspdDocument;
import org.apache.commons.io.output.CountingOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@SessionAttributes("espd")
class WelcomeController {

    private final EspdExchangeMarshaller exchangeMarshaller;

    @Autowired
    WelcomeController(EspdExchangeMarshaller exchangeMarshaller) {
        this.exchangeMarshaller = exchangeMarshaller;
    }

    @ModelAttribute("espd")
    public EspdDocument newDocument() {
        return new EspdDocument();
    }

    @RequestMapping("/")
    public String getWelcome() {
        return "welcome";
    }

    @RequestMapping("/{page:welcome|filter|print}")
    public String getPage(@PathVariable String page) {
        return page;
    }

    @RequestMapping(value = "/filter", params = "ca_create_espd", method = POST)
    public String requestCACreate(@RequestParam String agent, @RequestParam("authority.country") Country country,
            Map<String, Object> model) throws IOException {
        if (agent.matches("eo|ca")) {
            EspdDocument espd = (EspdDocument) model.get("espd");
            espd.getAuthority().setCountry(country);
            return "redirect:/request/" + agent + "/procedure";
        }
        return null;
    }

    @RequestMapping(value = "/filter", params = "eo_import_espd", method = POST)
    public String requestEOImport(@RequestParam String agent, @RequestParam("authority.country") Country country,
            @RequestParam(required = false) MultipartFile attachment, Map<String, Object> model) throws IOException {
        if (agent.matches("eo|ca")) {
            try (InputStream is = attachment.getInputStream()) {
                EspdDocument espd = importXmlFile(is);
                if (espd.getEconomicOperator() == null) {
                    espd.setEconomicOperator(new EconomicOperatorImpl());
                }
                espd.getEconomicOperator().setCountry(country);
                model.put("espd", espd);
                return "redirect:/response/" + agent + "/procedure";
            }
        }
        return null;
    }

    private EspdDocument importXmlFile(InputStream xmlStream) throws IOException {
        // peek at the first bytes in the file to see if it is a ESPD Request or Response
        BufferedInputStream bis = new BufferedInputStream(xmlStream);
        int readLimit = 80;
        bis.mark(readLimit);
        byte[] peek = new byte[readLimit];
        int bytesRead = bis.read(peek, 0, readLimit - 1);
        String importError = "The uploaded file could not be correctly read. Is it a valid ESPD Request or Response?";
        if (bytesRead < 0) {
            throw new IllegalArgumentException(importError);
        }
        bis.reset(); // need to read from the beginning afterwards
        String firstBytes = new String(peek, "UTF-8");

        // decide how to read the uploaded file
        if (firstBytes.contains("ESPDResponse")) {
            return exchangeMarshaller.importEspdResponse(bis);
        } else if (firstBytes.contains("ESPDRequest")) {
            return exchangeMarshaller.importEspdRequest(bis);
        }
        throw new IllegalArgumentException(importError);
    }

    @RequestMapping("/{flow:request|response}/{agent:ca|eo}/{step:procedure|exclusion|selection|finish}")
    public String view(@PathVariable String flow, @PathVariable String agent, @PathVariable String step,
            @ModelAttribute("espd") EspdDocument espd) {
        return flow + "_" + agent + "_" + step;
    }

    @RequestMapping(value = "/{flow:request|response}/{agent:ca|eo}/{step:procedure|exclusion|selection|finish}", method = POST, params = "prev")
    public String prev(@PathVariable String flow, @PathVariable String agent, @PathVariable String step,
            @RequestParam String prev, @ModelAttribute("espd") EspdDocument espd, BindingResult bindingResult) {
        return bindingResult.hasErrors() ?
                flow + "_" + agent + "_" + step : "redirect:/" + flow + "/" + agent + "/" + prev;
    }

    @RequestMapping(value = "/{flow:request|response}/{agent:ca|eo}/{step:procedure|exclusion|selection|finish|generate}", method = POST, params = "next")
    public String next(@PathVariable String flow, @PathVariable String agent, @PathVariable String step,
            @RequestParam String next,
            @ModelAttribute("espd") EspdDocument espd, HttpServletResponse response, SessionStatus status,
            BindingResult bindingResult) throws IOException {
        if (bindingResult.hasErrors()) {
            return flow + "_" + agent + "_" + step;
        }
        if (!"generate".equals(next)) {
            return "redirect:/" + flow + "/" + agent + "/" + next;
        }

        try (CountingOutputStream out = new CountingOutputStream(response.getOutputStream())) {
            response.setContentType(APPLICATION_XML_VALUE);
            if ("eo".equals(agent)) {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename =\"espd-response.xml\"");
                exchangeMarshaller.generateEspdResponse(espd, out);
            } else {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename =\"espd-request.xml\"");
                exchangeMarshaller.generateEspdRequest(espd, out);
            }
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(out.getByteCount()));
            out.flush();
        } finally {
            status.setComplete();
        }
        return null;
    }

    @InitBinder
    private void dateBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        CustomDateEditor editor = new CustomDateEditor(dateFormat, true);
        binder.registerCustomEditor(Date.class, editor);
    }
}
