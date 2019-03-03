package datareader;

public class VarcharField implements IFieldDefinition<String> {
    String value;
    String name;
    public VarcharField( String name )
    {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String prepareQueryText(String value) {
        this.value = value;
        return "'" + value + "'";
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
