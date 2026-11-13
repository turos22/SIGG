package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.SingletonDB;

// A conexão é um Singleton criado uma vez (InitController) e a MESMA instância é entregue a todos os controllers.
@SpringBootTest
class ConexaoUnicaTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Conexao conexao;
    @Autowired
    DataSource dataSource;

    @Test
    void singletonDevolveSempreAMesmaInstanciaParaOMesmoPool() {
        assertSame(SingletonDB.getConexao(dataSource), SingletonDB.getConexao(dataSource));
        assertSame(conexao, SingletonDB.getConexao(dataSource));
    }

    @Test
    void todosOsControllersRecebemAMesmaInstanciaDeConexao() throws Exception {
        // só os controllers do projeto (o contexto também tem controllers do próprio Spring, como o de erro)
        Map<String, Object> controllers = new java.util.TreeMap<>(contexto.getBeansWithAnnotation(RestController.class));
        controllers.values().removeIf(c -> !c.getClass().getPackageName().equals("com.example.sigg_backend.controllers"));
        int comConexao = 0;
        for (Map.Entry<String, Object> controller : controllers.entrySet()) {
            for (Field campo : controller.getValue().getClass().getDeclaredFields()) {
                if (campo.getType() == Conexao.class) {
                    campo.setAccessible(true);
                    assertSame(conexao, campo.get(controller.getValue()),
                            controller.getKey() + " recebeu uma Conexao diferente da instância única");
                    comConexao++;
                }
            }
        }
        // todo controller do projeto recebe a conexão (o InitController é @Configuration: só a cria)
        assertEquals(controllers.size(), comConexao, "todo controller do projeto deve receber a Conexao: " + controllers.keySet());
        assertTrue(comConexao >= 17, "esperava ao menos 17 controllers, encontrou " + comConexao);
    }
}
