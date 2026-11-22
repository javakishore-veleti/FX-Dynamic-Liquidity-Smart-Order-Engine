package com.jk.apps.banking.fx.sor.exec.grpc;

import com.jk.apps.banking.fx.sor.exec.business.facade.LpRankingConfigFacade;
import com.jk.apps.banking.fx.sor.exec.dto.LpRankingEntryDto;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.Empty;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.LpRanking;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.LpRankingList;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.OrderTrace;
import com.jk.apps.banking.fx.sor.exec.grpc.v1.OrderTraceRequest;
import com.jk.apps.banking.fx.sor.exec.service.RouteExecService;
import com.jk.apps.banking.fx.sor.exec.dto.OrderTraceResponse;
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
    private final RouteExecService routeExec;

    public RouteReadServer(LpRankingConfigFacade rankings, RouteExecService routeExec) {
        this.rankings = rankings;
        this.routeExec = routeExec;
    }

    @Override
    public void getOrderTrace(OrderTraceRequest r, StreamObserver<OrderTrace> o) {
        try {
            OrderTraceResponse t = routeExec.trace(r.getClientOrderId());
            var b = OrderTrace.newBuilder()
                    .setClientOrderId(nz(t.getClientOrderId())).setOrderId(String.valueOf(t.getOrderId()))
                    .setStatus(nz(t.getStatus())).setExecutionMode(nz(t.getExecutionMode()))
                    .setExecutedRate(String.valueOf(t.getExecutedRate())).setSlippageBps(String.valueOf(t.getSlippageBps()))
                    .setDroolsRuleVersion(nz(t.getDroolsRuleVersion()));
            if (t.getEvaluatedLps() != null) {
                b.addAllEvaluatedLps(t.getEvaluatedLps());
            }
            if (t.getKnockoutCodes() != null) {
                b.addAllKnockoutCodes(t.getKnockoutCodes());
            }
            o.onNext(b.build());
            o.onCompleted();
        } catch (Exception ex) {
            o.onError(io.grpc.Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
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
