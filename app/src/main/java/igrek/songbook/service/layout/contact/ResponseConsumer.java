package igrek.songbook.service.layout.contact;

@FunctionalInterface
public interface ResponseConsumer<T> {
	
	void accept(T response);
	
}