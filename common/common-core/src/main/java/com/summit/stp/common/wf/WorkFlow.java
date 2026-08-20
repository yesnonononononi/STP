package com.summit.stp.common.wf;

import com.summit.stp.common.wf.plugins.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;


public class WorkFlow {
    private final List<Step> tasks = new LinkedList<>();
    private Step currentStep;
    @Setter
    StepExecutor executor = DefaultStepExecutor.getInstance();

    @EqualsAndHashCode(callSuper = true)
    @Data
    public class Step extends AbstractPluginsSupport {
        private Integer order;
        private Runnable task;
        private String description;
        private LinkedList<ErrorHandler> onError;


        public Step(int order) {
            this.order = order;
        }

        public Step(int order, String description) {
            this.order = order;
            this.description = description;
        }

        public Step step(int order) {
            return WorkFlow.this.step(order);
        }


        public Step attempt(Runnable task) {
            this.task = task;
            return this;
        }

        public <T extends Exception> Step thenError(Consumer<T> error, boolean continueOnError, Class<T> clazz) {
            ErrorHandler handler = ErrorHandler.builder()
                    .errorType(clazz)
                    .handle((e) -> {
                        if (clazz.isInstance(e)) {
                            error.accept(clazz.cast(e));
                        }
                    })
                    .continueOnError(continueOnError)
                    .build();
            this.getOnError().add(handler);


            return this;
        }

        public Step thenError(Consumer<Exception> error) {
            return this.thenError(error, false, Exception.class);
        }

        public Step thenError(Consumer<Exception> error, boolean continueOnError) {
            return this.thenError(error, continueOnError, Exception.class);
        }


        public Step use(Plugin plugin) {
            return super.use(plugin, this);
        }

        public void run() {
            WorkFlow.this.run();
        }
    }


    public void run() {
        if (currentStep != null) tasks.addLast(currentStep);
        tasks.forEach(this::invoke);
    }


    private void invoke(Step step) {
        executor.invoke(step);
    }

    public static WorkFlow create() {
        return new WorkFlow();
    }


    public Step step(int order) {
        if (currentStep != null) tasks.addLast(currentStep);
        currentStep = new Step(order);
        return currentStep;
    }

}



