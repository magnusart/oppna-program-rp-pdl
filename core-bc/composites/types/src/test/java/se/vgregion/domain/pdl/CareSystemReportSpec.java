package se.vgregion.domain.pdl;

import org.junit.Before;
import org.junit.Test;
import se.vgregion.domain.decorators.WithBlock;
import se.vgregion.domain.decorators.WithInfoType;
import se.vgregion.domain.decorators.WithOutcome;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CareSystemReportSpec {

    private PdlReport mockReport;

    private PdlContext ctx;

    @Before
    public void setup() throws Exception
    {

        ctx = MockContext.getMockContext();

        ctx = ctx.changeAssignment(MockContext.VE);

        CareSystem sameUnit1 = new CareSystem(CareSystemSource.BFR, ctx.currentAssignment.careProviderHsaId, "VGR", ctx.currentAssignment.careUnitHsaId, "Unit 1");
        CareSystem sameUnit2 = new CareSystem(CareSystemSource.RRE, ctx.currentAssignment.careProviderHsaId, "VGR", ctx.currentAssignment.careUnitHsaId, "Unit 1");


        CareSystem otherUnit1 = new CareSystem(CareSystemSource.BFR, ctx.currentAssignment.careProviderHsaId, "VGR", "otherCareUnitHsaId", "Unit 2");
        CareSystem otherUnit2 = new CareSystem(CareSystemSource.BFR, ctx.currentAssignment.careProviderHsaId, "VGR", "otherCareUnitHsaId", "Unit 2");

        ctx = ctx.changeAssignment(MockContext.SJF);

        CareSystem otherProvider1 = new CareSystem(CareSystemSource.BFR, ctx.currentAssignment.careProviderHsaId, "Capio Axess Lundby", ctx.currentAssignment.careUnitHsaId, "Unit 3");
        CareSystem otherProvider2 = new CareSystem(CareSystemSource.BFR, ctx.currentAssignment.careProviderHsaId, "Capio Axess Lundby", "otherCareUnitHsaId", "Unit 4");

        ArrayList<WithInfoType<WithBlock<CareSystem>>> sourceSystems = new ArrayList<WithInfoType<WithBlock<CareSystem>>>();
        sourceSystems.add(wrapSystem(InformationType.LAK, false, sameUnit1));
        sourceSystems.add(wrapSystem(InformationType.LAK, false, sameUnit2));
        sourceSystems.add(wrapSystem(InformationType.LAK, false, otherUnit1));
        sourceSystems.add(wrapSystem(InformationType.UPP, true, otherUnit2));
        sourceSystems.add(wrapSystem(InformationType.LAK, false, otherProvider1));
        sourceSystems.add(wrapSystem(InformationType.FUN, true, otherProvider2));

        WithOutcome<ArrayList<WithInfoType<WithBlock<CareSystem>>>> systems = WithOutcome.success(sourceSystems);

        WithOutcome<CheckedConsent> consent = WithOutcome.remoteFailure(
                new CheckedConsent(
                        PdlReport.ConsentType.Consent,
                        false
                )
        );

        WithOutcome<Boolean> relationship = WithOutcome.clientError(false);

        mockReport = new PdlReport(
                systems,
                consent,
                relationship
        );
    }

    @Test
    public void CareSystemReportShouldSegmentSystems() throws Exception {

        PdlContext newCtx = ctx.changeAssignment(MockContext.SJF);

        CareSystemsReport report = new CareSystemsReport(newCtx.currentAssignment, mockReport);

        assertEquals(Outcome.SUCCESS, report.aggregatedSystems.outcome);
        assertEquals(1, report.aggregatedSystems.value.size());

    }

    @Test
    public void CareSystemReportShouldRemoveOtherProviders() throws Exception {
        PdlContext newCtx = ctx.changeAssignment(MockContext.VE);

        CareSystemsReport report = new CareSystemsReport(newCtx.currentAssignment, mockReport);

        assertEquals(Outcome.SUCCESS, report.aggregatedSystems.outcome);
        assertEquals(1, report.aggregatedSystems.value.size());
    }

    private WithInfoType<WithBlock<CareSystem>> wrapSystem(
            InformationType informationType,
            Boolean blocked,
            CareSystem system
    ) {
        WithBlock<CareSystem> blockSystem = null;

        if(blocked) blockSystem = WithBlock.blocked(system);
        else blockSystem = WithBlock.unblocked(system);

        return new WithInfoType<WithBlock<CareSystem>>(informationType, blockSystem);
    }
}
