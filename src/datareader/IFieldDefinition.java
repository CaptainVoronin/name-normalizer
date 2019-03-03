package datareader;

public interface IFieldDefinition<T> {
    String getName();
    String prepareQueryText( String value );
    T getValue();
    void setValue( T value );
}
