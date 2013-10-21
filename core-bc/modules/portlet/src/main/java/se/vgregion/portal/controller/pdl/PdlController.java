package se.vgregion.portal.controller.pdl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import se.vgregion.domain.pdl.*;
import se.vgregion.service.pdl.CareSystems;
import se.vgregion.service.pdl.PatientEngagements;
import se.vgregion.service.pdl.PdlService;

import javax.portlet.ActionResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "VIEW")
@SessionAttributes("state")
public class PdlController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdlController.class.getName());
    @Autowired
    private PatientEngagements patientEngagements;
    @Autowired
    private PdlService pdl;
    @Autowired
    private PdlUserState state;
    @Autowired
    private CareSystems systems;

    private static List<Engagement.InformationType> asInformationTypes(List<Engagement> engagements) {
        ArrayList<Engagement.InformationType> is = new ArrayList<Engagement.InformationType>();
        for (Engagement e : engagements) {
            is.add(e.informationType);
        }
        return is;
    }

    @ModelAttribute("state")
    public PdlUserState initState() {
        if (state.getCtx() == null) {
            state.setCtx(currentContext());
        }
        return state;
    }

    // Matches personnummer: 900313-1245, 990313+1245 (100yrs+)
    // Matches samordningsnummer: 910572-3453
    // Will not verify last control sum number
    //private final static String personSamordningsNummerRegex =
    //        "[0-9]{2}(0[0-9]|1[0-2])(0[0-9]|1[0-9]|2[0-9]|3[0-1]|6[0-9]|7[0-9]|8[0-9]|9[0-1])[-+][0-9]{4}";

    //@RequestMapping(value = "/searchPatient/{ssn:"+personSamordningsNummerRegex+"}", method = RequestMethod.GET)
    //@PathVariable("ssn") String ssn

    @RenderMapping
    public String enterSearchPatient() {
        return "view";
    }

    @ActionMapping("searchPatient")
    public void searchPatientInformation(
            @RequestParam String ssn,
            ActionResponse response
    ) {
        // TODO: 2013-10-15: Magnus Andersson > Validate ssn. Existing libs available?

        LOGGER.trace("Looking for patient {}.", ssn);
        PatientWithEngagements pwe = patientEngagements.forPatient(ssn);
        state.setPwe(pwe);

        PdlReport report = pdl.pdlReport(state.getCtx(), pwe);

        // Engagements belonging to patient.
        List<Engagement.InformationType> information = asInformationTypes(pwe.engagements);
        List<CareSystem> careSystems = systems.byInformationType(information);

        state.setReport(report);
        state.setCareSystems(careSystems);

        List<CareSystem> systemsSameUnit = filterSameUnit(
                state.getCtx().careUnitHsaId,
                careSystems);

        state.setSystemsSameUnit(systemsSameUnit);

        response.setRenderParameter("view", "searchResult");
    }

    private List<CareSystem> filterSameUnit(
            String careUnitHsaId,
            List<CareSystem> careSystems
    ) {
        List<CareSystem> filtered = new ArrayList<CareSystem>();
        for (CareSystem sys : careSystems) {
            if(sys.careUnitHsaId.equals(careUnitHsaId)) {
                filtered.add(sys);
            }
        }
        return filtered;
    }

    @ActionMapping("establishRelationship")
    public void establishRelationship(
            ActionResponse response
    ) {
        LOGGER.trace(
                "Request to create relationship between employee {} and patient {}.",
                state.getCtx().employeeHsaId,
                state.getPwe().patientId
        );

        state.setReport(pdl.patientRelationship(state.getCtx(), state.getReport(), state.getPwe().patientId));

        response.setRenderParameter("view", "searchResult");
    }

    @RenderMapping(params = "view=searchResult")
    public String searchResult(final ModelMap model) {
        return "searchResult";
    }

    private PdlContext currentContext() {
        return new PdlContext(
                "careProviderHsaId",
                "careUnitHsaId",
                "employeeHsaId",
                "Sammanhållen Journalföring",
                "assignmentHsaId");
    }
}