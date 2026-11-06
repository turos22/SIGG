package com.example.sigg_backend.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.sigg_backend.models.TokenConta;

@Service
public class EnvioDeEmail {

    private static final Logger LOG = LoggerFactory.getLogger(EnvioDeEmail.class);

    private final ObjectProvider<JavaMailSender> remetente;
    private final boolean ativo;
    private final String de;
    private final String urlDoFront;

    public EnvioDeEmail(ObjectProvider<JavaMailSender> remetente,
                        @Value("${sigg.email.ativo:false}") boolean ativo,
                        @Value("${sigg.email.remetente:}") String de,
                        @Value("${sigg.front.url:http://localhost:5173}") String urlDoFront) {
        this.remetente = remetente;
        this.ativo = ativo;
        this.de = de;
        this.urlDoFront = urlDoFront;
    }

    public void enviarConfirmacao(String para, String nome, String token) {
        String link = urlDoFront + "/login?confirmar=" + token;
        enviar(para, "SIGG - confirme seu cadastro",
                "Olá, " + nome + "!\n\nPara ativar sua conta no SIGG, acesse o link abaixo (válido por "
                        + TokenConta.HORAS_PARA_CONFIRMAR + " horas):\n" + link
                        + "\n\nSe você não solicitou o cadastro, ignore esta mensagem.", link);
    }

    public void enviarRedefinicao(String para, String nome, String token) {
        String link = urlDoFront + "/login?token=" + token;
        enviar(para, "SIGG - redefinição de senha",
                "Olá, " + nome + "!\n\nPara criar uma nova senha, acesse o link abaixo (válido por "
                        + TokenConta.MINUTOS_PARA_REDEFINIR + " minutos, uso único):\n"
                        + link + "\n\nSe você não solicitou, ignore esta mensagem; sua senha continua a mesma.", link);
    }

    private void enviar(String para, String assunto, String texto, String link) {
        JavaMailSender enviador = remetente.getIfAvailable();
        if (!ativo || enviador == null) {
            LOG.info("E-mail desativado. Assunto '{}' para {}: {}", assunto, para, link);
            return;
        }
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            if (!de.isBlank())
                mensagem.setFrom(de);
            mensagem.setTo(para);
            mensagem.setSubject(assunto);
            mensagem.setText(texto);
            enviador.send(mensagem);
        } catch (MailException falha) {
            LOG.error("Falha ao enviar e-mail '{}': {}", assunto, falha.getMessage());
        }
    }
}
