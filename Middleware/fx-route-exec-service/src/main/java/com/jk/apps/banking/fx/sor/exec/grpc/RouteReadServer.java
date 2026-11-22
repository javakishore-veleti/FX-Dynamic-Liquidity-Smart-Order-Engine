package com.jk.apps.banking.fx.sor.exec.grpc;

import com.jk.apps.banking.fx.sor.exec.business.facade.LpRankingConfigFacade;
import com.jk.apps.banking.fx.sor.exec.dto.LpRankingEntryDto;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.Empty;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.LpRanking;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.LpRankingList;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.RouteReadGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

/** Read-only gRPC API for route-exec (fx.route.v1.RouteRead). Enabled by fx.features.grpc-enabled. */
@GrpcService
@ConditionalOnProperty(name = "fx.features.grpc-enabled", havingValue = "true")
public class RouteReadServer extends RouteReadGrpc.RouteReadImplBase {

    private final LpRankingConfigFacade rankings;

    public RouteReadServer(LpRankingConfigFacade rankings) {
        this.rankings = rankings;
    }

    @Override
    public void listLpRankings(Empty r, StreamObserver<LpRankingList> o) {
        try {
            var b = LpRankingList.newBuilder();
            for (LpRankingEntryDto e : rankings.listAll()) {
                b.addRankings(LpRanking.newBuilder()
                        .setLpId(e.getLpId() == null ? "" : e.getLpId())
                        .setRank(e.getRank())
                        .setRuleVersion(e.getRuleVersion() == null ? "" : e.getRuleVersion())
                        .setEnabled(e.isEnabled()));
            }
            o.onNext(b.build());
            o.onCompleted();
        } catch (Exception ex) {
            o.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }
}
