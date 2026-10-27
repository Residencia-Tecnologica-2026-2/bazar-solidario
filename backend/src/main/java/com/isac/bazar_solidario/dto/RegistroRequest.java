package com.isac.bazar_solidario.dto;

import com.isac.bazar_solidario.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter até 100 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
        String senha,

        Role tipo,

        @Pattern(regexp = "^[0-9]{10,13}$", message = "WhatsApp deve conter de 10 a 13 dígitos, com DDI e DDD")
        String whatsapp
) {
}
