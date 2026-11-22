package com.jk.apps.banking.fx.sor.admin.bpmn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class BpmnStepCatalogTest {

    @Test
    void fr01AdminHasUserConfirmTask() {
        var steps = BpmnStepCatalog.stepsFor(Fr01BpmnDefinition.PROCESS_KEY);
        assertFalse(steps.isEmpty());
        assertEquals(
                BpmnStepCatalog.TaskType.USER,
                steps.stream()
                        .filter(s -> "task_confirm_macro".equals(s.taskId()))
                        .findFirst()
                        .orElseThrow()
                        .type());
    }
}
