package igrek.songbook.contact;

@FunctionalInterface
public interface ResponseConsumer<T> {
	
	void accept(T response);
	
}