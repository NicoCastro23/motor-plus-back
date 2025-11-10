package com.motorplus.motorplus.model;

import java.time.Instant;
import java.util.UUID;

public class Supervision {
    private UUID supervisorId;
    private UUID supervisadoId;
    private UUID orderId;
    private Instant createdAt;
    private String notes;

    public UUID getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(UUID supervisorId) {
        this.supervisorId = supervisorId;
    }

    public UUID getSupervisadoId() {
        return supervisadoId;
    }

    public void setSupervisadoId(UUID supervisadoId) {
        this.supervisadoId = supervisadoId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
