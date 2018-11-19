package igrek.songbook.layout.contact;

@FunctionalInterface
public interface ResponseConsumer<T> {
	
	void accept(T response);
	
}