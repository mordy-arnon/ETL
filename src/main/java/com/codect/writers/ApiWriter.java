package com.codect.writers;

import java.io.IOException;

public class ApiWriter extends InMemoryWriter{
	@Override
    public void close() throws IOException {
        params.put("results", all);
    }
}
