package igrek.songbook.service.filesystem;


import java.util.LinkedList;
import java.util.List;

public class FirstRuleChecker<T> {
	
	// remembers inserting order
	private List<Rule> rules = new LinkedList<>();
	
	public FirstRuleChecker() {
	}
	
	public FirstRuleChecker<T> addRule(BooleanCondition when, Provider<T> then) {
		rules.add(new Rule(when, then));
		return this;
	}
	
	public FirstRuleChecker<T> addRule(BooleanCondition when, T then) {
		rules.add(new Rule(when, () -> then));
		return this;
	}
	
	public FirstRuleChecker<T> addRule(Provider<T> then) {
		return addRule(() -> true, then);
	}
	
	public FirstRuleChecker<T> addRule(T defaultValue) {
		return addRule(() -> true, () -> defaultValue);
	}
	
	public T find() {
		for (Rule rule : rules) {
			BooleanCondition when = rule.when;
			if (when.test()) {
				Provider<T> then = rule.then;
				if (then != null) {
					T value = then.get();
					if (value != null) // accept only not null values
						return value;
				}
			}
		}
		return null;
	}
	
	@FunctionalInterface
	public interface BooleanCondition {
		boolean test();
	}
	
	@FunctionalInterface
	public interface Provider<T> {
		T get();
	}
	private class Rule {
		BooleanCondition when;
		Provider<T> then;
		
		public Rule(BooleanCondition when, Provider<T> then) {
			this.when = when;
			this.then = then;
		}
	}
}
