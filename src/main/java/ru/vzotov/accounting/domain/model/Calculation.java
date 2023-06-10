package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang3.Validate;
import ru.vzotov.ddd.shared.ValueObject;
import ru.vzotov.domain.model.Money;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Currency;
import java.util.Map;
import java.util.Objects;

public class Calculation implements ValueObject<Calculation> {

    private String expression;

    public Calculation(String expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public String expression() {
        return expression;
    }

    public Money calculate(Map<String, Object> arguments) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }

        Double value = (Double) engine.eval(expression);
        return new Money(value, Currency.getInstance(arguments.get("currency").toString()));
    }

    @Override
    public boolean sameValueAs(Calculation that) {
        return that != null && Objects.equals(expression, that.expression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Calculation that = (Calculation) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    protected Calculation() {
        // for Hibernate
    }
}
