package com.isac.bazar_solidario.dto;
import jakarta.validation.constraints.*;
public record LojaAtualizacaoRequest(
 @NotBlank(message="Nome é obrigatório") @Size(max=100,message="Nome deve ter até 100 caracteres") String nome,
 String logoUrl,
 @Size(max=1000,message="Descrição deve ter até 1000 caracteres") String descricao,
 @Size(max=255,message="Endereço deve ter até 255 caracteres") String endereco,
 Double latitude, Double longitude,
 @NotBlank(message="WhatsApp é obrigatório") @Pattern(regexp="^[0-9]{10,13}$",message="WhatsApp deve conter de 10 a 13 dígitos, com DDI e DDD") String whatsapp) {}