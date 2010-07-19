package classes.server.shrink;

public interface ShrinkPerformer {

	void init();

	void shrink(StringBuilder newClientsActions);

}
