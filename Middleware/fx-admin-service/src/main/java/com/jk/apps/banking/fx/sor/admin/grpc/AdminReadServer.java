package com.jk.apps.banking.fx.sor.admin.grpc;

import com.jk.apps.banking.fx.sor.admin.entity.WorkflowDefinition;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.AdminReadGrpc;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.Empty;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.InitialDataList;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.InitialDataset;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowDefinitionList;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowFilter;
import com.jk.apps.banking.fx.sor.admin.initdata.InitDataFacade;
import com.jk.apps.banking.fx.sor.admin.initdata.InitDataView;
import com.jk.apps.banking.fx.sor.admin.service.BpmnWorkflowService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

/** Read-only gRPC API for admin (fx.admin.v1.AdminRead). Enabled by fx.features.grpc-enabled. */
@GrpcService
@ConditionalOnProperty(name = "fx.features.grpc-enabled", havingValue = "true")
public class AdminReadServer extends AdminReadGrpc.AdminReadImplBase {

    private final BpmnWorkflowService workflows;
    private final InitDataFacade initData;

    public AdminReadServer(BpmnWorkflowService workflows, InitDataFacade initData) {
        this.workflows = workflows;
        this.initData = initData;
    }

    @Override
    public void listWorkflowDefinitions(WorkflowFilter r, StreamObserver<WorkflowDefinitionList> o) {
        try {
            String scope = r.getPortalScope().isBlank() ? null : r.getPortalScope();
            var b = WorkflowDefinitionList.newBuilder();
            for (WorkflowDefinition d : workflows.listMaster(scope)) {
                b.addDefinitions(com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowDefinition.newBuilder()
                        .setWorkflowKey(nz(d.getWorkflowKey())).setName(nz(d.getName()))
                        .setBpmnResource(nz(d.getBpmnResource())).setVersion(nz(d.getVersion()))
                        .setPortalScope(nz(d.getPortalScope())).setStatus(nz(d.getStatus()))
                        .setFrId(nz(d.getFrId())).setPurpose(nz(d.getPurpose())));
            }
            o.onNext(b.build());
            o.onCompleted();
        } catch (Exception ex) {
            o.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listInitialData(Empty r, StreamObserver<InitialDataList> o) {
        try {
            var b = InitialDataList.newBuilder();
            for (InitDataView d : initData.listDatasets()) {
                b.addDatasets(InitialDataset.newBuilder()
                        .setDatasetKey(nz(d.datasetKey())).setTitle(nz(d.title()))
                        .setOwnerService(nz(d.ownerService())).setWorkflowKey(nz(d.workflowKey()))
                        .setSetupStatus(nz(d.setupStatus())).setSetupAllowed(d.setupAllowed())
                        .setCompletedAt(d.completedAt() == null ? "" : d.completedAt().toString()));
            }
            o.onNext(b.build());
            o.onCompleted();
        } catch (Exception ex) {
            o.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
