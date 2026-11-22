package com.jk.apps.banking.fx.sor.admin.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.jk.apps.banking.fx.sor.admin.controller.AdminController;
import com.jk.apps.banking.fx.sor.admin.entity.WorkflowDefinition;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.AdminReadGrpc;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.Empty;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.InitialDataList;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.InitialDataset;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.StructList;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowDefinitionList;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowFilter;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowId;
import com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowsRequest;
import com.jk.apps.banking.fx.sor.admin.initdata.InitDataFacade;
import com.jk.apps.banking.fx.sor.admin.initdata.InitDataView;
import com.jk.apps.banking.fx.sor.admin.service.BpmnWorkflowService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Read-only gRPC API for admin (fx.admin.v1.AdminRead). Enabled by fx.features.grpc-enabled.
 * Reuses the admin controller and services that REST uses, so both return the same data.
 */
@GrpcService
@ConditionalOnProperty(name = "fx.features.grpc-enabled", havingValue = "true")
public class AdminReadServer extends AdminReadGrpc.AdminReadImplBase {

    private final AdminController admin;
    private final BpmnWorkflowService workflows;
    private final InitDataFacade initData;
    private final ObjectMapper mapper;

    public AdminReadServer(AdminController admin, BpmnWorkflowService workflows,
                           InitDataFacade initData, ObjectMapper mapper) {
        this.admin = admin;
        this.workflows = workflows;
        this.initData = initData;
        this.mapper = mapper;
    }

    @Override
    public void listWorkflowDefinitions(WorkflowFilter r, StreamObserver<WorkflowDefinitionList> o) {
        run(o, () -> {
            String scope = r.getPortalScope().isBlank() ? null : r.getPortalScope();
            var b = WorkflowDefinitionList.newBuilder();
            for (WorkflowDefinition d : workflows.listMaster(scope)) {
                b.addDefinitions(com.jk.apps.banking.fx.sor.admin.grpc.v1.WorkflowDefinition.newBuilder()
                        .setWorkflowKey(nz(d.getWorkflowKey())).setName(nz(d.getName()))
                        .setBpmnResource(nz(d.getBpmnResource())).setVersion(nz(d.getVersion()))
                        .setPortalScope(nz(d.getPortalScope())).setStatus(nz(d.getStatus()))
                        .setFrId(nz(d.getFrId())).setPurpose(nz(d.getPurpose())));
            }
            return b.build();
        });
    }

    @Override
    public void listInitialData(Empty r, StreamObserver<InitialDataList> o) {
        run(o, () -> {
            var b = InitialDataList.newBuilder();
            for (InitDataView d : initData.listDatasets()) {
                b.addDatasets(InitialDataset.newBuilder()
                        .setDatasetKey(nz(d.datasetKey())).setTitle(nz(d.title()))
                        .setOwnerService(nz(d.ownerService())).setWorkflowKey(nz(d.workflowKey()))
                        .setSetupStatus(nz(d.setupStatus())).setSetupAllowed(d.setupAllowed())
                        .setCompletedAt(d.completedAt() == null ? "" : d.completedAt().toString()));
            }
            return b.build();
        });
    }

    @Override
    public void listSpreadSchedules(Empty r, StreamObserver<StructList> o) {
        run(o, () -> list(admin.listSpreadSchedules()));
    }

    @Override
    public void getMacroFlag(Empty r, StreamObserver<Struct> o) {
        run(o, () -> struct(admin.getMacroFlag()));
    }

    @Override
    public void listEconomicCalendar(Empty r, StreamObserver<StructList> o) {
        run(o, () -> list(admin.economicCalendar()));
    }

    @Override
    public void listLpBook(Empty r, StreamObserver<StructList> o) {
        run(o, () -> list(admin.listLpBook()));
    }

    @Override
    public void listLpRankings(Empty r, StreamObserver<StructList> o) {
        run(o, () -> list(admin.listLpRankings()));
    }

    @Override
    public void listSettlementPolicies(Empty r, StreamObserver<StructList> o) {
        run(o, () -> list(admin.listSettlementPolicies()));
    }

    @Override
    public void listCreditLines(Empty r, StreamObserver<StructList> o) {
        run(o, () -> list(admin.listCreditLines()));
    }

    @Override
    public void listWorkflows(WorkflowsRequest r, StreamObserver<StructList> o) {
        run(o, () -> {
            var page = admin.listWorkflows(
                    blankToNull(r.getStatus()), blankToNull(r.getWorkflowType()),
                    blankToNull(r.getWorkflowKey()), blankToNull(r.getTriggeredBy()),
                    r.getPage(), r.getSize() == 0 ? 20 : r.getSize());
            return list(page.getContent());
        });
    }

    @Override
    public void getWorkflow(WorkflowId r, StreamObserver<Struct> o) {
        run(o, () -> struct(admin.getWorkflow(UUID.fromString(r.getWfExecId()))));
    }

    @Override
    public void getBpmnExecution(WorkflowId r, StreamObserver<Struct> o) {
        run(o, () -> struct(admin.getBpmnExecution(UUID.fromString(r.getWfExecId()))));
    }

    private StructList list(Collection<?> items) {
        StructList.Builder b = StructList.newBuilder();
        for (Object item : items) {
            b.addItems(struct(item));
        }
        return b.build();
    }

    private Struct struct(Object value) {
        Map<String, Object> map = mapper.convertValue(value, Map.class);
        return toValue(map).getStructValue();
    }

    private static Value toValue(Object o) {
        if (o == null) {
            return Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build();
        }
        if (o instanceof Boolean b) {
            return Value.newBuilder().setBoolValue(b).build();
        }
        if (o instanceof Number n) {
            return Value.newBuilder().setNumberValue(n.doubleValue()).build();
        }
        if (o instanceof Map<?, ?> m) {
            Struct.Builder s = Struct.newBuilder();
            m.forEach((k, v) -> s.putFields(String.valueOf(k), toValue(v)));
            return Value.newBuilder().setStructValue(s).build();
        }
        if (o instanceof Collection<?> c) {
            ListValue.Builder l = ListValue.newBuilder();
            c.forEach(v -> l.addValues(toValue(v)));
            return Value.newBuilder().setListValue(l).build();
        }
        return Value.newBuilder().setStringValue(String.valueOf(o)).build();
    }

    private static <T> void run(StreamObserver<T> o, Supplier<T> call) {
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

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
