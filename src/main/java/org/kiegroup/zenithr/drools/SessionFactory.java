package org.kiegroup.zenithr.drools;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.model.Model;
import org.drools.model.Rule;
import org.drools.model.Variable;
import org.drools.model.impl.ModelImpl;
import org.drools.model.impl.RuleBuilder;
import org.drools.modelcompiler.builder.KieBaseBuilder;
import org.kie.api.runtime.KieSession;
import org.kiegroup.zenithr.drools.model.DoubleField;
import org.kiegroup.zenithr.drools.model.StringField;

import javax.json.*;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.drools.model.FlowDSL.*;

public class SessionFactory {
    private static SessionFactory INSTANCE;
    private InternalKnowledgeBase kieBase;

    private SessionFactory(InternalKnowledgeBase kieBase) {
        this.kieBase = kieBase;
    }

    public static SessionFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (SessionFactory.class) {
                if (INSTANCE == null) {
                    Model model = getModel();
                    InternalKnowledgeBase kieBase = KieBaseBuilder.createKieBaseFromModel(model);
                    INSTANCE = new SessionFactory(kieBase);
                }
            }
        }
        return INSTANCE;
    }

    KieSession newKieSession() {
        return kieBase.newKieSession();
    }

    private static Model getModel() {
        String getJson = System.getenv("GET");
        getJson = "{\"input\": [{\"name\": \"grade\", \"type\": \"double\"}]}";
        JsonObject spec = Json.createReader(new StringReader(getJson)).readObject();
        Map<String, String> inputTypeMap = getDataTypeMap(spec.getJsonArray("input"));
        String outputType = spec.getJsonObject("output").getString("type");
        ModelImpl model = new ModelImpl();
        List<JsonObject> rules = spec.getJsonArray("rules").getValuesAs(JsonObject.class);
        for (JsonObject rule : rules) {
            model.addRule( getRule(rule, inputTypeMap, outputType) );
        }
        return model;
    }

    private static Map<String, String> getDataTypeMap(JsonArray jsonArray) {
        Map<String, String> dataTypeMap = new HashMap<>();
        for (JsonObject entry : jsonArray.getValuesAs(JsonObject.class)) {
            dataTypeMap.put(entry.getString("name"), entry.getString("type"));
        }
        return dataTypeMap;
    }

    private static Rule getRule(JsonObject rule, Map<String, String> inputTypes, String outputType) {
        String when = rule.getString("when");
        String name = rule.getString("name", when);
        RuleBuilder ruleBuilder = rule(SessionFactory.class.getPackage().getName(), name);
        //Build and add an expr for each input/output variable
        //build a join expr based on variable type and operator
        String then = rule.getString("then");
        return null;
    }

    private static Model getModel2() {
        Variable<DoubleField> gradeField = declarationOf(DoubleField.class, "grade");
        Variable<StringField> letterField = declarationOf(StringField.class, "letter");
        ModelImpl model = new ModelImpl();
        model.addRule(
                rule("org.kiegroup.zenithr.drools", "grade A").build(
                        expr(gradeField, grade -> grade.getName().equals("grade") && grade.getValue() >= 90 && grade.getValue() <= 100),
                        expr(letterField, letter -> letter.getName().equals("letter")),
                        on(letterField).execute(letter -> letter.setValue("A"))
                )
        );
        model.addRule(
                rule("org.kiegroup.zenithr.drools", "grade B").build(
                        expr(gradeField, grade -> grade.getName().equals("grade") && grade.getValue() >= 80 && grade.getValue() < 90),
                        expr(letterField, letter -> letter.getName().equals("letter")),
                        on(letterField).execute(letter -> letter.setValue("B"))
                )
        );
        model.addRule(
                rule("org.kiegroup.zenithr.drools", "grade C").build(
                        expr(gradeField, grade -> grade.getName().equals("grade") && grade.getValue() >= 70 && grade.getValue() < 80),
                        expr(letterField, letter -> letter.getName().equals("letter")),
                        on(letterField).execute(letter -> letter.setValue("C"))
                )
        );
        model.addRule(
                rule("org.kiegroup.zenithr.drools", "grade D").build(
                        expr(gradeField, grade -> grade.getName().equals("grade") && grade.getValue() >= 60 && grade.getValue() < 70),
                        expr(letterField, letter -> letter.getName().equals("letter")),
                        on(letterField).execute(letter -> letter.setValue("D"))
                )
        );
        model.addRule(
                rule("org.kiegroup.zenithr.drools", "grade F").build(
                        expr(gradeField, grade -> grade.getName().equals("grade") && grade.getValue() < 60),
                        expr(letterField, letter -> letter.getName().equals("letter")),
                        on(letterField).execute(letter -> letter.setValue("F"))
                )
        );
        return model;
    }
}
