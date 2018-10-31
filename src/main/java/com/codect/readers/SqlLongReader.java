import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SqlLongReader extends SqlReader{
	private String[] longQueryParams;
	private int bulkNum=0;
	private String sqlParts[];
	
	@Override
	public boolean hasNext() {
		if (next)
			return true;
		
	}

	@Override
	public List<Map<String, Object>> next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
}