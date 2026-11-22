package com.jk.apps.banking.fx.sor.qual.grpc;

import com.jk.apps.banking.fx.sor.qual.business.facade.CreditLineAdminFacade;
import com.jk.apps.banking.fx.sor.qual.business.facade.SettlementPolicyAdminFacade;
import com.jk.apps.banking.fx.sor.qual.cache.CreditLine;
import com.jk.apps.banking.fx.sor.qual.dto.SettlementPolicyDto;
import com.jk.apps.banking.fx.sor.qual.grpc.v1.CreditLineList;
import com.jk.apps.banking.fx.sor.qual.grpc.v1.Empty;
import com.jk.apps.banking.fx.sor.qual.grpc.v1.QualifyReadGrpc;
import com.jk.apps.banking.fx.sor.qual.grpc.v1.SettlementPolicy;
import com.jk.apps.banking.fx.sor.qual.grpc.v1.SettlementPolicyList;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

/** Read-only gRPC API for qualify (fx.qualify.v1.QualifyRead). Enabled by fx.features.grpc-enabled. */
@GrpcService
@ConditionalOnProperty(name = "fx.features.grpc-enabled", havingValue = "true")
public class QualifyReadServer extends QualifyReadGrpc.QualifyReadImplBase {

    private final SettlementPolicyAdminFacade settlement;
    private final CreditLineAdminFacade credit;

    public QualifyReadServer(SettlementPolicyAdminFacade settlement, CreditLineAdminFacade credit) {
        this.settlement = settlement;
        this.credit = credit;
    }

    @Override
    public void listSettlementPolicies(Empty r, StreamObserver<SettlementPolicyList> o) {
        run(o, () -> {
            var b = SettlementPolicyList.newBuilder();
            for (SettlementPolicyDto p : settlement.listAll()) {
                b.addPolicies(SettlementPolicy.newBuilder()
                        .setCcyPair(nz(p.getCcyPair())).setTenor(nz(p.getTenor()))
                        .setClsEligible(p.isClsEligible())
                        .setCutoffTimeUtc(String.valueOf(p.getCutoffTimeUtc()))
                        .setMinClipUsd(String.valueOf(p.getMinClipUsd()))
                        .setMaxClipUsd(String.valueOf(p.getMaxClipUsd()))
                        .setRestrictedFlag(p.isRestrictedFlag()));
            }
            return b.build();
        });
    }

    @Override
    public void listCreditLines(Empty r, StreamObserver<CreditLineList> o) {
        run(o, () -> {
            var b = CreditLineList.newBuilder();
            for (CreditLine c : credit.listAll()) {
                b.addLines(com.jk.apps.banking.fx.sor.qual.grpc.v1.CreditLine.newBuilder()
                        .setClientId(nz(c.getClientId())).setLpId(nz(c.getLpId()))
                        .setMaxCreditUsd(String.valueOf(c.getMaxCreditUsd()))
                        .setUtilizedUsd(String.valueOf(c.getUtilizedUsd()))
                        .setReservedUsd(String.valueOf(c.getReservedUsd())));
            }
            return b.build();
        });
    }

    private static <T> void run(StreamObserver<T> o, java.util.function.Supplier<T> call) {
        try {
            o.onNext(call.get());
            o.onCompleted();
        } catch (IllegalArgumentException ex) {
            o.onError(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            o.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
